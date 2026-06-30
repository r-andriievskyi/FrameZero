package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class RadiusRuleTest {

  private val rule = RadiusRule()

  @Test
  fun `flags wrong token system as corner radius`() {
    val findings = rule.lint("val s = RoundedCornerShape(AppTheme.spacingSystem.space8)")
    assertEquals(1, findings.size)
  }

  @Test
  fun `flags raw dp corner radius`() {
    val findings = rule.lint("val s = RoundedCornerShape(16.dp)")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows radiusSystem token`() {
    val findings = rule.lint("val s = RoundedCornerShape(AppTheme.radiusSystem.radius8)")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows plain Dp val whose name merely contains System`() {
    val findings = rule.lint("val s = RoundedCornerShape(cardSystemRadius)")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows percent int corner`() {
    val findings = rule.lint("val s = RoundedCornerShape(50)")
    assertEquals(0, findings.size)
  }

  @Test
  fun `ignores CircleShape`() {
    val findings = rule.lint("val s = CircleShape")
    assertEquals(0, findings.size)
  }
}
