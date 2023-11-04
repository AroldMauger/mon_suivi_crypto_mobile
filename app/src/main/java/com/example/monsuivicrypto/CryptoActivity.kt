package com.example.monsuivicrypto
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
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
import com.example.monsuivicrypto.data.MarketChartResponse
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.nio.charset.StandardCharsets
import retrofit2.Response as RetrofitResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class CryptoActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var favoritesRecyclerView: RecyclerView
    private var selectedCrypto: CryptoResponse? = null
    private val REQUEST_CODE_IMAGE_PICK = 100
    private val READ_MEDIA_IMAGES = 1002
    private val REQUEST_STORAGE_PERMISSION = 1001
    private var userId: Int = -1
    private lateinit var profileImageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crypto)

        requestQueue = Volley.newRequestQueue(this)
        userId = intent.getIntExtra("USER_ID", -1)
        profileImageView = findViewById(R.id.profile_image_view)
        val photoUrl = intent.getStringExtra("PHOTO")
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(profileImageView)
        }


        initViews()
        updateUIFromIntent()
        fetchCryptoData()
        loadFavorites()
        findViewById<TextView>(R.id.deleteProfileButton).setOnClickListener {
            showDeleteConfirmationDialog()
        }
        findViewById<TextView>(R.id.updateProfile).setOnClickListener {
            showUpdateProfileDialog()
        }
        val changeAvatarButton: TextView = findViewById(R.id.changeAvatarButton)
        changeAvatarButton.setOnClickListener {
            if (hasReadExternalStoragePermission()) {
                openGallery()
            } else {
                requestReadExternalStoragePermission()
            }
        }
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_view)
        val returnToTopButton = findViewById<View>(R.id.returnToTop)
        returnToTopButton.setOnClickListener {
            nestedScrollView.smoothScrollTo(0, 0)
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
                response: retrofit2.Response<List<CryptoResponse>>
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
                                id ="",
                                symbol = "",
                                name = item.optString("name", ""),
                                image = "",
                                current_price = item.optDouble("price", 0.0).toFloat(),
                                price_change_percentage_24h = item.optDouble("percent", 0.0).toFloat(),
                                market_cap = "",
                                circulating_supply = "",
                                last_updated = "",
                                market_cap_rank = ""
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


    @SuppressLint("SetTextI18n")
    private fun showCryptoModal(modal: CryptoResponse?) {
        val modalView = LayoutInflater.from(this).inflate(R.layout.crypto_modal_layout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(modalView)
            .create()

        val cryptoImageView = modalView.findViewById<ImageView>(R.id.imageModal)
        cryptoImageView.setImageResource(R.drawable.avatar)

        val nameModal = modalView.findViewById<TextView>(R.id.nameModal)
        val priceModal = modalView.findViewById<TextView>(R.id.priceModal)
        val percentModal = modalView.findViewById<TextView>(R.id.percentModal)
        val rankModal = modalView.findViewById<TextView>(R.id.rankModal)
        val capitalisationModal = modalView.findViewById<TextView>(R.id.capitalisationModal)
        val quantityModal = modalView.findViewById<TextView>(R.id.quantityModal)
        val updateModal = modalView.findViewById<TextView>(R.id.updateModal)
        val closeButton = modalView.findViewById<TextView>(R.id.closeModal)

        modal?.let { selectedModal ->
            Glide.with(this)
                .load(selectedModal.image)
                .into(cryptoImageView)

            fun formatBold(value: String): SpannableString {
                val spannable = SpannableString(value)
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannable
            }

            fun formatBoldWithColor(value: String, color: Int): SpannableString {
                val spannable = SpannableString(value)
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(color), 0, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannable
            }

            fun formatDate(dateString: String): String {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                return outputFormat.format(date ?: "")
            }

            nameModal.text = selectedModal.name
            priceModal.text = buildSpannedString {
                append("Valeur en euros : ")
                append(formatBold(String.format("%.2f€", selectedModal.current_price)))
            }

            val percentageColor = if (selectedModal.price_change_percentage_24h < 0)
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            else
                ContextCompat.getColor(this, android.R.color.holo_green_dark)

            percentModal.text = buildSpannedString {
                append("Variation du prix en % depuis 24H : ")
                append(formatBoldWithColor(
                    String.format("%.2f%%", selectedModal.price_change_percentage_24h),
                    percentageColor
                ))
            }

            rankModal.text = buildSpannedString {
                append("Classement par capitalisation boursière : N°")
                append(formatBold(selectedModal.market_cap_rank.toString()))
            }

            capitalisationModal.text = buildSpannedString {
                append("Capitalisation boursière : ")
                append(formatBold("${selectedModal.market_cap}€"))
            }

            quantityModal.text = buildSpannedString {
                append("Quantité en circulation : ")
                append(formatBold(selectedModal.circulating_supply.toString()))
            }

            updateModal.text = buildSpannedString {
                append("Dernière actualisation : ")
                append(formatBold(formatDate(selectedModal.last_updated)))
            }

            val graph = modalView.findViewById<GraphView>(R.id.graph)
            val currencyId = selectedModal.id
            fetchGraphData(currencyId, graph)
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun fetchGraphData(currencyId: String, graph: GraphView) {
        val days = 7

        api.getMarketChart(currencyId, "eur", days).enqueue(object : Callback<MarketChartResponse> {
            override fun onResponse(call: Call<MarketChartResponse>, response: RetrofitResponse <MarketChartResponse>) {
                if (response.isSuccessful) {
                    val marketChartResponse = response.body()
                    val series = LineGraphSeries<DataPoint>()
                    marketChartResponse?.prices?.forEach { priceEntry ->
                        val xValue = Date(priceEntry[0].toLong()).time.toDouble()
                        val yValue = priceEntry[1]
                        series.appendData(DataPoint(xValue, yValue), true, marketChartResponse.prices.size)
                    }

                    graph.addSeries(series)

                    graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this@CryptoActivity, SimpleDateFormat("dd/MM"))
                    graph.gridLabelRenderer.textSize = 30f
                    graph.gridLabelRenderer.horizontalAxisTitle = "Date"
                    graph.gridLabelRenderer.verticalAxisTitle = "Prix"
                    graph.gridLabelRenderer.padding = 15

                    graph.gridLabelRenderer.numHorizontalLabels = 7
                    graph.gridLabelRenderer.numVerticalLabels = 5
                    graph.gridLabelRenderer.gridColor = Color.LTGRAY
                    graph.gridLabelRenderer.verticalAxisTitleColor = Color.DKGRAY
                    graph.gridLabelRenderer.horizontalAxisTitleColor = Color.DKGRAY

                    graph.setBackgroundColor(Color.parseColor("#FFE5E5E5"))

                    series.color = Color.BLUE
                    series.thickness = 3
                    series.isDrawDataPoints = true
                    series.dataPointsRadius = 5f
                    series.setDrawDataPoints(true)



                    graph.addSeries(series)

                    val viewport = graph.viewport
                    viewport.isXAxisBoundsManual = true
                    viewport.setMinX(series.lowestValueX - 100000)
                    viewport.setMaxX(series.highestValueX + 100000)
                    viewport.isYAxisBoundsManual = true
                    viewport.setMinY(series.lowestValueY - 10)
                    viewport.setMaxY(series.highestValueY + 10)

                    graph.invalidate()

                }
            }

            override fun onFailure(call: Call<MarketChartResponse>, t: Throwable) {
            }
        })
    }

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            READ_MEDIA_IMAGES
        )
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_MEDIA_IMAGES) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission nécessaire pour accéder à la galerie", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            val filePath = getPathFromUri(uri)
            filePath?.let {
                val file = File(it)
                uploadImageToServer(file)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }


    private fun getPathFromUri(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return it.getString(columnIndex)
        }
        return null
    }
    private fun uploadImageToServer(file: File) {
        val url = "http://10.0.2.2/api/api.php/uploadpicture"
        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Log.e("UploadError", "USER_ID is not passed correctly.")
            return
        }
        Log.d("UploadInfo", "User ID: $userId")

        val imageBytes = file.readBytes()
        val imageBase64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val jsonBody = JSONObject()
        try {
            jsonBody.put("user_id", userId)
            jsonBody.put("profileImage", imageBase64String)
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating JSON body.", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = jsonBody.toString().toByteArray(Charsets.UTF_8)

        val volleyBase64Request = object : VolleyBase64Request(
            Method.POST,
            url,
            Response.Listener { response ->
                // Handle response here
                val responseString = String(response.data, Charsets.UTF_8)
                Log.d("UploadResponse", "Response from server: $responseString")
                try {
                    val jsonResponse = JSONObject(responseString)
                    val success = jsonResponse.getBoolean("success")
                    if (success) {
                        // Ici nous obtenons l'URL de la nouvelle image à partir de la réponse du serveur
                        val newImageUrl = jsonResponse.getString("imageUrl") // Remplacez par la clé réelle utilisée par votre serveur

                        runOnUiThread {
                            // Mettre à jour l'ImageView avec la nouvelle image
                            Glide.with(this@CryptoActivity)
                                .load(newImageUrl) // utilisez la nouvelle URL de l'image
                                .placeholder(R.drawable.avatar) // Remplacez par votre image placeholder
                                .error(R.drawable.avatar) // Remplacez par votre image d'erreur
                                .into(profileImageView)

                            // Afficher le dialogue de confirmation
                            AlertDialog.Builder(this@CryptoActivity).apply {
                                setTitle("Confirmation")
                                setMessage("Votre photo a été modifiée avec succès!")
                                setPositiveButton("Ok") { dialog, which ->
                                    dialog.dismiss()
                                }
                                show()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e("UploadResponse", "Error parsing JSON", e)
                }
            },
            Response.ErrorListener { error ->
                // Handle error here
                error.networkResponse?.let {
                    val errorData = String(it.data, Charsets.UTF_8)
                    Log.e("UploadError", "Error response from server: $errorData")
                }
                Log.e("UploadError", "Volley error: ${error.localizedMessage}")
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody
            }
        }

        // Add the request to Volley's RequestQueue
        Volley.newRequestQueue(this).add(volleyBase64Request)
    }


}
