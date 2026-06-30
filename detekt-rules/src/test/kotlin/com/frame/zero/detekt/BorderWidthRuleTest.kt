package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class BorderWidthRuleTest {

  private val rule = BorderWidthRule()

  @Test
  fun `flags raw dp border width`() {
    val findings = rule.lint("val m = Modifier.border(width = 1.dp, color = c, shape = s)")
    assertEquals(1, findings.size)
  }

  @Test
  fun `flags wrong token system as border width`() {
    val findings = rule.lint("val m = Modifier.border(AppTheme.spacingSystem.space2, c, s)")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows borderSystem token`() {
    val findings = rule.lint("val m = Modifier.border(width = AppTheme.borderSystem.hairline, color = c, shape = s)")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows positional borderSystem token`() {
    val findings = rule.lint("val m = Modifier.border(AppTheme.borderSystem.hairline, c, s)")
    assertEquals(0, findings.size)
  }
}
