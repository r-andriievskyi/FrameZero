package com.frame.zero.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class ForbiddenComposeApiRuleTest {

  private val rule = ForbiddenComposeApiRule()

  @Test
  fun `flags raw gap Spacer`() {
    val findings = rule.lint("fun f() { Spacer(modifier = Modifier.width(s)) }")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows weight Spacer`() {
    val findings = rule.lint("fun f() { Spacer(modifier = Modifier.weight(1f)) }")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows size Spacer`() {
    val findings = rule.lint("fun f() { Spacer(modifier = Modifier.size(s)) }")
    assertEquals(0, findings.size)
  }

  @Test
  fun `flags raw clickable`() {
    val findings = rule.lint("val m = Modifier.clickable { onClick() }")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows clickable with indication disabled`() {
    val findings = rule.lint("val m = Modifier.clickable(indication = null, onClick = onClick)")
    assertEquals(0, findings.size)
  }

  @Test
  fun `flags raw Preview annotation`() {
    val findings = rule.lint("@Preview\nfun PreviewFoo() {}")
    assertEquals(1, findings.size)
  }

  @Test
  fun `allows design system Spacer wrappers`() {
    val findings = rule.lint("fun f() { VerticalSpacer(spaceBy = s)\nHorizontalSpacer(spaceBy = s) }")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows clickableWithRipple`() {
    val findings = rule.lint("val m = Modifier.clickableWithRipple(color = c) { onClick() }")
    assertEquals(0, findings.size)
  }

  @Test
  fun `allows LightDarkPreview`() {
    val findings = rule.lint("@LightDarkPreview\nfun PreviewFoo() {}")
    assertEquals(0, findings.size)
  }

  @Test
  fun `ignores Spacer definition file`() {
    val findings = rule.lint(
      compileContentForTest("fun f() { Spacer(modifier = Modifier) }", "widgets/Spacer.kt"),
    )
    assertEquals(0, findings.size)
  }
}
