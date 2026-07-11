package com.example.deltacards

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object Api {
    private const val TAG = "DeltaApi"

    fun post(base: String, action: String, token: String?, body: JSONObject): JSONObject {
        val url = URL("$base?action=$action")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        if (!token.isNullOrEmpty()) conn.setRequestProperty("X-Token", token)
        val wr = OutputStreamWriter(conn.outputStream, "UTF-8")
        wr.write(body.toString())
        wr.flush()
        wr.close()
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
        val text = stream.bufferedReader().use { it.readText() }
        conn.disconnect()
        Log.d(TAG, "POST $action -> $code $text")
        return JSONObject(text)
    }

    fun get(base: String, action: String, token: String?): JSONObject {
        val url = URL("$base?action=$action" + if (!token.isNullOrEmpty()) "&token=$token" else "")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
        val text = stream.bufferedReader().use { it.readText() }
        conn.disconnect()
        Log.d(TAG, "GET $action -> $code $text")
        return JSONObject(text)
    }
}
