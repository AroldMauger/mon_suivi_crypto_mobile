package com.example.monsuivicrypto.api

import com.example.monsuivicrypto.data.CryptoResponse
import com.example.monsuivicrypto.data.MarketChartResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/markets")
    fun getMarkets(@Query("vs_currency") currency: String): Call<List<CryptoResponse>>


    @GET("coins/{id}/market_chart")
    fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: Int
    ): Call<MarketChartResponse>
}
