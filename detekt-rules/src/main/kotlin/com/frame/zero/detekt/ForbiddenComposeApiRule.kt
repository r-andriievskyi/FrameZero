package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Flags raw Compose primitives that the design system wraps:
 *  - `Spacer(...)`        → use `VerticalSpacer` / `HorizontalSpacer`
 *  - `clickable(...)`     → use `Modifier.clickableWithRipple(...)`
 *  - `@Preview`           → use `@LightDarkPreview`
 *
 * The design-system files that define those wrappers are exempt.
 */
class ForbiddenComposeApiRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "ForbiddenComposeApiRule",
    severity = Severity.Style,
    description = "Use the design-system wrapper instead of the raw Compose API.",
    debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    when (expression.calleeExpression?.text) {
      "Spacer" -> if (expression.fileName() != "Spacer.kt" && isGapSpacer(expression)) {
        report(
          CodeSmell(
            issue,
            Entity.from(expression),
            "Raw gap Spacer(...). Use VerticalSpacer/HorizontalSpacer from the design system.",
          ),
        )
      }
      "clickable" -> if (expression.fileName() != "ClickableWithRipple.kt" && !disablesIndication(expression)) {
        report(
          CodeSmell(
            issue,
            Entity.from(expression),
            "Raw Modifier.clickable(...). Use Modifier.clickableWithRipple(...).",
          ),
        )
      }
    }
  }

  // Only a fixed-gap Spacer (Modifier.height(...)/width(...)) maps to the design-system
  // wrappers. Flexible weight() fillers and size() placeholders are left alone.
  private fun isGapSpacer(expression: KtCallExpression): Boolean {
    val modifier = expression.valueArguments.firstOrNull()?.text ?: return false
    return (".height(" in modifier || ".width(" in modifier) && ".weight(" !in modifier
  }

  // A clickable that explicitly opts out of indication (indication = null) intentionally
  // has no ripple; clickableWithRipple would change that, so it is not a violation.
  private fun disablesIndication(expression: KtCallExpression): Boolean =
    expression.valueArguments.any {
      it.getArgumentName()?.asName?.asString() == "indication" &&
        it.getArgumentExpression()?.text == "null"
    }

  override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
    super.visitAnnotationEntry(annotationEntry)
    if (annotationEntry.shortName?.asString() == "Preview" &&
      annotationEntry.fileName() != "LightDarkPreview.kt"
    ) {
      report(
        CodeSmell(
          issue,
          Entity.from(annotationEntry),
          "Raw @Preview. Use @LightDarkPreview from the design system.",
        ),
      )
    }
  }
}
