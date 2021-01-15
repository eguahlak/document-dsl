package dk.kalhauge.document.dsl

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InlineTest {

  @Test
  fun testSimpleContent() {
    val content = "This is a text"
    val text = text(content)
    val raw = text.nativeString()
    assertEquals(content, raw)
    }

  @Test
  fun testSimpleContentInline() {
    val content = "This is a text"
    val text = text() {
        text(content)
    }
    val raw = text.nativeString()
    assertEquals(content, raw)

    }

  @Test
  fun testContentWithBold() {
    val content = "This is a *bold* text"
    val text = text(content)
    val raw = text.nativeString()
    assertEquals(content, raw)
    }

  @Test
  fun testContentWithItalic() {
    val content = "This is a text it is /italic/"
    val text = Text(null)
    text.readContent(content.iterator())
    assertTrue(text.parts[0] is Content)
    assertTrue((text.parts[0] as Content).value == "This is a text it is ")
    assertTrue(text.parts[1] is Text)
    assertTrue((text.parts[1] as Text).parts[0] is Content)
    assertTrue((text.parts[1] as Text).format == Text.Format.ITALIC)
    }

  @Test
  fun testNestedContent() {
    val content = "This is a *bold* and an /italic text width _underlined_ / passages"
    val text = Text(null)
    text.readContent(content.iterator())
    assertEquals(content, text.nativeString())
    }

  @Test
  fun testNestedText() {
    val content = "This is a *bold* and an /italic text width _underlined_ / passages"
    val text = text(content)
    assertEquals(text.nativeString(), content)
    }

  @Test
  fun testNestedContentAndText() {
    val content = "This is a *bold* and an /italic text width _underlined_ / passages"
    val text = text(content) {
        bold("just a test")
    }
    println(text.toString())
    assertEquals(text.nativeString(), content+"*just a test*")
    }

}

