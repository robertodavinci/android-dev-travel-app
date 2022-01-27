package com.apps.travel_app.ui.components

import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.ui.theme.textNormal
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType

class NiceSwitchStates(T: FaIconType = FaIcons.Check, F: FaIconType = FaIcons.Times) {
    var t: FaIconType = T
    var f: FaIconType = F
}

@Composable
fun NiceSwitch(
    modifier: Modifier = Modifier.padding(5.dp),
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    states: NiceSwitchStates = NiceSwitchStates(),
    label: String? = null

) {
    var _checked by remember {
        mutableStateOf(checked)
    }
    var widthController by remember { mutableStateOf(false) }
    val offset: Float by animateFloatAsState(
        if (!_checked) 50f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    val width: Float by animateFloatAsState(
        if (!widthController) 50f else 100f, animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )
    if (width >= 90)
        widthController = false
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    shape = RoundedCornerShape(100)
                    clip = true
                }
                .width(100.dp)
                .background(MaterialTheme.colors.onBackground)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = offset.dp)
                    .width(width.dp)
                    .height(50.dp)
                    .align(Alignment.CenterStart)

                    .graphicsLayer {
                        shape = RoundedCornerShape(100)
                        clip = true
                    }
                    .background(primaryColor)

            )
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .weight(1f),
                    onClick = {
                        _checked = true
                        widthController = true
                        onChecked(true)
                    }
                ) {
                    FaIcon(
                        states.t,
                        tint = when (_checked) {
                            true -> {
                                Color.White
                            }
                            else -> {
                                MaterialTheme.colors.surface
                            }

                        }
                    )
                }
                IconButton(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .weight(1f),
                    onClick = {
                        _checked = false
                        onChecked(false)
                    }
                ) {
                    FaIcon(
                        states.f, tint = when (_checked) {
                            false -> {
                                Color.White
                            }
                            else -> {
                                MaterialTheme.colors.surface
                            }

                        }
                    )
                }

            }
        }
        if (!label.isNullOrEmpty()) {
            Text(
                label,
                fontSize = textNormal,
                color = MaterialTheme.colors.surface,
                modifier = Modifier
                    .align(CenterVertically)
                    .padding(start = cardPadding)
            )
        }

    }
}