package com.example.monsuivicrypto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monsuivicrypto.api.ApiManager.api
import com.example.monsuivicrypto.data.CryptoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CryptoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crypto)

        // Appel API
        fetchCryptoData()
    }

    private fun fetchCryptoData() {
        api.getMarkets("eur").enqueue(object: Callback<List<CryptoResponse>> {
            override fun onResponse(
                call: Call<List<CryptoResponse>>,
                response: Response<List<CryptoResponse>>
            ) {
                if (response.isSuccessful) {
                    val cryptoList = response.body()!!
                    val recyclerView = findViewById<RecyclerView>(R.id.cryptoRecyclerView)

                    // On définit le LayoutManager et l'Adapter pour le RecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(this@CryptoActivity)
                    recyclerView.adapter = CryptoAdapter(cryptoList)

                    // On retire le défilement automatique
                    recyclerView.isNestedScrollingEnabled = false
                }
            }

            override fun onFailure(call: Call<List<CryptoResponse>>, t: Throwable) {

            }
        })
    }
}
