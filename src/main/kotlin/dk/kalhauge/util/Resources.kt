package dk.kalhauge.util

import java.io.File
import java.net.URL

fun URL.copyTo(target: File, skip: (File) -> Boolean = { false }) {
  if (skip(target)) return
  target.outputStream().use { output ->
    this.openConnection().getInputStream().use { input -> input.copyTo(output) }
    }
  }
