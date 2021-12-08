package com.apps.travel_app

import FaIcons
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
//import com.apps.travel_app.data.room.db.DB
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.BottomBarItem
import com.apps.travel_app.ui.components.BottomNavigationBar
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.components.login.User
import com.apps.travel_app.ui.pages.*
import com.apps.travel_app.ui.theme.MainActivity_Travel_AppTheme
import com.facebook.login.LoginManager
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.guru.fontawesomecomposelib.FaIconType

class MainActivity : ComponentActivity() {

    var user: User = User("","")
    private var destination: Destination? = null
    lateinit var navController: NavHostController
    var prova: MutableState<Boolean> = mutableStateOf(true)



    @OptIn(ExperimentalFoundationApi::class,
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        kotlinx.coroutines.ExperimentalCoroutinesApi::class
    )
    fun signOut() {

        val auth = Firebase.auth
        LoginManager.getInstance().logOut()
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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

    fun setTrip(trip: Trip, active: Boolean = false) {
        if (active) {
            val intent = Intent(this, ActiveTripActivity::class.java)
            intent.putExtra("trip", trip)
            startActivity(intent)
        } else {
            val intent = Intent(this, TripActivity::class.java)
            intent.putExtra("trip", trip)
            startActivity(intent)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = Firebase.auth
        user.displayName = auth.currentUser?.displayName
        user.email = auth.currentUser?.email.toString()


        // maybe do this Async way - Room DB creation
      /*  val db = Room.databaseBuilder(
            applicationContext,
            DB::class.java, "travel-db"
        ).build()*/

      /* val db: FirebaseFirestore = Firebase.firestore
        // Firebase database auth
        // val db = Firebase.firestore
       /* val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
        */

        // Create a new user with a first and last name
       val user = hashMapOf(
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
            }*/



        // IS USER NEW
        /* val metadata = auth.currentUser!!.metadata
         if (metadata!!.creationTimestamp == metadata!!.lastSignInTimestamp) {
             // The user is new, show them a fancy intro screen!
         } else {
             // This is an existing user, show them a welcome back screen.
         }*/


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
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            Navigation(navController,context, activity)
        }
    }


    @Composable
    fun Navigation(navController: NavHostController, context: Context, activity: MainActivity) {
        val mapScreen = remember{ mutableStateOf(MapScreen())}
        NavHost(navController, startDestination = BottomBarItem.Home.route) {
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
    }


    override fun onResume() {

        super.onResume()
    }
}
