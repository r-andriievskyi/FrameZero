package com.frame.zero.shared.design_system

import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO, backgroundColor = 0xFFFFFFFF, showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000, showBackground = true)
annotation class LightDarkPreview
