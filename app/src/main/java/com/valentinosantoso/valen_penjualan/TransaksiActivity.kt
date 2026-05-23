package com.valentinosantoso.valen_penjualan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.produk.ModelProduk

class TransaksiActivity : AppCompatActivity(), AdapterTransaksiProduk.OnQuantityChangeListener {

    private lateinit var spinnerCabang: Spinner
    private val cabangList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var rvProduk: RecyclerView
    private lateinit var produkAdapter: AdapterTransaksiProduk
    private val allProdukList = ArrayList<ModelProduk>()
    private val filteredProdukList = ArrayList<ModelProduk>()

    private lateinit var tvTotalItem: TextView
    private lateinit var tvTotalPrice: TextView

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
        
        spinnerCabang.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterProdukByCabang()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        tvTotalItem = findViewById(R.id.tvTotalItem)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)

        rvProduk = findViewById(R.id.recyclerViewTransaksi)
        rvProduk.layoutManager = LinearLayoutManager(this)
        produkAdapter = AdapterTransaksiProduk(filteredProdukList, this)
        rvProduk.adapter = produkAdapter
        
        loadCabangData()
        loadProdukData()
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

    private fun loadProdukData() {
        val database = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = database.getReference("produk")
        
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProdukList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val status = data.child("statusAktif").getValue(Boolean::class.java) ?: false
                        if (status) {
                            val id = data.child("idProduk").getValue(String::class.java) ?: ""
                            val nama = data.child("namaProduk").getValue(String::class.java) ?: ""
                            val harga = data.child("harga").getValue(Double::class.java) ?: 0.0
                            val kategori = data.child("kategori").getValue(String::class.java) ?: ""
                            val cabang = data.child("cabang").getValue(String::class.java) ?: ""
                            val stok = data.child("stok").getValue(Int::class.java) ?: 0
                            val stokTakTerbatas = data.child("stokTakTerbatas").getValue(Boolean::class.java) ?: false
                            val fotoUrl = data.child("fotoUrl").getValue(String::class.java) ?: ""
                            
                            val produk = ModelProduk(id, nama, harga, kategori, cabang, stok, stokTakTerbatas, status, fotoUrl)
                            allProdukList.add(produk)
                        }
                    }
                }
                filterProdukByCabang()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterProdukByCabang() {
        val selectedCabang = spinnerCabang.selectedItem as? String ?: return
        filteredProdukList.clear()
        
        if (selectedCabang == "Tidak ada cabang aktif") {
            produkAdapter.updateData(filteredProdukList)
            return
        }

        for (produk in allProdukList) {
            val cabangList = produk.cabang.split(",").map { it.trim() }
            if (cabangList.contains(selectedCabang)) {
                filteredProdukList.add(produk)
            }
        }
        produkAdapter.updateData(filteredProdukList)
    }

    override fun onQuantityChanged(totalPrice: Double, totalItems: Int) {
        tvTotalItem.text = "$totalItems Item"
        tvTotalPrice.text = "Rp${String.format("%,.0f", totalPrice)}"
    }
}
