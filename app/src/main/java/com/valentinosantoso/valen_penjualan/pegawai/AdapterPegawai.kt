package com.valentinosantoso.valen_penjualan.pegawai

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

class AdapterPegawai(private var list: List<DataPegawaiModel>) :
    RecyclerView.Adapter<AdapterPegawai.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPegawai: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvJabatan: TextView = itemView.findViewById(R.id.tvJabatan)
        val tvTeleponPegawai: TextView = itemView.findViewById(R.id.tvTeleponPegawai)
        val tvAktif: TextView = itemView.findViewById(R.id.tvAktif)
        val imgStatusAktif: ImageView = itemView.findViewById(R.id.imgStatusAktif)
        val layoutAktif: LinearLayout = itemView.findViewById(R.id.layoutAktif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNamaPegawai.text = item.namaPegawai
        holder.tvJabatan.text = item.jabatan
        holder.tvTeleponPegawai.text = item.teleponPegawai

        if (item.statusAktif) {
            holder.layoutAktif.setBackgroundColor(Color.TRANSPARENT)
            holder.tvAktif.text = "Aktif"
            holder.tvAktif.setTextColor(Color.parseColor("#4CAF50"))
            holder.imgStatusAktif.setImageResource(R.drawable.checklist)
            holder.imgStatusAktif.imageTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            holder.layoutAktif.setBackgroundColor(Color.TRANSPARENT)
            holder.tvAktif.text = "Tidak Aktif"
            holder.tvAktif.setTextColor(Color.parseColor("#F44336"))
            holder.imgStatusAktif.setImageResource(android.R.drawable.ic_delete)
            holder.imgStatusAktif.imageTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ModPegawaiActivity::class.java).apply {
                putExtra("EXTRA_ID_PEGAWAI", item.idPegawai)
                putExtra("EXTRA_NAMA_PEGAWAI", item.namaPegawai)
                putExtra("EXTRA_JABATAN", item.jabatan)
                putExtra("EXTRA_TELEPON_PEGAWAI", item.teleponPegawai)
                putExtra("EXTRA_EMAIL_PEGAWAI", item.emailPegawai)
                putExtra("EXTRA_STATUS_AKTIF", item.statusAktif)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<DataPegawaiModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
