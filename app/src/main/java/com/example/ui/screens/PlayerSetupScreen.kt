package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VesselViewModel
import com.example.ui.theme.*
import com.example.ui.widgets.NeonButton
import com.example.ui.widgets.ParticleBackground

@Composable
fun PlayerSetupScreen(
    viewModel: VesselViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBackground)
            .testTag("player_setup_container")
    ) {
        // High-tech floating particle background
        ParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Aegis Eye Icon / Seal
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Brush.radialGradient(listOf(SoloGold.copy(alpha = 0.2f), Color.Transparent)))
                    .border(BorderStroke(2.dp, SoloGold), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Aegis Eye Seal",
                    tint = SoloGold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subtitle protocol / Awakening
            Text(
                text = "AEGIS SYSTEM awakening PROTOCOL",
                color = SoloPrimaryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main questioning header
            Text(
                text = "ما اسمك أيها الصياد؟",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "أدخل اسمك الحقيقي لتزامن الروابط العصبية واستكشاف زنزانات التحقق الفائق.",
                color = SoloMutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic name input panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoloCardBg)
                    .border(
                        BorderStroke(1.dp, if (isError) SoloAccentRed else SoloBorderSlate),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "IDENTIFICATION TOKEN: CODENAME",
                        color = SoloMutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    TextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            if (it.trim().isNotEmpty()) isError = false
                        },
                        placeholder = { Text("أدخل اسم الصياد هنا...", color = SoloMutedText.copy(alpha = 0.7f), fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = SoloPrimaryCyan,
                            unfocusedIndicatorColor = SoloBorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hunter_name_input")
                    )

                    if (isError) {
                        Text(
                            text = "لا يمكن أن يكون اسم الصياد فارغاً!",
                            color = SoloAccentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Neon button to initiate sequence
            NeonButton(
                text = "ابدأ الرحلة / ARISE SYSTEM",
                accentColor = SoloGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_journey_button"),
                onClick = {
                    val trimmedName = nameInput.trim()
                    if (trimmedName.isEmpty()) {
                        isError = true
                    } else {
                        // Persist custom name
                        viewModel.updateUsername(trimmedName)
                        
                        // Save configuration setup completed locally
                        val prefs = context.getSharedPreferences("aegis_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_setup_completed", true).apply()
                        
                        onComplete()
                    }
                }
            )
        }
    }
}
