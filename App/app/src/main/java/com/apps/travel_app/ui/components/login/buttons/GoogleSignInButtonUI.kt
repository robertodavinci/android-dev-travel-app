package com.apps.travel_app.ui.components.login

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
import androidx.compose.ui.tooling.preview.Preview
import com.apps.travel_app.R
import java.util.function.IntConsumer

@ExperimentalMaterialApi
@Composable
fun GoogleSignInButtonUI(
    text : String = "",
    loadingText: String = "" ,
    onClicked:() -> Unit){

    var clicked by remember { mutableStateOf(false)}
    Surface(
        onClick = {clicked = !clicked},
        shape = Shapes.medium,
        border = BorderStroke(width = 1.dp,color = Color.LightGray),
        color = MaterialTheme.colors.surface
    ) {
        Row (modifier = Modifier
            .padding(
                start = 12.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            )
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            ),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center){
            Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google sign button", tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (clicked) loadingText else text)

            if (clicked){
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colors.primary
                )
                onClicked()



            }

        }
    }


    @Composable
    fun GoogleButton(
        modifier: Modifier = Modifier,
        text: String = "Sign Up with Google",
        loadingText: String = "Creating Account...",
        icon: Int = R.drawable.ic_google_logo,
        shape: Shape = Shapes.medium,
        borderColor: Color = Color.LightGray,
        backgroundColor: Color = MaterialTheme.colors.surface,
        progressIndicatorColor: Color = MaterialTheme.colors.primary,
        onClicked: () -> Unit
    ) {
        var clicked by remember { mutableStateOf(false) }

        Surface(
            modifier = modifier.clickable { clicked = !clicked },
            shape = shape,
            border = BorderStroke(width = 1.dp, color = borderColor),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = 12.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Google Button",
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (clicked) loadingText else text)
                if (clicked) {
                    Spacer(modifier = Modifier.width(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp),
                        strokeWidth = 2.dp,
                        color = progressIndicatorColor
                    )
                    onClicked()
                }
            }
        }
    }



}

@ExperimentalMaterialApi
@Composable
@Preview
fun GoogleButtonPreview(){
    GoogleSignInButtonUI(
        text = "Sign Up With Google",
        loadingText = "Signing In....",
        onClicked = {}
    )
}