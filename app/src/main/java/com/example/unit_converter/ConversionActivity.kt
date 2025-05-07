package com.example.unit_converter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.unit_converter.ui.theme.UnitConverterTheme

/**
 * Activity for handling unit conversions.
 *
 * This screen is launched when a category is selected from [HomeScreen].
 * It receives the selected category via Intent extras and displays the [ConversionScreen].
 */
class ConversionActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // Get the selected category from the Intent, defaulting to "Length"
        val category = intent.getStringExtra("category") ?: "Length"

        // Set the main content of this screen using Jetpack Compose
        setContent()
        {
            // Apply the custom theme
            UnitConverterTheme()
            {
                // Set a dark background color
                Surface(color = Color(0xFF181818))
                {
                    // Launch the main UI for conversion
                    ConversionScreen(category = category)
                }
            }
        }
    }
}
