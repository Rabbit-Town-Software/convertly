package com.example.unit_converter

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import com.rabbittownsoftware.convertly.R

/**
 * Home screen for the Unit Converter app.
 *
 * Displays a prompt and a grid of animated category buttons.
 * When a category is selected, [onCategorySelected] is triggered with the category name.
 */
@Composable
fun HomeScreen(onCategorySelected: (String) -> Unit)
{
    // List of all conversion categories, alphabetically sorted
    val categories = listOf(
        "Area", "Data", "Density", "Energy", "Force", "Frequency",
        "Length", "Power", "Pressure", "Speed", "Temperature",
        "Time", "Volume", "Weight"
    ).sorted()

    val modernFont = FontFamily(Font(R.font.roboto_condensed_regular))

    // Layout root
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818)),
        contentAlignment = Alignment.Center
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            // Title prompt
            Text(
                text = "What would you like to convert?",
                fontSize = 20.sp,
                fontFamily = modernFont,
                color = Color(0xFFCCCCCC),
                textAlign = TextAlign.Center
            )

            // Grid of category buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight(0.7f)
            )
            {
                items(categories)
                { category ->
                    AnimatedButton(category = category)
                    {
                        onCategorySelected(category)
                    }
                }
            }
        }
    }
}

/**
 * An animated, pressable button that shows a category name.
 *
 * Includes:
 * - Animated RGB border
 * - Tap feedback and scale animation
 * - Haptic feedback
 */
@Composable
fun AnimatedButton(category: String, onClick: () -> Unit)
{
    val haptics = LocalHapticFeedback.current
    val modernFont = FontFamily(Font(R.font.roboto_condensed_regular))

    // Infinite RGB border animation
    val infiniteTransition = rememberInfiniteTransition(label = "button-border")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Base color gradient for the animated border
    val baseColors = listOf(
        Color(0xFFBB86FC), Color(0xFF8C9EFF), Color(0xFF64B5F6),
        Color(0xFF40C4FF), Color(0xFF80D8FF), Color(0xFFBB86FC)
    )

    // Expands gradient stops to simulate a moving sweep
    val expandedStops = buildList()
    {
        val steps = 60
        for (i in 0 until steps)
        {
            val t = i / steps.toFloat()
            val index = (t * (baseColors.size - 1)).toInt()
            val blend = t * (baseColors.size - 1) % 1f
            val color = lerp(
                baseColors[index],
                baseColors.getOrElse(index + 1) { baseColors.last() },
                blend
            )
            add((t + progress) % 1f to color)
        }
    }.sortedBy { it.first }

    // Fade-in animation on first appearance
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit)
    {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    // Scale animation for tap effect
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "pressScale"
    )

    // Button layout
    Box(
        modifier = Modifier
            .graphicsLayer
            {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha.value
            }
            .fillMaxWidth()
            .height(90.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    )
    {
        // Draw animated border
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(2.dp))
        {
            val stroke = 2.dp.toPx()
            val cornerRadius = 16.dp.toPx()

            drawRoundRect(
                brush = Brush.sweepGradient(*expandedStops.toTypedArray()),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = stroke)
            )
        }

        // Main button
        Button(
            onClick =
                {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .pointerInput(Unit)
                {
                    detectTapGestures(
                        onPress =
                            {
                                pressed = true
                                try
                                {
                                    tryAwaitRelease()
                                }
                                finally
                                {
                                    delay(100) // short delay for tactile feedback
                                    pressed = false
                                }
                            }
                    )
                }
        )
        {
            Text(
                text = category,
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = modernFont
            )
        }
    }
}
