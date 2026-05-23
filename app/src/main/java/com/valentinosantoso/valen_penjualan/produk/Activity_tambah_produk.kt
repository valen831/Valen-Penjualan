package com.valentinosantoso.valen_penjualan.produk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.valentinosantoso.valen_penjualan.R
import java.util.UUID

class TambahProdukActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imgProduk: ImageView
    private lateinit var btnKamera: Button
    private lateinit var btnGaleri: Button
    private lateinit var etNamaProduk: EditText
    private lateinit var etHarga: EditText
    private lateinit var btnPilihKategori: Button
    private lateinit var btnPilihCabang: Button
    private lateinit var etStok: EditText
    private lateinit var cbStokTakTerbatas: CheckBox
    private lateinit var btnSimpan: Button

    private val db = FirebaseDatabase.getInstance(
        "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null
    private var selectedKategori: String = ""
    private var selectedCabang: String = ""
    private val kategoriList = mutableListOf<String>()
    private val cabangList = mutableListOf<String>()

    private var isEditMode = false
    private var existingIdProduk = ""
    private var existingFotoUrl = ""
    private var existingStatusAktif = true

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            imgProduk.setImageURI(selectedImageUri)
            imgProduk.setPadding(0, 0, 0, 0)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imgProduk.setImageBitmap(bitmap)
            imgProduk.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_produk)

        initViews()
        loadKategori()
        loadCabang()
        setupListeners()
        checkIntentData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgProduk = findViewById(R.id.imgProduk)
        btnKamera = findViewById(R.id.btnKamera)
        btnGaleri = findViewById(R.id.btnGaleri)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHarga = findViewById(R.id.etHarga)
        btnPilihKategori = findViewById(R.id.btnPilihKategori)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)
        etStok = findViewById(R.id.etStok)
        cbStokTakTerbatas = findViewById(R.id.cbStokTakTerbatas)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun checkIntentData() {
        isEditMode = intent.getBooleanExtra("EXTRA_IS_EDIT", false)
        if (isEditMode) {
            existingIdProduk = intent.getStringExtra("EXTRA_ID_PRODUK") ?: ""
            etNamaProduk.setText(intent.getStringExtra("EXTRA_NAMA_PRODUK") ?: "")
            val harga = intent.getDoubleExtra("EXTRA_HARGA", 0.0)
            etHarga.setText(if (harga == Math.floor(harga)) harga.toLong().toString() else harga.toString())
            
            selectedKategori = intent.getStringExtra("EXTRA_KATEGORI") ?: ""
            if (selectedKategori.isNotEmpty()) btnPilihKategori.text = selectedKategori
            
            selectedCabang = intent.getStringExtra("EXTRA_CABANG") ?: ""
            if (selectedCabang.isNotEmpty()) btnPilihCabang.text = selectedCabang
            
            val stokTakTerbatas = intent.getBooleanExtra("EXTRA_STOK_TAK_TERBATAS", false)
            cbStokTakTerbatas.isChecked = stokTakTerbatas
            if (!stokTakTerbatas) {
                etStok.setText(intent.getIntExtra("EXTRA_STOK", 0).toString())
            }
            
            existingFotoUrl = intent.getStringExtra("EXTRA_FOTO_URL") ?: ""
            if (existingFotoUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(this).load(existingFotoUrl).into(imgProduk)
                imgProduk.setPadding(0, 0, 0, 0)
            }
            
            existingStatusAktif = intent.getBooleanExtra("EXTRA_STATUS_AKTIF", true)
            
            btnSimpan.text = "Simpan Perubahan"
            findViewById<TextView>(R.id.tvTitleBar)?.text = "Edit Produk"
            findViewById<TextView>(R.id.tvTitleContent)?.text = "Edit Produk"
        }
    }

    private fun loadKategori() {
        db.getReference("kategori").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kategoriList.clear()
                for (data in snapshot.children) {
                    val nama = data.child("namaKategori").getValue(String::class.java) ?: continue
                    kategoriList.add(nama)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadCabang() {
        db.getReference("cabang").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangList.clear()
                for (data in snapshot.children) {
                    val nama = data.child("namaCabang").getValue(String::class.java) ?: continue
                    cabangList.add(nama)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnKamera.setOnClickListener {
            cameraLauncher.launch(null)
        }

        btnGaleri.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        btnPilihKategori.setOnClickListener {
            if (kategoriList.isEmpty()) {
                Toast.makeText(this, "Data kategori kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Pilih Kategori")
                .setItems(kategoriList.toTypedArray()) { _, which ->
                    selectedKategori = kategoriList[which]
                    btnPilihKategori.text = selectedKategori
                }
                .show()
        }

        btnPilihCabang.setOnClickListener {
            if (cabangList.isEmpty()) {
                Toast.makeText(this, "Data cabang kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val checkedItems = BooleanArray(cabangList.size)
            val selectedList = selectedCabang.split(", ").filter { it.isNotEmpty() }
            for (i in cabangList.indices) {
                if (selectedList.contains(cabangList[i])) {
                    checkedItems[i] = true
                }
            }

            AlertDialog.Builder(this)
                .setTitle("Pilih Cabang")
                .setMultiChoiceItems(cabangList.toTypedArray(), checkedItems) { _, which, isChecked ->
                    checkedItems[which] = isChecked
                }
                .setPositiveButton("Pilih") { _, _ ->
                    val selected = mutableListOf<String>()
                    for (i in checkedItems.indices) {
                        if (checkedItems[i]) selected.add(cabangList[i])
                    }
                    selectedCabang = selected.joinToString(", ")
                    btnPilihCabang.text = if (selectedCabang.isEmpty()) "Pilih Cabang" else selectedCabang
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        cbStokTakTerbatas.setOnCheckedChangeListener { _, isChecked ->
            etStok.isEnabled = !isChecked
            if (isChecked) etStok.setText("")
        }

        btnSimpan.setOnClickListener {
            simpanProduk()
        }
    }

    private fun simpanProduk() {
        val nama = etNamaProduk.text.toString().trim()
        val hargaStr = etHarga.text.toString().trim()
        val stokTakTerbatas = cbStokTakTerbatas.isChecked
        val stok = if (stokTakTerbatas) -1 else etStok.text.toString().trim().toIntOrNull() ?: 0

        if (nama.isEmpty()) { etNamaProduk.error = "Nama produk wajib diisi"; return }
        if (hargaStr.isEmpty()) { etHarga.error = "Harga wajib diisi"; return }
        val harga = hargaStr.replace(",", ".").toDoubleOrNull()
        if (harga == null) { etHarga.error = "Harga tidak valid"; return }
        if (selectedKategori.isEmpty()) { Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show(); return }
        if (selectedCabang.isEmpty()) { Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show(); return }

        btnSimpan.isEnabled = false
        btnSimpan.text = "Menyimpan..."

        if (selectedImageUri != null) {
            val ref = storage.reference.child("produk/${UUID.randomUUID()}.jpg")
            ref.putFile(selectedImageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    simpanKeDatabase(nama, harga, selectedKategori, selectedCabang, stok, stokTakTerbatas, uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
        } else {
            simpanKeDatabase(nama, harga, selectedKategori, selectedCabang, stok, stokTakTerbatas, "")
        }
    }

    private fun simpanKeDatabase(
        nama: String, harga: Double, kategori: String,
        cabang: String, stok: Int, stokTakTerbatas: Boolean, fotoUrl: String
    ) {
        val ref = if (isEditMode) db.getReference("produk").child(existingIdProduk) else db.getReference("produk").push()
        val id = if (isEditMode) existingIdProduk else ref.key ?: ""
        val finalFotoUrl = if (fotoUrl.isEmpty() && isEditMode) existingFotoUrl else fotoUrl
        
        val data = mapOf(
            "idProduk" to id,
            "namaProduk" to nama,
            "harga" to harga,
            "kategori" to kategori,
            "cabang" to cabang,
            "stok" to stok,
            "stokTakTerbatas" to stokTakTerbatas,
            "fotoUrl" to finalFotoUrl,
            "statusAktif" to existingStatusAktif
        )
        ref.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan produk", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
    }
}