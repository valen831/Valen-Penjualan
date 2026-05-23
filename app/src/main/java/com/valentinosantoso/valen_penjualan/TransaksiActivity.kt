package com.valentinosantoso.valen_penjualan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class TransaksiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val spinnerCabang = findViewById<Spinner>(R.id.spinnerCabang)
        // Dummy data for Cabang
        val cabangList = arrayOf("Cabang Solo", "Cabang Jakarta", "Cabang Surabaya")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cabangList)
        spinnerCabang.adapter = adapter
    }
}
