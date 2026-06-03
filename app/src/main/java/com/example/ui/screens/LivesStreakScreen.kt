package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.ui.VesselViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivesStreakScreen(
    viewModel: VesselViewModel,
    stats: UserStats,
    onClose: () -> Unit
) {
    var showDebtSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ارادة الوعاء",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                TextButton(onClick = onClose) {
                    Text("إغلاق", color = Color(0xFFAAAAAA))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lives
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until stats.maxLives) {
                    if (i < stats.lives) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Life",
                            tint = Color(0xFFFF0044),
                            modifier = Modifier.size(48.dp).padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.HeartBroken,
                            contentDescription = "Lost Life",
                            tint = Color(0xFF444444),
                            modifier = Modifier.size(48.dp).padding(4.dp)
                        )
                    }
                }
            }
            Text(
                text = "الأيام الملتزمة المطلوبة لاستعادة قلب: ${stats.consecutiveCompleteDays}/3",
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Streak Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "سلسلة الانضباط",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${stats.streak} يوم",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أفضل سلسلة: ${stats.bestStreak} يوم",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Debt Button
            if (stats.originalDebt > 0) {
                Button(
                    onClick = { showDebtSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "دين النظام: ${stats.remainingDebt} AP (استعرض)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Milestones list
            Text("المحطات القادمة للسلسلة:", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            val milestones = listOf(
                Pair(7, "أسبوع الصمود (AP +5%)"),
                Pair(30, "الشهر الكامل (AP +10%)"),
                Pair(100, "مئة يوم من النار (AP +15%)"),
                Pair(365, "لا شيء يوقفه (AP +25%)")
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(milestones) { (days, desc) ->
                    val isReached = stats.bestStreak >= days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isReached) Color(0xFFFFD700).copy(alpha = 0.1f) else Color(0xFF161616))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$days يوم",
                            color = if (isReached) Color(0xFFFFD700) else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = desc,
                            color = if (isReached) Color.White else Color(0xFFAAAAAA),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isReached) {
                            Text("✓", color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    if (showDebtSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDebtSheet = false },
            containerColor = Color(0xFF1F0000)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "دين أغريس",
                    color = Color(0xFFFFD700),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("الدين الأصلي", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Text("${stats.originalDebt} AP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("المتبقي", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Text("${stats.remainingDebt} AP", color = Color(0xFFFF0044), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val progress = if (stats.originalDebt > 0) {
                    (stats.originalDebt - stats.remainingDebt).toFloat() / stats.originalDebt.toFloat()
                } else 1f
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0xFF440000)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "يتم خصم 30% من مكاسب الـ AP الخاصة بك تلقائياً لصالح أغريس حتى يتم سداد الدين بالكامل.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(30.dp))
                
                var manualAmount by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = manualAmount,
                    onValueChange = { manualAmount = it.filter { char -> char.isDigit() } },
                    label = { Text("سداد مبكر (AP)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        val amount = manualAmount.toIntOrNull() ?: 0
                        viewModel.manualRepayDebt(amount)
                        manualAmount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("سداد يدوي", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
