package com.apps.travel_app.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apps.travel_app.ui.theme.cardElevation
import com.apps.travel_app.ui.theme.iconLightColor
import com.apps.travel_app.ui.theme.primaryColor
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
        elevation = cardElevation
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { FaIcon(item.icon, tint = iconLightColor) },
                selectedContentColor = primaryColor,
                unselectedContentColor = iconLightColor,
                alwaysShowLabel = false,
                modifier = Modifier.then(
                      Modifier.scale(
                          if(currentRoute == item.route) 1.25f
                            else 1f
                      )
                  ),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    //BottomNavigationBar()
}
