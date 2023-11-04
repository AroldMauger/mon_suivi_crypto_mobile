package com.example.monsuivicrypto

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.monsuivicrypto.data.CryptoResponse
class FavoriteAdapter(private val favoritesList: MutableList<CryptoResponse>, private val onDeleteClickListener: (Int) -> Unit) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.favoriteName)
        val priceTextView: TextView = itemView.findViewById(R.id.favoritePrice)
        val percentTextView: TextView = itemView.findViewById(R.id.favoritePercent)
        val deleteButton: TextView = itemView.findViewById(R.id.deleteFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun getItemCount(): Int = favoritesList.size

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favoriteItem = favoritesList[position]

        holder.nameTextView.text = favoriteItem.name
        holder.priceTextView.text = String.format("%.2f", favoriteItem.current_price) + "€"

        val percentageChangeText = if (favoriteItem.price_change_percentage_24h < 0) {
            String.format("%.2f%%", favoriteItem.price_change_percentage_24h)
        } else {
            String.format("+%.2f%%", favoriteItem.price_change_percentage_24h)
        }
        holder.percentTextView.text = percentageChangeText

        val percentColor = if (favoriteItem.price_change_percentage_24h < 0) {
            Color.RED
        } else {
            Color.GREEN
        }
        holder.percentTextView.setTextColor(percentColor)


        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(position)
        }
    }

    fun removeItem(position: Int) {
        favoritesList.removeAt(position)
        notifyItemRemoved(position)
    }
}

