package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Ranking") }) }) { inner ->
        LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
            items(ranking.value) { (name, pts) ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name)
                    Text("$pts")
                }
            }
        }
    }
}
