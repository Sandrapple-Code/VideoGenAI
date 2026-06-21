package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.local.ImageDatabase
import com.example.data.repository.ImageRepository
import com.example.ui.screens.ImageGeneratorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ImageGenerationViewModel
import com.example.ui.viewmodel.ImageGenerationViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup Room architecture components
        val database = ImageDatabase.getDatabase(applicationContext)
        val imageDao = database.imageDao()
        val repository = ImageRepository(imageDao)
        
        // Instantiate the ViewModel
        val viewModel: ImageGenerationViewModel by viewModels {
            ImageGenerationViewModelFactory(repository)
        }

        enableEdgeToEdge()

        setContent {
            // Keep a local dark theme toggling state
            var isDarkTheme by remember { mutableStateOf(true) }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        ImageGeneratorScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { isDarkTheme = !isDarkTheme },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
