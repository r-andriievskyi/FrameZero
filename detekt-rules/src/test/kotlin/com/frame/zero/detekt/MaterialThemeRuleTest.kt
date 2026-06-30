package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialThemeRuleTest {

  private val rule = MaterialThemeRule()

  @Test
  fun `flags MaterialTheme access`() {
    val findings = rule.lint("val c = MaterialTheme.colorScheme")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows AppTheme access`() {
    val findings = rule.lint("val c = AppTheme.colorSystem")
    assertEquals(0, findings.size)
  }
}
