package dk.kalhauge.course.dsl

class Course(override val title: String, val root: String) : Target {
  override val link: String
    get() = if (root.isBlank()) "README.md" else "$root/course-info.md"

  val flows = mutableListOf<Flow>()
  }

fun course(title: String, root: String = "", build: Course.() -> Unit = {}) =
    Course(title, root).also { it.build() }