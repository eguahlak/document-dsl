package dk.kalhauge.util

import java.security.MessageDigest

fun String.labelize() = toLowerCase().replace("[^a-z0-9.]+".toRegex(), "-")

infix fun Int.of(text: String) = (1..this).map { text }.joinToString(separator = "")

val String.back: String get() = this.replace("[^/]+".toRegex(), "..")


infix fun String.from(other: String): String {
  val target = pathOf(normalize(other, this).split('/'))
  val source = pathOf(other.split('/'))
  return skipEquals(target, source)?.joinToString('/') ?: ""
  }

private fun skipEquals(target: Path<String>?, source: Path<String>?): Path<String>? {
  if (source == null)
      return target
  if (target != null && target.first == source.first)
      return skipEquals(target.rest, source.rest)
  return dotOut(target, source)
  }

private fun dotOut(target: Path<String>?, source: Path<String>?): Path<String>? {
  if (source == null)
      return target
  return Path("..", dotOut(target, source.rest))
  }

fun String.toMD5() =
  MessageDigest.getInstance("MD5").digest(this.toByteArray()).take(4).joinToString("") { "%02x".format(it) }

fun MutableList<*>.remove() { if (this.size > 0) this.removeAt(this.size - 1) }

fun normalize(path: String, name: String? = null): String {
  val parts =
      if (name == null) path.split('/')
      else if (name.startsWith('/')) name.split('/')
      else "$path/$name".split('/')
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
