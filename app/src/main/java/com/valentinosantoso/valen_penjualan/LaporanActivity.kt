package com.valentinosantoso.valen_penjualan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var tvTotalHariIni: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvTanggalHariIni: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var rvTransaksi: RecyclerView

    private val transaksiList = mutableListOf<Map<String, Any>>()
    private var selectedCalendar = Calendar.getInstance()
    private var dbListener: ValueEventListener? = null
    private val dbRef = FirebaseDatabase.getInstance("https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("transaksi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tvTotalHariIni = findViewById(R.id.tvTotalHariIni)
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi)
        tvTanggalHariIni = findViewById(R.id.tvTanggalHariIni)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvTransaksi = findViewById(R.id.rvTransaksi)

        val btnPrevDay = findViewById<ImageButton>(R.id.btnPrevDay)
        val btnNextDay = findViewById<ImageButton>(R.id.btnNextDay)

        btnPrevDay.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, -1)
            updateDateAndLoad()
        }

        btnNextDay.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, 1)
            updateDateAndLoad()
        }

        rvTransaksi.layoutManager = LinearLayoutManager(this)
        rvTransaksi.adapter = LaporanAdapter()

        updateDateAndLoad()
    }

    private fun updateDateAndLoad() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvTanggalHariIni.text = dateFormat.format(selectedCalendar.time)

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")).format(selectedCalendar.time)
        loadTransaksiForDate(dateStr)
    }

    private fun loadTransaksiForDate(dateStr: String) {
        dbListener?.let { dbRef.removeEventListener(it) }

        dbListener = dbRef.orderByChild("tanggal").equalTo(dateStr)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    transaksiList.clear()
                    var totalHariIni = 0.0

                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val map = mutableMapOf<String, Any>()
                            map["waktu"] = data.child("waktu").getValue(String::class.java) ?: ""
                            map["cabang"] = data.child("cabang").getValue(String::class.java) ?: ""
                            map["kasir"] = data.child("kasir").getValue(String::class.java) ?: ""
                            map["totalHarga"] = data.child("totalHarga").getValue(Double::class.java) ?: 0.0
                            map["totalItem"] = data.child("totalItem").getValue(Long::class.java) ?: 0L

                            val itemsBuilder = StringBuilder()
                            val itemsSnap = data.child("items")
                            for (itemData in itemsSnap.children) {
                                val nama = itemData.child("namaProduk").getValue(String::class.java) ?: ""
                                val jumlah = itemData.child("jumlah").getValue(Long::class.java) ?: 0L
                                val subtotal = itemData.child("subtotal").getValue(Double::class.java) ?: 0.0
                                itemsBuilder.append("\u2022 $nama x$jumlah = Rp${String.format("%,.0f", subtotal)}\n")
                            }
                            map["itemsText"] = itemsBuilder.toString().trimEnd()

                            totalHariIni += map["totalHarga"] as Double
                            transaksiList.add(map)
                        }
                    }

                    // Sort newest first
                    transaksiList.sortByDescending { it["waktu"] as String }

                    tvTotalHariIni.text = "Rp${String.format("%,.0f", totalHariIni)}"
                    tvJumlahTransaksi.text = "${transaksiList.size} Transaksi"

                    val dateFormatDisplay = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(selectedCalendar.time)
                    tvEmpty.text = "Belum ada transaksi pada $dateFormatDisplay"
                    tvEmpty.visibility = if (transaksiList.isEmpty()) View.VISIBLE else View.GONE
                    
                    rvTransaksi.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener?.let { dbRef.removeEventListener(it) }
    }

    inner class LaporanAdapter : RecyclerView.Adapter<LaporanAdapter.VH>() {
        inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWaktu: TextView = itemView.findViewById(R.id.tvWaktu)
            val tvCabang: TextView = itemView.findViewById(R.id.tvCabang)
            val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
            val tvItems: TextView = itemView.findViewById(R.id.tvItems)
            val tvKasir: TextView = itemView.findViewById(R.id.tvKasir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_laporan_transaksi, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = transaksiList[position]
            holder.tvWaktu.text = item["waktu"] as String
            holder.tvCabang.text = item["cabang"] as String
            holder.tvTotalHarga.text = "Rp${String.format("%,.0f", item["totalHarga"] as Double)}"
            holder.tvItems.text = item["itemsText"] as String
            holder.tvKasir.text = "Kasir: ${item["kasir"]}"
        }

        override fun getItemCount() = transaksiList.size
    }
}
