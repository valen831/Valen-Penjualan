Valen Penjualan 🛒Valen Penjualan adalah aplikasi manajemen penjualan (Point of Sale - POS) berbasis Android yang dirancang untuk mempermudah pengelolaan data barang, kategori, dan transaksi. Aplikasi ini dilengkapi dengan integrasi Firebase Realtime Database dan dukungan cetak nota melalui Bluetooth Thermal Printer.✨ Fitur Utama•Autentikasi Pengguna: Login dan Register menggunakan Firebase Database.•Dukungan Mode Gelap (Dark Mode): Antarmuka aplikasi otomatis menyesuaikan dengan tema sistem (Light/Dark).•Manajemen Produk & Kategori: CRUD (Create, Read, Update, Delete) data barang dan kategori secara real-time.•Cetak Nota Bluetooth: Helper khusus untuk mencetak nota belanja langsung ke Printer Thermal Bluetooth.•Real-time Database: Sinkronisasi data secara langsung menggunakan Firebase.•View Binding: Implementasi UI yang aman dan efisien menggunakan Android ViewBinding.🛠️ Teknologi yang Digunakan•Bahasa: Kotlin•Database: Firebase Realtime Database•Storage: Firebase Storage (untuk gambar produk)•Library Pihak Ketiga:•Glide: Untuk pemuatan gambar secara efisien.•ViewBinding: Untuk interaksi aman dengan komponen UI.•Bluetooth API: Untuk integrasi printer thermal.🚀 Cara Instalasi1.Clone Repositorycom.valentinosantoso.valen_penjualan
├── kategori           # Activity & Adapter untuk Kategori
├── produk             # Activity & Adapter untuk Produk
├── BluetoothPrinterHelper.kt # Logic cetak nota Bluetooth
├── LoginActivity.kt   # Fitur login
├── RegisterActivity.kt# Fitur daftar akun
└── MainActivity.kt    # Dashboard Utama2.Konfigurasi Firebase•Buka Firebase Console.•Buat proyek baru dan tambahkan aplikasi Android dengan package name com.valentinosantoso.valen_penjualan.•Unduh file google-services.json dan letakkan di folder app/.•Aktifkan Realtime Database dan Firebase Storage.3.Pengaturan Database (Rules) Ubah rules Realtime Database agar aplikasi dapat membaca dan menulis:{
  "rules": {
    ".read": "true",
    ".write": "true"
  }
}4.Build Project Buka proyek di Android Studio, tunggu proses Gradle Sync selesai, dan jalankan aplikasi di perangkat fisik atau emulator.🖨️ Fitur Printer BluetoothAplikasi ini menggunakan BluetoothPrinterHelper.kt untuk berkomunikasi dengan printer thermal.•Izin: Membutuhkan izin BLUETOOTH_CONNECT dan BLUETOOTH_SCAN pada Android 12+.•Format Nota: Header cabang, nama kasir, daftar item belanja, total harga, dan footer kustom.📂 Struktur Folder Utamacom.valentinosantoso.valen_penjualan
├── kategori           # Activity & Adapter untuk Kategori
├── produk             # Activity & Adapter untuk Produk
├── BluetoothPrinterHelper.kt # Logic cetak nota Bluetooth
├── LoginActivity.kt   # Fitur login
├── RegisterActivity.kt# Fitur daftar akun
└── MainActivity.kt    # Dashboard Utama📝 Izin Aplikasi (Permissions)Aplikasi memerlukan izin berikut agar berfungsi maksimal:•INTERNET: Untuk koneksi ke Firebase.•BLUETOOTH & BLUETOOTH_ADMIN: Untuk mencetak nota (Android 11 kebawah).•BLUETOOTH_CONNECT: Untuk mencetak nota (Android 12 keatas).👨‍💻 Kontributor•Valentino Santoso - Developer Utama📄 LisensiProyek ini dilisensikan di bawah MIT License.
