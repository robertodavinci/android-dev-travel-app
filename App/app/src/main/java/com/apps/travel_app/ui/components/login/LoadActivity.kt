package com.apps.travel_app.ui.components.login
/**
 * A simple activity used for handling Deep links that are coming from outisde of the app.
 */
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import com.apps.travel_app.MainActivity

class LoadActivity : AppCompatActivity() {
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentTwo = Intent(this, LoginActivity::class.java)
        if(intent?.data != null) {
            intentTwo.putExtra("findTripID", intent.dataString.toString())
            //Log.i("AAA ",  intent.dataString.toString())
        }
        startActivity(intentTwo)
        finish()
    }
}