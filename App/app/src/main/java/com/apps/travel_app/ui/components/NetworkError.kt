package com.apps.travel_app.ui.components

import FaIcons
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.cardPadding
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun NetworkError() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        //GlideImage(imageModel = R.drawable.astro, modifier = Modifier.size(50.dp))
        FaIcon(FaIcons.Meteor, tint = MaterialTheme.colors.surface, size = 50.dp)

        Heading(
            "Dear astronaut, it seems that you are alone without any connectivity signal with other humans",
            Modifier.padding(
                cardPadding
            )
        )
        Text(
            "Please, check the connection signal. We'll keep waiting for you",
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(
                cardPadding
            ),
            textAlign = TextAlign.Center
        )
    }
}