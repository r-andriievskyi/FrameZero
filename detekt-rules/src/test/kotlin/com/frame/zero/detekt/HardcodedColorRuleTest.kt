package com.frame.zero.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class HardcodedColorRuleTest {

  private val rule = HardcodedColorRule()

  @Test
  fun `flags Color constructor literal`() {
    val findings = rule.lint("val c = Color(0xFFFF0000)")
    assertEquals(1, findings.size)
  }

  @Test
  fun `flags Color named reference`() {
    val findings = rule.lint("val c = Color.Red")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows AppTheme color token`() {
    val findings = rule.lint("val c = AppTheme.colorSystem.surface")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows dynamic Color construction from data`() {
    val findings = rule.lint("""fun f(hex: String) = Color(("FF" + hex).toLong(16))""")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows Transparent and Unspecified`() {
    val findings = rule.lint("val a = Color.Transparent\nval b = Color.Unspecified")
    assertEquals(0, findings.size)
  }

  @Test
  fun `ignores token files`() {
    val findings = rule.lint(
      compileContentForTest(
        "val token = Color(0xFFFF0000)",
        "tokens/ColorTokens.kt",
      ),
    )
    assertEquals(0, findings.size)
  }
}
