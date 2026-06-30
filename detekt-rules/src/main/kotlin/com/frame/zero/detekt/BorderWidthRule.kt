package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * The width of `Modifier.border(...)` must come from `AppTheme.borderSystem.*`. Catches a raw `.dp`
 * width as well as the wrong-token case (a `spacingSystem`/`radiusSystem` token used as a border
 * width). Only the call-site direction is enforced; whether `borderSystem` is used elsewhere is out
 * of scope.
 */
class BorderWidthRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "BorderWidthRule",
    severity = Severity.Style,
    description = "Border width must come from AppTheme.borderSystem.*.",
    debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (expression.calleeExpression?.text != "border") return

    val args = expression.valueArguments
    val widthArg = args.firstOrNull { it.getArgumentName()?.asName?.asString() == "width" }
      ?: args.firstOrNull { it.getArgumentName() == null }
      ?: return

    if (!referencesBorderSystem(widthArg)) {
      report(
        CodeSmell(
          issue,
          Entity.from(widthArg),
          "Border width '${widthArg.text}' must come from AppTheme.borderSystem.* (e.g. borderSystem.hairline).",
        ),
      )
    }
  }

  private fun referencesBorderSystem(arg: KtValueArgument): Boolean =
    arg.getArgumentExpression()?.text?.contains("borderSystem") == true
}
