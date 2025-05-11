package com.example.unit_converter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.rabbittownsoftware.convertly.R

/**
 * Main entry point of the Unit Converter app.
 *
 * This activity launches the [HomeScreen] UI, which allows the user to choose
 * a conversion category (e.g., Length, Weight, Temperature, etc.).
 * When a category is selected, it navigates to [ConversionActivity] with
 * the selected category passed as an extra.
 * Now also has a splash screen!
 */
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent()
        {
            var splashVisible by remember { mutableStateOf(true) }
            var showMain by remember { mutableStateOf(false) }

            // Control fade in/out sequence
            LaunchedEffect(Unit)
            {
                delay(500)   // slight delay before fade-in
                splashVisible = true
                delay(1000)  // time the splash stays visible
                splashVisible = false
                delay(500)   // wait for fade-out
                showMain = true
            }

            if (!showMain)
            {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                )
                {
                    AnimatedVisibility(
                        visible = splashVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.splash_logo),
                            contentDescription = "Splash Logo",
                            modifier = Modifier.size(300.dp)
                        )
                    }
                }
            }
            else
            {
                HomeScreen()
                {
                    category ->

                    // Create an intent to launch ConversionActivity
                    // and pass the selected category name as an extra.
                    val intent = Intent(this, ConversionActivity::class.java)
                    intent.putExtra("category", category)

                    // Start the conversion screen activity
                    startActivity(intent)
                }
            }
        }
    }
}
