package com.apps.travel_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.cardlightBackground
import com.apps.travel_app.ui.theme.contrastColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    background: Color = MaterialTheme.colors.onBackground,
    content: @Composable RowScope.() -> Unit
) {


    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100),
        color = background,
        contentColor = contrastColor(background),
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