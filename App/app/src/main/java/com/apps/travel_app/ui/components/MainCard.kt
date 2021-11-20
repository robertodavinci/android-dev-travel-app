package com.apps.travel_app.ui.components

import FaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainCard(
    destination: Destination,
    rating: Float,
    squaredImage: Boolean = false,
    badges: List<String> = ArrayList(),
    views: Int = 0,
    padding: Dp = cardPadding,
    infoScale: Float = 1f,
    shadow: Dp = cardElevation,
    mainActivity: MainActivity,
    radius: Dp = cardRadius,
    icon: FaIconType? = null
) {
    Card(
        modifier = Modifier
            .padding(padding)
            .fillMaxWidth(),
        elevation = shadow,
        shape = RoundedCornerShape(radius),
        onClick = {
            mainActivity.setDestination(destination, true)
        }
    ) {
        Column(
            modifier = Modifier
                .background(lightBackground)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                val modifier = Modifier.fillMaxWidth()
                if (squaredImage)
                    modifier.aspectRatio(1f)
                GlideImage(
                    imageModel = destination.thumbnailUrl ?: destination.thumbnail,
                    contentDescription = "",
                    modifier = modifier,
                    contentScale = if (squaredImage) ContentScale.Fit else ContentScale.Crop,
                    circularReveal = CircularReveal(duration = 700),

                    )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (destination.name.isNotEmpty()) textLightColor else Color.Transparent
                                )
                            )

                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            FaIcon(
                                icon,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(start = cardPadding * infoScale)
                                    .scale(infoScale)
                            )
                        }
                        Text(
                            text = destination.name,
                            Modifier
                                .padding(start = cardPadding * infoScale)
                                .fillMaxWidth(),
                            color = Color.White,
                            fontSize = textHeading * infoScale,
                            fontWeight = FontWeight.Bold
                        )
                        if (views > 0) {
                            Row(
                                modifier = Modifier.padding(5.dp)
                            ) {
                                FaIcon(FaIcons.EyeRegular, tint = Color.White)
                                Text(
                                    text = views.toString(),
                                    color = Color.White,
                                    fontSize = textSmall * infoScale,
                                    modifier = Modifier.padding(5.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (rating > 0) {
                        Row(
                            modifier = Modifier.padding(
                                bottom = cardPadding * infoScale,
                                start = cardPadding * infoScale
                            )
                        ) {
                            RatingBar(
                                rating = rating,
                                modifier = Modifier
                                    .height(15.dp * infoScale)
                                    .graphicsLayer {
                                        shadowElevation = 3f
                                    },
                                emptyColor = Color(0x88FFFFFF)
                            )
                        }
                    }
                }
            }
            if (badges.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(cardPadding)
                ) {
                    badges.forEach { badge ->
                        Badge(
                            text = badge
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
            }
        }
    }
}