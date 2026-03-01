package com.caltlab.quickvox.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGroceryListScreen(navController: NavController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val grocerItems = remember {
        mutableStateListOf<String>().apply {
            addAll(loadVoiceGroceryItems(context))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Grocery List (Voice)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        // Toggles between recording and not recording
                        onClick = { isRecording = !isRecording },
                        // Red button when recording, default color when idle
                        colors = if (isRecording) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.size(8.dp))

                        Text(if (isRecording) "Stop" else "Record")
                    }
                }
            }
            if (grocerItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showDeleteDialog = true } ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete all items"
                            )
                        }
                    }
                }
            }

            itemsIndexed(grocerItems) { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        grocerItems.removeAt(idx)
                        saveVoiceGroceryItems(context, grocerItems)
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Items") },
                text = { Text("This will remove all items from the list") },
                confirmButton = {
                    TextButton(onClick = {
                        grocerItems.clear()
                        saveVoiceGroceryItems(context, grocerItems)
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun saveVoiceGroceryItems(context: android.content.Context, items: List<String>) {
    val sharedPreferences = context.getSharedPreferences(
        "voice_grocery",
        android.content.Context.MODE_PRIVATE
    )
    val jsonString = Json.encodeToString(items)
    sharedPreferences.edit {
        putString("items", jsonString)
    }
}

private fun loadVoiceGroceryItems(context: android.content.Context): List<String> {
    val sharedPreferences = context.getSharedPreferences(
        "voice_grocery",
        android.content.Context.MODE_PRIVATE
        )
    val jsonString = sharedPreferences.getString("items", null) ?: return emptyList()
    return Json.decodeFromString<List<String>>(jsonString)
}