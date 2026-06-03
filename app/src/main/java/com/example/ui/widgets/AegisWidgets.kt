package com.example.ui.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = SoloBorderSlate,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 24.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SoloCardBg,
                        SoloCardBg.copy(alpha = 0.85f)
                    )
                )
            )
            .border(BorderStroke(borderWidth, borderColor), shape)
            .then(clickModifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = SoloPrimaryCyan,
    textColor: Color = Color.White
) {
    val shape = RoundedCornerShape(12.dp)
    val opacity = if (enabled) 1.0f else 0.4f
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            Color.Black
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            SoloBorderSlate.copy(alpha = 0.1f),
                            SoloBorderSlate.copy(alpha = 0.1f)
                        )
                    )
                }
            )
            .border(
                BorderStroke(
                    1.2.dp, 
                    if (enabled) accentColor.copy(alpha = 0.8f) else SoloBorderSlate
                ), 
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = opacity),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun StatBar(
    progress: Float, // 0.0f to 1.0f
    label: String,
    valueStr: String,
    modifier: Modifier = Modifier,
    barColor: Color = SoloPrimaryCyan,
    trailColor: Color = SoloBorderSlate
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = SoloMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = valueStr,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(trailColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor, barColor.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val particles = remember {
        List(25) {
            ParticleData(
                relX = (0f..1f).randomFloat(),
                relY = (0f..1f).randomFloat(),
                size = (2f..6f).randomFloat(),
                speed = (0.2f..0.5f).randomFloat(),
                alpha = (0.15f..0.45f).randomFloat()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            var currentRelY = p.relY - (animProgress * p.speed)
            if (currentRelY < 0f) {
                currentRelY += 1.0f
            }

            val x = p.relX * size.width
            val y = currentRelY * size.height

            val color = if (p.speed > 0.35f) {
                SoloPrimaryCyan.copy(alpha = p.alpha)
            } else {
                Color(0xFFA55EFF).copy(alpha = p.alpha)
            }

            drawCircle(
                color = color,
                radius = p.size.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun ClosedRange<Float>.randomFloat() = 
    (Math.random() * (endInclusive - start) + start).toFloat()

private data class ParticleData(
    val relX: Float,
    val relY: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)
