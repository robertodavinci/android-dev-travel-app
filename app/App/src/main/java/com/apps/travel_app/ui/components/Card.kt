package com.apps.travel_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apps.travel_app.ui.theme.cardElevation
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius

@Composable
fun PoliCard(content: Unit) {
    Card(
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardPadding),
        shape = RoundedCornerShape(cardRadius),
    ) {
        content
    }
}