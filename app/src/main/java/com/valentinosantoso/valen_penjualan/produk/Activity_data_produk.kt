package com.valentinosantoso.valen_penjualan.produk

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.R

class DataProdukActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnRefresh: ImageView
    private lateinit var etSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnTambah: FloatingActionButton
    private lateinit var btnSemua: Button
    private lateinit var btnMakanan: Button
    private lateinit var btnMinuman: Button
    private lateinit var btnSnack: Button
    private lateinit var adapter: AdapterProduk

    private val db = FirebaseDatabase.getInstance(
        "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val originalList = mutableListOf<ModelProduk>()
    private var filterKategori = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_produk)

        initViews()
        setupRecyclerView()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)
        etSearch = findViewById(R.id.etSearch)
        recyclerView = findViewById(R.id.recyclerView)
        btnTambah = findViewById(R.id.btnTambah)
        btnSemua = findViewById(R.id.btnSemua)
        btnMakanan = findViewById(R.id.btnMakanan)
        btnMinuman = findViewById(R.id.btnMinuman)
        btnSnack = findViewById(R.id.btnSnack)
    }

    private fun setupRecyclerView() {
        adapter = AdapterProduk(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadData() {
        db.getReference("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalList.clear()
                for (data in snapshot.children) {
                    val produk = ModelProduk(
                        idProduk = data.child("idProduk").getValue(String::class.java) ?: "",
                        namaProduk = data.child("namaProduk").getValue(String::class.java) ?: "",
                        harga = data.child("harga").getValue(Double::class.java) ?: 0.0,
                        kategori = data.child("kategori").getValue(String::class.java) ?: "",
                        cabang = data.child("cabang").getValue(String::class.java) ?: "",
                        stok = data.child("stok").getValue(Int::class.java) ?: 0,
                        stokTakTerbatas = data.child("stokTakTerbatas").getValue(Boolean::class.java) ?: false,
                        statusAktif = data.child("statusAktif").getValue(Boolean::class.java) ?: true,
                        fotoUrl = data.child("fotoUrl").getValue(String::class.java) ?: ""
                    )
                    originalList.add(produk)
                }
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataProdukActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilter() {
        val keyword = etSearch.text.toString().trim()
        var filtered = if (filterKategori == "Semua") {
            originalList.toList()
        } else {
            originalList.filter { it.kategori.equals(filterKategori, ignoreCase = true) }
        }
        if (keyword.isNotEmpty()) {
            filtered = filtered.filter {
                it.namaProduk.contains(keyword, ignoreCase = true) ||
                        it.kategori.contains(keyword, ignoreCase = true)
            }
        }
        adapter.updateData(filtered)
    }

    private fun setFilterActive(active: Button) {
        val buttons = listOf(btnSemua, btnMakanan, btnMinuman, btnSnack)
        buttons.forEach { btn ->
            if (btn == active) {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EEE8F4"))
                btn.setTextColor(Color.parseColor("#7B1FA2"))
            } else {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                btn.setTextColor(Color.parseColor("#666666"))
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnRefresh.setOnClickListener {
            etSearch.setText("")
            filterKategori = "Semua"
            setFilterActive(btnSemua)
            loadData()
        }

        btnTambah.setOnClickListener {
            startActivity(Intent(this, TambahProdukActivity::class.java))
        }

        btnSemua.setOnClickListener {
            filterKategori = "Semua"
            setFilterActive(btnSemua)
            applyFilter()
        }

        btnMakanan.setOnClickListener {
            filterKategori = "Makanan"
            setFilterActive(btnMakanan)
            applyFilter()
        }

        btnMinuman.setOnClickListener {
            filterKategori = "Minuman"
            setFilterActive(btnMinuman)
            applyFilter()
        }

        btnSnack.setOnClickListener {
            filterKategori = "Snack"
            setFilterActive(btnSnack)
            applyFilter()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { applyFilter() }
        })
    }
}