package com.valentinosantoso.valen_penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val DB_URL = "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val nama = binding.etRegNama.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi semua field terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Sedang mendaftarkan akun...", Toast.LENGTH_SHORT).show()

            val dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("Users")
            dbRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(this@RegisterActivity, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val userId = dbRef.push().key ?: ""
                        val user = mapOf(
                            "id" to userId,
                            "nama" to nama,
                            "email" to email,
                            "password" to pass
                        )

                        dbRef.child(userId).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@RegisterActivity, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@RegisterActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}