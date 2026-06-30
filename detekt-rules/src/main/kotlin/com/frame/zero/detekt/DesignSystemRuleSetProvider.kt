package com.frame.zero.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class DesignSystemRuleSetProvider : RuleSetProvider {

  override val ruleSetId: String = "design-system"

  override fun instance(config: Config): RuleSet = RuleSet(
    ruleSetId,
    listOf(
      HardcodedColorRule(config),
      RawDimensionRule(config),
      MaterialThemeRule(config),
      BorderWidthRule(config),
      RadiusRule(config),
      ForbiddenComposeApiRule(config),
    ),
  )
}
