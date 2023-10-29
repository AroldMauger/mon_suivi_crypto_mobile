package com.example.monsuivicrypto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.monsuivicrypto.api.ApiManager.api
import com.example.monsuivicrypto.api.CryptoAdapter
import com.example.monsuivicrypto.data.CryptoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse

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

        findViewById<Button>(R.id.deleteProfileButton).setOnClickListener {
            showDeleteConfirmationDialog()
        }

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
                response: RetrofitResponse<List<CryptoResponse>>
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
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Êtes-vous sûr de vouloir supprimer votre compte?")
        builder.setPositiveButton("Oui") { _, _ ->
            deleteUserAccount()
        }
        builder.setNegativeButton("Non") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteUserAccount() {
        val userId = intent.getIntExtra("USER_ID", -1)
        val url = "http://10.0.2.2/api/api.php/delete"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("ServerResponse", "Response after deletion: $response")
                logoutAndReturnToMain()
            },
            Response.ErrorListener { error ->
                Log.e("ServerError", "Error during deletion: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = intent.getStringExtra("USERNAME")!!
                return params
            }
        }

        Volley.newRequestQueue(this@CryptoActivity).add(stringRequest)
    }

    private fun logoutAndReturnToMain() {
        val intent = Intent(this@CryptoActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
