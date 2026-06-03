package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.data.PlayerTitle
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import com.example.ui.theme.*

@Composable
fun TitlesScreen(
    viewModel: VesselViewModel,
    stats: UserStats,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("ALL") }
    val unlockedTitlesSet = remember(stats.unlockedTitles) {
        stats.unlockedTitles.split(",").map { it.trim() }.toSet()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("titles_screen_container"),
        containerColor = SoloBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back circular button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoloBorderSlate.copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, SoloBorderSlate), CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Title
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "نظام الألقاب الاستراتيجية",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "قدرات كامنة مفعّلة تغير طريقة اللعب",
                            color = SoloPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Info banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloPrimaryCyan.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoloPrimaryCyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SoloPrimaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اللقب النشط حالياً: [${stats.activeTitle}]",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "قم بإلغاء قفل المزيد وتجهيز اللقب المناسب لأسلوب لعبك لكسب ميزته الكامنة الدائمة.",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Rarity Tabs / Category Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val rarityTabs = listOf(
                    "ALL" to "الكلّ",
                    "COMMON" to "شائع",
                    "UNCOMMON" to "غير شائع",
                    "RARE" to "نادر",
                    "LEGENDARY" to "أسطوري"
                )

                rarityTabs.forEach { (rarityKey, arabicLabel) ->
                    val isSel = selectedTab == rarityKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) SoloPrimaryCyan else SoloCardBg)
                            .border(
                                BorderStroke(1.dp, if (isSel) SoloPrimaryCyan else SoloBorderSlate),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = rarityKey }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = arabicLabel,
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scrollable list of titles
            val filteredTitles = remember(selectedTab) {
                if (selectedTab == "ALL") {
                    PlayerTitle.ALL_TITLES
                } else {
                    PlayerTitle.ALL_TITLES.filter { it.rarity == selectedTab }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTitles) { title ->
                    val isUnlocked = unlockedTitlesSet.contains(title.id)
                    val isActive = stats.activeTitleId == title.id

                    TitleCardItem(
                        title = title,
                        isUnlocked = isUnlocked,
                        isActive = isActive,
                        onEquip = { viewModel.equipTitle(title.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TitleCardItem(
    title: PlayerTitle,
    isUnlocked: Boolean,
    isActive: Boolean,
    onEquip: () -> Unit
) {
    val blockBgColor = if (isActive) Color(0xFF0F1E28) else SoloCardBg
    val borderStrokeColor = if (isActive) SoloPrimaryCyan else if (isUnlocked) Color(title.rarityColor) else SoloBorderSlate
    val blockOpacity = if (isUnlocked) 1f else 0.55f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(blockBgColor)
            .border(BorderStroke(1.2.dp, borderStrokeColor), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header: Title Name + English + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title.name,
                            color = if (isUnlocked) Color.White else Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(title.rarityColor).copy(alpha = 0.15f))
                                .border(BorderStroke(0.8.dp, Color(title.rarityColor)), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (title.rarity) {
                                    "LEGENDARY" -> "أسطوري"
                                    "RARE" -> "نادر"
                                    "UNCOMMON" -> "غير شائع"
                                    else -> "شائع"
                                },
                                color = Color(title.rarityColor),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = title.englishName.uppercase(),
                        color = SoloMutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloPrimaryCyan.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, SoloPrimaryCyan), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SoloPrimaryCyan, CircleShape)
                            )
                            Text(
                                text = "نشط ومجهّز",
                                color = SoloPrimaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = SoloMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Description / Passive Perk
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "التأثير الكامن الدائم:",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title.description,
                    color = if (isUnlocked) SoloMutedText else Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Start
                )
            }

            // Requirement Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "شرط إلغاء القفل: ${title.requirement}",
                        color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // Action: Equip Button
            if (isUnlocked && !isActive) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(title.rarityColor),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = "تجهيز اللقب الكامن",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
