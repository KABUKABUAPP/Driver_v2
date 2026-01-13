package com.kabukabu.driver.features.home.presentation.views.components.map

import android.location.Location
import android.util.Log
import com.mapbox.geojson.Point
import kotlin.math.cos

/**
 * Utility functions for route geometry calculations
 */
object RouteGeometryUtils {

    /**
     * Trims route points to start from the driver's position using nearest-segment projection
     * @param points Original route points
     * @param driver Current driver location
     * @param maxDistanceToFirstPointMeters If driver is closer than this to first point, keep all points
     * @return Trimmed route starting from driver's position
     */
    fun trimRoutePointsForDriver(
        points: List<Point>,
        driver: Point,
        maxDistanceToFirstPointMeters: Double = 50.0
    ): List<Point> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1) return listOf(driver, points[0])

        // Check distance to first point
        val distanceToFirst = haversineDistance(driver, points.first())
        if (distanceToFirst < maxDistanceToFirstPointMeters) {
            // Very close to start, no need to trim
            return points
        }

        // Find closest segment using nearest-segment projection
        var minDist = Double.MAX_VALUE
        var bestSegmentIndex = 0
        var bestProjectedPoint: Point = driver
        var bestT = 0.0

        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]

            val (projectedPoint, t) = projectPointToSegment(driver, a, b)
            val dist = haversineDistance(driver, projectedPoint)

            if (dist < minDist) {
                minDist = dist
                bestSegmentIndex = i
                bestProjectedPoint = projectedPoint
                bestT = t
            }
        }

        Log.d("RouteGeometry", "Driver closest to segment $bestSegmentIndex at t=$bestT, dist=${minDist}m")

        // Build trimmed route: [driver, projected point, remaining points after segment]
        val trimmed = mutableListOf<Point>()
        trimmed.add(driver)

        // Only add projected point if it's not basically the same as driver or next vertex
        val isProjectedFarFromDriver = haversineDistance(driver, bestProjectedPoint) > 5.0
        val isProjectedFarFromNext =
            bestSegmentIndex + 1 < points.size && haversineDistance(
                bestProjectedPoint,
                points[bestSegmentIndex + 1]
            ) > 5.0

        if (isProjectedFarFromDriver && isProjectedFarFromNext) {
            trimmed.add(bestProjectedPoint)
        }

        // Add all points after the segment
        for (j in bestSegmentIndex + 1 until points.size) {
            trimmed.add(points[j])
        }

        return trimmed
    }

    /**
     * Projects point p onto segment ab and returns the projected Point and the t parameter (0..1)
     * using a simple equirectangular projection approximation (sufficient for short distances).
     */
    fun projectPointToSegment(p: Point, a: Point, b: Point): Pair<Point, Double> {
        // Convert to simple Cartesian coordinates in degrees, but scale longitude by cos(meanLat)
        val meanLat = Math.toRadians((a.latitude() + b.latitude()) / 2.0)
        val scale = cos(meanLat)

        val ax = a.longitude() * scale
        val ay = a.latitude()
        val bx = b.longitude() * scale
        val by = b.latitude()
        val px = p.longitude() * scale
        val py = p.latitude()

        val abx = bx - ax
        val aby = by - ay
        val apx = px - ax
        val apy = py - ay

        val abLen2 = abx * abx + aby * aby
        if (abLen2 == 0.0) {
            return Pair(a, 0.0)
        }

        var t = (apx * abx + apy * aby) / abLen2
        if (t < 0.0) t = 0.0
        if (t > 1.0) t = 1.0

        val projX = ax + t * abx
        val projY = ay + t * aby

        // Convert back to lon/lat degrees
        val projLon = projX / scale
        val projLat = projY

        val projPoint = Point.fromLngLat(projLon, projLat)
        return Pair(projPoint, t)
    }

    /**
     * Calculates haversine distance between two points in meters
     */
    fun haversineDistance(p1: Point, p2: Point): Double {
        val loc1 = Location("").apply {
            latitude = p1.latitude()
            longitude = p1.longitude()
        }
        val loc2 = Location("").apply {
            latitude = p2.latitude()
            longitude = p2.longitude()
        }
        return loc1.distanceTo(loc2).toDouble()
    }
}

