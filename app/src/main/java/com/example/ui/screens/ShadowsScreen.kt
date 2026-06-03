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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ShadowSoldier
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShadowsScreen(
    viewModel: VesselViewModel, 
    stats: UserStats,
    onBack: () -> Unit = {}
) {
    val shadowList by viewModel.shadows.collectAsState()
    val newlyUnlockedShadow by viewModel.newlyUnlockedShadow.collectAsState()
    
    var showMergeDialog by remember { mutableStateOf(false) }

    // Header Glow Brush
    val purpleGlow = Brush.verticalGradient(
        colors = listOf(Color(0xFF8A2BE2).copy(alpha = 0.2f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
    ) {
        // Main Screen Scaffold Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Top Bar Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(purpleGlow)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SoloCardBg, CircleShape)
                            .border(BorderStroke(1.dp, SoloBorderSlate), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "نظام فيلق الظلال العظيم",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = { showMergeDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF8A2BE2).copy(alpha = 0.15f), CircleShape)
                            .border(BorderStroke(1.5.dp, Color(0xFF8A2BE2).copy(alpha = 0.6f)), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cyclone,
                            contentDescription = "Merge Grid",
                            tint = Color(0xFFA855F7)
                        )
                    }
                }
            }

            // Quick Stats Board
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val activeCount = shadowList.count { it.isActive && it.level > 0 }
                
                // Active slot card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoloCardBg)
                        .border(BorderStroke(1.dp, Color(0xFF8A2BE2).copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF8A2BE2).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(text = "رصيد الرفاق المفعلين", color = SoloMutedText, fontSize = 10.sp)
                            Text(text = "$activeCount / 5 ظلال نشطة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Balance stats card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoloCardBg)
                        .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF22D3EE).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EnergySavingsLeaf,
                                contentDescription = null,
                                tint = SoloPrimaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(text = "الطاقة المتاحة للوعاء", color = SoloMutedText, fontSize = 10.sp)
                            Text(text = "${stats.apPoints} AP", color = SoloPrimaryCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Visible list
            val visibleShadows = shadowList.filter { !it.isMerged || it.id.startsWith("merged_") }
            
            if (visibleShadows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد بواعث روحية مسجلة حالياً.",
                        color = SoloMutedText,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
                ) {
                    items(visibleShadows) { shadow ->
                        ShadowCompanionCard(
                            shadow = shadow,
                            stats = stats,
                            onActivate = { viewModel.activateShadow(shadow.id) },
                            onDeactivate = { viewModel.deactivateShadow(shadow.id) },
                            onUpgrade = { viewModel.upgradeShadow(shadow.id) },
                            onFeed = { useGold -> viewModel.increaseShadowLoyalty(shadow.id, useGold) },
                            onSacrifice = { viewModel.sacrificeShadow(shadow.id) }
                        )
                    }
                }
            }
        }

        // Bottom Merging Trigger Area Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { showMergeDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A2BE2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(BorderStroke(1.5.dp, Color(0xFFD8B4FE)), RoundedCornerShape(16.dp))
                    .testTag("open_merge_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "طقس دمج وتوليد ظلال هجينة جديدة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Overlay Unlock Dialog Celebration
        if (newlyUnlockedShadow != null) {
            ShadowUnlockScreen(
                shadow = newlyUnlockedShadow!!,
                onDismiss = { viewModel.dismissShadowUnlock() },
                onActivate = { 
                    viewModel.activateShadow(newlyUnlockedShadow!!.id)
                    viewModel.dismissShadowUnlock()
                }
            )
        }

        // Overlay Merge Dialog Panel
        if (showMergeDialog) {
            ShadowMergeScreen(
                viewModel = viewModel,
                unmergedShadows = shadowList.filter { it.level > 0 && !it.isMerged },
                onDismiss = { showMergeDialog = false }
            )
        }
    }
}

@Composable
fun ShadowCompanionCard(
    shadow: ShadowSoldier,
    stats: UserStats,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onUpgrade: () -> Unit,
    onFeed: (Boolean) -> Unit,
    onSacrifice: () -> Unit
) {
    val isLocked = shadow.level == 0
    val opacity = if (isLocked) 0.6f else 1.0f

    // Color definitions
    val accentColor = Color(0xFF8A2BE2) // Purple accent
    val progressColor = Color(0xFFA855F7) // Loyalty violet color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SoloCardBg.copy(alpha = opacity))
            .border(
                BorderStroke(
                    width = if (shadow.isActive) 1.5.dp else 1.dp,
                    color = if (shadow.isActive) accentColor else SoloBorderSlate
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Silhouette badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isLocked) Color(0xFF1E293B) else accentColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .border(
                                BorderStroke(1.dp, if (isLocked) SoloBorderSlate else accentColor.copy(alpha = 0.5f)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLocked) "؟" else shadow.name.take(2).uppercase(),
                            color = if (isLocked) SoloMutedText else accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(
                            text = if (isLocked) "؟؟؟ (جند غامض)" else shadow.arabicName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isLocked) SoloMutedText else accentColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = if (isLocked) "مغلق" else "${shadow.rankArabic} (مستوى ${shadow.level})",
                                color = if (isLocked) SoloMutedText else accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Header status badge
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "مغلق", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (shadow.isSacrificed) {
                    Box(
                        modifier = Modifier
                            .background(SoloAccentRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, SoloAccentRed.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "فداء نشط مضحى", color = SoloAccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (shadow.isActive) {
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, accentColor), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "نشط قتالياً", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "في السبات", color = SoloMutedText, fontSize = 9.sp)
                    }
                }
            }

            // Passive or lock description
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "ميثاق وشروط فك القفل:", color = SoloMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = shadow.description, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Ability box + passive stats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "القدرة الفريدة الناشطة:", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = shadow.arabicDescription, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }

                    // Passive attributes booster register
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تأثير الوعاء الباعث:",
                            color = SoloMutedText,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${shadow.passiveStat.uppercase()} +${shadow.passiveAmount}",
                            color = SoloPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Loyalty panel slider
                    if (!shadow.isMerged) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "رابطة ولاء الكيان والعهد",
                                    color = SoloMutedText,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${shadow.loyalty}%",
                                    color = progressColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            LinearProgressIndicator(
                                progress = { shadow.loyalty.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = progressColor,
                                trackColor = Color(0xFF1E1430)
                            )
                        }
                    }
                }
            }

            // Buttons Actions Bar
            if (!isLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (shadow.isSacrificed) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "مضحى لدفع العذاب", color = SoloMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Activate toggle button
                        Button(
                            onClick = {
                                if (shadow.isActive) onDeactivate() else onActivate()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (shadow.isActive) Color.DarkGray else Color(0xFF22D3EE),
                                contentColor = if (shadow.isActive) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (shadow.isActive) "إلغاء التفعيل" else "استدعاء نشط",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Feed Joint button
                        var showFeedMenu by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { showFeedMenu = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E1430),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = progressColor, modifier = Modifier.size(12.dp))
                                    Text(text = "تحسين الرابطة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            DropdownMenu(
                                expanded = showFeedMenu,
                                onDismissRequest = { showFeedMenu = false },
                                modifier = Modifier.background(SoloCardBg).border(BorderStroke(1.dp, SoloBorderSlate))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🍖 تفطير معارك (300 ذهبة)", color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        showFeedMenu = false
                                        onFeed(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("⚔️ تدريب متزامن (50 AP)", color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        showFeedMenu = false
                                        onFeed(false)
                                    }
                                )
                            }
                        }

                        if (!shadow.isMerged) {
                            // Upgrade button
                            val isMaxLevel = shadow.level >= 3
                            Button(
                                onClick = onUpgrade,
                                enabled = !isMaxLevel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8A2BE2),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.DarkGray
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(38.dp)
                            ) {
                                Text(
                                    text = if (isMaxLevel) "رتبة كاملة" else "ارتقاء سحري",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Sacrifice button
                            if (shadow.loyalty >= 100) {
                                IconButton(
                                    onClick = onSacrifice,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(SoloAccentRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, SoloAccentRed), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OfflineBolt,
                                        contentDescription = "Sacrifice",
                                        tint = SoloAccentRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShadowMergeScreen(
    viewModel: VesselViewModel,
    unmergedShadows: List<ShadowSoldier>,
    onDismiss: () -> Unit
) {
    var s1 by remember { mutableStateOf<ShadowSoldier?>(null) }
    var s2 by remember { mutableStateOf<ShadowSoldier?>(null) }
    var s1MenuOpen by remember { mutableStateOf(false) }
    var s2MenuOpen by remember { mutableStateOf(false) }

    var isMergingAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { if (!isMergingAnimating) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isMergingAnimating,
            dismissOnClickOutside = !isMergingAnimating,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.5.dp, Color(0xFF8A2BE2)), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "مذبح دمج الأرواح الوراثي", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, enabled = !isMergingAnimating) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                // Dropdowns selections
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // First Dropdown selection box
                    Text(text = "اختر الكيان الظلي الأول:", color = SoloMutedText, fontSize = 11.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(12.dp))
                            .clickable { s1MenuOpen = true }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = s1?.arabicName ?: "--- انقر لاختيار الكيان الأول ---",
                            color = if (s1 != null) Color.White else SoloMutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        DropdownMenu(
                            expanded = s1MenuOpen,
                            onDismissRequest = { s1MenuOpen = false },
                            modifier = Modifier.background(SoloCardBg)
                        ) {
                            unmergedShadows.filter { it.id != s2?.id }.forEach { sh ->
                                DropdownMenuItem(
                                    text = { Text(sh.arabicName, color = Color.White) },
                                    onClick = {
                                        s1 = sh
                                        s1MenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Second Dropdown selection box
                    Text(text = "اختر الكيان الظلي المقابل للدمج:", color = SoloMutedText, fontSize = 11.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(12.dp))
                            .clickable { s2MenuOpen = true }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = s2?.arabicName ?: "--- انقر لاختيار الكيان الثاني ---",
                            color = if (s2 != null) Color.White else SoloMutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        DropdownMenu(
                            expanded = s2MenuOpen,
                            onDismissRequest = { s2MenuOpen = false },
                            modifier = Modifier.background(SoloCardBg)
                        ) {
                            unmergedShadows.filter { it.id != s1?.id }.forEach { sh ->
                                DropdownMenuItem(
                                    text = { Text(sh.arabicName, color = Color.White) },
                                    onClick = {
                                        s2 = sh
                                        s2MenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Dynamic preview combo results box
                if (s1 != null && s2 != null) {
                    val pair = setOf(s1!!.id, s2!!.id)
                    val resultLabel = when {
                        pair == setOf("iron", "igris") -> "الجنرال العظيم"
                        pair == setOf("tank", "fang") -> "الحارس السريع"
                        pair == setOf("beru", "bellion") -> "الملك القاتل"
                        pair == setOf("jima", "igris") -> "العراف الأكبر"
                        pair == setOf("iron", "tank") -> "الحصن المطلق"
                        pair == setOf("fang", "beru") -> "الصياد الخاطف"
                        else -> null
                    }
                    val resultDescAr = when {
                        pair == setOf("iron", "igris") -> "تخطيط استراتيجي مدموج بقوة عضلية مضاعفة، يمنحك ميزة هجومية 25%+."
                        pair == setOf("tank", "fang") -> "الحصن المسرع: تفادي كامل لضرر الفشل بنسبة تصاعدية ومضاعفة احتساب مسافات الجري 2x."
                        pair == setOf("beru", "bellion") -> "الملك القاتل: فرصة ضربة قاضية تزيد لـ 20% لتخطي الحراس مع إمكانية استبدال مهامك يومياً مجاناً."
                        pair == setOf("jima", "igris") -> "العراف الأعظم للوعاء: بصرية كاشفة لكافة نقاط ضعف حارس بواباتك ونسب النجاح فوراً."
                        pair == setOf("iron", "tank") -> "الحصن اللانهائي: درع صامد مدى الحياة يصد أي عقوبة أو تراجع 3 مرات متتالية دون كسر."
                        pair == setOf("fang", "beru") -> "الصياد البري الكاسر: زيادة استثنائية لخصائص الرشاقة +25 وحساب تمارين الكارديو بنسب مضاعفة."
                        else -> null
                    }

                    if (resultLabel != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1430))
                                .border(BorderStroke(1.dp, Color(0xFFA855F7)), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "نتيجة الدمج المتوقعة:",
                                    color = Color(0xFFA855F7),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = resultLabel,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = resultDescAr ?: "",
                                    color = SoloMutedText,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoloAccentRed.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚠️ الخيار المختار ليس لديه صيغة هجينة في ميثاق الاندماجات الحالي.",
                                color = SoloAccentRed,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Merge Button
                Button(
                    onClick = {
                        if (s1 != null && s2 != null) {
                            isMergingAnimating = true
                            scope.launch {
                                delay(2200) // Simulated cosmic transformation delay
                                viewModel.mergeShadows(s1!!.id, s2!!.id)
                                isMergingAnimating = false
                                onDismiss()
                            }
                        }
                    },
                    enabled = s1 != null && s2 != null && !isMergingAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A2BE2),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isMergingAnimating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "تنفيذ طقس الدمج الكوني (يتطلب 500 AP)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShadowUnlockScreen(
    shadow: ShadowSoldier,
    onActivate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(24.dp)
                .testTag("shadow_unlock_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF0F081E))
                    .border(BorderStroke(1.5.dp, Color(0xFF8A2BE2)), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Silhouette glow badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A2BE2).copy(alpha = 0.15f))
                            .border(BorderStroke(1.5.dp, Color(0xFF8A2BE2)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "قد استجاب جند ظلال جديد لميثاق الرنين الكهرومغناطيسي!",
                            color = SoloPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تم استنهاض الظل بنجاح",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Display shadow identity card representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, Color(0xFF8A2BE2).copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = shadow.arabicName,
                                color = Color(0xFFA855F7),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = shadow.name.uppercase(),
                                color = SoloMutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Details capability view
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "القدرة الفريدة والميزة الممنوحة:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = shadow.arabicDescription,
                            color = SoloMutedText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Decision actions buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onActivate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8A2BE2),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "تفعيل الكيان واستدعائه فوراً للميدان",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, SoloBorderSlate),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "موافق (إضافة للسبات)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
