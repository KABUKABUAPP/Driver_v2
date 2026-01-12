package com.kabukabu.driver.features.home.presentation.views.components

import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * SimulatedLocationSource emits android.location.Location objects following a path of Mapbox Points.
 * Consumers can collect from .locations to receive simulated updates. Start/stop controls simulation.
 *
 * Example usage from ViewModel:
 *
 * private val sim = SimulatedLocationSource(viewModelScope)
 * sim.setPath(polylinePoints)
 * sim.start(speedMetersPerSec = 6.0)
 * sim.locations.onEach { loc -> _locationState.value = loc }.launchIn(viewModelScope)
 *
 */
class SimulatedLocationSource(private val scope: CoroutineScope) {
    private val _locations = MutableSharedFlow<android.location.Location>(replay = 1)
    val locations: Flow<android.location.Location> = _locations

    private var job: Job? = null
    private var path: List<Point> = emptyList()
    private var intervalMs: Long = 1000L

    fun setPath(points: List<Point>) {
        path = points.toList()
    }

    private fun computeDistanceMeters(a: Point, b: Point): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude(),
            a.longitude(),
            b.latitude(),
            b.longitude(),
            results
        )
        return results[0].toDouble()
    }

    fun start(speedMetersPerSec: Double = 5.0, intervalMs: Long = 1000L) {
        stop()
        if (path.isEmpty()) return
        this.intervalMs = intervalMs

        job = scope.launch(Dispatchers.Default) {
            // Start at first point
            var curLon = path[0].longitude()
            var curLat = path[0].latitude()
            var vertexIndex = 0

            if (path.size == 1) {
                while (isActive) {
                    _locations.emit(makeLocation(curLat, curLon))
                    delay(this@SimulatedLocationSource.intervalMs)
                }
                return@launch
            }

            while (isActive) {
                val targetIndex = (vertexIndex + 1).coerceAtMost(path.size - 1)
                val target = path[targetIndex]
                val curPoint = Point.fromLngLat(curLon, curLat)
                val distToTarget = computeDistanceMeters(curPoint, target)

                if (distToTarget <= 0.5) {
                    curLon = target.longitude()
                    curLat = target.latitude()
                    vertexIndex = targetIndex
                    if (vertexIndex >= path.size - 1) {
                        _locations.emit(makeLocation(curLat, curLon))
                        break
                    }
                    continue
                }

                val distanceThisTick = speedMetersPerSec * (this@SimulatedLocationSource.intervalMs / 1000.0)
                if (distanceThisTick >= distToTarget) {
                    curLon = target.longitude()
                    curLat = target.latitude()
                    vertexIndex = targetIndex
                } else {
                    val fraction = distanceThisTick / distToTarget
                    val newLat = curLat + (target.latitude() - curLat) * fraction
                    val newLon = curLon + (target.longitude() - curLon) * fraction
                    curLat = newLat
                    curLon = newLon
                }

                _locations.emit(makeLocation(curLat, curLon))
                delay(this@SimulatedLocationSource.intervalMs)

                val lastPoint = path.last()
                val lastDist = computeDistanceMeters(Point.fromLngLat(curLon, curLat), lastPoint)
                if (vertexIndex >= path.size - 1 && lastDist <= 0.5) break
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean = job?.isActive == true

    private fun makeLocation(lat: Double, lon: Double): android.location.Location {
        val l = android.location.Location("simulated")
        l.latitude = lat
        l.longitude = lon
        l.accuracy = 5f
        l.time = System.currentTimeMillis()
        return l
    }
}
