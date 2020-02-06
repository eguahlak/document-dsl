package dk.kalhauge.util

import java.io.File
import java.net.URL

fun URL.copyTo(target: File, skip: (File) -> Boolean = { false }) {
  if (skip(target)) return
  target.outputStream().use { output ->
    this.openConnection().getInputStream().use { input -> input.copyTo(output) }
    }
  }

fun splitCsvLine(line: String) =
  """( *"([^"]*)")|([^";]+ *)""".toRegex().findAll(line)
    .map {
      when  {
        it.groups[3] != null -> it.groupValues[3].trim()
        it.groups[2] != null -> it.groupValues[2].trim()
        else -> null
        } }
    .filterNotNull()


/*
fun main() {
  val line = "\"Alex Langhoff\"; \"cph-al279@cphbusiness.dk\""
  splitCsvLine(line).forEach { println(it) }
  }
*/
