package dk.kalhauge.document.dsl

import java.io.File
import java.nio.charset.Charset

object CourseContext {
  val root: String = this::class.java.classLoader.getResource(".").path.substringBeforeLast("/dsl/")
  }

class Configuration(
    courseRoot: String? = null,
    val hasTitle: Boolean = false,
    val hasNumbers: Boolean = false,
    val outputLevel: OutputLevel = OutputLevel.WARNING
    ) {
  enum class OutputLevel { NONE, ERROR, WARNING, INFO, VERBOSE }
  val properties = mutableMapOf<String, String>()
  val contextRoot: String
  val root: File

  init {
    try {
      loadProperties("context.properties")
      }
    catch (e: Exception) {
      System.err.println("Please create a context.properties file under main/resources")
      System.err.println("The file should have at least an entry:")
      System.err.println("context.root=<path to context root>")
      System.err.println("Trying with default values ...")
      }
    this.contextRoot = courseRoot
        ?: properties["context.root"]
        ?: this::class.java.classLoader.getResource(".").path.substringBeforeLast("/dsl/")
    this.root = File(this.contextRoot)
    }

  operator fun get(key: String) =
    when (key) {
      "context.root" -> contextRoot
      else -> properties[key] ?: {
        println("Using the empty string for $key, consider adding an entry in context.properties")
        ""
        }()
      }

  fun loadProperties(name: String) {
    this::class.java.classLoader.getResourceAsStream(name).bufferedReader(Charsets.UTF_8).use { reader ->
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