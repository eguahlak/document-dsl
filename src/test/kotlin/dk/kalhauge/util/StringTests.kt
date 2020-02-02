package dk.kalhauge.util

import org.junit.Assert.*
import org.junit.Test

class StringTests {

  @Test
  fun testCommonRoot() {
    assertEquals(
      "../../ALG/cache/pic.png",
      "/docs/ALG/cache/pic.png" from "/docs/ML/week-09"
      )
    }

  @Test
  fun testCommonPath() {
    assertEquals(
      "all/files/pic.png",
      "/docs/ML/week-09/all/files/pic.png" from "/docs/ML/week-09"
      )
    }


  @Test
  fun testCommonParent() {
    assertEquals(
      "../resources/pic.png",
      "/docs/ML/resources/pic.png" from "/docs/ML/week-09"
      )
    }

  @Test
  fun testReoccuringNames() {
    assertEquals(
      "../../resources/pic.png",
      "/docs/ML/resources/pic.png" from "/docs/ML/week-09/resources"
      )
    }

  @Test
  fun testDefault() {
    assertEquals(
      "../../resources/pic.png",
      "/docs/resources/pic.png" from "/docs/ML/week-09"
      )
    }

  @Test
  fun testNomalizedNoNameNoDots() {
    assertEquals(
      "/A/B/C",
      normalize("/A/B/C")
      )
    }

  @Test
  fun testNomalizedNoNameSingleDot() {
    assertEquals(
      "/A/B/C",
      normalize("/A/B/./C")
      )
    }

  @Test
  fun testNomalizedNoNameSingleDots() {
    assertEquals(
      "/A/B/C",
      normalize("/A/././B/./C")
      )
    }

  @Test
  fun testNomalizedNoNameDoubleDot() {
    assertEquals(
      "/A/B/C",
      normalize("/A/D/../B/C")
      )
    }

  @Test
  fun testNomalizedRootName() {
    assertEquals(
      "/X/Y/Z",
      normalize("/A/D/B/C", "/X/Y/Z")
      )
    }

  @Test
  fun testNomalizedDottetName() {
    assertEquals(
      "/A/B/C/X/Y/Z",
      normalize("/A/B/C/D", "../X/./Y/Z")
      )
    }


  }
