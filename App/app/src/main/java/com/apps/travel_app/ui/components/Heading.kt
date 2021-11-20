package com.apps.travel_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.theme.textLightColor

@Composable
fun Heading(text: String = "") {
    Text(
        text = text,
        fontSize = textHeading,
        fontWeight = FontWeight.Bold,
        color = textLightColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(start = cardPadding, end = cardPadding).fillMaxWidth()
    )
}