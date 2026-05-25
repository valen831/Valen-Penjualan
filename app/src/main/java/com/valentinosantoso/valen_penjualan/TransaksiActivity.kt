package com.valentinosantoso.valen_penjualan

import android.Manifest
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.produk.ModelProduk
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity(), AdapterTransaksiProduk.OnQuantityChangeListener {

    private var tempCabang: String? = null
    private var tempKasir: String? = null
    private var tempWaktu: String? = null
    private var tempItemsText: String? = null
    private var tempTotalHarga: Double = 0.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Izin Bluetooth aktif.", Toast.LENGTH_SHORT).show()
            val sharedPrefPrint = getSharedPreferences("PrinterConfig", MODE_PRIVATE)
            val printerMac = sharedPrefPrint.getString("printer_mac", null)
            val cabang = tempCabang
            val kasir = tempKasir
            val waktu = tempWaktu
            val itemsText = tempItemsText
            if (printerMac != null && cabang != null && kasir != null && waktu != null && itemsText != null) {
                printReceiptNow(printerMac, cabang, kasir, waktu, itemsText, tempTotalHarga)
            }
            clearTempPrintData()
        } else {
            Toast.makeText(this, "Izin Bluetooth ditolak. Gagal mencetak struk.", Toast.LENGTH_SHORT).show()
            clearTempPrintData()
        }
    }

    private fun clearTempPrintData() {
        tempCabang = null
        tempKasir = null
        tempWaktu = null
        tempItemsText = null
        tempTotalHarga = 0.0
    }

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

        val btnCheckout = findViewById<MaterialButton>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            prosesCheckout()
        }
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
                            // Firebase stores Int as Long — read as Long then convert
                            val stokLong = data.child("stok").getValue(Long::class.java) ?: 0L
                            val stok = stokLong.toInt()
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

    private fun prosesCheckout() {
        val orderItems = produkAdapter.getOrderItems()
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Belum ada item yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCabang = spinnerCabang.selectedItem as? String ?: "Tidak diketahui"
        var totalHarga = 0.0
        var totalItem = 0
        val itemDetails = StringBuilder()
        for ((produk, qty) in orderItems) {
            totalHarga += produk.harga * qty
            totalItem += qty
            itemDetails.append("• ${produk.namaProduk} x$qty = Rp${String.format("%,.0f", produk.harga * qty)}\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Checkout")
            .setMessage("Cabang: $selectedCabang\n\n$itemDetails\nTotal: Rp${String.format("%,.0f", totalHarga)}")
            .setPositiveButton("Checkout") { _, _ ->
                simpanTransaksi(selectedCabang, orderItems, totalHarga, totalItem)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun simpanTransaksi(
        cabang: String,
        orderItems: List<Pair<ModelProduk, Int>>,
        totalHarga: Double,
        totalItem: Int
    ) {
        val db = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app")
        val ref = db.getReference("transaksi").push()
        val idTransaksi = ref.key ?: return

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        val now = Date()

        val items = mutableListOf<Map<String, Any>>()
        for ((produk, qty) in orderItems) {
            items.add(
                mapOf(
                    "idProduk" to produk.idProduk,
                    "namaProduk" to produk.namaProduk,
                    "harga" to produk.harga,
                    "jumlah" to qty,
                    "subtotal" to (produk.harga * qty)
                )
            )
        }

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val kasir = sharedPref.getString("nama", "Unknown") ?: "Unknown"

        val data = mapOf(
            "idTransaksi" to idTransaksi,
            "cabang" to cabang,
            "tanggal" to dateFormat.format(now),
            "waktu" to timeFormat.format(now),
            "timestamp" to System.currentTimeMillis(),
            "kasir" to kasir,
            "totalItem" to totalItem,
            "totalHarga" to totalHarga,
            "items" to items
        )

        ref.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Checkout berhasil!", Toast.LENGTH_SHORT).show()
                // Kurangi stok setiap produk
                kurangiStok(db, orderItems)

                // Format itemsText
                val itemsBuilder = StringBuilder()
                for ((produk, qty) in orderItems) {
                    val subtotal = produk.harga * qty
                    itemsBuilder.append("\u2022 ${produk.namaProduk} x$qty = Rp${String.format("%,.0f", subtotal)}\n")
                }
                val itemsText = itemsBuilder.toString().trimEnd()
                val waktuStr = timeFormat.format(now)

                tanyaCetakStruk(cabang, kasir, waktuStr, itemsText, totalHarga)

                produkAdapter.clearQuantities()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tanyaCetakStruk(
        cabang: String,
        kasir: String,
        waktu: String,
        itemsText: String,
        totalHarga: Double
    ) {
        val sharedPref = getSharedPreferences("PrinterConfig", MODE_PRIVATE)
        val printerMac = sharedPref.getString("printer_mac", null)

        if (printerMac == null) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Transaksi Berhasil")
            .setMessage("Apakah Anda ingin mencetak struk belanja?")
            .setCancelable(false)
            .setPositiveButton("Cetak") { _, _ ->
                if (!BluetoothPrinterHelper.hasBluetoothPermission(this)) {
                    tempCabang = cabang
                    tempKasir = kasir
                    tempWaktu = waktu
                    tempItemsText = itemsText
                    tempTotalHarga = totalHarga

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                            )
                        )
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN
                            )
                        )
                    }
                } else {
                    printReceiptNow(printerMac, cabang, kasir, waktu, itemsText, totalHarga)
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun printReceiptNow(
        macAddress: String,
        cabang: String,
        kasir: String,
        waktu: String,
        itemsText: String,
        totalHarga: Double
    ) {
        Toast.makeText(this, "Sedang mencetak...", Toast.LENGTH_SHORT).show()
        BluetoothPrinterHelper.printReceipt(
            context = this,
            macAddress = macAddress,
            cabang = cabang,
            kasir = kasir,
            waktu = waktu,
            itemsText = itemsText,
            totalHarga = totalHarga
        ) { success, message ->
            runOnUiThread {
                Toast.makeText(this, message ?: (if (success) "Sukses mencetak!" else "Gagal mencetak."), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun kurangiStok(
        db: com.google.firebase.database.FirebaseDatabase,
        orderItems: List<Pair<ModelProduk, Int>>
    ) {
        val produkRef = db.getReference("produk")
        for ((produk, qty) in orderItems) {
            if (produk.stokTakTerbatas) continue
            if (produk.idProduk.isEmpty()) continue

            produkRef.child(produk.idProduk).child("stok")
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val stokSekarang = (snapshot.getValue(Long::class.java) ?: 0L).toInt()
                        val stokBaru = maxOf(0, stokSekarang - qty)
                        produkRef.child(produk.idProduk).child("stok").setValue(stokBaru)
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                })
        }
    }
}
