package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PurityResult
import com.example.ui.VesselViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ExerciseVerificationScreen(
    viewModel: VesselViewModel,
    taskId: String,
    exerciseType: String,
    targetReps: Int,
    onDismiss: () -> Unit
) {
    // Keep internal values for live checking
    var score by remember { mutableStateOf(100) }
    var currentRep by remember { mutableStateOf(0) }
    var simulationActive by remember { mutableStateOf(true) }

    // Cheat options configuration sliders
    var isSimulatingCheating by remember { mutableStateOf(false) }
    var improperAngle by remember { mutableStateOf(false) }
    var deviceShaking by remember { mutableStateOf(false) }
    var readingGazeDiverted by remember { mutableStateOf(false) }
    
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_ratio"
    )

    // Calculate score based on cheating variables
    LaunchedEffect(isSimulatingCheating, improperAngle, deviceShaking, readingGazeDiverted, currentRep) {
        var baseScore = 100
        if (isSimulatingCheating) {
            baseScore -= 60
        }
        if (improperAngle) {
            baseScore -= 20
        }
        if (deviceShaking) {
            baseScore -= 15
        }
        if (readingGazeDiverted) {
            baseScore -= 25
        }
        score = baseScore.coerceIn(10, 100)
    }

    // Auto ticker for simulating exercises
    LaunchedEffect(simulationActive) {
        while (simulationActive && currentRep < targetReps) {
            delay(2500) // 2.5s per rep simulation
            currentRep++
        }
    }

    val purityColor = when {
        score >= 90 -> Color(0xFF33CC33) // green
        score >= 70 -> Color(0xFFFFD700) // yellow
        score >= 50 -> Color(0xFFFF8C00) // orange
        else -> Color(0xFFFF0044) // red
    }

    // List of localized conditions according to exercise profile rules
    val conditionsList = remember(exerciseType) {
        when (exerciseType.lowercase()) {
            "pushups" -> listOf("زاوية الكوع ≤ 90°", "استقامة عظام الجذع والظهر", "رصد تسارع نزول الصدر وتثبيته")
            "pullups" -> listOf("الذقن يتخطى عقلة الارتكاز", "فرد المرفق الكامل عند الانبساط", "استقرار الجسد الأفقي بدون تذبذب")
            "squats" -> listOf("نزول الورك أسفل خط الركبتين", "استقامة العمود الفقري", "مستشعرات القدم تؤكد بقاء الكعب ثابتاً")
            "running" -> listOf("الإحداثيات الجغرافية GPS متحركة", "سرعة الإرسال أقل من ٢٠ كم/ساعة", "الخطوات منتظمة ومتطابقة مع عداد الخطى")
            "meditation" -> listOf("الكاميرا تؤكد إغلاق العينين أو نظرة مستقرة", "رصد انخفاض كبير في معدل ضربات القلب", "استقرار كلي لمقاييس الاهتزاز")
            "reading" -> listOf("الكاميرا ترصد حركة العينين الترددية (Saccades)", "عدم الخروج عن إطار الصفحة", "معدل القراءة مستقر ومتصل")
            "shower" -> listOf("الميكروفون يرصد صوت تدفق المياه بصورة مستمرة", "تزامن رطوبة المستشعر أو حرارة البيئة", "بقاء الهاتف آمن وفي مدى محدد")
            else -> listOf("مطابقة تسارع الإيقاع الدوري", "التحقق من بصمة الوجه للتحقق من هوية الصياد", "تطابق نبضات الساعة الذكية")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF030508)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عين أغريس الذكية",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                
                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, purityColor, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "دائرة الحقيقة",
                        color = purityColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "تقوم العين الآن بمراقبة مستشعرات الوعاء الحيوية والفيزيائية لمنع الخداع ونمذجة نقاء التدريب.",
                color = Color(0xFF8B949E),
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Holographic Portal Container
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1A))
                    .border(2.dp, purityColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Spinning purity borders
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = purityColor.copy(alpha = 0.08f),
                        radius = size.minDimension / 2 * pulseRatio
                    )
                    drawCircle(
                        color = purityColor.copy(alpha = 0.3f),
                        radius = (size.minDimension / 2) - 8.dp.toPx(),
                        style = Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating skeletal joint node indicators to mimic camera/pose tracking
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (improperAngle) Color.Red else Color.Cyan)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (deviceShaking) Color.Red else Color.Green)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Cyan)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "$currentRep / $targetReps",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Text(
                        text = "التكرارات المسجلة",
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$score% النقاء",
                        color = purityColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sandbox Sensor Cheat Simulator Tool (Super rich interactive feature for AI Studio environments)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💻 محاكي مدخلات المستشعرات (للاختبار والتقييم والتحكم)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("غش صريح وتلاعب بالنمط (تجاوز الإنزياح) ", color = Color(0xFFC9D1D9), fontSize = 12.sp)
                        Switch(
                            checked = isSimulatingCheating,
                            onCheckedChange = { isSimulatingCheating = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Red,
                                checkedTrackColor = Color.Red.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("زوايا مفاصل غير مستوفية (مثلاً الكوع/الورك)", color = Color(0xFFC9D1D9), fontSize = 12.sp)
                        Checkbox(
                            checked = improperAngle,
                            onCheckedChange = { improperAngle = it ?: false }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("رصد اهتزاز عشوائي بالجهاز (عدم ثبات)", color = Color(0xFFC9D1D9), fontSize = 12.sp)
                        Checkbox(
                            checked = deviceShaking,
                            onCheckedChange = { deviceShaking = it ?: false }
                        )
                    }

                    if (exerciseType.lowercase() == "reading") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تشتت نظر الوعاء (عدم مطابقة حركة العين)", color = Color(0xFFC9D1D9), fontSize = 12.sp)
                            Checkbox(
                                checked = readingGazeDiverted,
                                onCheckedChange = { readingGazeDiverted = it ?: false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active sensors profile checkers list
            Text(
                text = "🔍 اختبارات النقاء النشطة لهذا البروفايل:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conditionsList.forEach { condition ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isViolated = (isSimulatingCheating && condition.contains("تسارع")) ||
                                (improperAngle && (condition.contains("زاوية") || condition.contains("الورك"))) ||
                                (deviceShaking && condition.contains("ثبات")) ||
                                (readingGazeDiverted && condition.contains("حركة العينين"))

                        Icon(
                            imageVector = if (isViolated) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = "Status",
                            tint = if (isViolated) Color.Red else Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = condition,
                            color = if (isViolated) Color.Red else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Complete action confirmation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val violations = mutableListOf<String>()
                        if (isSimulatingCheating) violations.add("تم رصد تلاعب صريح بنظام التكرار")
                        if (improperAngle) violations.add("زاوية حركة المفاصل غير مكتملة")
                        if (deviceShaking) violations.add("اهتزاز الجهاز بصورة عشوائية")
                        if (readingGazeDiverted) violations.add("انحراف نظر الوعاء عن شاشة القراءة")

                        val result = PurityResult(
                            score = score,
                            level = when {
                                score >= 90 -> "pure"
                                score >= 70 -> "acceptable"
                                score >= 50 -> "suspicious"
                                else -> "cheat"
                            },
                            violations = violations
                        )
                        viewModel.submitAegisResult(result)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purityColor),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (score < 50) "إنهاء وإرسال النتيجة المتدهورة" else "إنهاء وإرسال النقاء المستقر",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إلغاء", fontSize = 14.sp)
                }
            }
        }
    }
}
