package com.example.monsuivicrypto.data

data class CryptoResponse(
    val id : String,
    val name: String,
    val symbol: String,
    val image: String,
    val current_price: Float,
    val price_change_percentage_24h: Float,
    var isFavorite: Boolean = false,
    val market_cap_rank: String,
    val market_cap: String,
    val circulating_supply: String,
    val last_updated: String
)
/*


)
*/