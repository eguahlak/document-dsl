package dk.kalhauge.util

import java.io.File
import java.net.URL
import kotlin.reflect.KClass

fun URL.copyTo(target: File, skip: (File) -> Boolean = { false }) {
  if (skip(target)) return
  target.outputStream().use { output ->
    this.openConnection().getInputStream().use { input -> input.copyTo(output) }
    }
  }

fun MutableMap<String, String>.loadProperties(klass: KClass<*>, name: String) {
  klass::class.java.classLoader.getResourceAsStream(name).bufferedReader().use { reader ->
    reader.lines().forEach { line ->
      val parts = line.split('=', ':').map { it.trim().substringBefore("#") }
      when (parts.size) {
        0 -> { }
        1 -> this[parts[0]] = ""
        else -> this[parts[0]] = parts[1]
        }
      }
    }
  }


fun main() {
  val file = File("/Users/AKA/tmp/resources/raven.png")
  println(file.absolutePath.toMD5())
  println("/Users/AKA/tmp/resources/raven.png".toMD5())
  println("/Users/AKA/tmp/resourCes/raven.png".toMD5())

  }