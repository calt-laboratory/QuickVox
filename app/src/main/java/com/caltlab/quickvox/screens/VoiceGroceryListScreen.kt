package com.caltlab.quickvox.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGroceryListScreen(navController: NavController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var highlightedRecentlyAddedItems by remember { mutableStateOf<List<String>>(emptyList()) }
    val groceryListState = rememberLazyListState()
    var remainingRecordingSeconds by remember { mutableIntStateOf(0) }
    var currRootMeanSquareDecibel by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current

    // Checks on screen start if the app already has microphone permission (true/false)
    var hasPermission by remember {
        mutableStateOf(
            // Asks the OS: "Does this app have microphone permission?"
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                // PackageManager = Android's system that manages all installed apps and their
                // permissions
                // PERMISSION_GRANTED is just a constant (value 0) meaning "yes, allowed"
                PackageManager.PERMISSION_GRANTED,
        )
    }

    // Opens the system permission dialog when the user presses Record, then saves the user's choice
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasPermission = granted
        }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    // DisposableEffect = Compose lifecycle block that runs cleanup code
    DisposableEffect(Unit) {
        // onDispose is called when the user leaves this screen (e.g. back button)
        onDispose {
            // destroy() deletes the recognizer object and frees its system resources
            // If it's still recording, the recording stops too, but that's just a side effect
            // The main purpose is cleanup: free memory when leaving the screen
            speechRecognizer.destroy()
        }
    }

    val groceryItems =
        remember {
            mutableStateListOf<String>().apply {
                addAll(loadVoiceGroceryItems(context))
            }
        }

    // LaunchedEffect starts a coroutine that runs as long as the key (isRecording) stays the same
    // When isRecording changes, the running coroutine is canceled and restarted w/ the new val
    // This means: if the recognizer stops early (silence detection), isRecording becomes false,
    // and the countdown coroutine gets canceled automatically — no manual cleanup needed
    LaunchedEffect(isRecording) {
        if (isRecording) {
            remainingRecordingSeconds = 10
            while (remainingRecordingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                remainingRecordingSeconds--
            }
            speechRecognizer.stopListening()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding(),
            )
        },
        topBar = {
            TopAppBar(
                title = { Text(text = "Grocery List (Voice)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LaunchedEffect(highlightedRecentlyAddedItems) {
            if (highlightedRecentlyAddedItems.isNotEmpty()) {
                groceryListState.animateScrollToItem(1 + groceryItems.size)
            }
        }
        LazyColumn(
            state = groceryListState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
        ) {
            item {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = {
                            // If already recording, stop the recognizer
                            // onResults/onError will set isRecording = false via onRecognitionDone
                            if (isRecording) {
                                speechRecognizer.stopListening()
                                return@Button
                            }

                            // Request permission first
                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                // Labeled return: exits only the onClick lambda, not the whole fn
                                return@Button
                            }

                            startSpeechRecognition(
                                speechRecognizer = speechRecognizer,
                                groceryItems = groceryItems,
                                context = context,
                                onRecognitionDone = { newItems ->
                                    isRecording = false
                                    currRootMeanSquareDecibel = 0f
                                    highlightedRecentlyAddedItems = newItems
                                },
                                onDuplicatesFound = { duplicates ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Already in list: ${duplicates.joinToString(", ")}",
                                        )
                                    }
                                },
                                onRootMeansSquareDecibelChanged = {
                                        rootMeanSquareDecibel ->
                                    currRootMeanSquareDecibel = rootMeanSquareDecibel
                                },
                            )
                            isRecording = true
                        },
                        // Red button when recording, default color when idle
                        colors =
                            if (isRecording) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            },
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            modifier = Modifier.size(24.dp),
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            if (isRecording) {
                                // padStart ensures single digits get a leading zero (e.g. 7 → "07")
                                // so the countdown always shows two digits like "0:07" instead of "0:7"
                                "Stop (0:${remainingRecordingSeconds.toString().padStart(2, '0')})"
                            } else {
                                "Record"
                            },
                        )
                    }
                    if (isRecording) {
                        Spacer(modifier = Modifier.size(12.dp))
                        WaveVisualizer(rootMeanSquareDecibel = currRootMeanSquareDecibel)
                    }
                }
            }

            if (groceryItems.isNotEmpty()) {
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete all items",
                            )
                        }
                    }
                }
            }

            itemsIndexed(groceryItems) { idx, item ->
                val isHighlighted = highlightedRecentlyAddedItems.contains(item)

                val backgroundColor by animateColorAsState(
                    targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3F) else Color.Transparent,
                    animationSpec = tween(durationMillis = 500),
                    label = "highlightedAnimation",
                )

                if (isHighlighted) {
                    LaunchedEffect(item) {
                        kotlinx.coroutines.delay(1000L)
                        highlightedRecentlyAddedItems = highlightedRecentlyAddedItems - item
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor)
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        groceryItems.removeAt(idx)
                        saveVoiceGroceryItems(context, groceryItems)
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete",
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
                        groceryItems.clear()
                        saveVoiceGroceryItems(context, groceryItems)
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

/**
 * Starts the Android SpeechRecognizer, processes recognized text into grocery items,
 * filters duplicates, and saves the updated list to SharedPreferences.
 *
 * @param speechRecognizer Android's speech-to-text engine instance
 * @param groceryItems the current list of grocery items (will be modified in place)
 * @param context Android context needed for saving to SharedPreferences
 * @param onRecognitionDone called when recognition finishes (success or error)
 * @param onDuplicatesFound called with a list of items that were already in the list
 * @param onRootMeansSquareDecibelChanged called repeatedly w/ the curr microphone loudness (for the visualizer)
 */
private fun startSpeechRecognition(
    speechRecognizer: SpeechRecognizer,
    groceryItems: MutableList<String>,
    context: android.content.Context,
    onRecognitionDone: (List<String>) -> Unit,
    onDuplicatesFound: (List<String>) -> Unit,
    onRootMeansSquareDecibelChanged: (Float) -> Unit,
) {
    // Tell the recognizer what to do when something happens
    // (results, errors, etc.)
    // object : RecognitionListener creates an anonymous object that
    // implements the interface
    speechRecognizer.setRecognitionListener(
        object : RecognitionListener {
            // Called when speech recognition is finished and text is ready
            override fun onResults(results: Bundle?) {
                // Get the list of recognized texts from the Bundle
                val matches =
                    results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION,
                    )

                // Take the best match, or exit if nothing was recognized
                val spokenText = matches?.firstOrNull() ?: return

                // Split by commas, "Komma", "and", "und" to add multiple items
                // e.g. "milk, bread and eggs" → ["milk", "bread", "eggs"]
                val items =
                    spokenText
                        .split(",", " Komma ", " and ", " und ")
                        .map { it.trim() }
                        .filter { it.isNotBlank() } // remove empty entries

                val duplicates =
                    items.filter { uniqueItems ->
                        groceryItems.any {
                            it.equals(uniqueItems, ignoreCase = true)
                        }
                    }

                val uniqueItems = items - duplicates.toSet()

                groceryItems.addAll(uniqueItems)
                saveVoiceGroceryItems(context, groceryItems)
                onRecognitionDone(uniqueItems)

                if (duplicates.isNotEmpty()) {
                    onDuplicatesFound(duplicates)
                }
            }

            // Called when an error occurs, reset button back to "Record"
            override fun onError(error: Int) {
                onRecognitionDone(emptyList())
            }

            // Required by the interface but not needed for our use case
            override fun onReadyForSpeech(p0: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onEndOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                onRootMeansSquareDecibelChanged(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) {}
        },
    )

    // Intent = message to Android: "I want to recognize speech"
    // .apply {} configures it right after creation
    val intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Use free-form speech model (normal talking, not web search)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            // Only return the 1 best result
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

    // Start listening with the configured intent
    speechRecognizer.startListening(intent)
}

@Composable
private fun WaveVisualizer(rootMeanSquareDecibel: Float) {
    var minRootMeanSquareDecibel by remember { mutableFloatStateOf(rootMeanSquareDecibel) }
    var maxRootMeanSquareDecibel by remember { mutableFloatStateOf(rootMeanSquareDecibel) }

    // Track the lowest and highest loudness values seen so far during this recording session
    // The values come from Android's onRmsChanged: raw audio samples → RMS calculation → converted
    // to decibels. We only receive the final dB value. Since different devices report different
    // ranges, we learn the actual range dynamically and normalize relative to it (see below).
    if (rootMeanSquareDecibel < minRootMeanSquareDecibel) {
        minRootMeanSquareDecibel = rootMeanSquareDecibel
    }
    if (rootMeanSquareDecibel > maxRootMeanSquareDecibel) {
        maxRootMeanSquareDecibel = rootMeanSquareDecibel
    }

    // Min-Max Scaling: normalizes the current dB value to a 0..1 range
    // based on the lowest and highest values observed so far.
    // Formula: normalized = (value - min) / (max - min)
    // If the range is too small (<0.5), we default to 0.5 to avoid division issues
    // and keep the bars at a neutral height until enough data has been collected.
    val range = maxRootMeanSquareDecibel - minRootMeanSquareDecibel
    val normalizedLevel =
        if (range > 0.5f) {
            ((rootMeanSquareDecibel - minRootMeanSquareDecibel) / range).coerceIn(0f, 1f)
        } else {
            0.5f
        }

    val barMultipliers = listOf(0.3f, 0.5f, 0.6f, 0.8f, 0.9f, 1.0f, 0.9f, 0.8f, 0.6f, 0.5f, 0.3f)
    val maxBarHeight = 40f // Maximum height in dp

    Row(
        modifier = Modifier.height(maxBarHeight.dp),
        // Adds 4dp gap between each bar instead of placing them edge-to-edge
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        // Aligns all bars to the bottom so they grow upward (default would be center)
        verticalAlignment = Alignment.Bottom,
    ) {
        barMultipliers.forEach { multiplier ->
            // Each bar has min height of 4dp so it's always visible
            val targetHeight = (4f + normalizedLevel * multiplier * maxBarHeight)

            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 100),
                label = "barHeight",
            )

            Box(
                modifier =
                    Modifier
                        .width(6.dp)
                        .height(animatedHeight.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private fun saveVoiceGroceryItems(
    context: android.content.Context,
    items: List<String>,
) {
    val sharedPreferences =
        context.getSharedPreferences(
            "voice_grocery",
            android.content.Context.MODE_PRIVATE,
        )
    val jsonString = Json.encodeToString(items)
    sharedPreferences.edit {
        putString("items", jsonString)
    }
}

private fun loadVoiceGroceryItems(context: android.content.Context): List<String> {
    val sharedPreferences =
        context.getSharedPreferences(
            "voice_grocery",
            android.content.Context.MODE_PRIVATE,
        )
    val jsonString = sharedPreferences.getString("items", null) ?: return emptyList()
    return Json.decodeFromString<List<String>>(jsonString)
}
