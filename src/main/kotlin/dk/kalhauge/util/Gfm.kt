package dk.kalhauge.util

import kotlin.math.*

fun String.anchorize() =
  toLowerCase()
    .replace("""[^\w\- ]""".toRegex(), "")
    .replace(" ", "-")

fun nice(value: Double) = if (abs(value) < 0.0001) "" else "$value"