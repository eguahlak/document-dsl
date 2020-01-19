import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.handler.FileHost
import dk.kalhauge.document.handler.GfmHandler

val configuration = Configuration(hasTitle = true, hasNumbers = true)
val root = "/Users/AKA/tmp/course-dsl"

fun Folder.full() = document("full", "Main Page") {
    val r1 = link("https://www.dr.dk/p1", label = "slides-01")
    val s1 = section("First section version 1.10", label = "first") {
      link("https://www.kalhauge.dk")
      paragraph {
        text("A *tex*t")
        text("And one more", Text.Format.UNDERLINE)
        link("https://kotlin-lang.org", title = "Kotlin homepage")
        reference(r1)
        reference("../week-06/info/top", "Go to week 6")
        }
      paragraph {
        image("/Users/AKA/Pictures/Michellaneous/raven.png", link = "raven.png")
        image("$root/images/raven.png", link = "raven.png")
        image("http://www.kalhauge.dk/Mathias/mig_selv.png", link = "migselv.png")
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
        text { reference(s1) }
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
      reference("../../full/sec:second")
      }
    }
  section("Insertion sort") {
    paragraph("""bla *bla* bla""") {
      reference("../../full/top")
      }
    }
  }

fun main() {
  val host = FileHost(root)
  val context =
    folder("soft2020spring") {
      folder("ALG") {
        //resources = "cache"
        val doc1 = full()
        val doc2 = small()
        }
      }
  GfmHandler(host, configuration, context).handle(true, true)
  // Folder.targets.forEach { (label, target) -> println("$label --> $target") }
  }