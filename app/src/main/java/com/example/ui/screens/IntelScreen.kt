package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLog
import com.example.data.UserStats
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IntelScreen(viewModel: VesselViewModel, stats: UserStats) {
    val tasks by viewModel.exercises.collectAsState()
    val logsList by viewModel.logs.collectAsState()

    // Calculate actual active progress stats for the review board
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val completionPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat() * 100).toInt() else 0
    
    // Status text based on completion
    val currentWeeklyStatus = when {
        completionPercent >= 100 -> "MONARCH"
        completionPercent >= 70 -> "SHADOW COMMANDER"
        completionPercent >= 40 -> "S-RANK HUNTER"
        completionPercent >= 10 -> "WARRIOR"
        else -> "WARRIOR"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
            .padding(horizontal = 16.dp)
            .testTag("intel_screen_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // SHADOW MANA BANK Card (Purple themed)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderPurple), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "SHADOW MANA BANK",
                                color = Color(0xFFA55EFF), // purple
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "STORED MANA",
                                color = SoloMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${stats.manaStored}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Progress track of Mana
                    val manaProgress by animateFloatAsState(
                        targetValue = (stats.manaStored.toFloat() / stats.manaGoal.toFloat()).coerceIn(0f, 1f),
                        label = "mana_bar"
                    )
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(SoloBorderSlate)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(manaProgress)
                                    .fillMaxHeight()
                                    .background(Color(0xFFA55EFF))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FOR AVERT PENALTY ${stats.manaGoal}",
                                color = SoloMutedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(manaProgress * 100).toInt()}%",
                                color = Color(0xFFA55EFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SYSTEM EVALUATION
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SYSTEM EVALUATION",
                        color = SoloMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = SoloPrimaryCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // INITIATE SYSTEM AUDIT Button (styled cyan pulsing border)
                Button(
                    onClick = { viewModel.initiateSystemAudit() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, SoloPrimaryCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "INITIATE SYSTEM AUDIT",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = SoloPrimaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // WEEKLY REVIEW Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "WEEKLY REVIEW",
                    color = SoloMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = SoloPrimaryCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Completion Rate Row
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(BorderStroke(1.dp, SoloBorderSlate), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMPLETION RATE",
                            color = SoloMutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completionPercent%",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "WEEKLY STATUS",
                            color = SoloMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentWeeklyStatus,
                            color = SoloPrimaryCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 2x2 Grid of Weekly Attributes metrics (AP, XP, Dungeons, Exercises)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dungeons card
                    MetricCard(
                        title = "DUNGEONS",
                        value = if (stats.level >= 3) 2 else 0,
                        icon = Icons.Default.Cyclone,
                        iconColor = SoloPrimaryCyan
                    )

                    // Exercises Card
                    MetricCard(
                        title = "EXERCISES",
                        value = completedCount,
                        icon = Icons.Default.Adjust,
                        iconColor = SoloPrimaryCyan
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // AP Gained
                    MetricCard(
                        title = "AP GAINED",
                        value = stats.apPoints - 100, // starting point is 100
                        icon = Icons.Default.FlashOn,
                        iconColor = SoloActiveBlue
                    )

                    // XP Earned
                    MetricCard(
                        title = "XP EARNED",
                        value = stats.xp,
                        icon = Icons.Default.Star,
                        iconColor = SoloGold
                    )
                }
            }
        }

        // LIFETIME ACHIEVEMENTS Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "LIFETIME ACHIEVEMENTS",
                    color = SoloMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = SoloGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Gates cleared / Missions Row cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "GATES CLEARED",
                    value = if (stats.level >= 5) 4 else if (stats.level >= 2) 1 else 0,
                    icon = Icons.Default.DoubleArrow,
                    iconColor = SoloGold,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "MISSIONS",
                    value = completedCount / 3, // mock count
                    icon = Icons.Default.DoneAll,
                    iconColor = SoloNeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // RECENT ACTIVITY LOG Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "RECENT ACTIVITY LOG",
                    color = SoloMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = SoloPrimaryCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Historical Log text content list
        if (logsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoloCardBg)
                        .border(BorderStroke(1.dp, SoloBorderCyan), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO LOGS RECORDED YET",
                        color = SoloMutedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            items(logsList) { log ->
                LogItemRow(log)
            }

            // Easy clear logs action
            item {
                TextButton(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CLEAR ALL SYSTEM DIALECTIC RECORDS",
                        color = SoloAccentRed.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SoloCardBg),
        border = BorderStroke(1.dp, SoloBorderSlate),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = SoloMutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "${value.coerceAtLeast(0)}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun LogItemRow(log: ActivityLog) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timestampFormatted = sdf.format(Date(log.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SoloCardBg)
            .border(BorderStroke(1.dp, SoloBorderSlate.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = log.message,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = timestampFormatted,
                color = SoloPrimaryCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
