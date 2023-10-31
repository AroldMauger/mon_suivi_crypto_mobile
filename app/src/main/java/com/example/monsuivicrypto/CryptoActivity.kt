package com.example.monsuivicrypto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.bumptech.glide.Glide
import com.example.monsuivicrypto.api.ApiManager.api
import com.example.monsuivicrypto.api.OnFavoriteClickListener
import com.example.monsuivicrypto.api.CryptoAdapter
import com.example.monsuivicrypto.data.CryptoResponse
import org.json.JSONArray
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
    private lateinit var favoritesRecyclerView: RecyclerView
    private var selectedCrypto: CryptoResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crypto)
        requestQueue = Volley.newRequestQueue(this)

        initViews()
        updateUIFromIntent()
        fetchCryptoData()
        loadFavorites()
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
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
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
                            selectedCrypto = crypto
                        }
                        override fun onCryptoItemClick(crypto: CryptoResponse) {
                            // Traitement lorsque le nom ou le symbole de la crypto est cliqué
                            showCryptoModal(crypto)
                        }
                    })
                    recyclerView.adapter = adapter
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
        val url = "http://10.0.2.2/api/api.php/update"

        val params = JSONObject()
        params.put("user_id", userId.toString())
        params.put("username", username)
        params.put("email", email)
        params.put("datenaissance", date)

        val jsonObjectRequest = object : JsonObjectRequest(
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
        ) {}

        requestQueue.add(jsonObjectRequest)
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
                    loadFavorites()
                    favoritesRecyclerView.visibility = View.VISIBLE

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

    private fun loadFavorites() {
        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Log.e("loadFavorites", "Invalid userId")
            return
        }

        val url = "http://10.0.2.2/api/api.php/getfavorites"
        Log.d("loadFavorites", "Request URL: $url")

        val params = JSONObject()
        params.put("userId", userId)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
                Log.d("loadFavorites", "Received response: $response")

                val favoritesList = mutableListOf<CryptoResponse>()

                for (i in 1..3) {
                    val favKey = "fav$i"
                    if (response.has(favKey)) {
                        val item = response.get(favKey)
                        if (item is JSONObject) {
                            Log.d("loadFavorites", "Processing $favKey: $item")

                            val cryptoFavorite = CryptoResponse(
                                symbol = "",
                                name = item.optString("name", ""),
                                image = "",
                                current_price = item.optDouble("price", 0.0).toFloat(),
                                price_change_percentage_24h = item.optDouble("percent", 0.0).toFloat()
                            )

                            favoritesList.add(cryptoFavorite)
                        } else {
                            Log.d("loadFavorites", "$favKey is not a JSONObject: $item")
                        }
                    }
                }

                if (favoritesList.isNotEmpty()) {
                    Log.d("loadFavorites", "Setting up RecyclerView with favorites list")
                    favoritesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    favoritesRecyclerView.adapter = FavoriteAdapter(favoritesList) { position ->
                        val cryptoToRemove = favoritesList[position]
                        deleteFavoriteFromAPI(userId, cryptoToRemove.name)
                    }
                } else {
                    Log.d("loadFavorites", "No favorites found")
                    favoritesRecyclerView.visibility = View.GONE
                }


            },
            { error ->
                Log.e("ServerError", "Error fetching favorites: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }





    private fun deleteFavoriteFromAPI(userId: Int, cryptoName: String) {
        val url = "http://10.0.2.2/api/api.php/deletefavorite"

        val params = JSONObject()
        params.put("userId", userId)
        params.put("fav", cryptoName)

        Log.d("DEBUG_DELETE", "Envoi de la requête de suppression pour l'ID utilisateur: $userId et la cryptomonnaie: $cryptoName")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
                val success = response.getBoolean("success")
                if (success) {
                    Log.d("DEBUG_DELETE", "Suppression réussie pour l'ID utilisateur: $userId et la cryptomonnaie: $cryptoName")
                    showToast("La cryptomonnaie a été supprimée des favoris.")
                    loadFavorites()
                } else {
                    val message = response.getString("message")
                    Log.e("DEBUG_DELETE", "Erreur lors de la suppression: $message")
                    showToast("Erreur lors de la suppression des favoris: $message")
                }
            },
            { error ->
                Log.e("DEBUG_DELETE", "Erreur lors de l'envoi de la requête: ${error.localizedMessage}", error)
                showToast("Erreur lors de la suppression des favoris. Veuillez réessayer.")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun confirmLogout(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Êtes-vous sûr de vouloir vous déconnecter?")
            .setPositiveButton("Oui") { dialog, _ ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun showCryptoModal(crypto: CryptoResponse?) {
        val modalView = LayoutInflater.from(this).inflate(R.layout.crypto_modal_layout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(modalView)
            .create()

        val cryptoImageView = modalView.findViewById<ImageView>(R.id.imageModal)
        cryptoImageView.setImageResource(R.drawable.avatar)

        val nameModal = modalView.findViewById<TextView>(R.id.nameModal)
     //   val chart = modalView.findViewById<LineChart>(R.id.chart)
        val priceModal = modalView.findViewById<TextView>(R.id.priceModal)
        val percentModal = modalView.findViewById<TextView>(R.id.percentModal)
        val rankModal = modalView.findViewById<TextView>(R.id.rankModal)
        val quantityModal = modalView.findViewById<TextView>(R.id.quantityModal)
        val updateModal = modalView.findViewById<TextView>(R.id.updateModal)
        val closeButton = modalView.findViewById<TextView>(R.id.closeModal)

        crypto?.let { selectedCrypto ->
            Glide.with(this)
                .load(selectedCrypto.image) // Remplacez selectedCrypto.image par l'URL de l'image
               // .placeholder(R.drawable.placeholder_image) // Image de substitution
                //.error(R.drawable.error_image) // Image à afficher en cas d'erreur de chargement
                .into(cryptoImageView)

            nameModal.text = selectedCrypto.name

            // Affichez le prix, le pourcentage de changement, le rang et d'autres données
            priceModal.text = "Valeur en euros : ${selectedCrypto.current_price} €"
            percentModal.text = "Variation du prix en % depuis 24H : ${selectedCrypto.price_change_percentage_24h}%"
          //  rankModal.text = "Classement par capitalisation boursière : N°${selectedCrypto.market_cap_rank}"
          //  quantityModal.text = "Quantité en circulation : ${selectedCrypto.circulating_supply}"
          //  updateModal.text = "Dernière actualisation : ${selectedCrypto.last_updated}"


            // Configurez le graphique de prix (utilisez selectedCrypto pour obtenir les données de prix et d'horodatage)
          //  configurePriceChart(chart, selectedCrypto)

            closeButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

/*
    private fun configurePriceChart(chart: LineChart, crypto: CryptoResponse) {
        // Créez un ArrayList d'Entry pour stocker les données de prix
        val entries = ArrayList<Entry>()

        // Remplissez les données de prix en utilisant les données de votre CryptoResponse
        val prices = crypto.sparkline_in_7d.price
        for (i in prices.indices) {
            entries.add(Entry(i.toFloat(), prices[i]))
        }

        // Créez un ensemble de données de ligne avec vos données
        val dataSet = LineDataSet(entries, "Prix en EUR sur 7 jours")

        // Personnalisez l'apparence de la ligne
        //  dataSet.color = getColor(R.color.chartLineColor)
        dataSet.setDrawValues(false)
        dataSet.setDrawFilled(true)
        // dataSet.fillDrawable = getDrawable(R.drawable.chart_fill_color)
        //  dataSet.setCircleColor(getColor(R.color.chartCircleColor))

        // Créez un objet LineData avec le dataSet
        val lineData = LineData(dataSet)

        // Configurez la description du graphique
        val description = Description()
        description.text = "Prix en EUR sur 7 jours"
        chart.description = description

        // Configurez l'axe X
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Définissez le graphique de données
        chart.data = lineData

        // Rafraîchissez le graphique pour qu'il soit visible
        chart.invalidate()
    }
    */

}