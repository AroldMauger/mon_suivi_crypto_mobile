package com.example.monsuivicrypto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestQueue = Volley.newRequestQueue(this)

        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.motdepasse)
        val signupButton: Button = findViewById(R.id.signup_button)
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val motdepasse = passwordEditText.text.toString()
            loginUser(username, motdepasse)
        }
    }

    fun loginUser(username: String, motdepasse: String) {
        if (username == "admincrypto" && motdepasse == "Studi20@") {
            val intent = Intent(this@MainActivity, AdminActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        val url = "https://mon-suivi-crypto.alwaysdata.net/api/api.php/login"

        val stringRequest = object: StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("ServerResponse", response)
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    if (success) {
                        val userId = jsonResponse.getInt("user_id")
                        val userInfo = jsonResponse.getJSONObject("user_info")
                        val email = userInfo.getString("email")
                        val dateOfBirth = userInfo.getString("datenaissance")
                        val fav1 = userInfo.optString("fav1", null)
                        val fav2 = userInfo.optString("fav2", null)
                        val fav3 = userInfo.optString("fav3", null)
                        val photo = userInfo.optString("photo", null)
                        val photoUrl = if (photo != null) "https://mon-suivi-crypto.alwaysdata.net/$photo" else null

                        val intent = Intent(this@MainActivity, CryptoActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("USER_INFO_EMAIL", email)
                        intent.putExtra("USER_INFO_DATE_OF_BIRTH", dateOfBirth)
                        intent.putExtra("FAV1", fav1)
                        intent.putExtra("FAV2", fav2)
                        intent.putExtra("FAV3", fav3)
                        intent.putExtra("PHOTO", photoUrl)

                        startActivity(intent)
                        finish()

                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Erreur lors de l'analyse de la réponse", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["motdepasse"] = motdepasse
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
