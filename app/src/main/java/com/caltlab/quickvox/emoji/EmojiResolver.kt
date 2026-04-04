package com.caltlab.quickvox.emoji

import android.content.Context
import kotlinx.serialization.json.Json


class EmojiResolver(private val emojiMap: Map<String, String>) {

    // Matches items that start with a number like "4 Tomaten".
    // Group 1 captures the number (\d+), group 2 captures the item name (.+).
    // This allows resolve() to look up only the item name without the number.
    private val leadingNumberPattern = Regex(pattern = """^(\d+)\s+(.+)$""")

    // companion object = Kotlin's equivalent of Python's @classmethod.
    // Functions inside belong to the CLASS, not to an instance.
    // Call via: EmojiResolver.fromAssets(context) — no instance needed.
    // It's a factory pattern: loads the JSON file and creates the instance for you,
    // so the caller doesn't have to deal with file loading manually.
    companion object {
        fun fromAssets(context: Context): EmojiResolver {
            val jsonString = context.assets // access the app's assets/ folder
                .open("emoji_grocery.json") // open the file as a byte stream
                .bufferedReader() // converts bytes to readable text and buffers read access
                .use { it.readText() } // read all text and auto-close the file

            val emojiMap: Map<String, String> = Json.decodeFromString(jsonString)
            return EmojiResolver(emojiMap)
        }
    }

    fun formatWithEmoji(item: String) : String {
        val trimmedItem = item.trim()

        // Check if the item starts with a number (e.g. "4 Tomaten")
        // numberMatch would be null if no number is found
        val numberMatch = leadingNumberPattern.find(trimmedItem)

        // If the item starts with a number, resolve the item name w/o the number
        // Otherwise, resolve the entire item
        val emoji = if (numberMatch != null) {
            resolve(numberMatch.groupValues[2])
        } else {
            resolve(trimmedItem)
        }

        // Append the emoji to the item if found, otherwise return the item as it is
        return if (emoji != null) "$trimmedItem $emoji" else trimmedItem
    }

    private fun resolve(item: String) : String? {
        val normalizedItem = replaceGermanUmlauts(item.trim())

        // If not null, return value
        emojiMap[normalizedItem]?.let { return it }

        val singularizedItem = stripPlural(normalizedItem)
        if (singularizedItem != normalizedItem) {
            emojiMap[singularizedItem]?.let { return it }
        }

        return null
    }

    private fun replaceGermanUmlauts(word: String): String {
        return word.lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
    }

    private fun stripPlural(word: String): String {
        // English: -ies -> -y (cherries -> cherry, strawberries -> strawberry)
        if (word.endsWith("ies") && word.length > 4) {
            val stem = word.removeSuffix("ies") + "y"
            if (emojiMap.containsKey(stem)) return stem
        }

        // English: -oes -> -o (tomatoes -> tomato, potatoes -> potato)
        if (word.endsWith("oes") && word.length > 4) {
            val stem = word.removeSuffix("es")
            if (emojiMap.containsKey(stem)) return stem
        }

        // German: -en -> -e (Tomaten -> Tomate, Birnen -> Birne)
        if (word.endsWith("en") && word.length > 3) {
            val stem = word.removeSuffix("en") + "e"
            if (emojiMap.containsKey(stem)) return stem
        }

        // German + English: common suffixes, longest first
        val suffixes = listOf("nen", "er", "se", "es", "n", "e", "s")

        for (suffix in suffixes) {
            if (word.endsWith(suffix) && word.length >= suffix.length + 1) {
                val stem = word.removeSuffix(suffix)
                if (emojiMap.containsKey(stem)) return stem
            }
        }

        return word
    }
}
