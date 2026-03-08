package com.caltlab.quickvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.caltlab.quickvox.navigation.NavGraph
import com.caltlab.quickvox.ui.theme.QuickVoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickVoxTheme {
                NavGraph()
            }
        }
    }
}

// @Composable
// fun GroceryList() {
//    Column() {
//
//    }
// }

// @Preview(showBackground = true)
// @Composable
// fun QuickVoxPreview() {
//    QuickVoxTheme {
//        AppNameHeader()
// //        Greeting("Android")
//    }
// }
