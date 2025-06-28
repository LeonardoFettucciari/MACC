package com.example.macc.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.macc.data.local.TutorialPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltip(
    modifier: Modifier = Modifier,
    prefKey: String? = null,
    richTooltipSubheadText: String = "Custom Rich Tooltip",
    richTooltipText: String = "Rich tooltips support multiple lines of informational text.",
    richTooltipActionText: String = "Got it",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val shownFlow = remember(prefKey) {
        prefKey?.let { TutorialPreferences.tooltipShownFlow(context, it) }
    }

    val shown by shownFlow?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    var resolved by remember { mutableStateOf(false) }

    LaunchedEffect(shown) {
        // Mark that the value has been resolved from DataStore
        resolved = true
    }

    val tooltipState = rememberTooltipState(isPersistent = true)

    // Show tooltip only once, after the `shown` value is known
    LaunchedEffect(resolved, shown) {
        if (resolved && !shown && !tooltipState.isVisible) {
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
                    Row {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                tooltipState.dismiss()
                                prefKey?.let { TutorialPreferences.setTooltipShown(context, it) }
                            }
                        }) {
                            Text(richTooltipActionText)
                        }
                    }
                },
            ) {
                Text(richTooltipText)
            }
        },
        state = tooltipState
    ) {
        content()
    }
}

