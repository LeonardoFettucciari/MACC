package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.macc2025.presentation.ui.AppTopBar
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

    Scaffold(topBar = { AppTopBar(title = "Ranking", navController = navController) }) { inner ->
        if (ranking.value.isEmpty()) {
            Box(Modifier.padding(inner).padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No ranking data")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                itemsIndexed(ranking.value) { index, (name, pts) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. $name")
                        Text("$pts")
                    }
                }
            }

        }
    }
}
