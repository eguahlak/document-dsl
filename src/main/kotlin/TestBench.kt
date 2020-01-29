import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.handler.FileHost
import dk.kalhauge.document.handler.GfmHandler

val conf = Configuration(hasTitle = true, hasNumbers = true)
val imageRoot = conf["image.root"]

fun Folder.full() = document("full", "Main Page") {
    toc(1, level = 4)
    val r1 = link("https://www.dr.dk/p1", label = "slides-01")
    val s1 = section("First section version 1.10", label = "first") {
      link("https://www.kalhauge.dk")
      val p1 = paragraph("P1")
      p1 += "Hello"
      paragraph {
        text("A *tex*t")
        text("And one more", Text.Format.UNDERLINE)
        link("https://kotlin-lang.org", title = "Kotlin homepage")
        reference(r1)
        reference("../week-06/info/top", "Go to week 6")
        }

      paragraph {
        image("/Users/AKA/Pictures/Michellaneous/raven.png", name = "raven.png")
        image("$imageRoot/raven.png", name = "raven.png")
        image("http://www.kalhauge.dk/Mathias/mig_selv.png", name = "migselv.png")
        }
      paragraph("another text")
      java("""
        public class Person {
          private final int id;
          }
        """.trimIndent())
      list {
        paragraph("First")
        paragraph {
          text {
            text("The `Second` ")
            text("*Second and a half* ")
            text { reference("../week-06/info/sec:insertion-sort") }
            }
          }
        paragraph("Third")
        list(Listing.Type.ARABIC) {
          //section("In a list?") {
            paragraph("some content")
          //}
          paragraph("Uno")
          paragraph("Due")
          paragraph("Tre")
        }
        paragraph("Fourth")
      }
    }
    section("Second") {
      section("Seconds first")
      paragraph { text("Lorem ipsum") }
      section("More about killroy") {
        title = text {
          text("A very ")
          italic {
            text("cursive and ")
            code("code()")
            text(" ")
            bold { text("BOLD") }
            }
          text(" decision")
          }
        paragraph { reference(s1) }
      }

      table {
        left("Full Name")
        center("Grade")
        right("Salary")
        /*
        column("Full Name")
        column("Grade", HorisontalAlignment.CENTER)
        column("Salary", HorisontalAlignment.RIGHT)
        */

        row {
          paragraph {
            text("Kurt **Hansen** Bille")
          }
          paragraph("-3")
          paragraph("40.000")
        }
        row {
          paragraph("Sonja *Jensen*")
          paragraph("12")
          paragraph("30.000")
        }
      }

    }
  }

fun Folder.small() = document("week-06/info", "Sorting algorithms") {
  list {
    paragraph("First")
    paragraph("Second") {
      text(":pencil: ") {
        reference("../../full/sec:second")
        }
      }
    }
  section("Insertion sort") {
    paragraph("""bla *bla* bla""") {
      reference("../../full/top")
      }
    }
  }

fun main() {
//  val host = FileHost(root)
  val host = FileHost(conf)
  val context =
    folder("soft2020spring") {
      file("notes.txt", """Hello the {{name}} how are you?""", mapOf("name" to "Kurt Hansen"))
      folder("ALG") {
        //resources = "cache"
        full()
        small()
        }
      }
  GfmHandler(host, context).handle(true, true)
  // Folder.targets.forEach { (label, target) -> println("$label --> $target") }
  }