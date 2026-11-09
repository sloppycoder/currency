package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.CurrencySelectorScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CurrencyApp()
            }
        }
    }
}

@Composable
fun CurrencyApp(viewModel: MainViewModel = viewModel()) {
    val colors = AppTheme.colors
    var isCurrencySelectorVisible by rememberSaveable { mutableStateOf(false) }

    // Auto-refresh when app comes to foreground (onResume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResumeScreen()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = isCurrencySelectorVisible,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { isSelector ->
                if (isSelector) {
                    CurrencySelectorScreen(
                        viewModel = viewModel,
                        onBack = { isCurrencySelectorVisible = false }
                    )
                } else {
                    ConvertScreen(
                        viewModel = viewModel,
                        onNavigateToAddCurrencies = { isCurrencySelectorVisible = true }
                    )
                }
            }
        }
    }
}
