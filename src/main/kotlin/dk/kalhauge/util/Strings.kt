package dk.kalhauge.util

import java.security.MessageDigest

infix fun Int.of(text: String) = (1..this).map { text }.joinToString(separator = "")

val String.back: String get() = this.replace("[^/]+".toRegex(), "..")

infix fun String.from(other: String): String {
  val target = pathOf(this.split('/'))
  val source = pathOf(other.split('/'))
  return relate(target, source)?.joinToString('/') ?: ""
  }

fun relate(target: Path<String>?, source: Path<String>?, noDots: Boolean = true): Path<String>? {
  if (source == null || source.rest == null) return target
  if (noDots && target != null && target.first == source.first) return relate(target.rest, source.rest, noDots)
  return Path("..", relate(target, source.rest, false))
  }

fun String.toMD5() =
  MessageDigest.getInstance("MD5").digest(this.toByteArray()).take(4).joinToString("") { "%02x".format(it) }

fun MutableList<*>.remove() { this.removeAt(this.size - 1) }

fun normalizePath(path: String): String {
  val parts = path.split('/')
  var j = 0
  var normalized = mutableListOf<String>()
  loop@for (part in parts) {
    when (part) {
      "." -> continue@loop
      ".." -> normalized.remove()
      else -> normalized.add(part)
      }
    }
  return normalized.joinToString("/")
  }

operator fun String.minus(postfix: String) = if (this.isNotBlank()) "$this$postfix" else ""

/*
fun main() {
  println(normalizePath("soft2020spring/ALG/course-info/top"))
  println(normalizePath("soft2020spring/ALG/course-info/../top"))
  println(normalizePath("soft2020spring/ALG/course-info/../../AI/course-info/top"))
  }
*/