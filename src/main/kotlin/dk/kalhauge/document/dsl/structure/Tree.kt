package dk.kalhauge.document.dsl.structure

import dk.kalhauge.document.dsl.Target
import dk.kalhauge.document.dsl.UnknownTarget

interface Tree {
  val path: String
  val root: Root

  interface Trunk : Tree {
    val branches: List<Branch>
    fun add(branch: Branch)
    }

  abstract class BaseTrunk() : Trunk {
    override val branches = mutableListOf<Branch>()
    override fun add(branch: Branch) {
      branches += branch
      }
    }

  interface Branch : Tree {
    val trunk: Trunk
    val name: String
    override val path get() = "${trunk.path}/$name"
    override val root get() = trunk.root
    }

  class Root : Tree.BaseTrunk() {
    override val path = ""
    override val root = this
    val resources = "/docs/resources" // TODO generalize

    val targets = mutableMapOf<String, Target>()
    fun register(target: Target) {
      targets[target.key] = target
      }
    fun find(key: String) =  targets[key] ?: UnknownTarget("No target in root with $key", key)
    }

  }

fun root(build: Tree.Root.() -> Unit) =
  Tree.Root().apply {
    build()
    FreeContext.targets.forEach { register(it.fixate()) }
    // TODO post-processing
    }

