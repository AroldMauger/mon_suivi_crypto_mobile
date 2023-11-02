package com.example.monsuivicrypto

import android.util.Base64
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

open class VolleyBase64Request(
    method: Int,
    url: String,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, mErrorListener) {

    private val mHeaders = mutableMapOf<String, String>()
    private val mParams = mutableMapOf<String, String>()

    override fun getHeaders(): MutableMap<String, String> {
        return mHeaders
    }

    fun setHeader(name: String, value: String) {
        mHeaders[name] = value
    }

    fun setParams(params: MutableMap<String, String>) {
        mParams.putAll(params)
    }

    override fun getParams(): MutableMap<String, String> {
        return mParams
    }

    // Modifiez cette méthode selon vos besoins
    override fun getBodyContentType(): String {
        // Pour une charge utile en JSON:
        return "application/json; charset=utf-8"
        // Pour une charge utile x-www-form-urlencoded:
        // return "application/x-www-form-urlencoded; charset=UTF-8"
    }

    override fun getBody(): ByteArray {
        val params = getParams()
        val fileData = params["file_data"]

        return if (fileData != null) {
            val imageBase64String = Base64.encodeToString(fileData.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            params["file_data"] = imageBase64String
            val jsonObject = JSONObject(params as Map<*, *>?)
            jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
        } else {
            // Retourner un corps vide ou gérer l'absence de données comme nécessaire
            byteArrayOf()
        }
    }


    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }
}
