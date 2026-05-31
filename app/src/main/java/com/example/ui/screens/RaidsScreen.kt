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
            // GATES tab view (Living Dungeons Giga Boss Fight)
            val dungeonMood by viewModel.dungeonMood.collectAsState()
            val dungeonBossName by viewModel.dungeonBossName.collectAsState()
            val dungeonBossHp by viewModel.dungeonBossHp.collectAsState()
            val dungeonBossMaxHp by viewModel.dungeonBossMaxHp.collectAsState()
            
            val (moodColor, moodArabic, moodDesc) = when (dungeonMood) {
                "ANGRY" -> Triple(Color(0xFFEF4444), "غاضبة (صعوبة مضاعفة 2x مكافآت)", "تم رصد موجات طاقة غاضبة! الخصم أشرس والجوائز مضاعفة 2x!")
                "SLEEPING" -> Triple(Color(0xFF3B82F6), "نائمة (صعوبة معتدلة)", "بوابة نوم هادئ. قتال عادي مع مكافآت ثابتة.")
                "HUNGRY" -> Triple(Color(0xFFF97316), "جائعة (تطلب قربان 50 AP)", "البوابة تطلب التضحية بقربان 50 AP لحصد غنائمها!")
                "GENEROUS" -> Triple(Color(0xFF10B981), "سخية (مكافأة مضاعفة مجانية)", "البوابة تهب الكنوز السخية مجاناً دون زيادة الخطورة!")
                else -> Triple(Color(0xFFA55EFF), "أسطورية (الحارس الأعظم)", "تم استدعاء وحش أسطوري! مكافأة خرافية بانتظار قاهري الرقابة.")
            }

            val bossEmoji = when (dungeonBossName) {
                "WHITE WEREWOLF" -> "🐺 ذئب الهاوية الأبيض (White Werewolf)"
                "STONE GOLEM" -> "🧌 غولم الحجر الملعون (Stone Golem)"
                "EVIL EYE" -> "👁️ العين الشريرة الحارسة (Evil Eye)"
                else -> "💀 ملك الموتى الحارس الأعظم (King of Death)"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, moodColor), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mood capsule
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(moodColor.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, moodColor), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مزاج المغارة: $moodArabic",
                                    color = moodColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "LIVING PORTALS GATE ACTIVE",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = moodDesc,
                                color = SoloMutedText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }

                // Monster Boss Pod
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                        border = BorderStroke(1.dp, SoloBorderSlate),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "ACTIVE GATE GUARDIAN",
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            // Giant monster avatar
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(moodColor.copy(alpha = 0.05f), CircleShape)
                                    .border(BorderStroke(1.dp, moodColor.copy(alpha = 0.3f)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val pulseScale = rememberInfiniteTransition().animateFloat(
                                    initialValue = 0.9f,
                                    targetValue = 1.1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "boss_pulse"
                                )
                                Text(
                                    text = bossEmoji.take(2),
                                    fontSize = (48f * pulseScale.value).sp
                                )
                            }

                            Text(
                                text = bossEmoji.drop(2),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )

                            // Boss HP state gauge
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "BOSS HP: $dungeonBossHp/$dungeonBossMaxHp",
                                        color = moodColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val percent = ((dungeonBossHp.toFloat() / dungeonBossMaxHp.toFloat()) * 100).toInt()
                                    Text(
                                        text = "$percent%",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val animatedHp by animateFloatAsState(
                                    targetValue = dungeonBossHp.toFloat() / dungeonBossMaxHp.toFloat(),
                                    animationSpec = tween(400)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape)
                                        .background(SoloBorderSlate)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedHp)
                                            .fillMaxHeight()
                                            .background(moodColor)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Strike buttons
                            val strikePower = 10 + (stats.str * 2) + (stats.agi)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        if (dungeonMood == "HUNGRY" && dungeonBossHp == dungeonBossMaxHp) {
                                            val valid = viewModel.payDungeonEntrance()
                                            if (valid) viewModel.attackDungeonBoss(strikePower)
                                        } else {
                                            viewModel.attackDungeonBoss(strikePower)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = moodColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Cyclone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("STRIKE BLADE ⚔️ (دمج $strikePower)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Text(
                                text = "قوة الهجوم ترتكز على إحصائيات القوة (STR) والرشاقة (AGI) في حوزتك.",
                                color = SoloMutedText,
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
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
