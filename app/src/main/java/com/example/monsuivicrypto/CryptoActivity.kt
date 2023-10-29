package com.example.monsuivicrypto

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monsuivicrypto.api.ApiManager.api
import com.example.monsuivicrypto.api.CryptoAdapter
import com.example.monsuivicrypto.data.CryptoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CryptoActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crypto)

        initViews()
        updateUIFromIntent()
        fetchCryptoData()
    }

    private fun initViews() {
        usernameTextView = findViewById(R.id.username)
        emailTextView = findViewById(R.id.email)
        dateTextView = findViewById(R.id.date)
    }

    private fun updateUIFromIntent() {
        val username = intent.getStringExtra("USERNAME")
        val userInfoEmail: String? = intent.getStringExtra("USER_INFO_EMAIL")
        val userInfoDateOfBirth: String? = intent.getStringExtra("USER_INFO_DATE_OF_BIRTH")

        usernameTextView.text = username
        emailTextView.text = userInfoEmail
        dateTextView.text = userInfoDateOfBirth
    }

    private fun fetchCryptoData() {
        api.getMarkets("eur").enqueue(object: Callback<List<CryptoResponse>> {
            override fun onResponse(
                call: Call<List<CryptoResponse>>,
                response: Response<List<CryptoResponse>>
            ) {
                if (response.isSuccessful) {
                    val cryptoList = response.body()!!
                    val recyclerView: RecyclerView = findViewById(R.id.cryptoRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@CryptoActivity)
                    recyclerView.adapter = CryptoAdapter(cryptoList)
                    recyclerView.isNestedScrollingEnabled = false
                }
            }

            override fun onFailure(call: Call<List<CryptoResponse>>, t: Throwable) {
            }
        })
    }
}
