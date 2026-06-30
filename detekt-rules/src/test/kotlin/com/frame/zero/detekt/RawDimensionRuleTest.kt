package com.frame.zero.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class RawDimensionRuleTest {

  private val rule = RawDimensionRule()

  @Test
  fun `flags raw dp literal`() {
    val findings = rule.lint("fun f() { Modifier.height(16.dp) }")
    assertEquals(1, findings.size)
  }

  @Test
  fun `flags raw sp literal`() {
    val findings = rule.lint("fun f() { val style = TextStyle(fontSize = 14.sp) }")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows zero dp`() {
    val findings = rule.lint("val x = 0.dp")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows AppTheme spacing token`() {
    val findings = rule.lint("val x = AppTheme.spacingSystem.space16")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows hoisted top-level Dp val`() {
    val findings = rule.lint("private val ToolbarHeight = 56.dp")
    assertEquals(0, findings.size)
  }

  @Test
  fun `flags inline dp even when a local val`() {
    val findings = rule.lint("fun f() { val x = 16.dp }")
    assertEquals(1, findings.size)
  }

  @Test
  fun `ignores token files`() {
    val findings = rule.lint(
      compileContentForTest("val token = 16.dp", "tokens/SpacingTokens.kt"),
    )
    assertEquals(0, findings.size)
  }
}
