package com.example.macc.presentation.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.macc.data.local.TutorialPreferences
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltip(
    modifier: Modifier = Modifier,
    prefKey: String,
    richTooltipSubheadText: String = "Custom Rich Tooltip",
    richTooltipText: String = "Rich tooltips support multiple lines of informational text.",
    richTooltipActionText: String = "Got it",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val shownFlow = remember(prefKey) {
        TutorialPreferences.tooltipShownFlow(context, prefKey)
            .map { prefsValue -> prefsValue }
            .distinctUntilChanged()
    }

    val shown: Boolean? by shownFlow.collectAsState(initial = null)

    val isLoaded = shown != null

    val tooltipState = rememberTooltipState(isPersistent = true)

    LaunchedEffect(isLoaded, shown) {
        if (isLoaded && shown == false && !tooltipState.isVisible) {
            tooltipState.show()
        }
    }

    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(richTooltipSubheadText) },
                action = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            tooltipState.dismiss()
                            TutorialPreferences.setTooltipShown(context, prefKey)
                        }
                    }) {
                        Text(richTooltipActionText)
                    }
                }
            ) {
                Text(richTooltipText)
            }
        },
        state = tooltipState
    ) {
        content()
    }
}

