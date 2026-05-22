package com.valentinosantoso.valen_penjualan.kategori

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.valentinosantoso.valen_penjualan.R

class ModKategoriActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerJenis: Spinner
    private lateinit var btnSimpan: Button
    private var selectedStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_kategori)

        initViews()
        setupSpinner()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerJenis = findViewById(R.id.spinnerJenis)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupSpinner() {
        val statusOptions = arrayOf("Pilih", "Aktif", "Tidak Aktif")

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, statusOptions
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view as TextView
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.customTextColorPrimary, typedValue, true)
                tv.setTextColor(typedValue.data)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.customTextColorPrimary, typedValue, true)
                tv.setTextColor(
                    if (position == 0) android.graphics.Color.GRAY
                    else typedValue.data
                )
                tv.setBackgroundColor(
                    if (isDarkMode()) android.graphics.Color.parseColor("#1E1E1E")
                    else android.graphics.Color.WHITE
                )
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenis.adapter = adapter

        spinnerJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatus = if (position == 0) "" else statusOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedStatus = ""
            }
        }
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener { simpanKategori() }
    }

    private fun simpanKategori() {
        val namaKategori = etNamaKategori.text.toString().trim()

        when {
            namaKategori.isEmpty() -> {
                etNamaKategori.error = "Nama kategori harus diisi"
                etNamaKategori.requestFocus()
            }
            selectedStatus.isEmpty() -> {
                Toast.makeText(this, "Pilih status kategori", Toast.LENGTH_SHORT).show()
            }
            else -> {
                btnSimpan.isEnabled = false
                btnSimpan.text = "Menyimpan..."

                val ref = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("kategori")

                // Cek duplikat sebelum simpan
                ref.get().addOnSuccessListener { snapshot ->
                    val sudahAda = snapshot.children.any {
                        it.child("namaKategori").getValue(String::class.java)
                            ?.equals(namaKategori, ignoreCase = true) == true
                    }

                    if (sudahAda) {
                        Toast.makeText(this, "Kategori '$namaKategori' sudah ada!", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = "Simpan"
                        return@addOnSuccessListener
                    }

                    val newId = ref.push().key
                    if (newId == null) {
                        Toast.makeText(this, "Gagal: tidak dapat membuat ID baru", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = "Simpan"
                        return@addOnSuccessListener
                    }

                    val data = mapOf(
                        "namaKategori" to namaKategori,
                        "statusAktif" to (selectedStatus == "Aktif")
                    )

                    ref.child(newId).setValue(data)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Kategori berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                btnSimpan.isEnabled = true
                                btnSimpan.text = "Simpan"
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal cek data: ${it.message}", Toast.LENGTH_SHORT).show()
                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan"
                }
            }
        }
    }
}