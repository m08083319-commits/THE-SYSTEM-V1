package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FateCard
import com.example.ui.VesselViewModel
import kotlinx.coroutines.delay

@Composable
fun FateDrawScreen(
    viewModel: VesselViewModel,
    onAccept: () -> Unit
) {
    val drawnCard by viewModel.fateDrawnCard.collectAsState()
    
    var showAnimation by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        if (drawnCard == null) {
            viewModel.drawFateCard()
        }
        delay(3500) // Thread animation duration
        showAnimation = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        if (showAnimation) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Color(0xFFFFD700),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "أغريس ينسج قدرك...",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        } else {
            drawnCard?.let { card ->
                FateCardDisplay(card = card, onAccept = {
                    viewModel.applyFateCardEffect(card)
                    onAccept()
                })
            }
        }
    }
}

@Composable
fun FateCardDisplay(card: FateCard, onAccept: () -> Unit) {
    val categoryColor = when (card.type) {
        "blessing" -> Color(0xFFFFD700)
        "neutral" -> Color(0xFFC0C0C0)
        "curse" -> Color(0xFFFF0044)
        else -> Color.White
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "card_float")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .graphicsLayer { translationY = floatAnim }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(450.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161616))
                .border(BorderStroke(2.dp, categoryColor), RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = when(card.type) {
                        "blessing" -> "✦ هبة النور ✦"
                        "neutral" -> "✧ ورقة الميزان ✧"
                        "curse" -> "☠ سوط الظلام ☠"
                        else -> ""
                    },
                    color = categoryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                
                Text(
                    text = card.name,
                    color = categoryColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = card.description,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Divider(color = categoryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = card.effectDescription,
                    color = categoryColor.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(
                containerColor = categoryColor,
                contentColor = if (card.type == "neutral") Color.Black else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = "قبول وقائع القدر",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
