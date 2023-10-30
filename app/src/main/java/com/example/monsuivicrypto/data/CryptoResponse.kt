package com.example.monsuivicrypto.data

data class CryptoResponse(
    val name: String,
    val symbol: String,
    val image: String,
    val current_price: Float,
    val price_change_percentage_24h: Float,
    var isFavorite: Boolean = false,

)

