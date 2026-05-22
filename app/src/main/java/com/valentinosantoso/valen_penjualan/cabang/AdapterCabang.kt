package com.valentinosantoso.valen_penjualan.cabang

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valentinosantoso.valen_penjualan.R

class AdapterCabang(private var list: List<DataCabangModel>) :
    RecyclerView.Adapter<AdapterCabang.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNamaCabang)
        val tvAlamatCabang: TextView = itemView.findViewById(R.id.tvAlamatCabang)
        val tvTeleponCabang: TextView = itemView.findViewById(R.id.tvTeleponCabang)
        val tvAktif: TextView = itemView.findViewById(R.id.tvAktif)
        val imgStatusAktif: ImageView = itemView.findViewById(R.id.imgStatusAktif)
        val layoutAktif: LinearLayout = itemView.findViewById(R.id.layoutAktif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNamaCabang.text = item.namaCabang
        holder.tvAlamatCabang.text = item.alamatCabang
        holder.tvTeleponCabang.text = item.teleponCabang

        if (item.statusAktif) {
            holder.layoutAktif.setBackgroundColor(Color.TRANSPARENT)
            holder.tvAktif.text = "Aktif"
            holder.tvAktif.setTextColor(Color.parseColor("#4CAF50"))
            holder.imgStatusAktif.setImageResource(R.drawable.checklist)
            holder.imgStatusAktif.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            holder.layoutAktif.setBackgroundColor(Color.TRANSPARENT)
            holder.tvAktif.text = "Tidak Aktif"
            holder.tvAktif.setTextColor(Color.parseColor("#F44336"))
            holder.imgStatusAktif.setImageResource(android.R.drawable.ic_delete)
            holder.imgStatusAktif.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        // Click listener untuk edit cabang
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ModCabangActivity::class.java).apply {
                putExtra("EXTRA_ID_CABANG", item.idCabang)
                putExtra("EXTRA_NAMA_CABANG", item.namaCabang)
                putExtra("EXTRA_ALAMAT_CABANG", item.alamatCabang)
                putExtra("EXTRA_TELEPON_CABANG", item.teleponCabang)
                putExtra("EXTRA_STATUS_AKTIF", item.statusAktif)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<DataCabangModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
