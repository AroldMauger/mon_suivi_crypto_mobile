package com.example.monsuivicrypto.api

import com.example.monsuivicrypto.data.CryptoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/markets")
    fun getMarkets(@Query("vs_currency") currency: String): Call<List<CryptoResponse>>
}
