package com.apps.travel_app.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.cardElevation
import com.apps.travel_app.ui.theme.cardLightBackground
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.textLightColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    popup: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {


    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cardRadius),
        color = cardLightBackground,
        contentColor = textLightColor,
        elevation = elevation,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        indication = rememberRipple()
    ) {

        ProvideTextStyle(
            value = MaterialTheme.typography.button
        ) {
            Row(
                Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}