package com.apps.travel_app.ui.pages
/**
 * File that acts both as an activity and a composable function.
 * Accessed by selecting "The wall" in the explore option of the navigation
 * bar. Displays a large grid set of different destinations in the database,
 * in a form of destination cards, which when clicked, can take the user
 * directly to the destination screen.
 */
// Vincenzo Manto
import android.app.Activity
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.DestinationCard
import com.apps.travel_app.ui.components.Loader
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.darkBackground
import com.apps.travel_app.ui.theme.requireFullscreenMode
import com.apps.travel_app.ui.utils.API
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.landscapist.glide.GlideImage
import java.lang.Math.random
import kotlin.math.abs
import kotlin.math.sign


class InspirationActivity : ComponentActivity() {

    private var destinations: MutableList<Destination> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireFullscreenMode(window, this)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val request = "x" // NON-NLS




        setContent {
            destinations = remember { ArrayList() }
            var loaded by remember { mutableStateOf(false) }
            Thread {
                val citiesText = sendPostRequest(request, action = "polygonCities") // NON-NLS
                if (!citiesText.isNullOrEmpty()) {
                    try {
                        val gson = Gson()
                        val itemType = object : TypeToken<List<Destination>>() {}.type
                        val cities: List<Destination> = gson.fromJson(citiesText, itemType)
                        for (city in cities) {
                            destinations.add(city)
                        }
                        loaded = true
                    } catch (e: Exception) {
                        errorMessage(window.decorView.rootView).show()
                    }
                }
            }.start()
            Travel_AppTheme(systemTheme = systemTheme) {

                Box(
                    contentAlignment = Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    if (loaded) {
                        Grid(this@InspirationActivity)
                    }
                    else  {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .fillMaxSize()
                            .padding(100.dp)) {
                            Loader()
                        }
                    }
                }
                }
            }
        }



    @Composable
    fun Grid(activity: Activity) {

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
                if ((j / 10) > 6)
                    break

                val p = random() > 0.8f && !prevDoubled && j % 10 != 0

                doubles.add(p)
                prevDoubled = p

                if (p) {
                    array.add(
                        arrayListOf(
                            (-width / 2f + (j % 10) * width / 3f),// + ((j / 10) % 2) * 40,
                            (j / 10).toFloat()
                        )
                    )
                    j += 2
                } else {
                    array.add(
                        arrayListOf(
                            (-width / 2f + (j % 10) * width / 3f),// + ((j / 10) % 2) * 40,
                            (j / 10).toFloat()
                        )
                    )
                    j++
                }


                i++
            }

        }

        var opened by remember { mutableStateOf(-1) }

        val infiniteTransition = rememberInfiniteTransition()
        val scroll by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
        ) {
            Box(
                modifier = Modifier
                    //.scale(1.25f)
                    .rotate(-10f)
                    .fillMaxSize(),
                contentAlignment = Center
            ) {
                var i = 0
                while (i < doubles.size) {
                    val element = array[i]

                    val m = if (element[1] % 2 == 0f) 1 else -1
                    var x = element[0]
                    if (scroll > -1f) {
                        x += m
                    }
                    element[0] = x

                    val w = (if (doubles[i]) 2 else 1) * width / 3f

                    if (abs(x) > width * 1.5f) {
                        x += -sign(x) * (width * 3f)
                    }

                    val index by remember { mutableStateOf((random() * destinations.size).toInt()) }

                    Card(
                        modifier = Modifier
                            .offset(
                                (x + w / 2 - 5).dp,
                                (-height / 2f + element[1] * height / 6f).dp
                            )
                            .width((w - 5).dp)
                            .height((height / 6f - 5).dp),
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
                            val uri: String = Uri.parse("$API?action=getImage")
                                .buildUpon()
                                .appendQueryParameter("url", destinations[index].thumbnailUrl)
                                .build().toString()
                            val url by remember { mutableStateOf(
                                uri) }
                            GlideImage(
                                imageModel = url,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    i++


                }
            }
            DestinationCard(
                destination = if (opened != -1) destinations[opened] else null, modifier = Modifier.align(
                    BottomCenter
                ),
                open = opened != -1,
                activity = activity
            )

        }
    }
}