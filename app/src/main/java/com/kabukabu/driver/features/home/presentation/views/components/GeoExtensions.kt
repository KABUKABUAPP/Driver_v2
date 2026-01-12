package com.kabukabu.driver.features.home.presentation.views.components

import com.mapbox.geojson.Point

/**
 * Public geo utility extensions used across map components.
 */
fun Point.distanceTo(other: Point): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        this.latitude(),
        this.longitude(),
        other.latitude(),
        other.longitude(),
        results
    )
    return results[0].toDouble()
}

