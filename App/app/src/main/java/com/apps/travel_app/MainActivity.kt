package com.apps.travel_app


import FaIcons
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.BottomBarItem
import com.apps.travel_app.ui.components.BottomNavigationBar
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.components.login.models.User
import com.apps.travel_app.ui.pages.*
import com.apps.travel_app.ui.theme.MainActivity_Travel_AppTheme
import com.apps.travel_app.ui.utils.Response
import com.facebook.login.LoginManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIconType
import java.util.prefs.Preferences
import kotlin.reflect.KProperty


var user: User = User("","", "","","")


class MainActivity : ComponentActivity() {


    private var destination: Destination? = null
    lateinit var navController: NavHostController
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth


    @OptIn(ExperimentalFoundationApi::class,
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        kotlinx.coroutines.ExperimentalCoroutinesApi::class
    )
    fun signOut() {

        val auth = Firebase.auth
        LoginManager.getInstance().logOut()
        auth.signOut()
        getSharedPreferences("CURRENT_USER", Context.MODE_PRIVATE).edit().clear().commit()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goHome() {
        navController.navigate(BottomBarItem.Home.route) {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun setGooglePlace(destination: Destination, openScreen: Boolean = false) {
        this.destination = destination
        if (openScreen) {
            navController.navigate(SubPages.GooglePlace.route) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun setDestination(destination: Destination, openScreen: Boolean = false) {
        this.destination = destination
        if (openScreen) {
            navController.navigate(SubPages.Location.route) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun createTrip() {
        val intent = Intent(this, TripCreationActivity::class.java)
        startActivity(intent)
    }

    fun setTrip(trip: Trip, active: Boolean = false) {
        if (active) {
            val intent = Intent(this, ActiveTripActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        } else {
            val intent = Intent(this, TripActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destinationText = intent.getStringExtra("destination")
        if (!destinationText.isNullOrEmpty() && intent.action == "destination") {
            try {
                val gson = Gson()
                val itemType = object : TypeToken<Destination>() {}.type
                val dest: Destination = gson.fromJson(destinationText, itemType)
                setDestination(dest, true)
            } catch (e: Exception) {

                currentFocus?.let {
                    Snackbar.make(
                        it, "Ops, there is a connectivity problem",
                        Snackbar.LENGTH_LONG).show()
                }

            }
        }

        auth = Firebase.auth
        user.displayName = auth.currentUser?.displayName
        user.email = auth.currentUser?.email.toString()
        user.id = auth.currentUser?.uid.toString()
        user.realName = getSharedPreferences("CURRENT_USER", Context.MODE_PRIVATE).getString("realName", "")
        user.realSurname = getSharedPreferences("CURRENT_USER", Context.MODE_PRIVATE).getString("realSurname", "")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }


        // maybe do this Async way - Room DB creation
        /*  val db = Room.databaseBuilder(
              applicationContext,
              DB::class.java, "travel-db"
          ).build()*/


        // Firebase database auth
        db = Firebase.firestore
        /* val settings = firestoreSettings {
             isPersistenceEnabled = true
         }
         db.firestoreSettings = settings
         */

        // Create a new user with a first and last name
        /* val user = hashMapOf(
             "first" to "Ada",
             "last" to "Lovelace",
             "born" to 1815
         )
 // Add a new document with a generated ID
         db.collection("users")
             .add(user)
             .addOnSuccessListener { documentReference ->
                 Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
             }
             .addOnFailureListener { e ->
                 Log.w("Firestore", "Error adding document", e)
             }
 */
        FirebaseMessaging.getInstance().subscribeToTopic("all").addOnCompleteListener {
            Log.d("FCM","Subscribed")
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        setContent {
            MainActivity_Travel_AppTheme(systemTheme = systemTheme) {
                MainScreen(this,this)
            }
        }
    }


    @Composable
    fun MainScreen(context: Context, activity: MainActivity) {
        navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, this) }
        ) {
            Navigation(navController,context, activity)
        }
    }


    @Composable
    fun Navigation(navController: NavHostController, context: Context, activity: MainActivity) {
        val mapScreen = remember{ mutableStateOf(MapScreen())}
        NavHost(navController, startDestination = BottomBarItem.Home.route, modifier = Modifier.padding(bottom = 120.dp)) {
            composable(BottomBarItem.Home.route) {
                HomeScreen(navController, activity)
            }
            composable(BottomBarItem.Map.route) {
                mapScreen.value.MapScreen(context, activity)
            }
            composable(BottomBarItem.Trips.route) {
                TripsScreen(activity)
            }
            composable(BottomBarItem.Explore.route) {
                ExploreScreen(navController, activity)
            }
            composable(BottomBarItem.Profile.route) {
                ProfileScreen(activity)
            }
            composable(SubPages.GooglePlace.route) {
                GooglePlaceScreen(navController, destination!!, activity)
            }
            composable(SubPages.Location.route) {
                if (destination != null) {
                    LocationScreen(navController, destination!!, activity)
                }
            }
        }
    }

    sealed class SubPages(var route: String, var icon: FaIconType, var title: String) {
        object Location : SubPages("location", FaIcons.Home, "Location")
        object Trip : SubPages("trip", FaIcons.Home, "Trip")
        object GooglePlace : SubPages("googlePlace", FaIcons.Home, "Place")
    }

    override fun onDestroy() {
        super.onDestroy()
        System.exit(0)
    }
}