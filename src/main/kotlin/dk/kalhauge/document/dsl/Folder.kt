package dk.kalhauge.document.dsl

import dk.kalhauge.document.dsl.structure.Tree

class Folder(
    override val trunk: Tree.Trunk,
    override val name: String
    ) : Tree.BaseTrunk(), Tree.Branch {
  override val root = trunk.root
  override val path = "${trunk.path}/$name"
  }

fun Tree.Trunk.folder(name: String, build: Folder.() -> Unit) =
    Folder(this, name).also {
      it.build()
      add(it)
      }
