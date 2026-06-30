package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement

/**
 * Flags hardcoded colors — `Color(0xFF…)` constructor calls and `Color.<X>` references — that should
 * instead come from `AppTheme.colorSystem.*`. The design-system files that legitimately define raw
 * colors (token files, color systems, theme assembly) are exempt.
 */
class HardcodedColorRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "HardcodedColorRule",
    severity = Severity.Style,
    description = "Hardcoded colors are not allowed; use AppTheme.colorSystem.* tokens instead.",
    debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (isExcluded(expression)) return
    // Only a literal Color(...) is "hardcoded". Dynamic construction from data
    // (e.g. Color(("FF" + hex).toLong(16))) is legitimate and must not be flagged.
    if (expression.calleeExpression?.text == "Color" && isLiteralColor(expression)) {
      report(
        CodeSmell(
          issue,
          Entity.from(expression),
          "Hardcoded Color(...) literal. Use an AppTheme.colorSystem.* token.",
        ),
      )
    }
  }

  private fun isLiteralColor(expression: KtCallExpression): Boolean {
    val args = expression.valueArguments
    return args.isNotEmpty() && args.all { it.getArgumentExpression() is KtConstantExpression }
  }

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (isExcluded(expression)) return
    if (expression.receiverExpression.text == "Color") {
      val selector = expression.selectorExpression?.text
      if (selector != null && ALLOWED.none { selector == it || selector.startsWith("$it.") }) {
        report(
          CodeSmell(
            issue,
            Entity.from(expression),
            "Hardcoded Color.$selector. Use an AppTheme.colorSystem.* token.",
          ),
        )
      }
    }
  }

  private fun isExcluded(element: KtElement): Boolean {
    val name = element.fileName()
    return name.endsWith("Tokens.kt") ||
      name == "ColorSystem.kt" ||
      name == "ColorExtensions.kt" ||
      name.contains("Theme") ||
      element.filePath().contains("/tokens/")
  }

  private companion object {
    val ALLOWED = setOf("Transparent", "Unspecified")
  }
}
