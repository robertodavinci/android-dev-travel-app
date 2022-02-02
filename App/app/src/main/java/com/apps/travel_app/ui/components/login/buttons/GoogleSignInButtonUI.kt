package com.apps.travel_app.ui.components.login.buttons

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ContentAlpha.medium
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.tooling.preview.Preview
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.primaryColor
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import java.util.function.IntConsumer

@ExperimentalMaterialApi
@Composable
fun GoogleSignInButtonUI(
    text: String = "",
    loadingText: String = "",
    onClicked: () -> Unit
) {

    var clicked by remember { mutableStateOf(false) }
    com.apps.travel_app.ui.components.Button(
        onClick = { clicked = !clicked },
        background = White
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FaIcon(FaIcons.Google, tint = primaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (clicked) loadingText else text, color = primaryColor)

            if (clicked) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = primaryColor
                )
                onClicked()


            }

        }
    }


}