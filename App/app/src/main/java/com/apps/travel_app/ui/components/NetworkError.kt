package com.apps.travel_app.ui.components
/**
 * Composable function that informs the user of a network error.
 */
import FaIcons
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.textNormal
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun NetworkError() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.size(40.dp))
        FaIcon(FaIcons.Meteor, tint = MaterialTheme.colors.surface, size = 50.dp)

        Heading(
            stringResource(R.string.alone),
            Modifier.padding(
                cardPadding
            )
        )
        Text(
            stringResource(R.string.alone_2),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(
                cardPadding
            ),
            fontSize = textNormal,
            textAlign = TextAlign.Center
        )
    }
}