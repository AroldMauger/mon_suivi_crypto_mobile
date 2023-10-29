package com.example.monsuivicrypto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        fetchUsers()

        val returnToMain: TextView = findViewById(R.id.returnToMain)
        returnToMain.setOnClickListener {
            logoutAndReturnToMain()
        }
    }

    private fun logoutAndReturnToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchUsers() {
        val url = "http://10.0.2.2/api/api.php/admin"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener<JSONArray> { response ->
                displayUsers(response)
            },
            Response.ErrorListener { error ->
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun displayUsers(users: JSONArray) {
        val userListLayout: LinearLayout = findViewById(R.id.userList)
        userListLayout.removeAllViews()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            val view = LayoutInflater.from(this).inflate(R.layout.user_item, null)

            view.findViewById<TextView>(R.id.admin_username).text = "Utilisateur: ${user.getString("username")}"
            view.findViewById<TextView>(R.id.admin_email).text = "Email: ${user.getString("email")}"
            view.findViewById<TextView>(R.id.admin_datenaissance).text = "Date de naissance: ${user.getString("datenaissance")}"

            val deleteButton: Button = view.findViewById(R.id.deleteButton)
            deleteButton.setOnClickListener {
                deleteUser(user.getString("id"))
            }

            userListLayout.addView(view)
        }
    }

    private fun deleteUser(userId: String) {
        val url = "http://10.0.2.2/api/api.php/admin_deleteuser"

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getBoolean("success")) {
                    Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()

                    fetchUsers()
                } else {
                    Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}
