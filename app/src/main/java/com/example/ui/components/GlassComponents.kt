package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CollegeBlue
import com.example.ui.theme.CollegeBlueLight
import com.example.ui.theme.EmeraldSuccess

@Composable
fun GlassmorphicCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF),
                        Color(0xFFF1F5F9)
                    )
                )
            )
    ) {
        // Decorative top-right ambient frosted glass glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-60).dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CollegeBlueLight.copy(alpha = 0.45f),
                            CollegeBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Decorative bottom-left emerald accent glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 120.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldSuccess.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    containerColor: Color = Color.White.copy(alpha = 0.88f),
    borderColor: Color = Color.White.copy(alpha = 0.92f),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = CollegeBlue.copy(alpha = 0.12f),
                ambientColor = Color.Black.copy(alpha = 0.04f)
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        containerColor,
                        containerColor.copy(alpha = 0.78f)
                    )
                )
            )
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = shape,
        elevation = 6.dp,
        containerColor = Color.White.copy(alpha = 0.92f),
        borderColor = Color(0x442563EB),
        onClick = onClick,
        content = content
    )
}
