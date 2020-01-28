package dk.kalhauge.document.handler

import dk.kalhauge.document.dsl.Configuration
import dk.kalhauge.util.copyTo
import dk.kalhauge.util.of
import dk.kalhauge.util.stackOf
import java.io.File
import java.io.PrintWriter
import java.net.URL

interface Host {
  val configuration: Configuration
  var indent: Int
  fun print(text: String?)
  fun printLine(text: String?, emptyLineCount: Int = 1)
  fun open(filename: String)
  fun close()
  fun printLine() { printLine("", 0) }
  fun printLine(prefix: String, text: String?, emptyLineCount: Int = 1) {
    if (text != null) printLine("$prefix $text", emptyLineCount)
    }
  fun updateFile(sourcePath: String, targetPath: String)
  fun downloadFile(url: String, targetPath: String)
  }

class ConsoleHost(override val configuration: Configuration) : Host {
  val filenames = stackOf<String>()

  override var indent: Int = 0
    get() = field
    set(value) {
      field = value
      if (field < 0) throw IllegalArgumentException("Indent can't be negative")
      }

  override fun print(text: String?) {
    if (text == null) return
    kotlin.io.print(text)
    }

  override fun printLine(text: String?, emptyLineCount: Int) {
    if (text == null) return
    println("${indent of " "}$text")
    for (i in 1..emptyLineCount) println()
    }

  override fun open(filename: String) {
    filenames.push(filename)
    println(">>> OPEN : $filename")
    }

  override fun close() {
    println("<<< CLOSE: ${filenames.pop()}")
    }

  override fun updateFile(sourcePath: String, targetPath: String) {
    println("=== UPDATING: $sourcePath to $targetPath")
    }

  override fun downloadFile(url: String, targetPath: String) {
    println("=== DONLOADING: $url to $targetPath")
    }

  }

class FileHost(override val configuration: Configuration) : Host {
  val courseRoot get() = configuration.contextRoot
  val root: File get() = configuration.root
  val outputs = stackOf<PrintWriter>()

  override var indent: Int = 0
    get() = field
    set(value) {
      field = value
      if (field < 0) throw IllegalArgumentException("Indent can't be negative")
      }

  override fun print(text: String?) {
    if (text == null) return
    kotlin.io.print(text)
    }

  override fun printLine(text: String?, emptyLineCount: Int) {
    // top file's active writer should never be null
    if (text == null) return
    with (outputs.peek()) {
      println("${indent of " "}$text")
      for (i in 1..emptyLineCount) println()
      }
    }

  override fun open(filename: String) {
    val file = File(courseRoot, filename)
    val parent = file.parentFile
    parent.mkdirs()
    file.delete()
    file.createNewFile()
    outputs.push(file.printWriter())
    }

  override fun close() {
    outputs.pop().close()
    }

  override fun updateFile(sourcePath: String, targetPath: String) {
    print("updating $sourcePath to $targetPath ")
    val source = File(sourcePath)
    val target = File(courseRoot, targetPath)
    if (!source.exists()) {
      println("$sourcePath doesn't exist on this machine")
      }
    else if (!target.exists() || source.lastModified() > target.lastModified()) {
      source.copyTo(target, overwrite = true)
      println("done!")
      }
    else {
      println("already up to date!")
      }
    }

  override fun downloadFile(url: String, targetPath: String) {
    print("downloading $url to $targetPath ")
    val source = URL(url)
    val target = File(courseRoot, targetPath)
    if (!target.exists()) {
      source.copyTo(target)
      println("done!")
      }
    else {
      println("already downloaded!")
      }
    }

  }