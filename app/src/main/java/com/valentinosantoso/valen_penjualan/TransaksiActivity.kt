package com.valentinosantoso.valen_penjualan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransaksiActivity : AppCompatActivity() {

    private lateinit var spinnerCabang: Spinner
    private val cabangList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        spinnerCabang = findViewById<Spinner>(R.id.spinnerCabang)
        
        adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, cabangList
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val tv = view as android.widget.TextView
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.customTextColorPrimary, typedValue, true)
                tv.setTextColor(typedValue.data)
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as android.widget.TextView
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.customTextColorPrimary, typedValue, true)
                tv.setTextColor(typedValue.data)
                
                val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                tv.setBackgroundColor(if (isDarkMode) android.graphics.Color.parseColor("#1A1A1E") else android.graphics.Color.WHITE)
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = adapter
        
        loadCabangData()
    }

    private fun loadCabangData() {
        val database = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("cabang")
        
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val nama = data.child("namaCabang").getValue(String::class.java) ?: ""
                        val status = data.child("statusAktif").getValue(Boolean::class.java) ?: false
                        if (status && nama.isNotEmpty()) {
                            cabangList.add(nama)
                        }
                    }
                }
                if (cabangList.isEmpty()) {
                    cabangList.add("Tidak ada cabang aktif")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani error jika perlu
            }
        })
    }
}
