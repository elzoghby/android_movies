package com.example.myapplicationxx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationxx.ui.theme.MyApplicationxxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationxxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MovieExplorerScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowMoviesTap = { launchFlutterMoviesModule() }
                    )
                }
            }
        }
    }

    private fun launchFlutterMoviesModule() {
        try {
            val intent = Intent(this, FlutterMoviesActivity::class.java)
            intent.putExtra("route", "/movies")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch Flutter module: ${e.message}", e)
            android.widget.Toast.makeText(
                this,
                "Error launching movies: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}

@Composable
fun MovieExplorerScreen(modifier: Modifier = Modifier, onShowMoviesTap: () -> Unit) {
    val isLoading = remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎬 Movies Explorer",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Discover popular movies & watch trailers",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF8B949E)
            ),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (isLoading.value) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF01D277),
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Loading movies...",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF8B949E)
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            Button(
                onClick = {
                    isLoading.value = true
                    onShowMoviesTap()
                },
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF01D277)
                )
            ) {
                Text(
                    text = "Show List of Movies",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieExplorerScreenPreview() {
    MyApplicationxxTheme {
        MovieExplorerScreen(onShowMoviesTap = {})
    }
}