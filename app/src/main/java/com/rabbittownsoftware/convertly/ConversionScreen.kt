package com.rabbittownsoftware.convertly

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


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
                                val formatted = String.format("%.2f", res)
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
                        val formatted = String.format("%.2f", res)
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
    height: Dp = 56.dp,
    fontSize: TextUnit = 20.sp
)
{
    val haptics = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .clickable
            {
                showDialog = true
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }.padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = selected,
            color = Color.White,
            fontFamily = font,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog)
    {
        Dialog(onDismissRequest = { showDialog = false })
        {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2C2C2C),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {
                var searchQuery by remember { mutableStateOf("") }
                val filteredOptions = options.filter()
                {
                    it.contains(searchQuery, ignoreCase = true)
                }

                Column(modifier = Modifier.padding(16.dp))
                {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search", color = Color.Gray) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontFamily = font,
                            fontSize = 18.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8C9EFF),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFF8C9EFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2C2C2C))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    )
                    {
                        items(filteredOptions)
                        {
                            item ->
                            Text(
                                text = item,
                                color = Color.White,
                                fontFamily = font,
                                fontSize = fontSize,
                                modifier = Modifier.fillMaxWidth().clickable
                                {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelected(item)
                                    showDialog = false
                                }.padding(vertical = 12.dp, horizontal = 8.dp)
                            )
                        }

                        if (filteredOptions.isEmpty())
                        {
                            item()
                            {
                                Text(
                                    text = "No results",
                                    color = Color.Gray,
                                    fontFamily = font,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                            }
                        }
                    }
                }
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
