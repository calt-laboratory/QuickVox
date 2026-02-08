package com.caltlab.quickvox.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caltlab.quickvox.screens.GroceryListScreen
import com.caltlab.quickvox.screens.MainScreen
import com.caltlab.quickvox.screens.NotesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.GroceryListScreen.route) {
            GroceryListScreen()
        }

        composable(Screen.NotesScreen.route) {
            NotesScreen()
        }
    }
}
