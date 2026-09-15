package com.artrubadur.teno.ui.screens.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artrubadur.teno.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun toolsShortcutOpensTools() {
        var clicked = false

        composeRule.setContent {
            AppTheme {
                HomeScreenContent(
                    state = HomeState(),
                    activeConnectionName = null,
                    activeConnectionKind = null,
                    onOpenChat = {},
                    onOpenConnections = {},
                    onOpenSettings = {},
                    onOpenTools = { clicked = true },
                    onOverlayEnabledChange = {},
                )
            }
        }

        composeRule.onNodeWithText("Tools").performClick()

        assertTrue(clicked)
    }
}
