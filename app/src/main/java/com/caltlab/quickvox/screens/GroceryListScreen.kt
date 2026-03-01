package com.caltlab.quickvox.screens

import android.R.attr.navigationIcon
import android.R.attr.title
import androidx.core.content.edit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var groceryItems = remember {
        mutableStateListOf<String>().apply {
            addAll(loadGroceryItems(context))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Grocery List") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // item {} adds a single entry to the LazyColumn's scrollable list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("New Item") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true
                    )
                    OutlinedButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                groceryItems.add(inputText.trim())
                                saveGroceryItems(context, groceryItems)
                                inputText = ""
                            }
                        }) {
                        Text("Add")
                    }
                }
            }

            if (groceryItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete all items"
                            )
                        }
                    }
                }
            }

            itemsIndexed(groceryItems) { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item, modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        groceryItems.removeAt(idx)
                        saveGroceryItems(context, groceryItems)
                    }) {
                        Icon(
                            Icons.Default.Close, contentDescription = "Delete"
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                // onDismissRequest = {} Called when the user taps outside the dialog or presses the
                // back button
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Items?") },
                text = { Text("This will remove all items from the list") },
                confirmButton = {
                    TextButton(onClick = {
                        groceryItems.clear()
                        saveGroceryItems(context, groceryItems)
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                })
        }
    }
}

private fun saveGroceryItems(context: android.content.Context, items: List<String>) {
    // SharedPreferences is a simple key-value storage, saved as an XML file on the device
    // Data is kept even after closing the app
    // Only this app can access it (MODE_PRIVATE)
    // "grocery" is the name of the SharedPreferences file
    val sharedPreferences = context.getSharedPreferences(
        "grocery",
        android.content.Context.MODE_PRIVATE,
    )

    // Convert to JSON string
    val jsonString = Json.encodeToString(items)

    // edit {} opens the editor for editing the SharedPreferences, calls apply() automatically
    // putString() saves the JSON string under the key "items"
    // apply() writes the change in the background (async) to the file
    sharedPreferences.edit {
        putString("items", jsonString)
    }
}

private fun loadGroceryItems(context: android.content.Context): List<String> {
    val sharedPreferences =
        context.getSharedPreferences("grocery", android.content.Context.MODE_PRIVATE)

    // getString() reads the value under the key "items", returns null if nothing is saved
    // ?: is the Elvis operator: if the left side is null, execute the right side instead
    // -> if nothing is saved, exit the function early and return an empty list
    val jsonString = sharedPreferences.getString("items", null) ?: return emptyList()

    // Convert the JSON string back to a list of strings
    return Json.decodeFromString<List<String>>(jsonString)
}
