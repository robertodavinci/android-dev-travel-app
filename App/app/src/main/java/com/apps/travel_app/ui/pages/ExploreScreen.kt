package com.apps.travel_app.ui.pages

import FaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon
import java.lang.Math.random
import kotlin.math.roundToInt


@Composable
fun ExploreScreen(navController: NavController, mainActivity: MainActivity) {

    var searchTerm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = searchTerm, onValueChange = { searchTerm = it },
                shape = RoundedCornerShape(cardRadius),
                modifier = Modifier
                    .shadow(cardElevation)
                    .fillMaxWidth()
                    .weight(1f),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = lightBackground,
                ),
                placeholder = {
                    Text(
                        "Search",
                        color = textLightColor,
                        modifier = Modifier.alpha(0.5f)
                    )
                },
                trailingIcon = { FaIcon(FaIcons.Search, tint = iconLightColor) },
                singleLine = true,
                textStyle = TextStyle(color = textLightColor, fontWeight = FontWeight.Bold),
            )
            IconButton(onClick = {
                navController.navigate("map") {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                FaIcon(FaIcons.GlobeEurope, tint = iconLightColor)
            }
        }
        Grid(10, mainActivity)
    }
}

@Composable
fun Grid(n: Int, mainActivity: MainActivity) {
    var i = 0
    val destination = Destination()
    destination.name = "Milan"
    destination.thumbnailUrl =
        "https://www.welcometoitalia.com/wp-content/uploads/2020/10/galleria_Milan.jpg"
    LazyColumn(
        modifier = Modifier
            .rotate(3.5f)
            .scale(1.1f)
            .padding(top = cardPadding)
    ) {
        item {
            while (i < n) {
                val howMany = 1 + random().roundToInt()
                val first = random() * (0.4f / howMany)
                val last = random() * (0.4f / howMany)
                val weight = (1f - first - last) / howMany
                Row {
                    Box(
                        modifier = Modifier
                            .weight(first.toFloat(), true)
                            .padding(5.dp)
                    ) {
                        NullCard()
                    }
                    for(x in 1..howMany) {
                        Box(
                            modifier = Modifier
                                .weight(weight.toFloat(), true)
                                .padding(5.dp)
                        ) {
                            MainCard(
                                destination = destination,
                                rating = 3.5f,
                                mainActivity = mainActivity,
                                imageMaxHeight = 100f,
                                padding = 0.dp,
                                shadow = 0.dp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(last.toFloat(), true)
                            .padding(5.dp)
                    ) {
                        NullCard()
                    }
                }
                i += howMany
            }
        }
    }
}

@Composable
fun NullCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(cardRadius),
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFFD9D9E9))
                .fillMaxWidth()
        ) {}
    }
}
