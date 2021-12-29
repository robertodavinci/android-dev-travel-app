package com.apps.travel_app.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.apps.travel_app.user
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


const val API = "https://safeitaly.org/travel/controller.php"

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

fun sendPostRequest(body: String, url: String = API, action: String = ""): String? {
   val mURL = URL("${url}?action=${action}&user=${user.id}&user_m=${user.email}")



    var responseBody: String?

    with(mURL.openConnection() as HttpURLConnection) {
        requestMethod = "POST"



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
