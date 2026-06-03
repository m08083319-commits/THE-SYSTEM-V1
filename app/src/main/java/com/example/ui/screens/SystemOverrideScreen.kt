package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VesselViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemOverrideScreen(viewModel: VesselViewModel) {
    BackHandler(true) {
        // block back
    }
    
    var confirmationText by remember { mutableStateOf("") }
    val requiredText = "أنا أقبل العقوبة"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "SYSTEM OVERRIDE",
                color = Color(0xFFFF0044),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "لقد فقدت كل الأرواح.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "الاستمرار يتطلب التضحية. النظام سيفرض عليك 2000 AP كعقوبة (أو دين). للموافقة واجه فراغ الهاوية واكتب:",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "«$requiredText»",
                color = Color(0xFFFFD700),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            OutlinedTextField(
                value = confirmationText,
                onValueChange = { confirmationText = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFF0044),
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { viewModel.acceptSystemOverride() },
                enabled = confirmationText.trim() == requiredText,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0044), 
                    contentColor = Color.White,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color(0xFFAAAAAA)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "تأكيد واستعادة الروح",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
