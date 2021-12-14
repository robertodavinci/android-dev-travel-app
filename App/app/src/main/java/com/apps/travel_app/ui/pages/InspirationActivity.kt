package com.apps.travel_app.ui.pages

import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.cardRadius
import com.skydoves.landscapist.glide.GlideImage
import java.lang.Math.random
import kotlin.math.abs
import kotlin.math.sign


class InspirationActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        setContent {
            Travel_AppTheme(systemTheme = systemTheme) {
                Box(
                    contentAlignment = Center,
                    modifier = Modifier.fillMaxSize().background(colors.background)
                ) {
                    Grid()
                }
            }
        }

    }

}

@Composable
fun Grid() {
    val thumbs = arrayListOf(
        "https://www.veneto.info/wp-content/uploads/sites/114/verona-hd.jpg",
        "https://www.thetrainline.com/content/vul/hero-images/city/bologna/1x.jpg",
        "https://thumbor.forbes.com/thumbor/960x0/https%3A%2F%2Fspecials-images.forbesimg.com%2Fimageserve%2F609bccd983c8e785ad906ba2%2FVenice-at-sunny-evening%2F960x0.jpg%3Ffit%3Dscale",
        "https://www.ansa.it/webimages/ch_600x/2020/12/29/631b8cc7656655a7a35a32166c6592c7.jpg",
        "https://www.sololibri.net/local/cache-gd2/fb/55d21198cce8dd44a4260de99d5fac.jpg",
        "https://deih43ym53wif.cloudfront.net/forum-romanum-rome-shutterstock_1486313342_98790c5210.jpeg",
        "https://siviaggia.it/wp-content/uploads/sites/2/2020/08/innamorarsi-napoli.jpg"
    )
    val density = Resources.getSystem().displayMetrics.density
    val height = Resources.getSystem().displayMetrics.heightPixels / density
    val width = Resources.getSystem().displayMetrics.widthPixels / density

    val array by remember {
        mutableStateOf(ArrayList<ArrayList<Float>>())
    }

    val doubles by remember {
        mutableStateOf(ArrayList<Boolean>())
    }


    if (array.size == 0) {
        var prevDoubled = false
        var i = 0
        var j = 0
        while (true) {
            if ((j / 6) > 5)
                break

            val p = random() > 0.7f && !prevDoubled && j % 6 != 0

            doubles.add(p)
            prevDoubled = p

            if (p) {
                array.add(arrayListOf(
                    (-width / 2f  + (j % 6) * width / 3f),// + ((j / 10) % 2) * 40,
                    (-height / 2f + (j / 6) * height / 6f)
                ))
                j += 2
            } else {
                array.add(arrayListOf(
                    (-width / 2f  + (j % 6) * width / 3f),// + ((j / 10) % 2) * 40,
                    (-height / 2f + (j / 6) * height / 6f)
                ))
                j++
            }


            i++
        }

    }

    var dragging by remember { mutableStateOf(false) }

    val velocity: Float by animateFloatAsState(
        if (!dragging) 1f else 0f, animationSpec = tween(
            durationMillis = 2000,
            easing = LinearEasing
        )
    )


    var point by remember { mutableStateOf(Offset(0f, 0f)) }
    var last by remember { mutableStateOf(Offset(0f, 0f)) }
    var delta by remember { mutableStateOf(Offset(0f, 0f)) }

    var opened by remember { mutableStateOf(-1)}

    if (!dragging) {
        delta = last * (1 - velocity * velocity)
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .rotate(-10f)
                .fillMaxSize().pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { position ->
                            dragging = true
                            point = position
                        },
                        onDrag = { event, _ ->
                            delta = (event.position - point) * 0.5f
                            point = event.position
                        },
                        onDragEnd = {
                            dragging = false
                            last = delta
                        }
                    )
                }, contentAlignment = Center
        ) {
            var i = 0
            while (i < doubles.size) {
                val element = array[i]

                    element[0] += delta.x
                    element[1] += delta.y

                    val w = (if (doubles[i]) 2 else 1) * width / 3f

                    if (abs(element[0]) > width) {
                        element[0] += -sign(element[0]) * (width * 2f)
                    }
                    if (abs(element[1] - 1) > height * 0.5f) {
                        element[1] += -sign(element[1]) * height * 1f
                    }

                    val index by remember { mutableStateOf(i) }


                    val v: Float by animateFloatAsState(
                        if (opened == index) 1f else 0f, animationSpec = tween(
                            durationMillis = 1000,
                            easing = LinearOutSlowInEasing
                        )
                    )


                    if (opened == index) {
                        Box (Modifier.background(Color(0x66111122)).fillMaxSize().zIndex(1f).rotate(10f))
                    }
                    Card(
                        modifier = Modifier
                            .offset((element[0] + w/2 - 5).dp * (1 - v), element[1].dp * (1 - v))
                            .width( (w - 5).dp * (v + 1))
                            .height((height / 6f - 5).dp * (v + 1))
                            .rotate(10f * v)
                            .zIndex(2 * v),
                        shape = RoundedCornerShape(cardRadius)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(colors.onBackground)
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            opened = index
                                        }
                                    )
                                }
                        ) {
                            val url by remember { mutableStateOf(thumbs[(random() * thumbs.size).toInt()]) }
                            GlideImage(
                                imageModel = url,
                                contentScale = ContentScale.Crop


                            )

                        }

                    }
                    i++


            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(BottomStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Transparent,
                                colors.background
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(TopStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(

                                colors.background,
                                Transparent
                            )
                        )
                    )
            ) {
                Heading("Spin and pick your place")
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .align(TopEnd)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Transparent,
                                colors.background
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .align(TopStart)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.background,
                                Transparent
                            )
                        )
                    )
            )
        }

    }
}
