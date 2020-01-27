package dk.kalhauge.document.dsl

import java.io.File

class Configuration(
    courseRoot: String? = null,
    val hasTitle: Boolean = false,
    val hasNumbers: Boolean = false
    ) {
  val properties = mutableMapOf<String, String>()
  val courseRoot: String
  val root: File

  init {
    try {
      loadProperties("course.properties")
      }
    catch (e: Exception) {
      println("Please create a course.properties file under main/resources")
      println("The file should have at least an entry:")
      println("course.root=<path to course root>")
      throw e
      }
    this.courseRoot = courseRoot ?: properties["course.root"] ?: throw RuntimeException("Could not find 'course.root' in 'course.properties'")
    this.root = File(this.courseRoot)
    }

  operator fun get(key: String) =
    when (key) {
      "course.root" -> courseRoot
      else -> properties[key] ?: throw RuntimeException("Could not find '$key' in 'course.properties'")
      }

  fun loadProperties(name: String) {
    this::class.java.classLoader.getResourceAsStream(name).bufferedReader().use { reader ->
      reader.lines().forEach { line ->
        val parts = line.split('=', ':').map { it.trim().substringBefore("#") }
        when (parts.size) {
          0 -> { }
          1 -> properties[parts[0]] = ""
          else -> properties[parts[0]] = parts[1]
          }
        }
      }
    }
  }