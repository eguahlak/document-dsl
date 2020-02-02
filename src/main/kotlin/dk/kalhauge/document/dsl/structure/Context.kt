package dk.kalhauge.document.dsl.structure

import dk.kalhauge.document.dsl.Target
import dk.kalhauge.document.dsl.UnknownTarget

interface Prefixed {
  val prefix: String
  }

interface Context {
  val filePath: String
  val keyPath: String
  fun register(target: Target)
  fun find(key: String): Target
  }

object FreeContext : Context {
  override val filePath = "?"
  override val keyPath = ""
  val targets = mutableListOf<Target>()
  override fun register(target: Target) { targets += target }
  override fun find(key: String) =
      targets
        .filter { it.key == key }
        .firstOrNull()
        ?: UnknownTarget("Trying to find target from free context", key)
  }

class IllegalStructure(message: String): RuntimeException(message)
