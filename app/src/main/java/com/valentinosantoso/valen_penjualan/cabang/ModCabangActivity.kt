package com.valentinosantoso.valen_penjualan.cabang

import android.graphics.Color
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.valentinosantoso.valen_penjualan.R

class ModCabangActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitleHeader: TextView
    private lateinit var tvTitlePage: TextView
    private lateinit var etNamaCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var etTeleponCabang: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var selectedStatus = ""
    private var isEditMode = false
    private var existingIdCabang = ""
    private var existingNamaCabang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_cabang)

        initViews()
        setupSpinner()
        checkIntentData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
        tvTitlePage = findViewById(R.id.tvTitlePage)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
        etTeleponCabang = findViewById(R.id.etTeleponCabang)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinner() {
        val statusOptions = arrayOf("Pilih Status", "Aktif", "Tidak Aktif")

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
                    if (position == 0) Color.GRAY
                    else typedValue.data
                )
                tv.setBackgroundColor(
                    if (isDarkMode()) Color.parseColor("#1E1E1E")
                    else Color.WHITE
                )
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    private fun checkIntentData() {
        val id = intent.getStringExtra("EXTRA_ID_CABANG")
        if (!id.isNullOrEmpty()) {
            isEditMode = true
            existingIdCabang = id
            existingNamaCabang = intent.getStringExtra("EXTRA_NAMA_CABANG") ?: ""
            val alamat = intent.getStringExtra("EXTRA_ALAMAT_CABANG") ?: ""
            val telepon = intent.getStringExtra("EXTRA_TELEPON_CABANG") ?: ""
            val statusAktif = intent.getBooleanExtra("EXTRA_STATUS_AKTIF", false)

            tvTitleHeader.text = "Ubah Cabang"
            tvTitlePage.text = "Edit Data Cabang"
            etNamaCabang.setText(existingNamaCabang)
            etAlamatCabang.setText(alamat)
            etTeleponCabang.setText(telepon)
            
            val statusPos = if (statusAktif) 1 else 2
            spinnerStatus.setSelection(statusPos)
            
            btnSimpan.text = "Simpan Perubahan"
            btnHapus.visibility = View.VISIBLE
        } else {
            isEditMode = false
            tvTitleHeader.text = "Tambah Cabang"
            tvTitlePage.text = "Tambah Cabang Baru"
            btnSimpan.text = "Simpan Data"
            btnHapus.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpanCabang() }
        btnHapus.setOnClickListener { konfirmasiHapus() }
    }

    private fun simpanCabang() {
        val namaCabang = etNamaCabang.text.toString().trim()
        val alamatCabang = etAlamatCabang.text.toString().trim()
        val teleponCabang = etTeleponCabang.text.toString().trim()

        when {
            namaCabang.isEmpty() -> {
                etNamaCabang.error = "Nama cabang harus diisi"
                etNamaCabang.requestFocus()
            }
            alamatCabang.isEmpty() -> {
                etAlamatCabang.error = "Alamat cabang harus diisi"
                etAlamatCabang.requestFocus()
            }
            teleponCabang.isEmpty() -> {
                etTeleponCabang.error = "Nomor telepon cabang harus diisi"
                etTeleponCabang.requestFocus()
            }
            selectedStatus.isEmpty() -> {
                Toast.makeText(this, "Pilih status keaktifan cabang", Toast.LENGTH_SHORT).show()
            }
            else -> {
                btnSimpan.isEnabled = false
                btnSimpan.text = if (isEditMode) "Mengubah..." else "Menyimpan..."

                val ref = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("cabang")

                // Cek duplikasi nama cabang
                ref.get().addOnSuccessListener { snapshot ->
                    var sudahAda = false
                    for (dataSnapshot in snapshot.children) {
                        val dbId = dataSnapshot.key ?: ""
                        val dbNama = dataSnapshot.child("namaCabang").getValue(String::class.java) ?: ""
                        
                        // Jika mode edit, abaikan pengecekan jika nama sama dengan nama sebelumnya
                        if (isEditMode && dbId == existingIdCabang) {
                            continue
                        }
                        
                        if (dbNama.equals(namaCabang, ignoreCase = true)) {
                            sudahAda = true
                            break
                        }
                    }

                    if (sudahAda) {
                        Toast.makeText(this, "Cabang dengan nama '$namaCabang' sudah terdaftar!", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                        return@addOnSuccessListener
                    }

                    val targetId = if (isEditMode) existingIdCabang else ref.push().key
                    if (targetId == null) {
                        Toast.makeText(this, "Gagal mendapatkan ID baru", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                        return@addOnSuccessListener
                    }

                    val data = mapOf(
                        "namaCabang" to namaCabang,
                        "alamatCabang" to alamatCabang,
                        "teleponCabang" to teleponCabang,
                        "statusAktif" to (selectedStatus == "Aktif")
                    )

                    ref.child(targetId).setValue(data)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val msg = if (isEditMode) "Cabang berhasil diubah!" else "Cabang berhasil ditambahkan!"
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Gagal menyimpan: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                btnSimpan.isEnabled = true
                                btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal memverifikasi database: ${it.message}", Toast.LENGTH_SHORT).show()
                    btnSimpan.isEnabled = true
                    btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                }
            }
        }
    }

    private fun konfirmasiHapus() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus cabang '$existingNamaCabang'? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                dialog.dismiss()
                hapusCabang()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun hapusCabang() {
        btnHapus.isEnabled = false
        btnHapus.text = "Menghapus..."
        
        val ref = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("cabang")

        ref.child(existingIdCabang).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cabang '$existingNamaCabang' berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menghapus: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    btnHapus.isEnabled = true
                    btnHapus.text = "Hapus Cabang"
                }
            }
    }
}
