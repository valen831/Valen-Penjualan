package com.valentinosantoso.valen_penjualan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.valentinosantoso.valen_penjualan.kategori.DataKategoriActivity
import com.valentinosantoso.valen_penjualan.produk.DataProdukActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var greetingText: TextView
    private lateinit var dateText: TextView
    private lateinit var estimateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupGreeting()
        setupDate()
        setupEstimate()
        setupNavigation()
    }

    private fun initViews() {
        greetingText = findViewById(R.id.greetingText)
        dateText = findViewById(R.id.dateText)
        estimateText = findViewById(R.id.estimateText)
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 11 -> "Selamat Pagi"
            hour < 15 -> "Selamat Siang"
            hour < 18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
        greetingText.text = "$greeting, Valen"
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        dateText.text = dateFormat.format(Date())
    }

    private fun setupEstimate() {
        val estimate = 671000
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatter.currency = Currency.getInstance("IDR")
        estimateText.text = formatter.format(estimate).replace("IDR", "Rp")
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.transactionLayout).setOnClickListener {
            // startActivity(Intent(this, TransaksiActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.reportLayout).setOnClickListener {
            // startActivity(Intent(this, LaporanActivity::class.java))
        }

        findViewById<CardView>(R.id.accountCard).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java)) // ✅ Fix
        }

        findViewById<CardView>(R.id.productCard).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }

        findViewById<CardView>(R.id.categoryCard).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        findViewById<CardView>(R.id.employeeCard).setOnClickListener {
            // startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        findViewById<CardView>(R.id.branchCard).setOnClickListener {
            // startActivity(Intent(this, DataCabangActivity::class.java))
        }

        findViewById<CardView>(R.id.printerCard).setOnClickListener {
            // startActivity(Intent(this, DataPrinterActivity::class.java))
        }
    }

    fun toggleTheme() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}