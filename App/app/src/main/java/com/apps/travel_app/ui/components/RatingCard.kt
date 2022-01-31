package com.apps.travel_app.ui.components


import com.apps.travel_app.R
import androidx.compose.material.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apps.travel_app.models.Rating
import com.apps.travel_app.ui.theme.*
import com.skydoves.landscapist.glide.GlideImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RatingCard(rating: Rating) {

    fun epochToDate(netDate: Long): String {
        if (netDate > 0) {
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = Date(netDate * 1000)
            return simpleDateFormat.format(date)
        }
        return ""
    }

    val open = remember { mutableStateOf(false) }
    val maxHeight: Float by animateFloatAsState(
        if (open.value) 500f else 0f, animationSpec = tween(
            durationMillis = 1000
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardPadding / 2),
        onClick = {open.value = !open.value},
        elevation = cardElevation / 2,
        shape = RoundedCornerShape(cardRadius / 2)
    ) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.onBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                imageModel = R.mipmap.icon,
                contentDescription = "",
                modifier = Modifier
                    .padding(cardPadding)
                    .width(40.dp)
                    .height(40.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(100)
                        clip = true
                    }
            )
            Column(
                modifier = Modifier.padding(cardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RatingBar(
                        rating = rating.rating,
                        modifier = Modifier.height(17.dp),
                        emptyColor = Color(0x448888AA)
                    )
                    Text(
                        text = epochToDate(rating.time),
                        fontSize = textExtraSmall,
                        color = MaterialTheme.colors.surface,
                    )
                }
                Text(
                    text = rating.message,
                    fontSize = textNormal,
                    lineHeight = textSmall / 2,
                    color = MaterialTheme.colors.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, maxHeight.dp)
                )
                Text(
                    text = rating.username,
                    color = MaterialTheme.colors.surface,
                    fontSize = textNormal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
