package dk.kalhauge.util

import java.lang.Math.abs

fun String.anchorize() = toLowerCase().replace("[^a-z0-9]+".toRegex(), "-")

fun nice(value: Double) = if (abs(value) < 0.0001) "" else "$value"