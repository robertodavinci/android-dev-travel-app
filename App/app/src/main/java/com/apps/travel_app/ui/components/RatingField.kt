package com.apps.travel_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apps.travel_app.R
import com.apps.travel_app.models.Rating
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.textNormal
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun RatingField(
    onSubmit: (Rating) -> Unit
) {
    var ratingText by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(5f) }

    Column(
        modifier = Modifier.padding(cardPadding)
    ) {
        RatingBar(ratingValue, enabled = true, onChange = {
            ratingValue = it
        },modifier = Modifier.height(30.dp).padding(bottom = 10.dp))
        TextField(
            value = ratingText, onValueChange = { ratingText = it },
            shape = RoundedCornerShape(cardRadius),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = colors.onBackground,
            ),
            placeholder = {
                Text(
                    stringResource(R.string.share_what_you_think),
                    fontSize = textNormal,
                    color = colors.surface,
                    modifier = Modifier.alpha(0.5f)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onSubmit(createRating(ratingText, ratingValue))
                    }) {
                    FaIcon(FaIcons.PaperPlane, tint = colors.surface)
                }
            },
            textStyle = TextStyle(
                color = colors.surface,
                fontWeight = FontWeight.Bold
            ),
        )
    }
}

private fun createRating(text: String, value: Float): Rating {
    val rating = Rating()
    rating.rating = value
    rating.message = text
    rating.username = String()
    return rating
}