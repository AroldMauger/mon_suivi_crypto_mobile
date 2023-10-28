package com.example.monsuivicrypto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.monsuivicrypto.data.CryptoResponse
import java.util.Locale

class CryptoAdapter(private val cryptoList: List<CryptoResponse>) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cryptoImageView: ImageView = itemView.findViewById(R.id.cryptoImage)
        val cryptoSymbolTextView: TextView = itemView.findViewById(R.id.cryptoSymbol)
        val cryptoNameTextView: TextView = itemView.findViewById(R.id.cryptoName)
        val cryptoPriceTextView: TextView = itemView.findViewById(R.id.cryptoPrice)
        val cryptoPercentTextView: TextView = itemView.findViewById(R.id.cryptoPercent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_crypto, parent, false)
        return CryptoViewHolder(itemView)
    }

    // On associe les données à la View
    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val currentItem = cryptoList[position]
        Glide.with(holder.itemView.context)
            .load(currentItem.image)
            .into(holder.cryptoImageView)
        holder.cryptoSymbolTextView.text = currentItem.symbol.uppercase(Locale.ROOT)
        holder.cryptoNameTextView.text = currentItem.name
        holder.cryptoPriceTextView.text = currentItem.current_price.toString()
        holder.cryptoPercentTextView.text = currentItem.price_change_percentage_24h.toString()
    }

    override fun getItemCount() = cryptoList.size
}
