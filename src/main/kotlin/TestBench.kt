import dk.kalhauge.document.dsl.*
import dk.kalhauge.document.dsl.graphs.graph
import dk.kalhauge.document.dsl.structure.root
import dk.kalhauge.document.handler.FileHost
import dk.kalhauge.document.handler.GfmHandler

val conf = Configuration(hasTitle = true, hasNumbers = true, outputLevel = Configuration.OutputLevel.VERBOSE)
val imageRoot = conf["image.root"]

fun Folder.full() = document("full", "Main Page") {
    toc()
    val r1 = link(Address.Web("https://www.dr.dk/p1"), label = "/slides-01")
    val s1 = section("First section version 1.10", label = "first") {
      link(Address.Web("https://www.kalhauge.dk"))
      val p1 = paragraph("P1")
      p1 += "Hello"
      paragraph {
        text("A *tex*t")
        text("And one more", Text.Format.UNDERLINE)
        link(Address.Web("https://kotlin-lang.org"), title = "Kotlin homepage", label = "KOTLIN")
        reference(r1)
        reference("../week-06/info", "Go to week 6")
        }
      graph("A Graph", "GRAPHA", "GRAPHA") {
        val GRAPHT = box("Graph Theory")
        val c1 = cluster("Databases") {
          val SQL = box("SQL Server")
          val DB = ellipse("Databases")
          val GRAPH_DB = ellipse("Graph DB")
          SQL.edge(DB)
          }
        c1["Graph DB"]?.edge(GRAPHT)
        }
      paragraph {
        image(Address("/Users/AKA/Pictures/Michellaneous/raven.png"), name = "raven.png")
        image(Address("$imageRoot/raven.png"), name = "raven.png")
        image(Address("http://www.kalhauge.dk/Mathias/mig_selv.png"), name = "migselv.png")
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
            text { reference("../week-06/info/sec=insertion-sort") }
            }
          }
        paragraph("Third")
        list(Listing.Type.ARABIC) {
          //section("In a list?") {
            paragraph("some `content` with {*P1*:/slides-01} and {KOTLIN} `look_in` section: {sec=second}")
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
    section("Third", label = "THIRD") {
      paragraph("Tredje")
      }
    section("Fourth", label = "/NO4") {
      paragraph {
        reference("sec=second", "T2")
        reference("THIRD", "T3")
        reference("/NO4", "T4")
        }
      }
    section("Table from csv") {
      table {
        left("First Name")
        left("Last Name") { it.toUpperCase() }
        left("E-mail")
        row {
          paragraph("Martin")
          paragraph("Kurt")
          paragraph("abc@mail.dk")
          }
        row {
          paragraph("Marianne")
          paragraph("Herbert")
          paragraph("andersen@mail.dk")
          }
        csv("info/l20sou1ae.csv", skipLineCount = 1)
        criterion(1) { it.length > 6 }
        criterion(0) { it.startsWith("M") }
        }
      }
    section("Two empty tables") {
      paragraph("with hideIfEmpty = `true` (/default/)")
      table {
        left("Name")
        right("Salary")
        }
      paragraph("with hideIfEmpty = `false`")
      table {
        hideIfEmpty = false
        left("Name")
        right("Salary")
        }
      quote("""
        And a quoted string
        It downcases the string
        remove anything that is not a letter, number, space or hyphen 
        (see the source for how Unicode is handled)
        changes any space to a hyphen.
        If that is not unique, add "-1", "-2", "-3",... to make it unique
        """.trimIndent())
      capture("to mock a Mockingbird") {
        text("Raymond Smullyan")
        text("Oxford University Press")
        }
      list {
        capture("to mock a Mockingbird") {
          text("Raymond Smullyan")
          text("Oxford University Press")
          }
        capture("to mock a Mockingbird") {
          text("Raymond Smullyan")
          text("Oxford University Press")
          }
        }
      }
  }

val someSection = section("Some Section", label = "SOME") {
  paragraph("Text to some section")
  }

fun Folder.small() = document("week-06/info", "Sorting algorithms") {
  list {
    paragraph("First")
    paragraph("Secondisch") {
      text(":pencil: ") {
        reference("../../full/sec=second")
        }
      }
    }
  add(someSection)
  section("Insertion sort") {
    paragraph("""bla *bla* bla""") {
      reference("../../full")
      reference("SOME")
      }
    }
  section("Student List") {
    table {
      left("Name")
      left("Email") { "[$it](mailto:$it)" }
      csv("info/l20sou1af.csv")
      }
    }
  }

fun main() {
  val host = FileHost(conf)
  val context = root {
    folder("docs") {
      file("notes.txt", """Hello the {{name}} how are you?""", mapOf("name" to "Kurt Hansen"))
      folder("ALG") {
        //resources = "cache"
        full()
        small()
      }
    }
  }
  GfmHandler(host, context).handle(true)
  // Folder.targets.forEach { (label, target) -> println("$label --> $target") }
  }