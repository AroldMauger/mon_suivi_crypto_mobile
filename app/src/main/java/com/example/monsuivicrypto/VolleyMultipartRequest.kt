package com.example.monsuivicrypto

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException

open class VolleyMultipartRequest(method: Int, url: String, private val mListener: Response.Listener<NetworkResponse>, private val mErrorListener: Response.ErrorListener) : Request<NetworkResponse>(method, url, mErrorListener) {

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

    @Throws(AuthFailureError::class)
    override fun getBodyContentType(): String {
        return "multipart/form-data; charset=utf-8"
    }

    override fun getBody(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)
        try {
            for ((key, value) in getByteData()) {
                buildPart(dataOutputStream, key, value)
            }
            dataOutputStream.flush()
            return byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return super.getBody()
    }

    open fun getByteData(): MutableMap<String, DataPart> {
        return mutableMapOf()
    }

    @Throws(IOException::class)
    private fun buildPart(dataOutputStream: DataOutputStream, key: String, dataFile: DataPart) {
        dataOutputStream.writeBytes("--$BOUNDARY\r\n")
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$key\"; filename=\"${dataFile.fileName}\"\r\n")
        if (dataFile.type != null && !dataFile.type!!.isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: ${dataFile.type}\r\n")
        }
        dataOutputStream.writeBytes("\r\n")
        val fileInputStream = ByteArrayInputStream(dataFile.content)
        var bytesAvailable = fileInputStream.available()
        val maxBufferSize = 1024 * 1024
        var bufferSize = Math.min(bytesAvailable, maxBufferSize)
        val buffer = ByteArray(bufferSize)
        var bytesRead = fileInputStream.read(buffer, 0, bufferSize)
        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize)
            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
        }
        dataOutputStream.writeBytes("\r\n")
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

    class DataPart {
        var fileName: String? = null
        var content: ByteArray
        var type: String? = null

        constructor(name: String, data: ByteArray) {
            fileName = name
            content = data
        }

        constructor(name: String, data: ByteArray, mimeType: String) {
            fileName = name
            content = data
            type = mimeType
        }
    }

    companion object {
        private const val BOUNDARY = "VolleyMultipartRequestBoundary"
    }
}
