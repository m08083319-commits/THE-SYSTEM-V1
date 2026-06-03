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
    val itemsList by viewModel.items.collectAsState(initial = emptyList())
    val blackMarketItems by viewModel.blackMarketItems.collectAsState(initial = emptyList())
    val blackMarketExpiry by viewModel.blackMarketExpiry.collectAsState(initial = null)

    // Closing timer on market tab
    var marketClosingTimer by remember { mutableStateOf("02:00:00") }
    LaunchedEffect(blackMarketExpiry) {
        while (true) {
            val expiry = blackMarketExpiry
            if (expiry != null) {
                val rem = expiry - System.currentTimeMillis()
                if (rem > 0) {
                    val hours = rem / 3600000
                    val minutes = (rem % 3600000) / 60000
                    val seconds = (rem % 60000) / 1000
                    marketClosingTimer = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    marketClosingTimer = "00:00:00"
                }
            } else {
                marketClosingTimer = "02:00:00"
            }
            delay(1000)
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Abyss Shards balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Abyss Shards",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "SHARDS ${stats.abyssShards}",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp,
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
                    fontSize = 12.sp,
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
            contentPadding = PaddingValues(bottom = 90.dp)
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
                                .height(125.dp)
                                .background(
                                    when (activeCategoryTab) {
                                        "AP" -> SoloPrimaryCyan
                                        "GOLD" -> SoloGold
                                        "MARKET" -> SoloAccentRed
                                        else -> Color(0xFF9E1010)
                                    }
                                )
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
                                    text = "SHOP PROTOCOL: " + when (activeCategoryTab) {
                                        "AP" -> "AP OUTPOST"
                                        "GOLD" -> "GOLD MERCHANTS"
                                        "MARKET" -> "BLACK MARKET"
                                        else -> "ABYSS VOID"
                                    },
                                    color = when (activeCategoryTab) {
                                        "AP" -> SoloPrimaryCyan
                                        "GOLD" -> SoloGold
                                        "MARKET" -> SoloAccentRed
                                        else -> Color(0xFFE91E63)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    lineHeight = 15.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = SoloMutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = when (activeCategoryTab) {
                                    "AP" -> "استخدم نقاط المجهود (AP) لتملك دروع ولفائف الحماية لدحض كسر سلسلة البقاء وتجنب عقوبات النظام."
                                    "GOLD" -> "السبائك الذهبية المكتسبة من المغارات والبوابات تسمح بشراء الأسلحة والتطويرات لزيادة عوائد المغامرات."
                                    "MARKET" -> "السوق الأسود السري يعرض لفائف غامضة منزوعة الهوية. تملكها يعطي تجاوزات هائلة لكنها محفوفة بلعنات خبيثة."
                                    else -> "الهاوية السحيقة: هنا فقط تُستثمر شظايا العدم (Shards) المطلقة لتملك القدرات الأبدية والألقاب الملكية النادرة."
                                },
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Right,
                                lineHeight = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Tab selects: AP, GOLD, MARKET, ABYSS
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
                    val catTabs = listOf("AP", "GOLD", "MARKET", "ABYSS")
                    catTabs.forEach { tab ->
                        val isCurrent = activeCategoryTab == tab
                        val highlightColor = when (tab) {
                            "AP" -> SoloPrimaryCyan
                            "GOLD" -> SoloGold
                            "MARKET" -> SoloAccentRed
                            else -> Color(0xFFE91E63)
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

            // MARKET TAB EXCLUSIVE: Closing Countdown + Manual Refresh Button
            if (activeCategoryTab == "MARKET") {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoloAccentRed.copy(alpha = 0.05f))
                                .border(BorderStroke(1.dp, SoloAccentRed), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
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
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = SoloAccentRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "تحديث صفقات السوق الأسود: $marketClosingTimer",
                                        color = SoloAccentRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.refreshBlackMarket() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoloAccentRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحديث", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Filter items based on selected tab representation
            val catalogItems = if (activeCategoryTab == "MARKET") {
                blackMarketItems
            } else {
                itemsList.filter { it.category == activeCategoryTab }
            }

            if (catalogItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد بضاعة معروضة حالياً في هذا القسم.",
                            color = SoloMutedText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(catalogItems, key = { it.id }) { item ->
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
        else -> Color(0xFFE91E63) // Abyss pink/crimson
    }

    // Dynamic timer ticker to calculate remaining cooldown on item card
    var remainingCooldownText by remember { mutableStateOf("") }
    LaunchedEffect(item.lastPurchaseTimestamp) {
        while (true) {
            if (item.cooldownHours > 0 && item.lastPurchaseTimestamp > 0L) {
                val cooldownExpiry = item.lastPurchaseTimestamp + (item.cooldownHours * 3600 * 1000L)
                val remaining = cooldownExpiry - System.currentTimeMillis()
                if (remaining > 0) {
                    val diffSecs = remaining / 1000
                    val hours = diffSecs / 3600
                    val mins = (diffSecs % 3600) / 60
                    val secs = diffSecs % 60
                    remainingCooldownText = String.format("%02d:%02d:%02d", hours, mins, secs)
                } else {
                    remainingCooldownText = ""
                }
            } else {
                remainingCooldownText = ""
            }
            delay(1000)
        }
    }

    // Evaluate rank lock requirement state
    val isRankLocked = remember(item.requiredRank, stats.rank) {
        val ranks = listOf("E", "D", "C", "B", "A", "S", "SS", "SSS")
        val reqIdx = ranks.indexOf(item.requiredRank ?: "E").coerceAtLeast(0)
        val statsIdx = ranks.indexOf(stats.rank).coerceAtLeast(0)
        statsIdx < reqIdx
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (item.category == "ABYSS") Color(0xFF140808) else SoloCardBg)
            .border(
                BorderStroke(
                    1.dp,
                    if (isRankLocked) SoloBorderSlate else themeColor
                ),
                RoundedCornerShape(24.dp)
            )
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
                    // Item avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColor.copy(alpha = 0.1f))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    themeColor.copy(alpha = 0.3f)
                                ), RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val emoji = when (item.id) {
                            "AP_01" -> "🛡️"
                            "AP_02" -> "🧪"
                            "AP_03" -> "⚡"
                            "AP_04" -> "🎗️"
                            "AP_05" -> "⏳"
                            "AP_06" -> "❤️"
                            "AP_07" -> "🔑"
                            "AP_08" -> "🔋"
                            "GOLD_01" -> "⚔️"
                            "GOLD_02" -> "🗺️"
                            "GOLD_03" -> "📦"
                            "GOLD_04" -> "🌅"
                            "GOLD_05" -> "🧲"
                            "GOLD_06" -> "🎭"
                            "GOLD_07" -> "💎"
                            "GOLD_08" -> "🧪"
                            "MARKET_01" -> "☠️"
                            "MARKET_02" -> "🗝️"
                            "MARKET_03" -> "🧬"
                            "MARKET_04" -> "🛡️"
                            "MARKET_05" -> "🔮"
                            "MARKET_06" -> "🖤"
                            "MARKET_07" -> "📜"
                            "MARKET_08" -> "🧭"
                            "ABYSS_01" -> "👹"
                            "ABYSS_02" -> "🩸"
                            "ABYSS_03" -> "👑"
                            "ABYSS_04" -> "👥"
                            "ABYSS_05" -> "🌌"
                            "ABYSS_06" -> "🔮"
                            "ABYSS_07" -> "🚪"
                            "ABYSS_08" -> "👁️"
                            else -> "📦"
                        }
                        Text(text = emoji, fontSize = 28.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.arabicName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (item.durationHours > 0) "الدوام: ${item.durationHours} ساعة" else "استعمال فوري",
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
                                    text = "ممتلك: ${item.count}",
                                    color = SoloNeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Description text (Right-to-Left alignment for Arabic)
            Text(
                text = item.arabicDescription,
                color = SoloMutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 18.sp
            )

            // Constraints row: Required Rank & Stock limits
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Lock Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isRankLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isRankLocked) SoloAccentRed else SoloNeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "الرتبة المطلوبة: [${item.requiredRank}]",
                        color = if (isRankLocked) SoloAccentRed else SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cooldown Warning displayed dynamically if active
                if (remainingCooldownText.isNotEmpty()) {
                    Text(
                        text = "تبريد: $remainingCooldownText",
                        color = SoloAccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (item.stock != -1) {
                    Text(
                        text = "المتاح: ${item.count}/${item.stock}",
                        color = SoloGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Sync buying cost
            Text(
                text = "السعر: ${item.cost} ${item.costType}",
                color = themeColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            text = "تفعيل الميزة",
                            color = SoloNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Buy button
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRankLocked) Color.Gray.copy(alpha = 0.2f) else themeColor
                    ),
                    enabled = !isRankLocked,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text(
                        text = if (isRankLocked) "مغلق الرتبة" else "شراء الرابط",
                        color = if (themeColor == SoloPrimaryCyan && !isRankLocked) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
