package com.valentinosantoso.valen_penjualan.pegawai

data class DataPegawaiModel(
    var idPegawai: String = "",
    var namaPegawai: String = "",
    var jabatan: String = "",
    var teleponPegawai: String = "",
    var emailPegawai: String = "",
    var statusAktif: Boolean = false
)
