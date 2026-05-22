package com.valentinosantoso.valen_penjualan.cabang

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
import com.valentinosantoso.valen_penjualan.viewmodel.DataCabangViewModel

class DataCabangActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvCabang: RecyclerView
    private lateinit var btnTambah: FloatingActionButton
    private lateinit var adapter: AdapterCabang

    private val viewModel: DataCabangViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        initViews()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        rvCabang = findViewById(R.id.recyclerView)
        btnTambah = findViewById(R.id.btnTambah)
    }

    private fun setupRecyclerView() {
        adapter = AdapterCabang(emptyList())
        rvCabang.layoutManager = LinearLayoutManager(this)
        rvCabang.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.cabangList.observe(this) { list ->
            adapter.updateData(list)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnTambah.setOnClickListener {
            startActivity(Intent(this, ModCabangActivity::class.java))
        }

        // Search real-time setiap huruf diketik
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchCabang(s.toString())
            }
        })
    }
}
