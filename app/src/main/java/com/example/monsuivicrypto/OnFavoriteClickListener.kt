package com.example.monsuivicrypto

import com.example.monsuivicrypto.data.CryptoResponse

interface OnFavoriteClickListener {
    fun onFavoriteClick(symbol: String, isFavorite: Boolean, crypto: CryptoResponse)
}

