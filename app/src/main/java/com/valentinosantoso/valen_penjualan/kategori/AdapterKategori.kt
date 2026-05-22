package com.valentinosantoso.valen_penjualan.kategori

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valentinosantoso.valen_penjualan.R

class AdapterKategori(private var list: List<DataKategoriModel>) :
    RecyclerView.Adapter<AdapterKategori.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvAktif: TextView = itemView.findViewById(R.id.tvAktif)
        val imgStatusAktif: ImageView = itemView.findViewById(R.id.imgStatusAktif)
        val layoutAktif: LinearLayout = itemView.findViewById(R.id.layoutAktif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvKategori.text = item.namaKategori

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
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<DataKategoriModel>) {
        list = newList
        notifyDataSetChanged()
    }
}