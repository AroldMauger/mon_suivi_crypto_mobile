package com.example.monsuivicrypto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.monsuivicrypto.api.ApiManager.api
import com.example.monsuivicrypto.api.OnFavoriteClickListener
import com.example.monsuivicrypto.api.CryptoAdapter
import com.example.monsuivicrypto.data.CryptoResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse

class CryptoActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crypto)
        requestQueue = Volley.newRequestQueue(this)

        initViews()
        updateUIFromIntent()
        fetchCryptoData()
       // loadFavorites()
        findViewById<Button>(R.id.deleteProfileButton).setOnClickListener {
            showDeleteConfirmationDialog()
        }
        findViewById<Button>(R.id.updateProfile).setOnClickListener {
            showUpdateProfileDialog()
        }
    }

    private fun initViews() {
        usernameTextView = findViewById(R.id.username)
        emailTextView = findViewById(R.id.email)
        dateTextView = findViewById(R.id.date)
        recyclerView = findViewById(R.id.cryptoRecyclerView)
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
        api.getMarkets("eur").enqueue(object : Callback<List<CryptoResponse>> {
            override fun onResponse(
                call: Call<List<CryptoResponse>>,
                response: RetrofitResponse<List<CryptoResponse>>
            ) {
                if (response.isSuccessful) {
                    val cryptoList = response.body()!!
                    recyclerView.layoutManager = LinearLayoutManager(this@CryptoActivity)

                    val adapter = CryptoAdapter(cryptoList, object : OnFavoriteClickListener {
                        override fun onFavoriteClick(symbol: String, isFavorite: Boolean, crypto: CryptoResponse) {
                            if (isFavorite) {
                                addToFavoritesAPI(crypto)
                            }
                        }
                    })
                    recyclerView.adapter = adapter
                    recyclerView.isNestedScrollingEnabled = false
                }
            }

            override fun onFailure(call: Call<List<CryptoResponse>>, t: Throwable) {
                // Handle failure
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

        requestQueue.add(stringRequest)
    }

    private fun logoutAndReturnToMain() {
        val intent = Intent(this@CryptoActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showUpdateProfileDialog() {
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_profile, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val usernameEditText = dialogView.findViewById<EditText>(R.id.dialogUsername)
        val emailEditText = dialogView.findViewById<EditText>(R.id.dialogEmail)
        val dateEditText = dialogView.findViewById<EditText>(R.id.dialogDate)

        usernameEditText.setText(usernameTextView.text)
        emailEditText.setText(emailTextView.text)
        dateEditText.setText(dateTextView.text)


        builder.setTitle("Mettre à jour le profil")
        builder.setPositiveButton("Mettre à jour") { _, _ ->
            val newUsername = usernameEditText.text.toString()
            val newEmail = emailEditText.text.toString()
            val newDate = dateEditText.text.toString()

            updateProfileAPI(newUsername, newEmail, newDate)
        }
        builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun updateProfileAPI(username: String, email: String, date: String) {
        val userId = intent.getIntExtra("USER_ID", -1)
        Log.d("DEBUG_USER_ID", "User ID: $userId")
        val url = "http://10.0.2.2/api/api.php/update"

        val params = JSONObject()
        params.put("user_id", userId.toString())
        params.put("username", username)
        params.put("email", email)
        params.put("datenaissance", date)


        val jsonObjectRequest  = object : JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
                Log.d("ServerResponse", "Response after update: $response")
                usernameTextView.text = username
                emailTextView.text = email
                dateTextView.text = date
            },
            Response.ErrorListener { error ->
                Log.e("ServerError", "Error during update: ${error.message}")
            }
        ) {

        }
        Log.d("DEBUG_PARAMS", "Params: $params")
        requestQueue.add(jsonObjectRequest )
    }

    private fun addToFavoritesAPI(crypto: CryptoResponse) {
        val userId = intent.getIntExtra("USER_ID", -1)
        val url = "http://10.0.2.2/api/api.php/addtofavorites"

        val params = JSONObject()
        params.put("cryptoName", crypto.name)
        params.put("cryptoPrice", crypto.current_price)
        params.put("cryptoPercent", crypto.price_change_percentage_24h)

        val requestData = JSONObject()
        requestData.put("cryptoData", params)
        requestData.put("userId", userId)

        Log.d("DEBUG_AddToFavorites", "User ID: ${intent.getIntExtra("USER_ID", -1)}")
        Log.d("DEBUG_AddToFavorites", "Sending data to server: $requestData")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestData,
            { response ->
                Log.d("DEBUG_AddToFavorites", "Response received: $response")
                val success = response.getBoolean("success")
                if (success) {
                    Log.d("DEBUG_AddToFavorites", "Cryptocurrency added to favorites successfully.")
                    showToast("La cryptomonnaie a bien été ajoutée aux favoris!")
                } else {
                    val message = response.getString("message")
                    Log.e("DEBUG_AddToFavorites", "Error adding cryptocurrency to favorites: $message")
                    showToast("Erreur lors de l'ajout aux favoris: $message")
                }
            },
            { error ->
                Log.e("ServerError", "Error during adding to favorites: ${error.message}")
                showToast("Erreur lors de l'ajout aux favoris. Veuillez réessayer.")
            }
        )

        Log.d("DEBUG_AddToFavorites", "Sending request to server...")

        requestQueue.add(jsonObjectRequest)

        Log.d("DEBUG_AddToFavorites", "Request sent to server.")
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@CryptoActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
/*
    private fun loadFavorites() {
        // Remplacez ce lien par le bon lien d'API pour récupérer les favoris.
        val url = "http://10.0.2.2/api/api.php/getfavorites"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                // Transformez la réponse en liste de favoris et mettez à jour la liste.
                // Par exemple (en supposant que la réponse est une liste de JSONObjects représentant les cryptos favoris):
                val favoritesJSONArray = response.getJSONArray("favorites")
                val favoritesList = mutableListOf<CryptoResponse>()

                for (i in 0 until favoritesJSONArray.length()) {
                    val item = favoritesJSONArray.getJSONObject(i)
                    val cryptoFavorite = CryptoResponse(
                        symbol = item.getString("symbol"),
                        name = item.getString("name"),
                        image = item.getString("image"),
                        current_price = item.getDouble("current_price").toFloat(),
                        price_change_percentage_24h = item.getDouble("price_change_percentage_24h").toFloat()
                        // Ajoutez d'autres attributs si nécessaire
                    )
                    favoritesList.add(cryptoFavorite)
                }

                favoritesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                favoritesRecyclerView.adapter = CryptoAdapter(favoritesList, object : OnFavoriteClickListener {
                    override fun onFavoriteClick(symbol: String, isFavorite: Boolean, crypto: CryptoResponse) {
                        if (isFavorite) {
                            // Gérer le clic sur un favori si nécessaire
                        }
                    }
                })
            },
            Response.ErrorListener { error ->
                Log.e("ServerError", "Error fetching favorites: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
*/
}
