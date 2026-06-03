package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyJournalEntry
import com.example.ui.VesselViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: VesselViewModel) {
    val entries by viewModel.journalEntries.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A)) // Dark base
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "دفتر الصياد",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF161616),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFFFFD700)
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("اليومي", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("الأسبوعي", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            DailyJournalTab(entries = entries)
        } else {
            WeeklyJournalTab(entries = entries, onDayClick = { selectedTab = 0 })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyJournalTab(entries: List<DailyJournalEntry>) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    var selectedEntryId by remember(entries) {
        mutableStateOf(entries.firstOrNull()?.id ?: todayStr)
    }

    var expanded by remember { mutableStateOf(false) }

    val currentEntry = entries.find { it.id == selectedEntryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding)
    ) {
        // Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            val label = if (selectedEntryId == todayStr) "اليوم ($selectedEntryId)" else selectedEntryId
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF161616))
            ) {
                entries.forEach { entry ->
                    val text = if (entry.id == todayStr) "اليوم (${entry.id})" else entry.id
                    DropdownMenuItem(
                        text = { Text(text, color = Color.White) },
                        onClick = {
                            selectedEntryId = entry.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (currentEntry == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد بيانات لهذا اليوم.", color = Color.Gray)
            }
        } else {
            // Journal Card
            val color = getMoodColor(currentEntry.mood)
            val bgColor = color.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 ${currentEntry.id}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    JournalRow(label = "المهام:", value = "${currentEntry.missionsCompleted}/${currentEntry.totalMissions}")
                    JournalRow(label = "الزنزانات:", value = "دخلت ${currentEntry.gatesEntered}، هزمت ${currentEntry.gatesCompleted}")
                    
                    val guards = currentEntry.guardsDefeated.ifEmpty { "لا يوجد" }
                    JournalRow(label = "الحراس المهزومون:", value = guards)
                    
                    JournalRow(label = "مهام جانبية:", value = "${currentEntry.sideQuestsCompleted}")
                    
                    val fate = currentEntry.fateCardDrawn.ifEmpty { "لم تُسحب" }
                    JournalRow(label = "بطاقة القدر:", value = fate)
                    
                    JournalRow(label = "المكاسب:", value = "XP: +${currentEntry.xpEarned} | AP: +${currentEntry.apEarned} | ذهب: +${currentEntry.goldEarned}")
                    
                    val dawnStatus = if (currentEntry.dawnMissionCompleted) "✅ اكتملت" else "❌ لم تكتمل"
                    JournalRow(label = "مهمة الفجر:", value = dawnStatus)

                    if (currentEntry.wasPunished) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "العقوبة: سجن النظام",
                            color = Color(0xFFFF0044),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        JournalRow(label = "العقوبة:", value = "لا يوجد")
                    }
                }
            }
        }
    }
}

@Composable
fun JournalRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = Color(0xFFAAAAAA), fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
        Text(text = value, color = Color.White, modifier = Modifier.weight(1f))
    }
}

@Composable
fun WeeklyJournalTab(entries: List<DailyJournalEntry>, onDayClick: () -> Unit) {
    val weekEntries = entries.take(7)
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding)
    ) {
        Text("آخر 7 أيام", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (weekEntries.isEmpty()) {
            Text("لا تتوفر بيانات كافية.", color = Color.Gray)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weekEntries) { entry ->
                    val color = getMoodColor(entry.mood)
                    val isToday = entry.id == todayStr
                    val borderModifier = if (isToday) Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)) 
                                         else Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f))
                            .then(borderModifier)
                            .clickable { onDayClick() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateObj = sdf.parse(entry.id)
                            val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(dateObj ?: Date())
                            
                            Text(text = dayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = entry.id.substring(5), color = Color(0xFFAAAAAA), fontSize = 12.sp) // MM-dd
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "${entry.missionsCompleted}/${entry.totalMissions}", color = color, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Compare Last Week
            val currentWeekCompletion = getWeeklyCompletionRate(weekEntries)
            val lastWeekEntries = entries.drop(7).take(7)
            
            Text("المقارنة بالأسبوع الماضي", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                if (lastWeekEntries.isEmpty()) {
                    Text("لا توجد بيانات كافية للمقارنة.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                } else {
                    val lastWeekCompletion = getWeeklyCompletionRate(lastWeekEntries)
                    val diff = currentWeekCompletion - lastWeekCompletion
                    
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("هذا الأسبوع:", color = Color.White)
                            Text("${currentWeekCompletion.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الأسبوع الماضي:", color = Color.White)
                            Text("${lastWeekCompletion.toInt()}%", color = Color(0xFFAAAAAA), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val diffColor = if (diff >= 0) Color(0xFF33CC33) else Color(0xFFFF0044)
                        val diffSign = if (diff >= 0) "+" else ""
                        Text(
                            text = "📈 تحسن بنسبة $diffSign${diff.toInt()}%",
                            color = diffColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun getWeeklyCompletionRate(entries: List<DailyJournalEntry>): Double {
    if (entries.isEmpty()) return 0.0
    var completed = 0
    var total = 0
    for (entry in entries) {
        completed += entry.missionsCompleted
        total += entry.totalMissions
    }
    if (total == 0) return 0.0
    return (completed.toDouble() / total) * 100
}

fun getMoodColor(mood: String): Color {
    return when (mood) {
        "green" -> Color(0xFF33CC33)
        "yellow" -> Color(0xFFFFD700)
        "orange" -> Color(0xFFFF8C00)
        "red" -> Color(0xFFFF0044)
        else -> Color(0xFFFF8C00)
    }
}

private val screenPadding = 24.dp
