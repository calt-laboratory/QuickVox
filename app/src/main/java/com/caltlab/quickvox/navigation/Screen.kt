package com.caltlab.quickvox.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main")
    object GroceryListScreen : Screen("grocery_list")
    object NotesScreen : Screen("notes")
}
