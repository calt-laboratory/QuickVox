package com.caltlab.quickvox.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.caltlab.quickvox.navigation.Screen
import com.caltlab.quickvox.ui.components.AppNameHeader

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AppNameHeader()

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate(Screen.GroceryListScreen.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Grocery List")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { navController.navigate(Screen.NotesScreen.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Notes")
        }
    }
}