package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * Flags any `MaterialTheme.*` access. The app exposes design tokens exclusively through `AppTheme.*`.
 */
class MaterialThemeRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "MaterialThemeRule",
    severity = Severity.Style,
    description = "MaterialTheme is not allowed; use AppTheme.* tokens instead.",
    debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (expression.receiverExpression.text == "MaterialTheme") {
      report(
        CodeSmell(
          issue,
          Entity.from(expression),
          "MaterialTheme.${expression.selectorExpression?.text}. Use AppTheme.* tokens instead.",
        ),
      )
    }
  }
}
