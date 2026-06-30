package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Flags raw `N.dp` / `N.sp` literals on numeric constants. Visual dimensions must come from
 * `AppTheme.spacingSystem` / `typographySystem` / `radiusSystem` tokens, or a hoisted top-of-file
 * `Dp` val. The `design_system/tokens/` definitions are exempt. `allowedValues` (default `["0"]`)
 * permits a small set of literals.
 */
class RawDimensionRule(config: Config = Config.empty) : Rule(config) {

  override val issue = Issue(
    id = "RawDimensionRule",
    severity = Severity.Style,
    description = "Raw dp/sp literals are not allowed; use AppTheme tokens or a hoisted Dp val.",
    debt = Debt.FIVE_MINS,
  )

  private val allowedValues: List<String>
    get() = valueOrDefault("allowedValues", listOf("0"))

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (isExcluded(expression)) return
    if (isHoistedTopLevelVal(expression)) return
    val selector = expression.selectorExpression
    if (selector is KtNameReferenceExpression && (selector.text == "dp" || selector.text == "sp")) {
      val receiver = expression.receiverExpression
      if (receiver is KtConstantExpression && receiver.text !in allowedValues) {
        report(
          CodeSmell(
            issue,
            Entity.from(expression),
            "Raw ${receiver.text}.${selector.text} literal. Use an AppTheme token or a hoisted Dp val.",
          ),
        )
      }
    }
  }

  private fun isExcluded(element: KtElement): Boolean =
    element.fileName().endsWith("Tokens.kt") || element.filePath().contains("/tokens/")

  // The design system allows element/border sizes hoisted to top-of-file Dp vals,
  // e.g. `private val ToolbarHeight = 56.dp`. Allow a literal that is the direct
  // initializer of such a top-level property.
  private fun isHoistedTopLevelVal(expression: KtDotQualifiedExpression): Boolean {
    val property = expression.parent as? KtProperty ?: return false
    return property.isTopLevel && property.initializer === expression
  }
}
