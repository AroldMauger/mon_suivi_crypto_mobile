package com.example.monsuivicrypto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.monsuivicrypto.data.CryptoResponse

class FavoriteAdapter(private val favorites: List<CryptoResponse>) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.favoriteName)
        val price: TextView = itemView.findViewById(R.id.favoritePrice)
        val percent: TextView = itemView.findViewById(R.id.favoritePercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentItem = favorites[position]
        holder.name.text = currentItem.name
        holder.price.text = currentItem.current_price.toString()
        holder.percent.text = currentItem.price_change_percentage_24h.toString()
    }

    override fun getItemCount() = favorites.size
}
