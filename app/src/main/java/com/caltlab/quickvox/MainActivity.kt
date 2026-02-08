package com.caltlab.quickvox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caltlab.quickvox.navigation.NavGraph
import com.caltlab.quickvox.ui.components.AppNameHeader
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



//@Composable
//fun GroceryList() {
//    Column() {
//
//    }
//}


//@Preview(showBackground = true)
//@Composable
//fun QuickVoxPreview() {
//    QuickVoxTheme {
//        AppNameHeader()
////        Greeting("Android")
//    }
//}