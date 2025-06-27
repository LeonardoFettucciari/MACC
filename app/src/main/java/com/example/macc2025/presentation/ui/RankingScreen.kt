package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.example.macc2025.presentation.ui.AppTopBar
import com.example.macc2025.presentation.ui.AppBottomBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(navController: NavController, viewModel: ProfileViewModel) {
    val ranking = viewModel.ranking.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRanking() }

    Scaffold(
        topBar = { AppTopBar(title = "Ranking") },
        bottomBar = { AppBottomBar(navController) }
    ) { inner ->
        if (ranking.value.isEmpty()) {
            Box(Modifier.padding(inner).padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No ranking data")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                itemsIndexed(ranking.value) { index, (name, pts) ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        ListItem(
                            headlineText = { Text("${index + 1}. $name") },
                            supportingText = { Text("$pts pts") }
                        )
                    }
                }
            }

        }
    }
}
