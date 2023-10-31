package com.example.monsuivicrypto.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.monsuivicrypto.R
import com.example.monsuivicrypto.data.CryptoResponse
import com.example.monsuivicrypto.data.FavoriteItem
import java.util.Locale

interface OnFavoriteClickListener {
    fun onFavoriteClick(symbol: String, isFavorite: Boolean, crypto: CryptoResponse)
    fun onCryptoItemClick(crypto: CryptoResponse)
}

class CryptoAdapter(
    private val itemList: List<Any>, // Peut être soit CryptoResponse soit FavoriteItem
    private val favoriteClickListener: OnFavoriteClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val favorites = mutableSetOf<String>()

    companion object {
        private const val ITEM_CRYPTO = 0
        private const val ITEM_FAVORITE = 1
    }

    class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cryptoImageView: ImageView = itemView.findViewById(R.id.cryptoImage)
        val cryptoSymbolTextView: TextView = itemView.findViewById(R.id.cryptoSymbol)
        val cryptoNameTextView: TextView = itemView.findViewById(R.id.cryptoName)
        val cryptoPriceTextView: TextView = itemView.findViewById(R.id.cryptoPrice)
        val cryptoPercentTextView: TextView = itemView.findViewById(R.id.cryptoPercent)
        val heartTextView: TextView = itemView.findViewById(R.id.heart)
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteNameTextView: TextView = itemView.findViewById(R.id.favoriteName)
        val favoritePriceTextView: TextView = itemView.findViewById(R.id.favoritePrice)
        val favoritePercentTextView: TextView = itemView.findViewById(R.id.favoritePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_CRYPTO -> {
                val view = inflater.inflate(R.layout.item_crypto, parent, false)
                CryptoViewHolder(view)
            }
            ITEM_FAVORITE -> {
                val view = inflater.inflate(R.layout.item_favorite, parent, false)
                FavoriteViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_CRYPTO -> {
                val cryptoHolder = holder as CryptoViewHolder
                val currentItem = itemList[position] as CryptoResponse

                Glide.with(cryptoHolder.itemView.context)
                    .load(currentItem.image)
                    .into(cryptoHolder.cryptoImageView)

                cryptoHolder.cryptoSymbolTextView.text = currentItem.symbol.uppercase(Locale.ROOT)
                cryptoHolder.cryptoNameTextView.text = currentItem.name
                cryptoHolder.cryptoPriceTextView.text = currentItem.current_price.toString()
                cryptoHolder.cryptoPercentTextView.text = currentItem.price_change_percentage_24h.toString()

                if (favorites.contains(currentItem.symbol)) {
                    cryptoHolder.heartTextView.setTypeface(ResourcesCompat.getFont(cryptoHolder.itemView.context, R.font.fa_solid900))
                    cryptoHolder.heartTextView.setTextColor(ResourcesCompat.getColor(cryptoHolder.itemView.context.resources, R.color.black, null))
                    currentItem.isFavorite = true
                } else {
                    cryptoHolder.heartTextView.setTypeface(ResourcesCompat.getFont(cryptoHolder.itemView.context, R.font.fa_regular400))
                    cryptoHolder.heartTextView.setTextColor(ResourcesCompat.getColor(cryptoHolder.itemView.context.resources, R.color.yellow, null))
                    currentItem.isFavorite = false
                }

                cryptoHolder.heartTextView.setOnClickListener {
                    if (currentItem.isFavorite) {
                        favorites.remove(currentItem.symbol)
                        cryptoHolder.heartTextView.setTypeface(ResourcesCompat.getFont(cryptoHolder.itemView.context, R.font.fa_regular400))
                        cryptoHolder.heartTextView.setTextColor(ResourcesCompat.getColor(cryptoHolder.itemView.context.resources, R.color.yellow, null))
                        currentItem.isFavorite = false
                    } else {
                        favorites.add(currentItem.symbol)
                        cryptoHolder.heartTextView.setTypeface(ResourcesCompat.getFont(cryptoHolder.itemView.context, R.font.fa_solid900))
                        cryptoHolder.heartTextView.setTextColor(ResourcesCompat.getColor(cryptoHolder.itemView.context.resources, R.color.black, null))
                        currentItem.isFavorite = true
                    }
                    favoriteClickListener.onFavoriteClick(currentItem.symbol, currentItem.isFavorite, currentItem)
                }

                // Ajoutez un OnClickListener au nom de la crypto
                cryptoHolder.cryptoNameTextView.setOnClickListener {
                    favoriteClickListener.onCryptoItemClick(currentItem)
                }

                // Ajoutez un OnClickListener au symbole de la crypto
                cryptoHolder.cryptoSymbolTextView.setOnClickListener {
                    favoriteClickListener.onCryptoItemClick(currentItem)
                }
            }
            ITEM_FAVORITE -> {
                val favoriteHolder = holder as FavoriteViewHolder
                val currentItem = itemList[position] as FavoriteItem

                // Remplissez les éléments de type favori ici
                favoriteHolder.favoriteNameTextView.text = currentItem.favoriteName
                favoriteHolder.favoritePriceTextView.text = currentItem.favoritePrice
                favoriteHolder.favoritePercentTextView.text = currentItem.favoritePercent
            }
        }
    }

    override fun getItemCount() = itemList.size

    override fun getItemViewType(position: Int): Int {
        val item = itemList[position]
        return when (item) {
            is CryptoResponse -> ITEM_CRYPTO
            is FavoriteItem -> ITEM_FAVORITE
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }
}
