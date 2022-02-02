package com.apps.travel_app.ui.components
/**
 * Determines the style of heading and subheading of a text.
 */
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.pacifico
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.theme.textNormal

@Composable
fun Heading(text: String = "", modifier: Modifier = Modifier, color: Color = MaterialTheme.colors.surface) {
    Text(
        text = text,
        fontSize = textHeading,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(start = cardPadding, end = cardPadding).fillMaxWidth()
    )
}

@Composable
fun Subheading(text: String = "", modifier: Modifier = Modifier, color: Color = MaterialTheme.colors.surface) {
    Text(
        text = text,
        fontSize = textNormal,
        fontWeight = FontWeight.Bold,
        fontFamily = pacifico,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(start = cardPadding, end = cardPadding).fillMaxWidth()
    )}