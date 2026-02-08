package com.caltlab.quickvox.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object GroceryListScreen : Screen("grocery_list_screen")
    object NotesScreen : Screen("notes_screen")
}
