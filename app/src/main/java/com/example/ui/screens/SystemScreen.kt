package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    viewModel: VesselViewModel,
    stats: UserStats,
    onNavigateToRaids: () -> Unit,
    onNavigateToTitles: () -> Unit = {}
) {
    val isPenaltyActive by viewModel.isPenaltyActive.collectAsState()
    val tasks by viewModel.exercises.collectAsState()
    val aegisCheatAttempts by viewModel.aegisCheatAttempts.collectAsState()
    val aegisShameLog by viewModel.aegisShameLog.collectAsState()

    var showAegrisLogSheet by remember { mutableStateOf(false) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var currentQuoteIndex by remember { mutableStateOf(0) }
    var showDawnScreen by remember { mutableStateOf(false) }
    val isDawnMissionAvailable by viewModel.isDawnMissionAvailable.collectAsState()
    val isDawnCompleteToday = stats.lastDawnMissionDate == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    if (showDawnScreen) {
        com.example.ui.screens.DawnMissionScreen(
            viewModel = viewModel,
            stats = stats,
            onClose = { showDawnScreen = false }
        )
        return
    }

    val quotesList = listOf(
        "كل يوم تمر دون تدريب هو يوم يُضاف لضعفك." to "Every day spent without training is a day added to your weakness.",
        "الضعفاء لا يملكون خيار البحث عن الكرامة." to "The weak have no choice but to search for dignity.",
        "أنهض... فالنظام لا يرحم الكسالى." to "Arise... for the System has no mercy for the idle.",
        "هل ستقبل بالعجز؟ أم ستجتاز البوابات وتتجاوز حدودك?" to "Will you accept helplessness? Or will you breach the gates and exceed your limits?",
        "الظل ينتظر أمرك، وروح الملك تنبض في عروقك." to "The Shadow awaits your command, and the Monarch's soul pulses in your veins."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
    ) {
        // High-performance animated matrix background particle field
        com.example.ui.widgets.ParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .testTag("system_screen_container")
        ) {
            // Top Sync Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vessel Lock",
                        tint = SoloPrimaryCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "VESSEL_ID: ${stats.username}",
                        color = SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.toggleGlobalSync() }
                    ) {
                        Text(
                            text = "GLOBAL_SYNC",
                            color = if (stats.globalSync) Color.White else SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (stats.globalSync) SoloNeonGreen else SoloAccentRed)
                        )
                    }

                    IconButton(
                        onClick = { showAegrisLogSheet = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Aegis Eye Logs Inquiry",
                            tint = SoloGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(8.dp))

        if (isDawnMissionAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4A3E00), Color(0xFF000000))))
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    .clickable { showDawnScreen = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("🌅", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "مهمة الفجر متاحة. الوقت ينفد.",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انقر للمطالبة بمكافأتك المضاعفة",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else if (isDawnCompleteToday) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFF4DA6FF), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("✨", fontSize = 24.sp)
                    Text(
                        text = "اكتملت مهمة الفجر بنجاح.",
                        color = Color(0xFF4DA6FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Large Premium Rank Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SoloCardBg)
                .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "RANK STATUS / مستوى التقييم",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stats.rank,
                            color = Color(stats.rankColor),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stats.rankArabicTitle,
                            color = Color(stats.rankColor).copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stats.username,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Clickable active title badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1430))
                                    .border(BorderStroke(0.8.dp, SoloPrimaryCyan.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToTitles() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SoloPrimaryCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = stats.activeTitle,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoloActiveBlue.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, SoloActiveBlue.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LVL ${stats.level}",
                                    color = SoloPrimaryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Stability Metric bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Progress description
                        val percentValue = (stats.xp.toFloat() / stats.maxXp.toFloat() * 100).coerceAtMost(100f)
                        Text(
                            text = String.format("%.1f%%", percentValue),
                            color = SoloPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NEURAL STABILITY (XP)",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Linear progress custom track with a pointer tag
                    val animatedProgress by animateFloatAsState(
                        targetValue = (stats.xp.toFloat() / stats.maxXp.toFloat()).coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                        label = "progress"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(SoloBorderSlate)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0891B2), Color(0xFF3B82F6))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SoloBorderSlate.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val diffMult = viewModel.getRankQuestDifficultyMultiplier(stats.rank)
                    val rewardMult = viewModel.getRankRewardsMultiplier(stats.rank)
                    val penaltyMult = when (stats.rank) {
                        "SSS" -> 10.0
                        "SS" -> 6.0
                        "S" -> 4.0
                        "A" -> 3.0
                        "B" -> 2.0
                        "C" -> 1.5
                        "D" -> 1.2
                        else -> 1.0
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "صعوبة المهام",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${diffMult}x",
                            color = Color(stats.rankColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مضاعف الجوائز",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${rewardMult}x",
                            color = Color(stats.rankColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مضاعف العقوبة",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${penaltyMult}x",
                            color = Color(stats.rankColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gold & Energy Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gold card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                border = BorderStroke(1.dp, SoloBorderSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.syncKineticLink("walking", 1) } // fun interaction
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GOLD",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stats.gold}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Gold Coin Icon stylized box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloGold.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, SoloGold.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Gold Coin",
                            tint = SoloGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Energy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                border = BorderStroke(1.dp, SoloBorderSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.resetDailyQuotas() } // Refills energy / tasks
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ENERGY",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stats.energy}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lightning Icon stylized box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloActiveBlue.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, SoloActiveBlue.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Energy Bolt",
                            tint = SoloActiveBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid row: Sync %, Streak fire, Hearts lives life
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sync percentage capsule
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                    .clickable { viewModel.toggleGlobalSync() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(BorderStroke(1.dp, SoloActiveBlue.copy(alpha = 0.4f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stats.level}%",
                            color = SoloPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SYNC",
                        color = SoloMutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Streak fire counter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                    .clickable { viewModel.syncKineticLink("running", 1) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Streak Fire",
                        tint = SoloGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stats.streak}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "STREAK",
                        color = SoloMutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Hearts lives node (which visually displays hunter health pool)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = if (index < stats.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Life heart",
                                tint = if (index < stats.lives) SoloAccentRed else SoloMutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VESSEL_LIFE",
                        color = SoloMutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Aegris Dynamic Watcher Eye Container
        val completedCount = tasks.count { it.isCompleted }
        
        // Determine Aegris' state based on daily achievements/alerts
        val eyeState = remember(isPenaltyActive, stats.lives, stats.vesselStatus, completedCount) {
            when {
                isPenaltyActive || stats.lives == 0 || stats.vesselStatus == "FRAGILE" -> "ANGRY"
                completedCount >= 5 -> "SATISFIED"
                stats.vesselStatus == "OVERCHARGED" || !stats.globalSync -> "ABYSS"
                else -> "NEUTRAL"
            }
        }

        val (eyeColor, eyeQuote, eyeArabicQuote) = when (eyeState) {
            "ANGRY" -> Triple(
                Color(0xFFEF4444),
                "YOU ARE DEVIATING FROM THE PROTOCOLS. CHASTISEMENT INITIATED.",
                "أنت تنحرف عن بروتوكولات الصحوة! خضع لعقوبة إعادة التأهيل لإنقاذ حياتك."
            )
            "SATISFIED" -> Triple(
                Color(0xFF00D4FF),
                "A satisfying kinetic output. The Monarch's core is stabilizing.",
                "حصاد رائع من القوة البدنية اليوم. نواة الملك في حالتها المستقرة."
            )
            "ABYSS" -> Triple(
                Color.Black,
                "The Void is observing. Do not feed your fears.",
                "أنت تنظر الآن في فراغ الهاوية... والعدم يراقب ب صمت مهيب."
            )
            else -> Triple(
                SoloGold,
                "I am Aegris... the Eye of the System. Never compromise your commitment.",
                "أنا أغريس... عين النظام الحارسة. الواجب لا يقبل التفاوض."
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SoloCardBg)
                .border(BorderStroke(1.dp, if (eyeState == "ABYSS") SoloBorderGold else eyeColor.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Gaseous Snake Eye Canvas
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF06070B))
                        .border(BorderStroke(1.dp, eyeColor.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                        .clickable {
                            showAegrisLogSheet = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val pulseScale = rememberInfiniteTransition().animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "eye_pulse"
                    )

                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        // Eye frame curve Path
                        val path = Path().apply {
                            moveTo(5f, size.height / 2)
                            quadraticBezierTo(size.width / 2, 5f, size.width - 5f, size.height / 2)
                            quadraticBezierTo(size.width / 2, size.height - 5f, 5f, size.height / 2)
                            close()
                        }
                        
                        // Sclera Darkness
                        drawPath(path = path, color = Color(0xFF0C0E14))
                        
                        // Glowing Aura Outline
                        drawPath(
                            path = path, 
                            color = if (eyeState == "ABYSS") SoloGold else eyeColor, 
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )

                        // Iris aura circle
                        drawCircle(
                            color = if (eyeState == "ABYSS") Color.Transparent else eyeColor.copy(alpha = 0.25f),
                            radius = 20f * pulseScale.value,
                            center = center
                        )

                        // Angry fracture stress marks
                        if (eyeState == "ANGRY") {
                            drawLine(Color.Black, center, Offset(center.x + 12f, center.y - 10f), strokeWidth = 2.5f)
                            drawLine(Color.Black, center, Offset(center.x - 10f, center.y + 12f), strokeWidth = 2.5f)
                        }

                        // Serpent Vertical Slit Pupil
                        val pupilWidth = 7f * pulseScale.value
                        val pupilHeight = 32f
                        if (eyeState == "ABYSS") {
                            // Thin Golden ring pupil inside dark void
                            drawCircle(
                                color = SoloGold,
                                radius = 6f,
                                center = center,
                                style = Stroke(width = 2f)
                            )
                        } else {
                            drawOval(
                                color = Color.Black,
                                topLeft = Offset(center.x - pupilWidth / 2, center.y - pupilHeight / 2),
                                size = Size(pupilWidth, pupilHeight)
                            )
                        }

                        // Light reflections
                        if (eyeState != "ABYSS") {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = 2.5f,
                                center = Offset(center.x + 6f, center.y - 6f)
                            )
                        }
                    }
                }

                // Voice text
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(eyeColor)
                        )
                        Text(
                            text = "AEGRIS: $eyeState MONITOR",
                            color = if (eyeState == "ABYSS") SoloGold else eyeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = eyeArabicQuote,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (stats.currentFateCardId != null) {
                    val activeCard = com.example.data.FateCardsDataSource.allCards.find { it.id == stats.currentFateCardId }
                    if (activeCard != null) {
                        ActiveFateCardWidget(card = activeCard)
                    }
                }
            }
        }

        // (Old fate weave logic removed)


        Spacer(modifier = Modifier.height(16.dp))

        // Simulated manual failure / trigger penalty directly
        Button(
            onClick = { viewModel.forcePenaltyActive() },
            colors = ButtonDefaults.buttonColors(containerColor = SoloAccentRed.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, SoloAccentRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SoloAccentRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("مواجهة حارس الجزاء (دخول شاشة العقاب) 👹", color = SoloAccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- AEGIS SMART VERIFICATION LOG PANEL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Aegis Monitor",
                            tint = if (aegisCheatAttempts > 0) Color(0xFFFF0044) else SoloPrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "شبكة عين أغريس الحارسة",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = if (aegisCheatAttempts > 0) Color(0xFF3B0B14) else Color(0xFF0F1A24),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "شبهات: $aegisCheatAttempts",
                            color = if (aegisCheatAttempts > 0) Color(0xFFFF0044) else SoloPrimaryCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "سجل التحقق من سلامة الأنباض والتمارين لكشف الغش وحماية تماسك الوعاء الفرعي:",
                    color = SoloMutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (aegisShameLog.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0E14), RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ لم يتم تسجيل أي خروقات سلوكية! الوعاء نقي بالكامل.",
                            color = Color(0xFF00FFCC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        aegisShameLog.forEach { record ->
                            val readableTime = java.text.SimpleDateFormat(
                                "HH:mm:ss", java.util.Locale.getDefault()
                            ).format(java.util.Date(record.timestamp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF13090F), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(0.5.dp, Color(0x33FF0044)), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التمرين: ${record.exerciseType.uppercase()}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = readableTime,
                                        color = SoloMutedText,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "طريقة التلاعب: ${record.cheatMethod}",
                                    color = Color(0xFFFFCCDD),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الجزاء: ${record.penaltyApplied}",
                                    color = Color(0xFFFF0044),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action to clear cheat reports and reset behaviour
                Button(
                    onClick = { viewModel.resetCheatAttemptsIfClean() },
                    enabled = aegisCheatAttempts > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoloPrimaryCyan.copy(alpha = 0.15f),
                        disabledContainerColor = Color(0xFF161B22)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (aegisCheatAttempts > 0) SoloPrimaryCyan else Color(0xFF30363D)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset warning log",
                        tint = if (aegisCheatAttempts > 0) SoloPrimaryCyan else SoloMutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "طلب تصفير سجل السلوك النظيف",
                        color = if (aegisCheatAttempts > 0) SoloPrimaryCyan else SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Prompt user to check Quotas (Raids Screen)
        Button(
            onClick = onNavigateToRaids,
            colors = ButtonDefaults.buttonColors(containerColor = SoloActiveBlue.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, SoloActiveBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Begin exercises",
                tint = SoloPrimaryCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ACCESS SYSTEM DIALECTIC CHALLENGE",
                color = SoloPrimaryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    } // Column end
} // Box end

    if (showAegrisLogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAegrisLogSheet = false },
            containerColor = Color(0xFF0F0813),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Aegis Eye",
                            tint = SoloGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "سجل رقابة عين أغريس / AEGIS LOG",
                            color = SoloGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "إنذارات: ${aegisCheatAttempts}",
                        color = if (aegisCheatAttempts > 0) SoloAccentRed else SoloPrimaryCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Divider(color = SoloBorderSlate)
                
                if (aegisShameLog.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ لم يتم تسجيل أي تجاوزات أو غش! أنت تتبع المسار النبيل.",
                            color = SoloNeonGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        items(aegisShameLog) { record ->
                            val readableTime = java.text.SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
                            ).format(java.util.Date(record.timestamp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF16101D), RoundedCornerShape(12.dp))
                                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "تمرين: ${record.exerciseType.uppercase()}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = readableTime,
                                        color = SoloMutedText,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "طريقة الكشف: ${record.cheatMethod}",
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 12.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Penalty",
                                        tint = SoloAccentRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "الجزاء: ${record.penaltyApplied}",
                                        color = SoloAccentRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal dialogue showing quotation details when eye clicked
    if (showQuoteDialog) {
        val completedCount = tasks.count { it.isCompleted }
        
        val eyeState = when {
            isPenaltyActive || stats.lives == 0 || stats.vesselStatus == "FRAGILE" -> "ANGRY"
            completedCount >= 5 -> "SATISFIED"
            stats.vesselStatus == "OVERCHARGED" || !stats.globalSync -> "ABYSS"
            else -> "NEUTRAL"
        }

        val (eyeColorCustom, currentTextEn, currentTextAr) = when (eyeState) {
            "ANGRY" -> Triple(
                Color(0xFFEF4444),
                "YOU ARE DEVIATING FROM THE PROTOCOLS. CHASTISEMENT INITIATED.",
                "أنت تنحرف عن بروتوكولات الصحوة! خضع لعقوبة إعادة التأهيل لإنقاذ حياتك."
            )
            "SATISFIED" -> Triple(
                Color(0xFF00D4FF),
                "A satisfying kinetic output. The Monarch's core is stabilizing.",
                "حصاد رائع من القوة البدنية اليوم. نواة الملك في حالتها المستقرة."
            )
            "ABYSS" -> Triple(
                Color.Black,
                "The Void is observing. Do not feed your fears.",
                "أنت تنظر الآن في فراغ الهاوية... والعدم يراقب ب صمت مهيب."
            )
            else -> Triple(
                SoloGold,
                "I am Aegris... the Eye of the System. Never compromise your commitment.",
                "أنا أغريس... عين النظام الحارسة. الواجب لا يقبل التفاوض."
            )
        }

        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.RemoveRedEye, contentDescription = null, tint = eyeColorCustom)
                    Text(text = "AEGRIS WATCHER INTELLIGENCE", color = eyeColorCustom, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentTextAr,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(color = SoloBorderCyan)
                    Text(
                        text = currentTextEn,
                        color = SoloMutedText,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aegris adjusts quest metrics based on your weaknesses. Avoid ignoring quotas to maintain vital life node stabilization.",
                        color = SoloPrimaryCyan.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuoteDialog = false }) {
                    Text("COMPLY", color = eyeColorCustom)
                }
            },
            containerColor = SoloCardBg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(BorderStroke(1.dp, eyeColorCustom), RoundedCornerShape(16.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFateCardWidget(card: com.example.data.FateCard, modifier: Modifier = Modifier) {
    var showSheet by remember { mutableStateOf(false) }

    val categoryColor = when (card.type) {
        "blessing" -> Color(0xFFFFD700)
        "neutral" -> Color(0xFFC0C0C0)
        "curse" -> Color(0xFFFF0044)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(categoryColor.copy(alpha = 0.2f))
            .border(BorderStroke(1.dp, categoryColor), RoundedCornerShape(8.dp))
            .clickable { showSheet = true },
        contentAlignment = Alignment.Center
    ) {
        val icon = when (card.type) {
            "blessing" -> "✦"
            "neutral" -> "✧"
            "curse" -> "☠"
            else -> "🃏"
        }
        Text(text = icon, fontSize = 20.sp, color = categoryColor)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color(0xFF161616)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "بطاقة القدر النشطة",
                    color = categoryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = card.description,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
                Divider(color = categoryColor.copy(alpha = 0.5f))
                Text(
                    text = card.effectDescription,
                    color = categoryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}