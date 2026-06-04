package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VesselViewModel
import kotlinx.coroutines.delay

@Composable
fun JudgmentScreen(viewModel: VesselViewModel, onDismiss: () -> Unit) {
    var showButton by remember { mutableStateOf(false) }
    
    val score = viewModel.judgmentScore.collectAsState().value
    val verdict = viewModel.judgmentVerdict.collectAsState().value
    
    val bodyChange = viewModel.judgmentBodyChange.collectAsState().value
    val mindChange = viewModel.judgmentMindChange.collectAsState().value
    val disciplineChange = viewModel.judgmentDisciplineChange.collectAsState().value

    LaunchedEffect(Unit) {
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF808080).copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "يوم الحساب",
                color = Color.LightGray,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Icon(
                imageVector = Icons.Default.Balance,
                contentDescription = "الميزان",
                tint = Color.Black,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val getIndicator = { change: Double -> 
                when {
                    change > 0 -> "🟢"
                    change < 0 -> "🔴"
                    else -> "🟡"
                }
            }
            
            Text(text = "الجسد: ${if(bodyChange > 0) "+" else ""}$bodyChange% ${getIndicator(bodyChange)}", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "العقل: ${if(mindChange > 0) "+" else ""}$mindChange% ${getIndicator(mindChange)}", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "الانضباط: ${if(disciplineChange > 0) "+" else ""}$disciplineChange% ${getIndicator(disciplineChange)}", color = Color.White, fontSize = 20.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "النقاط: $score / 3",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val message = when (verdict) {
                "excellent" -> "أنت تتحسن. الأرقام لا تكذب. استمر."
                "declining" -> "خزي. هذا كل ما أستطيع قوله. خزي."
                else -> "أنت ثابت. لم تتراجع. لكن لم تتقدم."
            }
            
            Text(
                text = message,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(1000)),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {
                        viewModel.applyJudgmentVerdict()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("متابعة", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}
