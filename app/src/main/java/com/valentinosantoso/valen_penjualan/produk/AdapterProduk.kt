package com.valentinosantoso.valen_penjualan.produk

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.valentinosantoso.valen_penjualan.R

class AdapterProduk(private var list: List<ModelProduk>) :
    RecyclerView.Adapter<AdapterProduk.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvStok: TextView = itemView.findViewById(R.id.tvStok)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCabang)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        val layoutStatus: LinearLayout = itemView.findViewById(R.id.layoutStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvNamaProduk.text = item.namaProduk
        holder.tvHarga.text = "Rp${String.format("%,.0f", item.harga)}"
        holder.tvKategori.text = item.kategori
        holder.tvCabang.text = item.cabang

        // Stok
        if (item.stokTakTerbatas) {
            holder.tvStok.text = "Tidak Terbatas"
            holder.tvStok.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            val stokFormatted = String.format("%,d", item.stok).replace(',', '.')
            holder.tvStok.text = "Stok: $stokFormatted"
            holder.tvStok.setTextColor(
                if (item.stok > 0) Color.parseColor("#FF9800")
                else Color.parseColor("#F44336")
            )
        }

        // Status aktif
        if (item.statusAktif) {
            holder.tvStatus.text = "Aktif"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            holder.imgStatus.setImageResource(R.drawable.checklist)
            holder.imgStatus.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.text = "Tidak Aktif"
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            holder.imgStatus.setImageResource(R.drawable.close)
            holder.imgStatus.setColorFilter(Color.parseColor("#F44336"))
        }

        // Foto produk
        if (item.fotoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.fotoUrl)
                .placeholder(R.drawable.product)
                .into(holder.imgProduk)
        } else {
            holder.imgProduk.setImageResource(R.drawable.product)
        }

        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, com.valentinosantoso.valen_penjualan.produk.TambahProdukActivity::class.java).apply {
                putExtra("EXTRA_IS_EDIT", true)
                putExtra("EXTRA_ID_PRODUK", item.idProduk)
                putExtra("EXTRA_NAMA_PRODUK", item.namaProduk)
                putExtra("EXTRA_HARGA", item.harga)
                putExtra("EXTRA_KATEGORI", item.kategori)
                putExtra("EXTRA_CABANG", item.cabang)
                putExtra("EXTRA_STOK", item.stok)
                putExtra("EXTRA_STOK_TAK_TERBATAS", item.stokTakTerbatas)
                putExtra("EXTRA_FOTO_URL", item.fotoUrl)
                putExtra("EXTRA_STATUS_AKTIF", item.statusAktif)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<ModelProduk>) {
        list = newList
        notifyDataSetChanged()
    }
}