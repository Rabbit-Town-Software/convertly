package com.example.unit_converter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Main entry point of the Unit Converter app.
 *
 * This activity launches the [HomeScreen] UI, which allows the user to choose
 * a conversion category (e.g., Length, Weight, Temperature, etc.).
 * When a category is selected, it navigates to [ConversionActivity] with
 * the selected category passed as an extra.
 */
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // Set the main content view to the HomeScreen composable.
        // The HomeScreen takes a lambda that handles category selection.
        setContent()
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
