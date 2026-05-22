package com.valentinosantoso.valen_penjualan.pegawai

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

class ModPegawaiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvTitleHeader: TextView
    private lateinit var tvTitlePage: TextView
    private lateinit var etNamaPegawai: EditText
    private lateinit var etJabatan: EditText
    private lateinit var etTeleponPegawai: EditText
    private lateinit var etEmailPegawai: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button

    private var selectedStatus = ""
    private var isEditMode = false
    private var existingIdPegawai = ""
    private var existingNamaPegawai = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_pegawai)

        initViews()
        setupSpinner()
        checkIntentData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
        tvTitlePage = findViewById(R.id.tvTitlePage)
        etNamaPegawai = findViewById(R.id.etNamaPegawai)
        etJabatan = findViewById(R.id.etJabatan)
        etTeleponPegawai = findViewById(R.id.etTeleponPegawai)
        etEmailPegawai = findViewById(R.id.etEmailPegawai)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
    }

    private fun setupSpinner() {
        val statusOptions = arrayOf("Pilih Status", "Aktif", "Tidak Aktif")

        val adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, statusOptions
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0

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
                tv.setTextColor(if (position == 0) Color.GRAY else typedValue.data)
                tv.setBackgroundColor(
                    if (isDarkMode()) Color.parseColor("#1E1E1E") else Color.WHITE
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
            override fun onNothingSelected(parent: AdapterView<*>?) { selectedStatus = "" }
        }
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun checkIntentData() {
        val id = intent.getStringExtra("EXTRA_ID_PEGAWAI")
        if (!id.isNullOrEmpty()) {
            isEditMode = true
            existingIdPegawai = id
            existingNamaPegawai = intent.getStringExtra("EXTRA_NAMA_PEGAWAI") ?: ""

            tvTitleHeader.text = "Ubah Pegawai"
            tvTitlePage.text = "Edit Data Pegawai"
            etNamaPegawai.setText(existingNamaPegawai)
            etJabatan.setText(intent.getStringExtra("EXTRA_JABATAN") ?: "")
            etTeleponPegawai.setText(intent.getStringExtra("EXTRA_TELEPON_PEGAWAI") ?: "")
            etEmailPegawai.setText(intent.getStringExtra("EXTRA_EMAIL_PEGAWAI") ?: "")
            spinnerStatus.setSelection(if (intent.getBooleanExtra("EXTRA_STATUS_AKTIF", false)) 1 else 2)
            btnSimpan.text = "Simpan Perubahan"
            btnHapus.visibility = View.VISIBLE
        } else {
            isEditMode = false
            tvTitleHeader.text = "Tambah Pegawai"
            tvTitlePage.text = "Tambah Pegawai Baru"
            btnSimpan.text = "Simpan Data"
            btnHapus.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpanPegawai() }
        btnHapus.setOnClickListener { konfirmasiHapus() }
    }

    private fun simpanPegawai() {
        val nama = etNamaPegawai.text.toString().trim()
        val jabatan = etJabatan.text.toString().trim()
        val telepon = etTeleponPegawai.text.toString().trim()
        val email = etEmailPegawai.text.toString().trim()

        when {
            nama.isEmpty() -> {
                etNamaPegawai.error = "Nama pegawai harus diisi"
                etNamaPegawai.requestFocus()
            }
            jabatan.isEmpty() -> {
                etJabatan.error = "Jabatan harus diisi"
                etJabatan.requestFocus()
            }
            telepon.isEmpty() -> {
                etTeleponPegawai.error = "Nomor telepon harus diisi"
                etTeleponPegawai.requestFocus()
            }
            selectedStatus.isEmpty() -> {
                Toast.makeText(this, "Pilih status keaktifan pegawai", Toast.LENGTH_SHORT).show()
            }
            else -> {
                btnSimpan.isEnabled = false
                btnSimpan.text = if (isEditMode) "Mengubah..." else "Menyimpan..."

                val ref = FirebaseDatabase.getInstance(
                    "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
                ).getReference("pegawai")

                ref.get().addOnSuccessListener { snapshot ->
                    var sudahAda = false
                    for (dataSnapshot in snapshot.children) {
                        val dbId = dataSnapshot.key ?: ""
                        val dbNama = dataSnapshot.child("namaPegawai").getValue(String::class.java) ?: ""
                        if (isEditMode && dbId == existingIdPegawai) continue
                        if (dbNama.equals(nama, ignoreCase = true)) {
                            sudahAda = true
                            break
                        }
                    }

                    if (sudahAda) {
                        Toast.makeText(this, "Pegawai '$nama' sudah terdaftar!", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                        return@addOnSuccessListener
                    }

                    val targetId = if (isEditMode) existingIdPegawai else ref.push().key
                    if (targetId == null) {
                        Toast.makeText(this, "Gagal mendapatkan ID baru", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                        return@addOnSuccessListener
                    }

                    val data = mapOf(
                        "namaPegawai" to nama,
                        "jabatan" to jabatan,
                        "teleponPegawai" to telepon,
                        "emailPegawai" to email,
                        "statusAktif" to (selectedStatus == "Aktif")
                    )

                    ref.child(targetId).setValue(data).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val msg = if (isEditMode) "Pegawai berhasil diubah!" else "Pegawai berhasil ditambahkan!"
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            btnSimpan.isEnabled = true
                            btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal memverifikasi: ${it.message}", Toast.LENGTH_SHORT).show()
                    btnSimpan.isEnabled = true
                    btnSimpan.text = if (isEditMode) "Simpan Perubahan" else "Simpan Data"
                }
            }
        }
    }

    private fun konfirmasiHapus() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Pegawai")
            .setMessage("Apakah Anda yakin ingin menghapus pegawai '$existingNamaPegawai'? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                dialog.dismiss()
                hapusPegawai()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun hapusPegawai() {
        btnHapus.isEnabled = false
        btnHapus.text = "Menghapus..."

        FirebaseDatabase.getInstance(
            "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("pegawai").child(existingIdPegawai).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pegawai '$existingNamaPegawai' berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menghapus: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    btnHapus.isEnabled = true
                    btnHapus.text = "Hapus Pegawai"
                }
            }
    }
}
