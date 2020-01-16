package dk.kalhauge.util

import org.junit.Assert.*
import org.junit.Test

class StringTests {

  @Test
  fun testCommonRoot() {
    assertEquals("../../ALG/cache/pic.png", "docs/ALG/cache/pic.png" from "docs/ML/week-09/info.md")
    }

  @Test
  fun testCommonPath() {
    assertEquals("all/files/pic.png", "docs/ML/week-09/all/files/pic.png" from "docs/ML/week-09/info.md")
    }


  @Test
  fun testCommonParent() {
    assertEquals("../resources/pic.png", "docs/ML/resources/pic.png" from "docs/ML/week-09/info.md")
    }

  @Test
  fun testReoccuringNames() {
    assertEquals("../../resources/pic.png", "docs/ML/resources/pic.png" from "docs/ML/week-09/resources/info.md")
    }

  @Test
  fun testDefault() {
    assertEquals("../../resources/pic.png", "docs/resources/pic.png" from "docs/ML/week-09/info.md")
    }

  }
