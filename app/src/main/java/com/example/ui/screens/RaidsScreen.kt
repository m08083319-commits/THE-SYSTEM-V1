package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExerciseTask
import com.example.data.UserStats
import com.example.data.Gate
import com.example.data.SideQuest
import androidx.activity.compose.BackHandler
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas

@Composable
fun RaidsScreen(viewModel: VesselViewModel, stats: UserStats) {
    var selectedTab by remember { mutableStateOf("DAILY QUOTA") }
    val tasks by viewModel.exercises.collectAsState()
    val gatesList by viewModel.gates.collectAsState()
    val sideQuestsList by viewModel.sideQuests.collectAsState()
    val remainingGateEnergy by viewModel.remainingGateEnergy.collectAsState()
    val gatesEnteredToday by viewModel.gatesEnteredToday.collectAsState()

    var activeExerciseForSensor by remember { mutableStateOf<ExerciseTask?>(null) }

    // Real-time ticking daily countdown
    var countdownText by remember { mutableStateOf("15:53:32") }
    
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val hoursLeft = 23 - now.get(Calendar.HOUR_OF_DAY)
            val minutesLeft = 59 - now.get(Calendar.MINUTE)
            val secondsLeft = 59 - now.get(Calendar.SECOND)
            countdownText = String.format("%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
            .padding(horizontal = 16.dp)
            .testTag("raids_screen_container")
    ) {
        // Gates / Daily Quota Tab Row Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoloCardBg)
                .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(12.dp))
        ) {
            // GATES tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "GATES" }
                    .background(if (selectedTab == "GATES") SoloActiveBlue else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GATES",
                        color = if (selectedTab == "GATES") Color.White else SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Cyclone,
                        contentDescription = "Gates",
                        tint = if (selectedTab == "GATES") Color.White else SoloMutedText,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // DAILY QUOTA tab
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clickable { selectedTab = "DAILY QUOTA" }
                    .background(if (selectedTab == "DAILY QUOTA") SoloActiveBlue else Color.Transparent)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DAILY QUOTA",
                        color = if (selectedTab == "DAILY QUOTA") Color.White else SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Daily Quota Target",
                        tint = if (selectedTab == "DAILY QUOTA") Color.White else SoloMutedText,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        if (selectedTab == "DAILY QUOTA") {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Dawn Protocol Countdown banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, SoloBorderGold), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAWN PROTOCOL",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SoloNeonGreen)
                                    )
                                    Text(
                                        text = "LINK STABLE",
                                        color = SoloNeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(18.dp))
                                
                                Text(
                                    text = "MISSION EXPIRES IN",
                                    color = SoloMutedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = countdownText,
                                    color = SoloNeonGreen,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Spiral Target Indicator Icon
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoloGold.copy(alpha = 0.1f))
                                    .border(BorderStroke(1.dp, SoloGold.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Adjust,
                                    contentDescription = "Target",
                                    tint = SoloGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Extreme Protocol Ascension Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, SoloBorderGold), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoloGold.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, SoloGold), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "EXTREME PROTOCOL",
                                    color = SoloGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = "DAWN PROTOCOL: ASCENSION",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "The human vessel is weakest at \"\nfirst light. Synchronize now for 3x\n\".XP multiplier",
                                color = SoloMutedText,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Headers & List of actual exercise tasks
                items(tasks) { task ->
                    ExerciseTaskItemCard(
                        task = task,
                        onOpenSensor = { activeExerciseForSensor = task },
                        onDirectComplete = { viewModel.completeExerciseDirectly(task.id) }
                    )
                }

                // Link established status panel
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloNeonGreen.copy(alpha = 0.05f))
                            .border(BorderStroke(1.dp, SoloNeonGreen.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LINK ESTABLISHED",
                                color = SoloNeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "STATUS: MONITORING VESSEL",
                                color = SoloNeonGreen.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        } else {
            // GATES and SIDE QUESTS Tab
            var gatesSubTab by remember { mutableStateOf("GATES_LIST") }
            var activeBattleGate by remember { mutableStateOf<Gate?>(null) }
            
            // Ticking state to trigger real-time countdown recompositions
            var tickingTime by remember { mutableStateOf(System.currentTimeMillis()) }
            LaunchedEffect(key1 = true) {
                while (true) {
                    delay(1000)
                    tickingTime = System.currentTimeMillis()
                }
            }
            
            // Check if there's any currently entered gate. If so, put the player straight into it (no retreat!)
            val currentlyEnteredGate = gatesList.find { it.isEntered }
            if (currentlyEnteredGate != null) {
                activeBattleGate = currentlyEnteredGate
            }

            if (activeBattleGate != null) {
                GateBattleScreen(
                    gate = activeBattleGate!!,
                    viewModel = viewModel,
                    onClose = { activeBattleGate = null }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sub-tab selection bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gatesSubTab = "GATES_LIST" }
                                .background(if (gatesSubTab == "GATES_LIST") SoloPrimaryCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "البوابات اليومية",
                                color = if (gatesSubTab == "GATES_LIST") SoloPrimaryCyan else SoloMutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { gatesSubTab = "SIDE_QUESTS" }
                                .background(if (gatesSubTab == "SIDE_QUESTS") SoloPrimaryCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "المهام الجانبية",
                                color = if (gatesSubTab == "SIDE_QUESTS") SoloPrimaryCyan else SoloMutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (gatesSubTab == "GATES_LIST") {
                        // Energy Panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                            border = BorderStroke(1.dp, SoloBorderSlate),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "طاقة بوابات المغارة",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "تتجدد تلقائياً عند منتصف الليل",
                                        color = SoloMutedText,
                                        fontSize = 10.sp
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(5) { idx ->
                                        val hasEnergy = idx < remainingGateEnergy
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = "Energy Gate Status",
                                            tint = if (hasEnergy) SoloPrimaryCyan else Color(0xFF1E293B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$remainingGateEnergy/5",
                                        color = SoloPrimaryCyan,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Gates list or empty trigger
                        if (gatesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("لا توجد بوابات نشطة حالياً", color = SoloMutedText, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.resetDaily() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoloPrimaryCyan)
                                    ) {
                                        Text("توليد البوابات اليومية", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(gatesList) { gate ->
                                    val gateColor = getGateRankColor(gate.rank)
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                                        border = BorderStroke(1.dp, if (gate.isEntered) gateColor else SoloBorderSlate),
                                        modifier = Modifier.fillMaxWidth().testTag("gate_${gate.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = gate.name,
                                                        color = Color.White,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "الحارس: ${gate.guardianName}",
                                                        color = SoloMutedText,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                // Rank Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(gateColor.copy(alpha = 0.15f))
                                                        .border(BorderStroke(1.dp, gateColor), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "رتبة ${gate.rank}",
                                                        color = gateColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Divider(color = SoloBorderSlate.copy(alpha = 0.5f))

                                            // Description / Challenge requirements
                                            Column {
                                                Text(
                                                    text = gate.challengeDescription,
                                                    color = SoloMutedText,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "المطلوب: ${gate.challengeReps} ${gate.challengeUnit}",
                                                    color = SoloPrimaryCyan,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (gate.weaknessName != null) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "نقطة الضعف: ${gate.weaknessName} (${gate.weaknessReps} ${gate.weaknessExercise})",
                                                        color = SoloGold,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            // Countdown
                                            val currentNow = tickingTime
                                            val timeLeft = (gate.expiryTime - currentNow).coerceAtLeast(0)
                                            val isExpired = timeLeft == 0L
                                            
                                            val hours = timeLeft / (1000 * 3600)
                                            val minutes = (timeLeft % (1000 * 3600)) / (1000 * 60)
                                            val seconds = (timeLeft % (1000 * 60)) / 1000
                                            val countdownString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rewards list
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "⭐ ${gate.baseXPReward} XP",
                                                        color = Color(0xFFA55EFF),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "🪙 ${gate.baseGoldReward}G",
                                                        color = SoloGold,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "⚡ ${gate.baseAPReward} AP",
                                                        color = SoloPrimaryCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                // Time Left Countdown
                                                Text(
                                                    text = if (isExpired) "انتهى الوقت" else "ينتهي في: $countdownString",
                                                    color = if (isExpired) Color.Red else SoloMutedText,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            // Action button
                                            when {
                                                gate.isCompleted -> {
                                                    Button(
                                                        onClick = { },
                                                        enabled = false,
                                                        colors = ButtonDefaults.buttonColors(
                                                            disabledContainerColor = Color(0xFF0F172A),
                                                            disabledContentColor = Color(0xFF10B981)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("✓ تم تطهير البوابة بنجاح", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                isExpired -> {
                                                    Button(
                                                        onClick = { },
                                                        enabled = false,
                                                        colors = ButtonDefaults.buttonColors(
                                                            disabledContainerColor = Color(0xFF11141A)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("البوابة مغلقة (انتهى وقت التطهير)", color = SoloMutedText, fontSize = 11.sp)
                                                    }
                                                }
                                                else -> {
                                                    val canEnter = remainingGateEnergy > 0 || gate.isEntered
                                                    Button(
                                                        onClick = {
                                                            if (gate.isEntered) {
                                                                activeBattleGate = gate
                                                            } else {
                                                                viewModel.enterGate(gate)
                                                                activeBattleGate = gate
                                                            }
                                                        },
                                                        enabled = canEnter,
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (gate.isEntered) Color(0xFF991B1B) else gateColor,
                                                            contentColor = Color.White
                                                        ),
                                                        modifier = Modifier.fillMaxWidth().testTag("enter_button_${gate.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = if (gate.isEntered) Icons.Default.PlayArrow else Icons.Default.Login,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (gate.isEntered) "متابعة قتال البوابة" else "دخول البوابة (طاقة 1-)",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // SIDE QUESTS list
                        if (sideQuestsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("لا توجد مهام جانبية نشطة حالياً", color = SoloMutedText, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.resetDaily() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoloPrimaryCyan)
                                    ) {
                                        Text("توليد مهام جانبية", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(sideQuestsList) { quest ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                                        border = BorderStroke(1.dp, if (quest.isCompleted) Color(0xFF10B981) else SoloBorderSlate),
                                        modifier = Modifier.fillMaxWidth().testTag("side_quest_${quest.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = quest.name,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                
                                                if (quest.isCompleted) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("مكتملة ✓", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(SoloGold.copy(alpha = 0.15f))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("جارية", color = SoloGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Text(
                                                text = quest.description,
                                                color = SoloMutedText,
                                                fontSize = 12.sp
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "المطلوب: ${quest.targetReps} ${quest.unit} من (${quest.exerciseType})",
                                                color = SoloPrimaryCyan,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rewards list
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "⭐ ${quest.xpReward} XP",
                                                        color = Color(0xFFA55EFF),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "🪙 ${quest.goldReward}G",
                                                        color = SoloGold,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "⚡ ${quest.apReward} AP",
                                                        color = SoloPrimaryCyan,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                if (!quest.isCompleted) {
                                                    Button(
                                                        onClick = { viewModel.completeSideQuest(quest) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SoloPrimaryCyan),
                                                        modifier = Modifier.testTag("complete_quest_${quest.id}")
                                                    ) {
                                                        Text("إنجاز", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    activeExerciseForSensor?.let { currentTask ->
        SensorCalibrationConsoleDialog(
            task = currentTask,
            tasks = tasks,
            onClose = { activeExerciseForSensor = null },
            onRepDetected = {
                viewModel.syncKineticLink(currentTask.id, 1)
            }
        )
    }
}

@Composable
fun ExerciseTaskItemCard(
    task: ExerciseTask,
    onOpenSensor: () -> Unit,
    onDirectComplete: () -> Unit
) {
    val cardBorder = if (task.isCompleted) {
        BorderStroke(1.5.dp, SoloNeonGreen)
    } else {
        BorderStroke(1.dp, SoloBorderSlate)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0D1217) else SoloCardBg
        ),
        border = cardBorder,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSensor() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val iconVector = getExerciseIcon(task.iconType)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (task.isCompleted) SoloNeonGreen.copy(alpha = 0.1f)
                                else SoloActiveBlue.copy(alpha = 0.1f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (task.isCompleted) SoloNeonGreen.copy(alpha = 0.5f)
                                    else SoloActiveBlue.copy(alpha = 0.3f)
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = task.name,
                            tint = if (task.isCompleted) SoloNeonGreen else SoloPrimaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = task.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            if (task.id == "lunges" || task.id == "burpees" || task.id == "pullups" || task.id == "walking" || task.id == "running" || task.id == "reading" || task.id == "meditation") {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "AP reward symbol",
                                    tint = SoloMutedText,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "XP +${task.xpReward}",
                            color = SoloActiveBlue.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right quantity target
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (task.isCompleted) "${task.target}" else "${task.progress}/${task.target}",
                        color = if (task.isCompleted) SoloNeonGreen else Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.type,
                        color = SoloMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Sync button if not completed
            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(14.dp))
                
                val btnText = when (task.iconType) {
                    "shower" -> "CONFIRM RECOVERY ❄️"
                    "reading", "meditation" -> "START MENTAL SYNC"
                    else -> "SYNC KINETIC LINK"
                }

                Button(
                    onClick = onDirectComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (btnText == "CONFIRM RECOVERY ❄️") Color(0xFF101B2E) else Color(0xFF0C1914)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (btnText == "CONFIRM RECOVERY ❄️") SoloActiveBlue.copy(alpha = 0.5f)
                        else SoloNeonGreen.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = btnText,
                        color = if (btnText == "CONFIRM RECOVERY ❄️") SoloActiveBlue else SoloNeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Completed state feedback
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = SoloNeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SYNCED",
                        color = SoloNeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

fun getExerciseIcon(iconType: String): ImageVector {
    return when (iconType) {
        "pushups" -> Icons.Default.TaskAlt
        "lunges" -> Icons.Default.DirectionsWalk
        "burpees" -> Icons.Default.Favorite
        "pullups" -> Icons.Default.KeyboardDoubleArrowUp
        "walking" -> Icons.Default.DirectionsWalk
        "running" -> Icons.Default.DirectionsRun
        "shower" -> Icons.Default.AcUnit
        "reading" -> Icons.Default.MenuBook
        "meditation" -> Icons.Default.SelfImprovement
        else -> Icons.Default.FitnessCenter
    }
}

@Composable
fun SensorCalibrationConsoleDialog(
    task: ExerciseTask,
    tasks: List<ExerciseTask>,
    onClose: () -> Unit,
    onRepDetected: () -> Unit
) {
    val liveTask = tasks.find { it.id == task.id } ?: task
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gyroSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }
    val hasSensors = accelSensor != null && gyroSensor != null

    // Sensor state throttled updater
    var smoothedAccel by remember { mutableStateOf(9.8f) }
    var currentGyroMagnitude by remember { mutableStateOf(0f) }
    var cheatActive by remember { mutableStateOf(false) }
    var violationCount by remember { mutableStateOf(0) }
    var phaseState by remember { mutableStateOf(0) }
    var lastUiUpdateTime by remember { mutableStateOf(0L) }

    // Dystopian Military Instructions
    val inArabic: String
    val inEnglish: String
    val isStillnessTask = (task.id == "reading" || task.id == "meditation")
    
    if (isStillnessTask) {
        inArabic = "اقفأ كتمثال عسكري صخري. يُمنع أي انحراف ميكانيكي بمقدار كسر من المليمتر وإلا سيُلغى التقدم فوراً."
        inEnglish = "FREEZE ABSOLUTELY. Any sub-millimeter mechanical deviation will instantly violate strict protocol."
    } else {
        inArabic = when (task.id) {
            "pushups" -> "انبطح أرضاً، ثبّت الهاتف بقبضة يدك أو مستوياً تحت صدرك. اهبط بصدرك لأسفل بريتم هادئ حتى يلامس المستشعر المدى، ثم ادفع لأعلى بهدوء عسكري مطلق."
            "squats" -> "احمل الهاتف في جيبك أو ثبته بيدك بقبضة فولاذية، اهبط ببطء لركوع القرفصاء الكامل (90 درجة) ثم ارتفع مع بسط الظهر بالكامل بدون رجّة عشوائية."
            "burpees" -> "احمل الهاتف، انزل للاسفل وافرد رجليك ثم اقفز برياضة تامة وسرعة حكيمة."
            else -> "تحرك كآلة ميكانيكية دقيقة؛ للأعلى وللأسفل ببطء محسوب، أي اهتزاز مفاجئ أو تزييف عشوائي ملغى عسكرياً."
        }
        inEnglish = when (task.id) {
            "pushups" -> "TACTICAL DEPLOYMENT: Prone alignment. Lower body smoothly to minimum depth, push up with uniform, strict tactical motion."
            "squats" -> "REGIMENTAL SQUAT: Handheld or pocket-locked. Deep pivot of knee joints (90°), rise holding core stabilization."
            "burpees" -> "HIGH INTENSITY PROTOCOL: Drop, strict push, explosive launch. Keep device trajectory locked without side rattle."
            else -> "MILITARY EXERCISE STANDARD: Continuous uniform up-and-down wave. All high-frequency noise is automatically blocked."
        }
    }

    // Register listeners with internal state tracking
    DisposableEffect(task.id) {

        val listener = object : SensorEventListener {
            var localSmoothedAccel = 9.8f
            var localGyroMag = 0f
            var localPhaseState = 0
            var localCheatActive = false
            var localViolationCount = 0
            var localLastInteractionTime = 0L
            var localLastRepTime = 0L

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val now = System.currentTimeMillis()

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val mag = kotlin.math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                    val alpha = 0.15f
                    localSmoothedAccel = alpha * mag + (1f - alpha) * localSmoothedAccel
                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    localGyroMag = kotlin.math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                }

                // Strict rep / vibration / cheat detection executed completely in local variables synchronously
                if (now - localLastInteractionTime >= 400L) {
                    if (localGyroMag > 4.5f) {
                        if (!localCheatActive) {
                            localCheatActive = true
                            localViolationCount++
                            cheatActive = true
                            violationCount = localViolationCount
                        }
                        localLastInteractionTime = now
                        localPhaseState = 0
                        phaseState = 0
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else if (localGyroMag < 1.5f && localCheatActive && now - localLastInteractionTime > 1800L) {
                        localCheatActive = false
                        cheatActive = false
                    }

                    if (!localCheatActive && !liveTask.isCompleted) {
                        if (isStillnessTask) {
                            val diffG = kotlin.math.abs(localSmoothedAccel - 9.8f)
                            if (diffG < 0.6f && localGyroMag < 0.25f) {
                                if (now - localLastRepTime >= 3000L) {
                                    onRepDetected()
                                    localLastRepTime = now
                                    localLastInteractionTime = now
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            } else {
                                localLastRepTime = now
                            }
                        } else {
                            when (liveTask.id) {
                                "pushups" -> {
                                    if (localPhaseState == 0 && localSmoothedAccel < 8.3f) {
                                        localPhaseState = 1
                                        localLastInteractionTime = now
                                        phaseState = 1
                                    } else if (localPhaseState == 1 && localSmoothedAccel > 11.4f) {
                                        localPhaseState = 2
                                        localLastInteractionTime = now
                                        phaseState = 2
                                    } else if (localPhaseState == 2 && kotlin.math.abs(localSmoothedAccel - 9.8f) < 0.8f) {
                                        localPhaseState = 0
                                        phaseState = 0
                                        onRepDetected()
                                        localLastRepTime = now
                                        localLastInteractionTime = now
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                                "squats" -> {
                                    if (localPhaseState == 0 && localSmoothedAccel < 7.8f) {
                                        localPhaseState = 1
                                        localLastInteractionTime = now
                                        phaseState = 1
                                    } else if (localPhaseState == 1 && localSmoothedAccel > 12.0f) {
                                        localPhaseState = 2
                                        localLastInteractionTime = now
                                        phaseState = 2
                                    } else if (localPhaseState == 2 && kotlin.math.abs(localSmoothedAccel - 9.8f) < 0.8f) {
                                        localPhaseState = 0
                                        phaseState = 0
                                        onRepDetected()
                                        localLastRepTime = now
                                        localLastInteractionTime = now
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                                "burpees" -> {
                                    if (localPhaseState == 0 && localSmoothedAccel < 6.8f) {
                                        localPhaseState = 1
                                        localLastInteractionTime = now
                                        phaseState = 1
                                    } else if (localPhaseState == 1 && localSmoothedAccel > 13.2f) {
                                        localPhaseState = 2
                                        localLastInteractionTime = now
                                        phaseState = 2
                                    } else if (localPhaseState == 2 && kotlin.math.abs(localSmoothedAccel - 9.8f) < 1.0f) {
                                        localPhaseState = 0
                                        phaseState = 0
                                        onRepDetected()
                                        localLastRepTime = now
                                        localLastInteractionTime = now
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                                else -> {
                                    if (localPhaseState == 0 && localSmoothedAccel < 8.3f) {
                                        localPhaseState = 1
                                        localLastInteractionTime = now
                                        phaseState = 1
                                    } else if (localPhaseState == 1 && localSmoothedAccel > 11.4f) {
                                        localPhaseState = 2
                                        localLastInteractionTime = now
                                        phaseState = 2
                                    } else if (localPhaseState == 2 && kotlin.math.abs(localSmoothedAccel - 9.8f) < 0.8f) {
                                        localPhaseState = 0
                                        phaseState = 0
                                        onRepDetected()
                                        localLastRepTime = now
                                        localLastInteractionTime = now
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                        }
                    }
                }

                // Throttle Compose updates to up to 25 FPS (every 40ms) to ensure absolute stability
                if (now - lastUiUpdateTime > 40L) {
                    smoothedAccel = localSmoothedAccel
                    currentGyroMagnitude = localGyroMag
                    lastUiUpdateTime = now
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelSensor != null) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (gyroSensor != null) {
            sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF020104)), // Absolute deep dark void
            color = Color(0xFF020104)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Abort command",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "REGIMENTAL MOTION SENTINEL",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "مراقب الانضباط العسكري للحركة",
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                            .border(BorderStroke(1.2.dp, Color(0xFFEF4444)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💀", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exercise Title Box showing absolute strictness
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = liveTask.name.uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SENTINEL PROTOCOL: ${if (cheatActive) "FRAUD ALERT" else "STRICT ZERO TOLERANCE"}",
                        color = if (cheatActive) Color(0xFFEF4444) else SoloGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (!hasSensors) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ SIMULATOR PROTOCOL: CLICK TARGET TO LOG REPS",
                            color = SoloPrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // High-precision Crosshair Scope Canvas (Military Target)
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .padding(10.dp)
                        .clickable {
                            if (!hasSensors) {
                                onRepDetected()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(150.dp)) {
                        val radius = size.minDimension / 2
                        val deviation = (smoothedAccel - 9.8f).coerceIn(-6f, 12f) * 3.5.dp.toPx()
                        
                        // Draw concentric rings of military target
                        drawCircle(
                            color = if (cheatActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = if (cheatActive) Color(0xFFEF4444) else SoloPrimaryCyan.copy(alpha = 0.4f),
                            radius = radius * 0.75f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                        )
                        
                        // Pulse ring corresponding to movement
                        drawCircle(
                            color = if (cheatActive) Color(0xFFEF4444) else SoloNeonGreen,
                            radius = (radius * 0.4f + deviation).coerceIn(10f, radius * 0.9f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                        )

                        // Center solid core
                        drawCircle(
                            color = if (cheatActive) Color(0xFFEF4444) else SoloNeonGreen,
                            radius = radius * 0.15f
                        )
                        
                        // Crosshairs
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(radius, 0f),
                            end = androidx.compose.ui.geometry.Offset(radius, size.height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(0f, radius),
                            end = androidx.compose.ui.geometry.Offset(size.width, radius),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    
                    // Terror level sign
                    Text(
                        text = if (cheatActive) "🚫" else "🎯",
                        fontSize = 24.sp
                    )
                }

                // Strict Real-Time Military Telemetry Logger
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10080B)),
                    border = BorderStroke(1.dp, if (cheatActive) Color(0xFFEF4444) else Color(0xFF321921)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COURT MARTIAL TELEMETRY (بيانات الفحص والنزاهة)",
                                color = SoloMutedText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(if (cheatActive) Color(0xFFEF4444) else Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (cheatActive) "CRITICAL BREACH" else "SECURE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = Color(0xFF321921), thickness = 0.8.dp)

                        // Accelerometer data row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ACCELERATION DEVIATION:", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = String.format("%.3f G-Force", smoothedAccel / 9.8f),
                                color = if (smoothedAccel < 7.5f || smoothedAccel > 12.5f) SoloGold else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Gyroscope wave row with strict limit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("FRAUD KINETIC ENERGY (MAX 4.5):", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = String.format("%.2f rad/s", currentGyroMagnitude),
                                color = if (cheatActive) Color(0xFFEF4444) else SoloPrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Low Pass filter state row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CYCLED CADENCE PHASE:", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            val phaseText = if (isStillnessTask) {
                                if (kotlin.math.abs(smoothedAccel - 9.8f) < 0.6f && currentGyroMagnitude < 0.25f) "LOCK STATE SECURED" else "VIOLATED - NOT STILL"
                            } else {
                                when (phaseState) {
                                    1 -> "STAGE 1: DEPTH CRITERION MET"
                                    2 -> "STAGE 2: EXPANSION CRITERION MET"
                                    else -> "STANDBY (VERIFYING CALIBRATION)"
                                }
                            }
                            Text(
                                text = phaseText,
                                color = if (cheatActive) Color(0xFFEF4444) else SoloNeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Total infraction counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SHAKE PROTOCOL VIOLATIONS:", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$violationCount INFRACTIONS",
                                color = if (violationCount > 0) Color(0xFFEF4444) else SoloNeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Blinking red warning box when fraud is detected
                if (cheatActive) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF400A0A)),
                        border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚔", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "SHAKE DETECTION LOCKOUT TRIGGERED!",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "تم رصد تلاعب عشوائي بنبضات المستشعر! تقدمك مجمد عسكرياً، ولن تقبل أي عِدّات إلا بحركة رياضية بالغة الانضباط والدقة.",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Command guidelines box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = inEnglish,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = inArabic,
                        color = SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // High-strictness progress tracker
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0B)),
                    border = BorderStroke(1.5.dp, if (cheatActive) Color(0xFFEF4444) else SoloNeonGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "OFFICIALLY AUTHENTICATED REPS",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = "${liveTask.progress} / ${liveTask.target}",
                            color = if (cheatActive) Color(0xFFEF4444) else SoloNeonGreen,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )

                        if (liveTask.isCompleted) {
                            Text(
                                text = "✓ COMTAC KINETIC TARGET SECURED",
                                color = SoloNeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Text(
                                text = "ZERO COMPROMISE MILITARY DRILL STATUS",
                                color = SoloGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getGateRankColor(rank: String): Color {
    return when (rank.uppercase()) {
        "E" -> Color(0xFF94A3B8) // Slate
        "D" -> Color(0xFF4ADE80) // Green
        "C" -> Color(0xFF60A5FA) // Blue
        "B" -> Color(0xFFA78BFA) // Purple
        "A" -> Color(0xFFFBBF24) // Gold/Yellow
        "S" -> Color(0xFFF87171) // Red
        "SS" -> Color(0xFFEC4899) // Pink
        "SSS" -> Color(0xFFEF4444) // Deep Crimson
        else -> Color(0xFFA55EFF) // Mythic Purple
    }
}

@Composable
fun GateBattleScreen(
    gate: Gate,
    viewModel: VesselViewModel,
    onClose: () -> Unit
) {
    // 100% Back navigation blocking! No retreat
    BackHandler(enabled = true) {
        // Blocks system back press to satisfy the "لا تراجع" specification
    }

    var currentProgress by remember { mutableStateOf(0) }
    var tickingTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var simulatedRepTrackerOpen by remember { mutableStateOf(false) }
    
    // Timer seconds left calculation
    val appearanceAge = System.currentTimeMillis() - gate.appearanceTime
    val remainingSecondsLeft = ((gate.timeLimitMinutes * 60) - (appearanceAge / 1000)).coerceAtLeast(0)
    var timerSecondsLeft by remember { mutableStateOf(remainingSecondsLeft.toInt()) }
    
    LaunchedEffect(key1 = true) {
        while (timerSecondsLeft > 0 && !gate.isCompleted && currentProgress < gate.challengeReps) {
            delay(1000)
            timerSecondsLeft--
        }
    }

    val isFailure = timerSecondsLeft <= 0 && currentProgress < gate.challengeReps
    val isVictory = currentProgress >= gate.challengeReps && !isFailure

    // Slowly writes dialogue character by character
    var typedDialogue by remember { mutableStateOf("") }
    LaunchedEffect(key1 = gate.guardianDialogue) {
        typedDialogue = ""
        for (char in gate.guardianDialogue) {
            typedDialogue += char
            delay(50)
        }
    }

    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)) // Deep space slate dark
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "بوابة تَطْهير نَشِطة",
                color = Color.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            val mm = timerSecondsLeft / 60
            val ss = timerSecondsLeft % 60
            Text(
                text = String.format("تِلْقاء الإغلاق: %02d:%02d", mm, ss),
                color = if (timerSecondsLeft < 60) Color.Red else SoloGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Guardian details card
        Card(
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = gate.guardianName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\" $typedDialogue \"",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Challenge description and target progress
        Card(
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            border = BorderStroke(1.dp, SoloBorderSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "مُهِمّة التَطْهير البَدَنِيّة",
                    color = SoloMutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = gate.challengeDescription,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress ratio display
                Text(
                    text = "$currentProgress / ${gate.challengeReps}",
                    color = SoloPrimaryCyan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = gate.challengeUnit,
                    color = SoloMutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // Visual progress bar
                val progressPercent = (currentProgress.toFloat() / gate.challengeReps.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressPercent)
                            .background(SoloPrimaryCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Interactivity states
        when {
            isVictory -> {
                // Victory Section with Animated Cascading Panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                    border = BorderStroke(2.dp, SoloNeonGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "تَمّ تَطْهير البَوّابة بنَجَاح 🏆",
                            color = SoloNeonGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "لقد هُزم الحارس الضخم وصارت طاقته رهن إشارتك.",
                            color = SoloMutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (gate.weaknessName != null) {
                                Button(
                                    onClick = {
                                        viewModel.completeGate(gate, useWeakness = true)
                                        onClose()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoloGold),
                                    modifier = Modifier.weight(1f).testTag("exploit_weakness")
                                ) {
                                    Text("نقطة الضعف (+50% جائزة)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.completeGate(gate, useWeakness = false)
                                    onClose()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoloNeonGreen),
                                modifier = Modifier.weight(1f).testTag("claim_rewards")
                            ) {
                                Text("جمع المكافأة الأساسية", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            isFailure -> {
                // Failure Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                    border = BorderStroke(2.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "فَشِل التَطْهير - لَقَد هَرَب الحارس 💀",
                            color = Color.Red,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "الوقت انتهى وانهار المونارك أمام الحارس.",
                            color = SoloMutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                viewModel.failGate(gate)
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth().testTag("failure_exit")
                        ) {
                            Text("الخروج الفوري (خصم 50% عقوبة)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                // Combat simulation control
                if (simulatedRepTrackerOpen) {
                    // Live AEGRIS wireframe coordinate panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0D15)),
                        border = BorderStroke(1.5.dp, SoloPrimaryCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AEGRIS VISION LINK • تتبع الأداء الحركي", color = SoloPrimaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                            }

                            // Dynamic Canvas simulation frame
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color.Black)
                                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(8.dp))
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val time = System.currentTimeMillis() / 300.0
                                    val movementOffset = (Math.sin(time) * 15f).toFloat()

                                    // Render Wireframe structure
                                    drawCircle(SoloPrimaryCyan, radius = 6f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.22f + movementOffset))
                                    drawCircle(SoloPrimaryCyan, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f + movementOffset))
                                    drawCircle(SoloPrimaryCyan, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f + movementOffset))
                                    drawLine(SoloPrimaryCyan, start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f + movementOffset), end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f + movementOffset), strokeWidth = 3f)

                                    drawCircle(SoloPrimaryCyan, radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.65f - movementOffset))
                                    drawCircle(SoloPrimaryCyan, radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.65f - movementOffset))
                                    drawLine(SoloPrimaryCyan, start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f + movementOffset), end = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.65f - movementOffset), strokeWidth = 3f)
                                    drawLine(SoloPrimaryCyan, start = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f + movementOffset), end = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.65f - movementOffset), strokeWidth = 3f)
                                }
                                Text("CAMERA STREAM SIMULATOR / نشط", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (currentProgress < gate.challengeReps) {
                                            currentProgress++
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoloPrimaryCyan),
                                    modifier = Modifier.weight(1.5f).testTag("trigger_rep")
                                ) {
                                    Text("تكرار صحيح +1 ⚔️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { simulatedRepTrackerOpen = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إيقاف", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { simulatedRepTrackerOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_drill_button")
                    ) {
                        Text("بَدْء تَتَبُّع تَمْرِين التَطْهير الكَامِل", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
