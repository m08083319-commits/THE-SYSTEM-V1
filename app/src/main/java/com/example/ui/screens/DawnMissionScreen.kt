package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.ui.VesselViewModel

@Composable
fun DawnMissionScreen(
    viewModel: VesselViewModel,
    stats: UserStats,
    onClose: () -> Unit
) {
    val mission by viewModel.currentDawnMission.collectAsState()
    
    // Background gradient for dawn (Black -> Dark Blue -> Gold)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF0A1A2F),
            Color(0xFF4A3E00)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        if (mission == null) {
            Text("المهمة غير متوفرة", color = Color.White)
            Button(onClick = onClose, modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)) {
                Text("عودة")
            }
            return@Box
        }
        
        val m = mission!!
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "مهمة الفجر",
                color = Color(0xFFFFD700),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            if (!m.isCompleted) {
                // Not completed UI
                Text(
                    text = "الوقت ينفد.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111111).copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "تحدي الفجر لصيادي الفئة ${stats.rank}",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = m.description,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "المكافأة: ×${m.xpMultiplier} XP + ×${m.apMultiplier} AP",
                            color = Color(0xFFA55EFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Button(
                    onClick = { viewModel.completeDawnMission() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "إكمال التحدي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                // Completed UI
                Text(
                    text = "الفجر يشهد. أنت من القلائل.",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                if (!m.isMeditationDone) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.completeDawnMeditation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4DA6FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "تأمل إضافي (+50% XP)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "حتى روحك استيقظت. ممتاز.",
                        color = Color(0xFF4DA6FF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            TextButton(onClick = onClose) {
                Text("العودة إلى النظام", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}
