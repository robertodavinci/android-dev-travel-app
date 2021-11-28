package com.apps.travel_app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.cardPadding
import androidx.compose.material.MaterialTheme
import com.apps.travel_app.ui.theme.textNormal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun Loader() {

    val infiniteTransition = rememberInfiniteTransition()
    val dots by infiniteTransition.animateValue(
        initialValue = 1,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            imageModel = R.drawable.loading,
            contentDescription = "",
            modifier = Modifier.width(100.dp).height(100.dp)
        )
        Text(
            text = "Loading" + ".".repeat(dots),
            fontSize = textNormal,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(cardPadding)
        )
    }
}