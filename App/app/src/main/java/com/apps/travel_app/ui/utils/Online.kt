package com.apps.travel_app.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


val API = "https://safeitaly.org/travel/controller.php"

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

fun sendPostRequest(body: String, url: String = API, action: String = ""): String? {
   val mURL = URL("${url}?action=${action}")

    var responseBody: String?

    with(mURL.openConnection() as HttpURLConnection) {
        requestMethod = "POST"



        try {
            val wr = OutputStreamWriter(outputStream)
            wr.write(body)
            wr.flush()

            println("URL : $url")
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
