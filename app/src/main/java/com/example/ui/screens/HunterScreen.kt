package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.graphics.Brush
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

@Composable
fun HunterScreen(viewModel: VesselViewModel, stats: UserStats) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAchievementTab by remember { mutableStateOf("ALL") }
    val shadowSoldiers by viewModel.shadows.collectAsState()
    var showShadowsScreen by remember { mutableStateOf(false) }

    val achievements = listOf(
        AchievementItem("Sovereign of Light", "سيد الضوء", "LEGENDARY RANK PROTOCOL", "Condition: نجاح بنسبة 100% في بروتوكول الفجر لمدة أسبوع", "RARE", isLocked = true),
        AchievementItem("Shadow Monarch", "ملك الظلال", "MONARCH RANK PROTOCOL", "Condition: الوصول إلى المستوى 10 في النظام وتحسين كل الخصائص", "RARE", isLocked = stats.level < 10),
        AchievementItem("Dungeon Sweeper", "كاسح المغارات", "ELITE RANK PROTOCOL", "Condition: إكمال 15 تمرين يومي بنجاح تام", "UNCOMMON", isLocked = stats.streak < 5),
        AchievementItem("First Awakening", "الصحوة الأولى", "BASIC RANK PROTOCOL", "Condition: إكمال أول خطوة في النظام بنجاح", "COMMON", isLocked = false)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoloBackground)
                .padding(horizontal = 16.dp)
                .testTag("hunter_screen_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // Core Hunter Profile Header Panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                // Background circle decals or design
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = SoloMutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stats.username,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "NEURAL VESSEL STATUS: ${stats.vesselStatus}",
                                    color = SoloPrimaryCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SoloNeonGreen, CircleShape)
                                )
                            }
                        }

                        // Big stylized CPU chips container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoloActiveBlue.copy(alpha = 0.1f))
                                .border(BorderStroke(1.dp, SoloActiveBlue), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Vessel chip Core",
                                tint = SoloPrimaryCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Progress stats & level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "XP PROGRESS",
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${stats.xp} / ${stats.maxXp}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "RANK E • HUNTER",
                                color = SoloMutedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "LVL ${stats.level}",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Bottom progress track
                    Column {
                        val animatedProgress by animateFloatAsState(
                            targetValue = (stats.xp.toFloat() / stats.maxXp.toFloat()).coerceIn(0f, 1f),
                            label = "xp_bar"
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val pct = (stats.xp.toFloat() / stats.maxXp.toFloat() * 100).coerceAtMost(100f)
                            Text(
                                text = String.format("%.1f%%", pct),
                                color = SoloPrimaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "NEURAL LINK STABILITY",
                                color = SoloMutedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Two Matrix grids (Skill Matrix, Shadow Army)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skill Matrix Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                    border = BorderStroke(1.dp, SoloBorderSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { viewModel.addLog("Triggered Skill Matrix. Diagnostics complete.") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Brain Core",
                            tint = SoloPrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SKILL MATRIX",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Shadow Army Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                    border = BorderStroke(1.dp, SoloBorderSlate),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { showShadowsScreen = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CoPresent,
                            contentDescription = "Monarch Shadow",
                            tint = Color(0xFFA55EFF), // Purple shadow hue
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SHADOW ARMY",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // VESSEL CORE ATTRIBUTES Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "VESSEL CORE ATTRIBUTES",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = SoloPrimaryCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (stats.statPoints > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoloGold.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, SoloGold), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AP POINTS: ${stats.statPoints}",
                                color = SoloGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (stats.statPoints > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You have unallocated stat points! Tapping an attribute below will spend 1 SP.",
                        color = SoloGold.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Attributes 2x2 grid
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AttributeCard(
                        name = "AGI",
                        value = stats.agi,
                        growth = "20%",
                        color = SoloGold,
                        icon = Icons.Default.FlashOn,
                        hasPoints = stats.statPoints > 0,
                        onUpgrade = { viewModel.upgradeAttribute("agi") }
                    )

                    AttributeCard(
                        name = "STR",
                        value = stats.str,
                        growth = "20%",
                        color = SoloAccentRed,
                        icon = Icons.Default.Shield,
                        hasPoints = stats.statPoints > 0,
                        onUpgrade = { viewModel.upgradeAttribute("str") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AttributeCard(
                        name = "END",
                        value = stats.endurance,
                        growth = "20%",
                        color = SoloNeonGreen,
                        icon = Icons.Default.VerifiedUser,
                        hasPoints = stats.statPoints > 0,
                        onUpgrade = { viewModel.upgradeAttribute("end") }
                    )

                    AttributeCard(
                        name = "INT",
                        value = stats.intl,
                        growth = "20%",
                        color = SoloPrimaryCyan,
                        icon = Icons.Default.Psychology,
                        hasPoints = stats.statPoints > 0,
                        onUpgrade = { viewModel.upgradeAttribute("int") }
                    )
                }
            }
        }

        // HISTORICAL ACHIEVEMENTS
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "HISTORICAL ACHIEVEMENTS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Achievements",
                    tint = SoloGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Search achievements & Category pills
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("...Search titles", color = SoloMutedText, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SoloMutedText) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SoloCardBg,
                        unfocusedContainerColor = SoloCardBg,
                        disabledContainerColor = SoloCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = SoloPrimaryCyan,
                        unfocusedIndicatorColor = SoloBorderSlate
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("RARE", "UNCOMMON", "COMMON", "ALL")
                    tabs.forEach { tab ->
                        val isSel = selectedAchievementTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) SoloPrimaryCyan else SoloCardBg)
                                .border(
                                    BorderStroke(1.dp, if (isSel) SoloPrimaryCyan else SoloBorderSlate),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedAchievementTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSel) Color.Black else SoloMutedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // List of Achievements
        val filteredAchievements = achievements.filter {
            (searchQuery.isEmpty() || it.engTitle.contains(searchQuery, ignoreCase = true) || it.arabicTitle.contains(searchQuery)) &&
                    (selectedAchievementTab == "ALL" || it.rarity == selectedAchievementTab)
        }

        if (filteredAchievements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No compatible level protocols found for search.", color = SoloMutedText, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredAchievements) { achievement ->
                AchievementCardItem(achievement)
            }
        }
    }

    if (showShadowsScreen) {
        ShadowMonarchSanctuary(
            stats = stats,
            shadows = shadowSoldiers,
            onClose = { showShadowsScreen = false },
            onExtract = { viewModel.extractShadow(it) },
            onUpgrade = { viewModel.upgradeShadow(it) }
        )
    }
}
}

@Composable
fun RowScope.AttributeCard(
    name: String,
    value: Int,
    growth: String,
    color: Color,
    icon: ImageVector,
    hasPoints: Boolean,
    onUpgrade: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(115.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SoloCardBg)
            .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
            .clickable(enabled = hasPoints, onClick = onUpgrade)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = name,
                        color = SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "....$value",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Mini stylized icon circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasPoints) Icons.Default.Add else icon,
                        contentDescription = name,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // progress level indicators
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SoloBorderSlate)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.2f + (value * 0.05f).coerceAtMost(0.8f))
                            .fillMaxHeight()
                            .background(color)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${20 + value * 5}%",
                        color = SoloPrimaryCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GROWTH FACTOR",
                        color = SoloMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class AchievementItem(
    val engTitle: String,
    val arabicTitle: String,
    val category: String,
    val conditionArabic: String,
    val rarity: String,
    val isLocked: Boolean
)

@Composable
fun AchievementCardItem(achievement: AchievementItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SoloCardBg.copy(alpha = if (achievement.isLocked) 0.5f else 1f))
            .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "(${achievement.engTitle}) ${achievement.arabicTitle}",
                    color = if (achievement.isLocked) SoloMutedText else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.category,
                    color = if (achievement.isLocked) SoloMutedText else SoloGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = achievement.conditionArabic,
                    color = SoloMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lock node
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isLocked) SoloMutedText.copy(alpha = 0.1f)
                        else SoloGold.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (achievement.isLocked) "Locked" else "Unlocked",
                    tint = if (achievement.isLocked) SoloMutedText else SoloGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ShadowMonarchSanctuary(
    stats: UserStats,
    shadows: List<com.example.data.ShadowSoldier>,
    onClose: () -> Unit,
    onExtract: (String) -> Unit,
    onUpgrade: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070014)) // Deep, dark purple shadow cosmos
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1235))
                        .border(BorderStroke(1.dp, Color(0xFF9E77ED)), CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFD6BBFB)
                    )
                }

                Column {
                    Text(
                        text = "SHADOW MONARCH'S SANCTUARY",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "معقل عرش ملك الظلال - جيش الظلال",
                        color = Color(0xFFB692F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mana and Gold Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F0824))
                    .border(BorderStroke(1.dp, Color(0xFF6F3FF5)), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFB692F6),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SHADOW MANA / طاقة الظلال",
                                color = Color(0xFFE9E5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${stats.manaStored} / ${stats.manaGoal} MP",
                            color = Color(0xFFA55EFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Mana Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF171032))
                    ) {
                        val runProgress = (stats.manaStored.toFloat() / stats.manaGoal.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(runProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7F56D9), Color(0xFFA55EFF))
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mana is extracted when clearing workout gates",
                            color = SoloMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = SoloGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${stats.gold} G",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Section Info Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMAND SHADOW SOLDIERS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "استدعاء وترقية جنود الظلال",
                    color = Color(0xFFB692F6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable List of Shadows
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(shadows) { shadow ->
                    ShadowSoldierCard(
                        shadow = shadow,
                        currentMana = stats.manaStored,
                        currentGold = stats.gold,
                        onExtract = { onExtract(shadow.id) },
                        onUpgrade = { onUpgrade(shadow.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShadowSoldierCard(
    shadow: com.example.data.ShadowSoldier,
    currentMana: Int,
    currentGold: Int,
    onExtract: () -> Unit,
    onUpgrade: () -> Unit
) {
    val isLocked = shadow.level == 0
    val borderClr = if (isLocked) Color(0xFF1F1235) else Color(0xFF7F56D9)
    val bgClr = if (isLocked) Color(0xFF0F0824).copy(alpha = 0.6f) else Color(0xFF0F0824)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgClr)
            .border(BorderStroke(1.2.dp, borderClr), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = shadow.name,
                            color = if (isLocked) Color.Gray else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = shadow.arabicName,
                            color = if (isLocked) Color.Gray else Color(0xFFB692F6),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isLocked) "SHADOW CORE LOCKED / نواة ظل مقفلة" else "${shadow.rank} • LVL ${shadow.level}",
                        color = if (isLocked) Color(0xFF475467) else Color(0xFF9E77ED),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Status circular badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLocked) Color(0xFF1F1235) else Color(0xFF7F56D9).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isLocked) Color.Gray else Color(0xFFB692F6),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Description block
            Text(
                text = if (isLocked) shadow.arabicDescription else "${shadow.description}\n${shadow.arabicDescription}",
                color = if (isLocked) Color(0xFF475467) else Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 15.sp
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFF1F1235))
            )

            // Dynamic Action / Benefits bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Benefits display
                Column {
                    Text(
                        text = "MONARCH PASSIVE BENEFIT",
                        color = Color(0xFFB692F6),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "+${shadow.passiveAmount} ${shadow.passiveStat.uppercase()}",
                        color = if (isLocked) Color.Gray else SoloNeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Action Button
                if (isLocked) {
                    val canAfford = currentMana >= shadow.manaCost
                    Button(
                        onClick = onExtract,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFFA55EFF) else Color(0xFF1F1235),
                            contentColor = if (canAfford) Color.White else Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        enabled = canAfford
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (canAfford) Color.White else Color.Gray
                            )
                            Text(
                                text = "ARISE / استخلاص",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val upgradeCost = shadow.goldCost * shadow.level
                    val canAfford = currentGold >= upgradeCost
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) SoloGold else Color(0xFF1F1235),
                            contentColor = if (canAfford) Color.Black else Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        enabled = canAfford
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (canAfford) Color.Black else Color.Gray
                            )
                            Text(
                                text = "EVOLVE / ترقية (${upgradeCost} G)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

