package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import kotlinx.coroutines.delay

@Composable
fun StreakBrokenScreen(
    viewModel: VesselViewModel,
    stats: UserStats
) {
    BackHandler(true) {
        // Disabled back button
    }
    
    var showAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3B0707)), // Dark red
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            val scale by animateFloatAsState(
                targetValue = if (showAnimation) 1.5f else 0.5f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f)
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (showAnimation) 1f else 0f,
                animationSpec = tween(500)
            )
            
            Text(
                text = "${stats.bestStreak}",
                color = Color.White.copy(alpha = alpha),
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "سلسلة ${stats.bestStreak} يوم... انكسرت.",
                color = Color(0xFFFF4444),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                }
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "لكن الوعاء لا يموت. ابدأ من جديد.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                }
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            Button(
                onClick = { viewModel.resetStreakBrokenFlag() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0044), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .graphicsLayer {
                        this.alpha = alpha
                    }
            ) {
                Text(
                    text = "أنهض من جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
