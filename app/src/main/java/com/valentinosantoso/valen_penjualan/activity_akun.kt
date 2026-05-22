package com.valentinosantoso.valen_penjualan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AkunActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        val tvNama = findViewById<TextView>(R.id.tvNamaAkun)
        val tvEmail = findViewById<TextView>(R.id.tvEmailAkun)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        tvNama.text = sharedPref.getString("nama", "-")
        tvEmail.text = sharedPref.getString("email", "-")

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah kamu yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    sharedPref.edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }
}