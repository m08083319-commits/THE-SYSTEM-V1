package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VesselViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoloBackground
import com.example.ui.theme.SoloCardBg
import com.example.ui.theme.SoloMutedText
import com.example.ui.theme.SoloPrimaryCyan

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val viewModel: VesselViewModel = viewModel()
    val statsState = viewModel.userStats.collectAsState()
    val isPenaltyActive by viewModel.isPenaltyActive.collectAsState()
    var currentTab by remember { mutableStateOf("SYSTEM") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground),
        bottomBar = {
            if (!isPenaltyActive) {
                CustomBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoloBackground)
                .padding(bottom = if (isPenaltyActive) 0.dp else innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            val stats = statsState.value
            if (isPenaltyActive) {
                PenaltyScreen(viewModel = viewModel)
            } else if (stats == null) {
                // Loading screen representing System Awakening
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = SoloPrimaryCyan)
                        Text(
                            text = "AWAKENING VESSEL NEURAL LINK...",
                            color = SoloPrimaryCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            } else {
                // Screen routers
                when (currentTab) {
                    "INTEL" -> IntelScreen(viewModel = viewModel, stats = stats)
                    "MARKET" -> MarketScreen(viewModel = viewModel, stats = stats)
                    "HUNTER" -> HunterScreen(viewModel = viewModel, stats = stats)
                    "RAIDS" -> RaidsScreen(viewModel = viewModel, stats = stats)
                    "SYSTEM" -> SystemScreen(
                        viewModel = viewModel,
                        stats = stats,
                        onNavigateToRaids = { currentTab = "RAIDS" }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    // Custom flat bottom nav mimicking the images
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF06070B)) // Darker absolute black for navigation contrast
            .navigationBarsPadding() // Preserve navigation bars inset padding for accessibility
    ) {
        // High contrast layout divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFF1E293B))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabsList = listOf(
                NavigationTabItem("INTEL", "INTEL"),
                NavigationTabItem("MARKET", "MARKET"),
                NavigationTabItem("HUNTER", "HUNTER"),
                NavigationTabItem("RAIDS", "RAIDS"),
                NavigationTabItem("SYSTEM", "SYSTEM")
            )

            tabsList.forEach { item ->
                val isActive = currentTab == item.id
                val tabColor = if (isActive) SoloPrimaryCyan else SoloMutedText

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.id) }
                        .testTag("nav_tab_${item.id.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Small active glow pill bar indicator on top of icon
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isActive) SoloPrimaryCyan else Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stylized item icons
                    if (item.id == "INTEL") {
                        // The stylized N icon
                        val intelBorderColor = if (isActive) SoloPrimaryCyan else SoloMutedText.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(BorderStroke(1.2.dp, intelBorderColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                color = tabColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val vector = when (item.id) {
                            "MARKET" -> Icons.Default.ShoppingBag
                            "HUNTER" -> Icons.Default.Person
                            "RAIDS" -> Icons.Default.Cyclone // Daggers representation
                            else -> Icons.Default.Home
                        }
                        Icon(
                            imageVector = vector,
                            contentDescription = item.label,
                            tint = tabColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.label,
                        color = tabColor,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

data class NavigationTabItem(
    val id: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenaltyScreen(viewModel: VesselViewModel) {
    val penaltyState by viewModel.activePenalty.collectAsState()
    val statsState by viewModel.userStats.collectAsState()
    val stats = statsState ?: com.example.data.UserStats()
    val doubleStreak = viewModel.streakThreeDaysFailed.collectAsState().value

    // If active penalty is null, generate a fallback mock penalty so the screen is always functional
    val penalty = penaltyState ?: remember {
        com.example.data.Penalty(
            id = "fallback",
            missedMissions = 3,
            startTime = System.currentTimeMillis()
        )
    }

    val rank = stats.rank
    val timeMult = penalty.getTimeMultiplier()
    val rankMult = penalty.getRankMultiplier(rank)
    val totalMultiplier = timeMult * rankMult * (if (doubleStreak) 2.0 else 1.0)

    val reqBurpees = penalty.getTotalBurpees(rank, doubleStreak)
    val reqPushups = penalty.getTotalPushups(rank, doubleStreak)
    val reqSquats = penalty.getTotalSquats(rank, doubleStreak)
    val reqPullups = penalty.getTotalPullups(rank, doubleStreak)
    val reqRunning = penalty.getTotalRunningKm(rank, doubleStreak)

    // Current screen context: "LOCKDOWN_HOME" or "EXERCISE_CAMERA"
    var currentUiMode by remember { mutableStateOf("LOCKDOWN_HOME") }
    var activeExerciseToVerify by remember { mutableStateOf<String?>(null) }

    // Disable device/soft back button
    androidx.activity.compose.BackHandler(enabled = true) {
        // Explicitly block navigating back
    }

    if (currentUiMode == "EXERCISE_CAMERA" && activeExerciseToVerify != null) {
        // Exercise Calibration Frame with Simulated Camera Feed
        ExerciseVerificationScreen(
            exerciseType = activeExerciseToVerify!!,
            targetCount = when (activeExerciseToVerify) {
                "pushups" -> reqPushups
                "squats" -> reqSquats
                "burpees" -> reqBurpees
                "pullups" -> reqPullups
                else -> reqPushups
            },
            currentProgress = when (activeExerciseToVerify) {
                "pushups" -> penalty.pushupsProgress
                "squats" -> penalty.squatsProgress
                "burpees" -> penalty.burpeesProgress
                "pullups" -> penalty.pullupsProgress
                else -> 0
            },
            cheatAttempts = penalty.cheatAttempts,
            onVerifiedRep = { rep ->
                viewModel.updatePenaltyExerciseProgress(activeExerciseToVerify!!, 1.0)
            },
            onCheatDetected = {
                viewModel.reportCheatAttempt()
            },
            onClose = {
                currentUiMode = "LOCKDOWN_HOME"
                activeExerciseToVerify = null
            }
        )
    } else {
        // PRIMARY LOCKED PANEL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0204)) // Dangerous deep crimson-black blood mood
                .padding(20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 SYSTEM OVERRIDE LOCKED / النظام قيد الإغلاق",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                
                // Cheat Attempts warning indicators
                Text(
                    text = "محاولات الغش: ${penalty.cheatAttempts} / 3",
                    color = if (penalty.cheatAttempts > 0) Color(0xFFFBBF24) else SoloMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Pulsing Aegris Eye Graphic representation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFB91C1C).copy(alpha = 0.12f), CircleShape)
                    .border(BorderStroke(2.dp, Color(0xFFEF4444)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                ) {
                    // Pulsing pupil effect
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Black, CircleShape)
                            .align(Alignment.Center)
                    )
                }
            }

            Text(
                text = "سجن النظام • SYSTEM LOCKDOWN",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            // Dynamic aegris dialogue quoting severity
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF200507)),
                border = BorderStroke(1.dp, Color(0xFF991B1B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "كلام أغريس (Aegris):",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "\"الهاتف أصبح سجنك الآن. لن تتحرر من سلطة النظام حتى تدفع ثمن فشلك في إتمام مهامك اليومية بالجهد والعرق البدني الصارم.\"",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Temporal Escalation Metrics Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130305)),
                    border = BorderStroke(1.dp, Color(0xFF450A0A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("مضاعف الزمن", color = SoloMutedText, fontSize = 9.sp)
                        Text("x$timeMult", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130305)),
                    border = BorderStroke(1.dp, Color(0xFF450A0A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("رتبتك ($rank)", color = SoloMutedText, fontSize = 9.sp)
                        Text("x$rankMult", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130305)),
                    border = BorderStroke(1.dp, Color(0xFF450A0A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("المضاعف الكلي", color = SoloMutedText, fontSize = 9.sp)
                        Text(String.format("x%.1f", totalMultiplier), color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Exercise parameters and targets
            Text(
                text = "قائمة العقوبات والواجبات المطلوبة لتوليد مفتاح الإعتاق:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SQUATS Task item
                item {
                    PenaltyExerciseItemRow(
                        name = "تمارين القرفصاء (SQUATS)",
                        progress = penalty.squatsProgress,
                        target = reqSquats,
                        onVerifyClick = {
                            activeExerciseToVerify = "squats"
                            currentUiMode = "EXERCISE_CAMERA"
                        }
                    )
                }

                // PUSHUPS Task item
                item {
                    PenaltyExerciseItemRow(
                        name = "تمارين الضغط (PUSH-UPS)",
                        progress = penalty.pushupsProgress,
                        target = reqPushups,
                        onVerifyClick = {
                            activeExerciseToVerify = "pushups"
                            currentUiMode = "EXERCISE_CAMERA"
                        }
                    )
                }

                // BURPEES Task item
                item {
                    PenaltyExerciseItemRow(
                        name = "تمارين البيربي المميتة (BURPEES)",
                        progress = penalty.burpeesProgress,
                        target = reqBurpees,
                        onVerifyClick = {
                            activeExerciseToVerify = "burpees"
                            currentUiMode = "EXERCISE_CAMERA"
                        }
                    )
                }

                // PULLUPS Task item
                item {
                    PenaltyExerciseItemRow(
                        name = "تمارين العقلة (PULL-UPS)",
                        progress = penalty.pullupsProgress,
                        target = reqPullups,
                        onVerifyClick = {
                            activeExerciseToVerify = "pullups"
                            currentUiMode = "EXERCISE_CAMERA"
                        }
                    )
                }

                // RUNNING (Simulation quick log to keep running friendly)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C0709)),
                        border = BorderStroke(1.dp, Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الجري الميداني (RUNNING)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = String.format("%.2f / %.2f KM", penalty.runningProgressKm, reqRunning),
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (penalty.runningProgressKm / reqRunning.coerceAtLeast(0.1)).toFloat().coerceIn(0f, 1f) },
                                color = Color(0xFFEF4444),
                                trackColor = Color(0xFF450A0A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "التقط وتتبع حركة نظام المواقع GPS",
                                    color = SoloMutedText,
                                    fontSize = 9.sp
                                )

                                Button(
                                    onClick = { viewModel.updatePenaltyExerciseProgress("running", 0.25) },
                                    enabled = penalty.runningProgressKm < reqRunning,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB91C1C),
                                        disabledContainerColor = Color(0xFF450A0A)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("محاكاة جري +0.25KM", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Debug Tools panel for simulating time shifts and audit escalations
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF26100E)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🛡️ لوحة محاكاة وتصحيح وتصعيد الزمن (Escalation Sim)",
                                color = Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulateTimePassage(1.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+1 ساعة (x2.0)", fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { viewModel.simulateTimePassage(6.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+6 ساعات (x5.0)", fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { viewModel.simulateTimePassage(24.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("+24 جرح (x10.0)", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Release button is locked tight until isCompleted == true
            Button(
                onClick = { viewModel.exitLockdown() },
                enabled = penalty.isCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("exit_lockdown_button")
            ) {
                Text(
                    text = if (penalty.isCompleted) "إعادة تشغيل النظام وإتاحة الاتصال 🔓" else "العقوبة معلقة • أتمم التمارين كاملة لفك القفل",
                    color = if (penalty.isCompleted) Color.White else SoloMutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PenaltyExerciseItemRow(
    name: String,
    progress: Int,
    target: Int,
    onVerifyClick: () -> Unit
) {
    val completed = progress >= target
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C0709)),
        border = BorderStroke(1.dp, if (completed) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF7F1D1D)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (completed) {
                        Text("✅ مكتمل", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text(
                    text = "الإنجاز الحالي: $progress / $target عدات صحيحة المجهرية",
                    color = SoloMutedText,
                    fontSize = 11.sp
                )

                LinearProgressIndicator(
                    progress = { (progress.toFloat() / target.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    color = if (completed) Color(0xFF10B981) else Color(0xFFEF4444),
                    trackColor = Color(0xFF450A0A),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                )
            }

            Button(
                onClick = onVerifyClick,
                enabled = !completed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    disabledContainerColor = Color(0xFF344154).copy(alpha = 0.2f)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "تشغيل الكاميرا 📷",
                    color = if (completed) SoloMutedText else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseVerificationScreen(
    exerciseType: String,
    targetCount: Int,
    currentProgress: Int,
    cheatAttempts: Int,
    onVerifiedRep: (Int) -> Unit,
    onCheatDetected: () -> Unit,
    onClose: () -> Unit
) {
    var detectedPersonInFrame by remember { mutableStateOf(true) }
    var detectedKeypointsCount by remember { mutableStateOf(33) }
    var progressAnglePercent by remember { mutableStateOf(0.4f) }
    var showRedCheatWarning by remember { mutableStateOf(false) }
    var framesTracked by remember { mutableStateOf(128) }

    // Coroutine triggers natural body coordinates shifts to represent realistic camera feeds
    LaunchedEffect(key1 = true) {
        while (true) {
            kotlinx.coroutines.delay(1800)
            framesTracked += 5
            progressAnglePercent = (0.2f + Math.sin(System.currentTimeMillis() / 400.0) * 0.5f).toFloat().coerceIn(0.1f, 0.9f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper telemetry info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111116))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            Text(
                text = "تتبع نقاط الكاميرا • AEGRIS AI VISION",
                color = SoloPrimaryCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Green, CircleShape)
            )
        }

        // Camera Feed simulation square
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .padding(horizontal = 14.dp)
                .border(BorderStroke(2.dp, if (showRedCheatWarning) Color.Red else Color(0xFF13151D)), RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1015))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (detectedPersonInFrame) {
                // Skeleton wires visual
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Face points
                    drawCircle(Color.Green, radius = 6f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f))
                    // Left and right shoulders
                    drawCircle(Color.Green, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f))
                    drawCircle(Color.Green, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f))
                    // Connect shoulders skeleton line
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f), end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f), strokeWidth = 3f)

                    // Left and right hip points
                    drawCircle(Color.Green, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.65f))
                    drawCircle(Color.Green, radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.65f))
                    // Torso links
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f), end = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.65f), strokeWidth = 3f)
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f), end = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.65f), strokeWidth = 3f)
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.65f), end = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.65f), strokeWidth = 3f)

                    // Elbows and knees points
                    drawCircle(Color.Green, radius = 6f, center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.51f))
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.38f), end = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.51f), strokeWidth = 3f)

                    drawCircle(Color.Green, radius = 6f, center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.51f))
                    drawLine(Color.Green, start = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.38f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.51f), strokeWidth = 3f)
                }
            } else {
                Text(
                    text = "🚨 WARNING: NO HUMAN BODY DETECTED IN CAMERA LENSE / الكاميرا لا تكتشف جسداً بشرياً",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )
            }

            // Green overlay text indicating active MLKit links
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(6.dp)
            ) {
                Text("ML_KIT STATUS: RUNNING_STABLE", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("LENSE: FRONT_CAMERA_ACTIVE", color = Color.Green, fontSize = 9.sp)
                Text("KEYPOINTS LINKED: $detectedKeypointsCount / 33", color = Color.Green, fontSize = 9.sp)
                Text("FRAMES RUNNING: $framesTracked", color = Color.Green, fontSize = 9.sp)
                Text(String.format("CALIBRATION_COEFF: %.2f", progressAnglePercent), color = Color.Green, fontSize = 9.sp)
            }

            if (showRedCheatWarning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ لم أحتسب هذه العدة!\nتم الكشف عن حركة مريبة أو اهتزاز حاد.",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Control HUD block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تتبع تمارين: ${exerciseType.uppercase()}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الهدف الإجمالي", color = SoloMutedText, fontSize = 11.sp)
                        Text("$targetCount", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المنجز الفعلي", color = SoloMutedText, fontSize = 11.sp)
                        Text("$currentProgress", color = SoloPrimaryCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("محاولات غش", color = SoloMutedText, fontSize = 11.sp)
                        Text("$cheatAttempts / 3", color = if (cheatAttempts > 0) Color.Red else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Exercise log buttons simulating motion thresholds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (detectedPersonInFrame) {
                                onVerifiedRep(1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloPrimaryCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("لوح رصد عدة صحيحة (Rep Complete)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showRedCheatWarning = true
                            onCheatDetected()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("محاكاة غش (Flag Cheat)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Toggle for testing body absence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محاكاة غياب الرصد البشري أمام الكاميرا:",
                        color = SoloMutedText,
                        fontSize = 11.sp
                    )

                    Switch(
                        checked = detectedPersonInFrame,
                        onCheckedChange = { detectedPersonInFrame = it }
                    )
                }

                // Clear/Disable cheat warning manually in simulation if locked
                if (showRedCheatWarning) {
                    Button(
                        onClick = { showRedCheatWarning = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إعادة تصفير إنذار الحركة المريبة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
