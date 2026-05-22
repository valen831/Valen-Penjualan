package com.valentinosantoso.valen_penjualan.kategori

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.valentinosantoso.valen_penjualan.R
import com.valentinosantoso.valen_penjualan.viewmodel.DataKategoriViewModel

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvKategori: RecyclerView
    private lateinit var btnTambah: FloatingActionButton
    private lateinit var adapter: AdapterKategori

    private val viewModel: DataKategoriViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        initViews()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        rvKategori = findViewById(R.id.recyclerView)
        btnTambah = findViewById(R.id.btnTambah)
    }

    private fun setupRecyclerView() {
        adapter = AdapterKategori(emptyList())
        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.kategoriList.observe(this) { list ->
            adapter.updateData(list)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnTambah.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        // Search real-time setiap huruf diketik
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchKategori(s.toString())
            }
        })
    }
}