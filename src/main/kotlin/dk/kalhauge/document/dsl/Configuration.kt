package dk.kalhauge.document.dsl

import java.io.File

class Configuration(
    courseRoot: String? = null,
    val hasTitle: Boolean = false,
    val hasNumbers: Boolean = false
    ) {
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
        ?: this::class.java.classLoader.getResource(".").path.substringBeforeLast("/dsl/build")
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