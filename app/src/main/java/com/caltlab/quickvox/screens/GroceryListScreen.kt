package com.caltlab.quickvox.screens

import android.R.attr.navigationIcon
import android.R.attr.title
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
                        }
                    ) {
                        Icon(
                            Icons.Default.Close, contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
}

private fun saveGroceryItems(context: android.content.Context, items: List<String>) {
    val sharedPreferences =
        context.getSharedPreferences("grocery", android.content.Context.MODE_PRIVATE)
    val jsonString = Json.encodeToString(items)
    sharedPreferences.edit().putString("items", jsonString).apply()
}

private fun loadGroceryItems(context: android.content.Context): List<String> {
    val sharedPreferences =
        context.getSharedPreferences("grocery", android.content.Context.MODE_PRIVATE)
    val jsonString = sharedPreferences.getString("items", null) ?: return emptyList()
    return Json.decodeFromString<List<String>>(jsonString)
}
