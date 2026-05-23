package com.valentinosantoso.valen_penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.valentinosantoso.valen_penjualan.produk.ModelProduk

class AdapterTransaksiProduk(
    private var list: List<ModelProduk>,
    private val listener: OnQuantityChangeListener
) : RecyclerView.Adapter<AdapterTransaksiProduk.ViewHolder>() {

    private val quantities = HashMap<String, Int>()

    interface OnQuantityChangeListener {
        fun onQuantityChanged(totalPrice: Double, totalItems: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvProductName.text = item.namaProduk
        holder.tvProductDesc.text = item.kategori
        holder.tvPrice.text = "Rp${String.format("%,.0f", item.harga)}"
        
        if (item.stokTakTerbatas) {
            holder.tvStock.text = "Stok: Tidak Terbatas"
        } else {
            holder.tvStock.text = "Stok: ${item.stok}"
        }

        if (item.fotoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.fotoUrl)
                .placeholder(R.drawable.product)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.product)
        }

        val currentQty = quantities[item.idProduk] ?: 0
        holder.tvQuantity.text = currentQty.toString()

        holder.btnMinus.setOnClickListener {
            val qty = quantities[item.idProduk] ?: 0
            if (qty > 0) {
                quantities[item.idProduk] = qty - 1
                holder.tvQuantity.text = (qty - 1).toString()
                notifyTotalChanged()
            }
        }

        holder.btnPlus.setOnClickListener {
            val qty = quantities[item.idProduk] ?: 0
            if (item.stokTakTerbatas || qty < item.stok) {
                quantities[item.idProduk] = qty + 1
                holder.tvQuantity.text = (qty + 1).toString()
                notifyTotalChanged()
            }
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<ModelProduk>) {
        list = newList
        notifyDataSetChanged()
        notifyTotalChanged()
    }

    private fun notifyTotalChanged() {
        var totalItems = 0
        var totalPrice = 0.0
        for (item in list) {
            val qty = quantities[item.idProduk] ?: 0
            if (qty > 0) {
                totalItems += qty
                totalPrice += (qty * item.harga)
            }
        }
        listener.onQuantityChanged(totalPrice, totalItems)
    }
}
