package com.example.monsuivicrypto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import okhttp3.Response
import java.io.IOException

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val prenomEditText: EditText = findViewById(R.id.name)
        val nomEditText: EditText = findViewById(R.id.surname)
        val datenaissanceEditText: EditText = findViewById(R.id.birthdate)
        val emailEditText: EditText = findViewById(R.id.email)
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val submitButton: Button = findViewById(R.id.submit_signup)

        submitButton.setOnClickListener {
            val prenom = prenomEditText.text.toString()
            val nom = nomEditText.text.toString()
            val datenaissance = datenaissanceEditText.text.toString()
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val motdepasse = passwordEditText.text.toString()

            sendDataToServer(prenom, nom, datenaissance, email, username, motdepasse)
            showSignupConfirmationDialog(username)
        }


        val returnToMainButton: Button = findViewById(R.id.returntomain_button)
        returnToMainButton.setOnClickListener {
            finish()
        }
    }

    private fun sendDataToServer(prenom: String, nom: String, datenaissance: String, email: String, username: String, motdepasse: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("prenom", prenom)
            .add("nom", nom)
            .add("datenaissance", datenaissance)
            .add("email", email)
            .add("username", username)
            .add("motdepasse", motdepasse)
            .build()

        val request = OkHttpRequest.Builder()
            .url("http://10.0.2.2/api/api.php/adduser")
            .addHeader("Header-Name", "Header-Value")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("SignupActivity", "Error: ${e.message}")
            }
        })
    }

    private fun showSignupConfirmationDialog(username: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Votre compte $username a bien été créé !")
        builder.setPositiveButton("OK") { _, _ ->
            val intent = Intent(this@SignupActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}
