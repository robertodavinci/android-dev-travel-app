package com.apps.travel_app.ui.components

import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun BottomNavigationBar(navController: NavController) {
    var currentTab = BottomBarItem.Home.route
    val items = listOf(
        BottomBarItem.Home,
        BottomBarItem.Map,
        BottomBarItem.Trips,
        BottomBarItem.Explore,
        BottomBarItem.Profile
    )

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = iconLightColor,
        modifier = Modifier
            .padding(10.dp)
            .height(60.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(60.dp)
                clip = true
                shadowElevation = cardElevation.value
            }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEachIndexed { index, item ->
            val scale: Float by animateFloatAsState(
                if (currentRoute == item.route) 1.35f else 1f, animationSpec = tween(
                    durationMillis = 700,
                    easing = LinearOutSlowInEasing
                )
            )
            BottomNavigationItem(
                icon = {
                    if (index == 2) {
                        Row(modifier = Modifier
                            .graphicsLayer {
                                shape = RoundedCornerShape(100)
                                clip = true
                            }
                            .width(40.dp)
                            .height(40.dp)
                            .background(primaryColor),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center) {
                            FaIcon(
                                item.icon,
                                tint = Color.White,

                                )
                        }
                    } else {
                        FaIcon(
                            item.icon,
                            tint = if (currentRoute == item.route) textLightColor else iconLightColor,
                        )
                    }
                },
                selectedContentColor = primaryColor,
                unselectedContentColor = iconLightColor,
                alwaysShowLabel = false,
                modifier = Modifier.then(
                    Modifier.scale(
                        scale
                    )
                ),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {

                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }

                        launchSingleTop = true

                        restoreState = true
                    }
                }
            )
        }
    }
}
