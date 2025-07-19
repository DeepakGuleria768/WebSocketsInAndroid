package com.example.wschat

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextComponent(text: String) {

    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .padding(10.dp),
        color = Color(0xFF009688),
        shadowElevation = 10.dp,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(
            topEnd = 30.dp,
            topStart = 1.dp,
            bottomStart = 30.dp,
            bottomEnd = 1.dp
        )
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(10.dp),
            fontWeight = FontWeight.Light,
            color = Color.Black
        )
    }
}

