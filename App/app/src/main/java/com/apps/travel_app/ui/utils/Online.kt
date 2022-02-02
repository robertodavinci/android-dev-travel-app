package com.apps.travel_app.ui.utils

/**
 * Controller used for communication with the middleware, that then connects to the backend.
 * Used when the app is functioning in the 'Online' mode.
 */
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import androidx.compose.ui.graphics.toArgb
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.user
import com.google.android.material.snackbar.Snackbar
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


const val API = "https://www.superiorgames.eu/travel/controller.php"

fun getBitmapFromURL(src: String?): Bitmap? {
    return try {
        val url = URL(src)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        // Log exception
        null
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
        }
    }
    return false
}

fun sendPostRequest(body: String, url: String = API, action: String = ""): String? { // NON-NLS
   val mURL = URL("${url}?action=${action}&user=${user.id}&user_m=${user.email}&lang=${Locale.getDefault().language}")

    val basicAuth = "Bearer 080042cad6356ad5dc0a720c18b53b8e53d4c274"




    var responseBody: String?

    with(mURL.openConnection() as HttpURLConnection) {
        requestMethod = "POST"

        setRequestProperty("Authorization", basicAuth)



        try {
            val wr = OutputStreamWriter(outputStream)
            wr.write(body)
            wr.flush()

            println("URL : $mURL")
            println("Response Code : $responseCode")
            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                println("Response : $response")
                responseBody = response.toString()
            }
        } catch (e: Exception) {
            responseBody = ""
        }
    }
    return responseBody
}

fun errorMessage(view: View, text: String = view.context.resources.getString(R.string.connectivity_problem)): Snackbar {

    val sb = Snackbar.make(
        view, text,
        Snackbar.LENGTH_LONG)
    sb.view.setBackgroundColor(danger.toArgb());
    return sb

}




class Response {
    var places: ArrayList<Destination> = arrayListOf()
    var cities: ArrayList<Destination> = arrayListOf()
    var trips: ArrayList<Trip> = arrayListOf()
}
