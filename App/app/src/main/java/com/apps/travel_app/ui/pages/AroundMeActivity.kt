package com.apps.travel_app.ui.pages

import FaIcons
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.DestinationCard
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.SphericalUtil
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.rememberDrawablePainter
import java.lang.Math.pow
import kotlin.math.*


class AroundMeActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locations = mutableStateOf(ArrayList<Destination>())
    private var destinationSelected: MutableState<Destination?> = mutableStateOf(null)
    private var trips = mutableStateOf(ArrayList<Trip>())
    private lateinit var centralLocation: LatLng
    private var radius = mutableStateOf(200000.0)

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun getLastKnownLocation() {
        if (checkPermission()) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                    }
                    else -> {
                    }
                }
            }
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    centralLocation = LatLng(location.latitude, location.longitude)
                    searchLocations(
                        com.google.android.gms.maps.model.LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                }

            }

    }

    private fun toBounds(
        center: com.google.android.gms.maps.model.LatLng,
        radiusInMeters: Double
    ): LatLngBounds {
        val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
        val southwestCorner: com.google.android.gms.maps.model.LatLng =
            SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northeastCorner: com.google.android.gms.maps.model.LatLng =
            SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(
            LatLng(southwestCorner.latitude, southwestCorner.longitude),
            LatLng(northeastCorner.latitude, northeastCorner.longitude)
        )
    }

    private fun searchLocations(location: com.google.android.gms.maps.model.LatLng) {
        val bounds = toBounds(location, radius.value)
        val points = arrayListOf(
            bounds.northeast,
            LatLng(bounds.northeast.latitude, bounds.southwest.longitude),
            bounds.southwest,
            LatLng(bounds.southwest.latitude, bounds.northeast.longitude)
        )
        val request = points.joinToString(",", "[", "]") { e ->
            "[${e.latitude},${e.longitude}]"
        }

        Thread {
            val citiesText = sendPostRequest(request, action = "polygonCities")
            val gson = Gson()
            val type1 = object : TypeToken<List<Destination>>() {}.type
            locations.value = gson.fromJson(citiesText, type1)
            val tripsText = sendPostRequest(request, action = "polygonTrips")
            val type2 = object : TypeToken<List<Trip>>() {}.type
            trips.value = gson.fromJson(tripsText, type2)
        }.start()
    }

    private fun getPosition(destination: Destination): ArrayList<Double> {
        val density = Resources.getSystem().displayMetrics.density
        val width = Resources.getSystem().displayMetrics.widthPixels / density - 60

        val dLatitude = centralLocation.latitude - destination.latitude
        val dLongitude = destination.longitude - centralLocation.longitude

        val y = dLatitude * 110574 * width / radius.value
        val x =
            dLongitude * 111320 * cos(centralLocation.latitude * (PI / 180)) * width / radius.value

        return arrayListOf(x, y)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermission()) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                    }
                    else -> {
                    }
                }
            }
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getLastKnownLocation()
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        setContent {
            val state = rememberTransformableState { zoomChange, _, _ ->
                radius.value *= zoomChange
            }
            locations = remember { mutableStateOf(ArrayList()) }
            radius = remember { mutableStateOf(radius.value) }
            destinationSelected = remember { mutableStateOf(null) }
            Travel_AppTheme(systemTheme = systemTheme) {
                Surface(color = colors.background) {
                    Box(modifier = Modifier.transformable(state = state)) {

                        Image(
                            painter = rememberDrawablePainter(RadarDrawable()),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Center)
                        )

                        for (location in locations.value) {
                            val pos = getPosition(location)
                            val distance = (SphericalUtil.computeDistanceBetween(

                                com.google.android.gms.maps.model.LatLng(
                                    location.latitude,
                                    location.longitude
                                ),
                                com.google.android.gms.maps.model.LatLng(
                                    centralLocation.latitude,
                                    centralLocation.longitude
                                )
                            ) / 1000).roundToInt()
                            Box(modifier = Modifier.align(Center)) {
                                MapElement(
                                    pos = pos,
                                    distance = distance,
                                    url = location.thumbnailUrl,
                                    destination = location
                                )
                            }
                        }

                        for (location in trips.value) {
                            val pos = getPosition(location.startingPoint)
                            val distance = (SphericalUtil.computeDistanceBetween(

                                com.google.android.gms.maps.model.LatLng(
                                    location.startingPoint.latitude,
                                    location.startingPoint.longitude
                                ),
                                com.google.android.gms.maps.model.LatLng(
                                    centralLocation.latitude,
                                    centralLocation.longitude
                                )
                            ) / 1000).roundToInt()
                            Box(modifier = Modifier.align(Center)) {
                                MapElement(
                                    pos = pos,
                                    distance = distance,
                                    url = location.thumbnailUrl,
                                    destination = location.startingPoint
                                )
                            }
                        }


                        GlideImage(
                            imageModel = R.mipmap.icon,
                            contentDescription = "",
                            modifier = Modifier
                                .width(80.dp)
                                .height(80.dp)
                                .align(Center)
                                .graphicsLayer {
                                    shape = RoundedCornerShape(100)
                                    clip = true
                                })

                        if (checkPermission()) {
                            IconButton(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(80.dp)
                                    .align(Center)
                                    .graphicsLayer {
                                        shape = RoundedCornerShape(100)
                                        clip = true
                                    }
                                    .background(Color(0x66FF0055)),
                                onClick = {
                                    getLastKnownLocation()
                                }
                            ) {
                                FaIcon(FaIcons.MapMarker, tint = danger)
                            }
                        }

                        Column {
                            Heading(
                                "Discover the nearest destinations",
                                modifier = Modifier
                                    .align(Start)
                                    .padding(
                                        cardPadding
                                    )
                            )
                            Text(
                                "${radius.value.toInt() / 1000} km",
                                color = colors.surface,
                                fontSize = textSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        DestinationCard(
                            destination = destinationSelected.value,
                            open = destinationSelected.value != null,
                            modifier = Modifier.align(BottomCenter)
                        )
                    }

                }
            }
        }
    }


    class RadarDrawable : Drawable(), Animatable {
        private var mMinRadius = 0
        private var mMaxRadius = 0
        private var mAnimator: AnimatorSet? = null
        private var mAnimating = false
        private val mCircles: MutableList<Circle> = ArrayList()

        // Drawable
        override fun onBoundsChange(bounds: Rect) {
            mMaxRadius = bounds.width().coerceAtMost(bounds.height()) shr 1
            if (mAnimating) {
                initAnimator()
            }
        }

        override fun draw(canvas: Canvas) {
            val rect: Rect = bounds
            if (isRunning) {
                for (circle in mCircles) {
                    circle.draw(canvas, rect)
                }
            }
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }

        // Animatable
        override fun start() {
            mAnimating = true
            if (!isRunning) {
                initAnimator()
            }
        }

        override fun stop() {
            mAnimating = false
            if (isRunning) {
                mAnimator!!.cancel()
            }
        }

        override fun isRunning(): Boolean {
            return _isRunning
        }

        private val _isRunning: Boolean
            get() = mAnimator != null && mAnimator!!.isRunning

        private fun initAnimator() {
            if (isRunning) {
                mAnimator!!.cancel()
                mCircles.clear()
            }
            if (mMaxRadius <= mMinRadius) {
                return
            }
            mAnimator = AnimatorSet()
            mAnimator!!.playTogether(
                createCircleAnimation(0),
                createCircleAnimation(700),
                createCircleAnimation(1400),
                createCircleAnimation(2100)
            )
            if (mAnimating) {
                mAnimator!!.start()
            }
        }

        private fun createCircleAnimation(startDelay: Int): Animator {
            val circle = Circle()
            mCircles.add(circle)
            return circle.getAnimator(startDelay, mMinRadius, mMaxRadius)
        }

        private inner class Circle : AnimatorUpdateListener {
            private val mStrokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
            private val mFillPaint: Paint
            private var mRadius = 0
            private var mAnimator: ValueAnimator? = null
            fun getAnimator(startDelay: Int, minRadius: Int, maxRadius: Int): Animator {
                mAnimator = ValueAnimator()
                mAnimator!!.setValues(
                    PropertyValuesHolder.ofInt(RADIUS, minRadius, maxRadius),
                    PropertyValuesHolder.ofInt(ALPHA_STROKE, 200, 0),
                    PropertyValuesHolder.ofInt(ALPHA_FILL, 30, 0)
                )
                mAnimator!!.startDelay = startDelay.toLong()
                mAnimator!!.duration = DURATION.toLong()
                mAnimator!!.repeatCount = ValueAnimator.INFINITE
                mAnimator!!.addUpdateListener(this)
                return mAnimator as ValueAnimator
            }

            override fun onAnimationUpdate(animation: ValueAnimator) {
                mRadius = animation.getAnimatedValue(RADIUS) as Int
                mStrokePaint.alpha = animation.getAnimatedValue(ALPHA_STROKE) as Int
                mFillPaint.alpha = animation.getAnimatedValue(ALPHA_FILL) as Int
                invalidateSelf()
            }

            fun draw(canvas: Canvas, rect: Rect) {
                canvas.drawCircle(
                    rect.centerX().toFloat(),
                    rect.centerY().toFloat(),
                    mRadius.toFloat(),
                    mFillPaint
                )
                canvas.drawCircle(
                    rect.centerX().toFloat(),
                    rect.centerY().toFloat(),
                    mRadius.toFloat(),
                    mStrokePaint
                )
            }


            private val RADIUS = "radius"
            private val ALPHA_STROKE = "alphaStroke"
            private val ALPHA_FILL = "alphaFill"


            init {
                mStrokePaint.style = Paint.Style.STROKE
                mStrokePaint.color = primaryColor.toArgb()
                mStrokePaint.strokeWidth = 12f
                mFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                mFillPaint.style = Paint.Style.FILL
            }
        }

        companion object {
            private const val DURATION = 3000
        }
    }

    @Composable
    fun MapElement(url: String, pos: ArrayList<Double>, distance: Int, destination: Destination) {

        Column(
            modifier = Modifier
                .offset(x = pos[0].dp, y = pos[1].dp)
        ) {
            GlideImage(
                imageModel = url,
                contentDescription = "",
                modifier = Modifier

                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                destinationSelected.value = destination
                            }
                        )
                    }
                    .width(60.dp)
                    .height(60.dp)
                    .padding(bottom = 5.dp)
                    .align(CenterHorizontally)
                    .graphicsLayer {
                        shape = RoundedCornerShape(10)
                        clip = true
                    })
            Row(modifier = Modifier.align(CenterHorizontally)) {
                FaIcon(
                    FaIcons.MapMarkerAlt,
                    tint = colors.surface,
                    size = textExtraSmall.value.dp,
                    modifier = Modifier
                        .align(CenterVertically)
                        .padding(end = 5.dp)
                )
                Text(
                    "$distance km",
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = textExtraSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

