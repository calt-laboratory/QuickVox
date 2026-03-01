package com.caltlab.quickvox.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main")
    object VoiceGroceryListScreen : Screen("voice_based_grocery_list")
    object TextGroceryListScreen : Screen("text_based_grocery_list")
    object NotesScreen : Screen("notes")
}
