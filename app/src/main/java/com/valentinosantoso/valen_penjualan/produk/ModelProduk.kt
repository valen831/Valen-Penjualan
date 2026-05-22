package com.valentinosantoso.valen_penjualan.produk

data class ModelProduk(
    var idProduk: String = "",
    var namaProduk: String = "",
    var harga: Double = 0.0,
    var kategori: String = "",
    var cabang: String = "",
    var stok: Int = 0,
    var stokTakTerbatas: Boolean = false,
    var statusAktif: Boolean = true,
    var fotoUrl: String = ""
)