package com.valentinosantoso.valen_penjualan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPref: SharedPreferences

    private val DB_URL = "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Cek apakah sudah login
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi email dan password terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Sedang memeriksa akun...", Toast.LENGTH_SHORT).show()

            val db = FirebaseDatabase.getInstance(DB_URL).getReference("Users")
            db.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(this@LoginActivity, "Akun tidak ditemukan!", Toast.LENGTH_SHORT).show()
                            return
                        }

                        var passwordCocok = false
                        var namaUser = ""
                        var emailUser = ""

                        for (userSnapshot in snapshot.children) {
                            val passwordDb = userSnapshot.child("password").getValue(String::class.java)
                            if (passwordDb == pass) {
                                passwordCocok = true
                                namaUser = userSnapshot.child("nama").getValue(String::class.java) ?: ""
                                emailUser = userSnapshot.child("email").getValue(String::class.java) ?: ""
                                break
                            }
                        }

                        if (passwordCocok) {
                            // Simpan session
                            sharedPref.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putString("nama", namaUser)
                                putString("email", emailUser)
                                apply()
                            }
                            Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Password salah!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}