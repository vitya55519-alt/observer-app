package ru.observer.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0B0D10)
val Text = Color(0xFFC9D3DE)
val Dim = Color(0xFF6F8199)
val Green = Color(0xFF3ECF8E)
val Amber = Color(0xFFE0B83F)

private val s = darkColorScheme(background = Bg, primary = Color(0xFF4DA3FF), onBackground = Text)

@Composable
fun Theme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = s, content = content)
