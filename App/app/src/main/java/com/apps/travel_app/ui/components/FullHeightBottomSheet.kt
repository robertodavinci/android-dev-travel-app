package com.apps.travel_app.ui.components

import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.iconLightColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


enum class States {
    EXPANDED,
    COLLAPSED
}

var changed: MutableState<States?> = mutableStateOf(null)

fun ChangeState(state: States) {
    changed.value = state
}


@ExperimentalMaterialApi
@Composable
fun FullHeightBottomSheet(
    mH: Float = convertPixelsToDp(Resources.getSystem().displayMetrics.heightPixels / 2),
    MH: Int = 0,
    button: @Composable (() -> Unit)? = null,
    background: Color = colors.background,
    body: @Composable (States) -> Unit
) {

    changed = remember {
        mutableStateOf(null)
    }

    val swipeableState = rememberSwipeableState(initialValue = States.COLLAPSED)


    val scrollState = rememberScrollState()
    val composableScope = rememberCoroutineScope()

    fun setStatus() {
        composableScope.launch {
            swipeableState.animateTo(changed.value!!, tween(
                durationMillis = 500
            )
            )
            changed.value = null
        }
    }

    if (changed.value != null) {
        setStatus()
    }

    val connection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return if (delta < 0) {
                    swipeableState.performDrag(delta).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                return swipeableState.performDrag(delta).toOffset()
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (available.y < 0 && scrollState.value == 0) {
                    swipeableState.performFling(available.y)
                    available
                } else {
                    Velocity.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                swipeableState.performFling(velocity = available.y)
                return super.onPostFling(consumed, available)
            }

            private fun Float.toOffset() = Offset(0f, this)
        }
    }


    val maxHeight =
        Resources.getSystem().displayMetrics.heightPixels -  convertDpToPixel(mH)


    Box(
        Modifier
            .offset {
                IntOffset(
                    0,
                    swipeableState.offset.value.roundToInt()
                )
            }
            .height(convertPixelsToDp(Resources.getSystem().displayMetrics.heightPixels).dp)
    ) {



        Column(
            Modifier
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        MH.toFloat() to States.EXPANDED,
                        maxHeight.toFloat() to States.COLLAPSED,
                    )
                )
                .graphicsLayer {
                    shape = RoundedCornerShape(
                        cardRadius
                    )
                    clip = true
                    shadowElevation = 200f
                }
                .background(
                    background
                )
                .fillMaxSize()
                .nestedScroll(connection)
        ) {



            Box(modifier = Modifier
                .padding(10.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(100)
                    clip = true
                }
                .background(iconLightColor)
                .height(7.dp)
                .width(70.dp)
                .align(CenterHorizontally)
                .alpha(0.5f))

            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                body(swipeableState.currentValue)
            }
        }
        if (button != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(
                        x = (-30).dp,
                        y = (20 + -swipeableState.offset.value / maxHeight * 40).dp
                    ),
                contentAlignment = TopEnd
            ) {
                button()
            }
        }
    }
}


/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent dp equivalent to px value
 */
fun convertPixelsToDp(px: Int): Float {
    return px / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun convertDpToPixel(dp: Float): Float {
    return dp * (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}