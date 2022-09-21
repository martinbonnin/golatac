package golatac

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class MainTest {

  @Test
  fun test() {
    init(File("test-files/libs.versions.toml"))

    assertEquals("com.mycompany:mylib:1.4", lib("my.lib1"))
    assertEquals("com.mycompany:mylib:1.4", lib("my.lib2"))
    assertEquals("com.mycompany:mylib:1.4", lib("my.lib3"))
    assertEquals("com.mycompany:mylib:1.4", lib("my.lib5"))
  }
}