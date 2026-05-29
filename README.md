# 🛒 Valen Penjualan

**Valen Penjualan** adalah aplikasi manajemen penjualan (Point of Sale - POS) berbasis Android yang dirancang untuk mempermudah pengelolaan data barang, kategori, dan transaksi. Aplikasi ini dilengkapi dengan integrasi **Firebase Realtime Database** dan dukungan cetak nota melalui **Bluetooth Thermal Printer**.

---

## ✨ Fitur Utama

- 🔐 **Autentikasi Pengguna** — Login dan Register menggunakan Firebase Database
- 🌙 **Dukungan Mode Gelap (Dark Mode)** — Antarmuka otomatis menyesuaikan dengan tema sistem (Light/Dark)
- 📦 **Manajemen Produk & Kategori** — CRUD (Create, Read, Update, Delete) data barang dan kategori secara real-time
- 🖨️ **Cetak Nota Bluetooth** — Helper khusus untuk mencetak nota belanja langsung ke Printer Thermal Bluetooth
- 🔄 **Real-time Database** — Sinkronisasi data secara langsung menggunakan Firebase
- 🔗 **View Binding** — Implementasi UI yang aman dan efisien menggunakan Android ViewBinding

---

## 🚀 Teknologi yang Digunakan

| Komponen | Detail |
|---|---|
| Bahasa | Kotlin |
| Database | Firebase Realtime Database |
| Storage | Firebase Storage (untuk gambar produk) |
| Image Loader | Glide |
| UI | ViewBinding |
| Printer | Bluetooth API (Thermal Printer) |

---

## 📁 Struktur Folder Utama

```
com.valentinosantoso.valen_penjualan
├── cabang
├── kategori          # Activity & Adapter untuk Kategori
├── produk            # Activity & Adapter untuk Produk
├── pegawai
├── viewmodel
├── BluetoothPrinterHelper.kt   # Logic cetak nota Bluetooth
├── LoginActivity.kt            # Fitur login
├── RegisterActivity.kt         # Fitur daftar akun
└── MainActivity.kt             # Dashboard Utama
```

---

## ⚙️ Cara Instalasi

### 1. Clone Repository
```bash
git clone https://github.com/valen831/Valen-Penjualan.git
```

### 2. Konfigurasi Firebase
- Buka [Firebase Console](https://console.firebase.google.com/)
- Buat proyek baru dan tambahkan aplikasi Android dengan package name `com.valentinosantoso.valen_penjualan`
- Unduh file `google-services.json` dan letakkan di folder `app/`
- Aktifkan **Realtime Database** dan **Firebase Storage**

### 3. Pengaturan Database (Rules)
Ubah rules Realtime Database agar aplikasi dapat membaca dan menulis:
```json
{
  "rules": {
    ".read": "true",
    ".write": "true"
  }
}
```

### 4. Build Project
Buka proyek di Android Studio, tunggu proses Gradle Sync selesai, lalu jalankan aplikasi di perangkat fisik atau emulator.

---

## 🖨️ Fitur Printer Bluetooth

Aplikasi ini menggunakan `BluetoothPrinterHelper.kt` untuk berkomunikasi dengan printer thermal.

**Izin yang dibutuhkan:**
- `BLUETOOTH_CONNECT` dan `BLUETOOTH_SCAN` pada Android 12+

**Format Nota:**
- Header cabang
- Nama kasir
- Daftar item belanja
- Total harga
- Footer kustom

---

## 📋 Izin Aplikasi (Permissions)

Aplikasi memerlukan izin berikut agar berfungsi maksimal:

| Izin | Keterangan |
|---|---|
| `INTERNET` | Untuk koneksi ke Firebase |
| `BLUETOOTH` & `BLUETOOTH_ADMIN` | Untuk mencetak nota (Android 11 ke bawah) |
| `BLUETOOTH_CONNECT` | Untuk mencetak nota (Android 12 ke atas) |

---

## 👤 Kontributor

- **Valentino Santoso** — Developer Utama

---

## 📄 Lisensi

Proyek ini dilisensikan di bawah **MIT License**.
