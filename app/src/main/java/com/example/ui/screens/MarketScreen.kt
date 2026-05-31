package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Item
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MarketScreen(viewModel: VesselViewModel, stats: UserStats) {
    var activeCategoryTab by remember { mutableStateOf("AP") }
    val itemsList by viewModel.items.collectAsState()

    // Closing timer on market tab
    var marketClosingTimer by remember { mutableStateOf("52M 42S") }
    LaunchedEffect(Unit) {
        var totalSec = 52 * 60 + 42
        while (totalSec > 0) {
            delay(1000)
            totalSec -= 1
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            marketClosingTimer = String.format("%02dM %02dS", minutes, seconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
            .padding(horizontal = 16.dp)
            .testTag("market_screen_container")
    ) {
        // Balances Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Gold Balance",
                    tint = SoloGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "GOLD ${stats.gold}",
                    color = SoloGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // AP balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "AP ${stats.apPoints}",
                    color = SoloPrimaryCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "AP points",
                    tint = SoloPrimaryCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // SHOP INTELLIGENCE BRIEFING Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SoloCardBg)
                        .border(
                            BorderStroke(1.dp, SoloBorderSlate),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cyan vertical accent border
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(115.dp)
                                .background(SoloActiveBlue)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHOP INTELLIGENCE\nBRIEFING",
                                    color = SoloPrimaryCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    lineHeight = 15.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = SoloPrimaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "استخدم المتجر لتعزيز بقائك. نقاط الـ AP للحماية من العقوبات، والذهب لتعزيز مواردك من المغارات. تذكر: القدر يمكن التلاعب به بذكاء.",
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.End,
                                lineHeight = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Tab selects: AP, GOLD, MARKET, FATE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoloCardBg)
                        .border(
                            BorderStroke(1.dp, SoloBorderSlate),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    val catTabs = listOf("AP", "GOLD", "MARKET", "FATE")
                    catTabs.forEach { tab ->
                        val isCurrent = activeCategoryTab == tab
                        val highlightColor = when (tab) {
                            "AP" -> SoloPrimaryCyan
                            "GOLD" -> SoloGold
                            "MARKET" -> SoloAccentRed
                            else -> Color(0xFFA55EFF) // purple
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeCategoryTab = tab }
                                .background(if (isCurrent) highlightColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isCurrent) highlightColor else Color.Transparent
                                    ),
                                    RoundedCornerShape(0.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isCurrent) highlightColor else SoloMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // MARKET TAB EXCLUSIVE: Closing Countdown notification banner
            if (activeCategoryTab == "MARKET") {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoloAccentRed.copy(alpha = 0.05f))
                            .border(BorderStroke(1.dp, SoloAccentRed), RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SoloAccentRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "MARKET CLOSING IN: $marketClosingTimer",
                                color = SoloAccentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // FATE TAB EXCLUSIVE: header banner
            if (activeCategoryTab == "FATE") {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoloCardBg)
                            .border(BorderStroke(1.dp, Color(0xFFA55EFF)), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = Color(0xFFA55EFF)
                                )
                                Text(
                                    text = "FATE MARKET",
                                    color = Color(0xFFA55EFF),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CONTROL THE THREAD OF YOUR DESTINY.",
                                color = SoloMutedText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "☠️ CURSE ANTIDOTES (AP)",
                        color = SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Filter items by selected catalog
            val catalogItems = itemsList.filter { it.category == activeCategoryTab }

            items(catalogItems) { item ->
                MarketItemCard(
                    item = item,
                    stats = stats,
                    onBuy = { viewModel.syncItem(item.id) },
                    onUse = { viewModel.useItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun MarketItemCard(
    item: Item,
    stats: UserStats,
    onBuy: () -> Unit,
    onUse: () -> Unit
) {
    val themeColor = when (item.category) {
        "AP" -> SoloPrimaryCyan
        "GOLD" -> SoloGold
        "MARKET" -> SoloAccentRed
        else -> Color(0xFFA55EFF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SoloCardBg)
            .border(BorderStroke(1.dp, themeColor), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item specific illustration/emoji avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColor.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when (item.usageType) {
                            "RECOVERY" -> Icons.Default.MedicalServices
                            "SHIELD" -> Icons.Default.Shield
                            "SWORD" -> Icons.Default.Shield // or dagger
                            "MAP" -> Icons.Default.Map
                            "PRESSURE" -> Icons.Default.Spa
                            else -> Icons.Default.Vaccines
                        }

                        // Customize or display emojis representing images
                        val emoji = when (item.usageType) {
                            "RECOVERY" -> "💊"
                            "SHIELD" -> "🛡️"
                            "SWORD" -> "⚔️"
                            "MAP" -> "🗺️"
                            "PRESSURE" -> "🧪"
                            else -> "🧪"
                        }
                        
                        Text(text = emoji, fontSize = 28.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.arabicName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (item.durationHours > 0) "ساعة ${item.durationHours}" else "استخدام واحد",
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                            if (item.count > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SoloNeonGreen, CircleShape)
                                )
                                Text(
                                    text = "Owned: ${item.count}",
                                    color = SoloNeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Description localized wording
            Text(
                text = item.arabicDescription,
                color = SoloMutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.id == "potion_fake") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SoloAccentRed.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, SoloAccentRed.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SoloAccentRed,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "CURSE: +5 مهام الغد",
                            color = SoloAccentRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sync buying cost
            Text(
                text = "COST: ${item.cost} ${item.costType}",
                color = themeColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buy button
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = "SYNC ITEM",
                        color = if (themeColor == SoloPrimaryCyan) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Use button (if owned)
                if (item.count > 0) {
                    Button(
                        onClick = onUse,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, SoloNeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "ACTIVATE LINK",
                            color = SoloNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
