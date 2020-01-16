package dk.kalhauge.util

import java.io.File
import java.net.URL

fun URL.copyTo(target: File, skip: (File) -> Boolean = { false }) {
  if (skip(target)) return
  target.outputStream().use { output ->
    this.openConnection().getInputStream().use { input -> input.copyTo(output) }
    }
  }

fun main() {
  val file = File("/Users/AKA/tmp/resources/raven.png")
  println(file.absolutePath.toMD5())
  println("/Users/AKA/tmp/resources/raven.png".toMD5())
  println("/Users/AKA/tmp/resourCes/raven.png".toMD5())

  }