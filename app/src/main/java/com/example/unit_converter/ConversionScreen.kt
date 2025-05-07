package com.example.unit_converter

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

/**
 * Screen for converting values between units.
 *
 * Allows user to input a value, select source and target units,
 * and view the conversion result with animated and styled UI.
 */
@SuppressLint("DefaultLocale")
@Composable
fun ConversionScreen(category: String)
{
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val unitList = remember { UnitConverter.getDisplayList(category) }

    var fromUnit by remember { mutableStateOf(unitList.first()) }
    var toUnit by remember { mutableStateOf(unitList.last()) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    val font = FontFamily(Font(R.font.roboto_condensed_regular))

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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            // From unit dropdown
            SpinnerDropdown(
                selected = fromUnit,
                options = unitList,
                onSelected = { fromUnit = it },
                font = font,
                backgroundColor = Color(0xFF8C9EFF),
                height = 56.dp,
                fontSize = 22.sp
            )

            // Input field for value
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder =
                    {
                        Text(
                            "Enter value",
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone =
                        {
                            keyboardController?.hide()
                            val value = input.toDoubleOrNull()
                            result = if (value == null)
                            {
                                "Please enter a valid number"
                            }
                            else
                            {
                                val res = UnitConverter.convert(category, value, fromUnit, toUnit)
                                val formatted = String.format("%.5f", res)
                                val fromAbbr = UnitConverter.getAbbreviation(fromUnit)
                                val toAbbr = UnitConverter.getAbbreviation(toUnit)
                                "$value $fromAbbr = $formatted $toAbbr"
                            }
                        }
                ),
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = font,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF8C9EFF)
                )
            )

            // To unit dropdown
            SpinnerDropdown(
                selected = toUnit,
                options = unitList,
                onSelected = { toUnit = it },
                font = font,
                backgroundColor = Color(0xFF8C9EFF),
                height = 56.dp,
                fontSize = 22.sp
            )

            // Result display
            Text(
                text = result.ifEmpty { "Result will appear here" },
                color = Color(0xFF40C4FF),
                fontSize = 26.sp,
                fontFamily = font,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Button row (Copy and Convert)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
                // Copy button
                AnimatedButton(
                    text = "Copy",
                    animate = true,
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.weight(1f),
                    fontSize = 22.sp
                )
                {
                    if (result.isNotEmpty())
                    {
                        val numericPart = result.substringAfter("=").trim().substringBefore(" ")
                        clipboardManager.setText(AnnotatedString(numericPart))
                        Toast.makeText(context, "Copied: $numericPart", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        Toast.makeText(context, "Nothing to copy", Toast.LENGTH_SHORT).show()
                    }
                }

                // Convert button
                AnimatedButton(
                    text = "Convert",
                    animate = true,
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.weight(1f),
                    fontSize = 22.sp
                )
                {
                    val value = input.toDoubleOrNull()
                    result = if (value == null)
                    {
                        "Please enter a valid number"
                    }
                    else
                    {
                        val res = UnitConverter.convert(category, value, fromUnit, toUnit)
                        val formatted = String.format("%.5f", res)
                        val fromAbbr = UnitConverter.getAbbreviation(fromUnit)
                        val toAbbr = UnitConverter.getAbbreviation(toUnit)
                        "$value $fromAbbr = $formatted $toAbbr"
                    }
                }
            }
        }
    }
}

/**
 * Custom dropdown selector for units.
 */
@Composable
fun SpinnerDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    font: FontFamily,
    backgroundColor: Color,
    height: Dp = 48.dp,
    fontSize: TextUnit = 16.sp
)
{
    var expanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = selected,
            color = Color.White,
            fontFamily = font,
            fontSize = fontSize,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF2C2C2C))
                .fillMaxWidth()
        )
        {
            options.forEach { item ->
                DropdownMenuItem(
                    text =
                        {
                            Text(item, fontFamily = font, color = Color.White, fontSize = fontSize)
                        },
                    onClick =
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelected(item)
                            expanded = false
                        }
                )
            }
        }
    }
}

/**
 * Reusable animated button with optional RGB border animation.
 */
@Composable
fun AnimatedButton(
    text: String,
    animate: Boolean = true,
    color: Color = Color(0xFF2C2C2C),
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    onClick: () -> Unit
)
{
    val font = FontFamily(Font(R.font.roboto_condensed_regular))
    val haptics = LocalHapticFeedback.current

    // Optional animated border effect
    val borderModifier = if (animate)
    {
        val progress by rememberInfiniteTransition(label = "button-border")
            .animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "progress"
            )

        val baseColors = listOf(
            Color(0xFFBB86FC), Color(0xFF8C9EFF), Color(0xFF64B5F6),
            Color(0xFF40C4FF), Color(0xFF80D8FF), Color(0xFFBB86FC)
        )

        val gradientStops = buildList()
        {
            val steps = 60
            for (i in 0 until steps)
            {
                val t = i / steps.toFloat()
                val index = (t * (baseColors.size - 1)).toInt()
                val blend = t * (baseColors.size - 1) % 1f
                val interpolated = lerp(
                    baseColors[index],
                    baseColors.getOrElse(index + 1) { baseColors.last() },
                    blend
                )
                add((t + progress) % 1f to interpolated)
            }
        }.sortedBy { it.first }

        Modifier.drawBehind()
        {
            val stroke = 2.dp.toPx()
            val cornerRadius = 16.dp.toPx()
            drawRoundRect(
                brush = Brush.sweepGradient(*gradientStops.toTypedArray()),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = stroke)
            )
        }
    }
    else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(2.dp)
            .then(borderModifier),
        contentAlignment = Alignment.Center
    )
    {
        Button(
            onClick =
                {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        )
        {
            Text(
                text = text,
                color = Color.White,
                fontFamily = font,
                fontSize = fontSize
            )
        }
    }
}
