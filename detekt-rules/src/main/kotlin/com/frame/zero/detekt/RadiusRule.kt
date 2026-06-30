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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * `Dp`-valued corner arguments of `RoundedCornerShape` / `rememberRoundedCornerShape` /
 * `CutCornerShape` must come from `AppTheme.radiusSystem.*`. Catches raw `N.dp` corners and the
 * wrong-token case (e.g. a `spacingSystem` token used as a corner radius). Percent-`Int` corners
 * (`RoundedCornerShape(50)`) and `CircleShape` (no args) are skipped.
 */
class RadiusRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "RadiusRule",
    severity = Severity.Style,
    description = "Corner radius must come from AppTheme.radiusSystem.*.",
    debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (expression.calleeExpression?.text !in SHAPE_CALLEES) return

    expression.valueArguments.forEach { arg -> checkArgument(arg) }
  }

  private fun checkArgument(arg: KtValueArgument) {
    val expr = arg.getArgumentExpression() ?: return

    // Percent corners, e.g. RoundedCornerShape(50): plain Int literal — allowed.
    if (expr is KtConstantExpression) return

    if (isRawDp(expr)) {
      report(
        CodeSmell(
          issue,
          Entity.from(arg),
          "Raw corner radius '${arg.text}'. Use an AppTheme.radiusSystem.* token.",
        ),
      )
      return
    }

    // Structural match on the token-system accessor names in the chain (not a text
    // substring), so a plain identifier that merely contains "System" is not flagged.
    val names = expr.collectDescendantsOfType<KtNameReferenceExpression>().mapTo(mutableSetOf()) { it.text }
    if (RADIUS_SYSTEM in names) return
    val wrongSystem = names.firstOrNull { it in OTHER_TOKEN_SYSTEMS } ?: return
    report(
      CodeSmell(
        issue,
        Entity.from(arg),
        "Corner radius '${expr.text}' uses $wrongSystem. Use AppTheme.radiusSystem.*.",
      ),
    )
  }

  private fun isRawDp(expr: KtExpression): Boolean {
    if (expr !is KtDotQualifiedExpression) return false
    val selector = expr.selectorExpression
    return selector is KtNameReferenceExpression &&
      selector.text == "dp" &&
      expr.receiverExpression is KtConstantExpression
  }

  private companion object {
    val SHAPE_CALLEES = setOf("RoundedCornerShape", "rememberRoundedCornerShape", "CutCornerShape")
    const val RADIUS_SYSTEM = "radiusSystem"
    val OTHER_TOKEN_SYSTEMS = setOf("spacingSystem", "borderSystem", "typographySystem", "colorSystem")
  }
}
