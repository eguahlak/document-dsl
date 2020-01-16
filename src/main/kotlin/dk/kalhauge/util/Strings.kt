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
