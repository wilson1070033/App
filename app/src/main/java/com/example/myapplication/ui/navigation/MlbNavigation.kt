package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screen.GamesScreen
import com.example.myapplication.ui.screen.HomeScreen
import com.example.myapplication.ui.screen.PlayerScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Games : Screen("games")
    object Players : Screen("players")
}

@Composable
fun MlbNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGames = { navController.navigate(Screen.Games.route) },
                onNavigateToPlayers = { navController.navigate(Screen.Players.route) }
            )
        }
        
        composable(Screen.Games.route) {
            GamesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Players.route) {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}