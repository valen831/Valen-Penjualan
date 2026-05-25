package com.valentinosantoso.valen_penjualan

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class DataPrinterActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnGrantPermission: Button
    private lateinit var spinnerPrinters: Spinner
    private lateinit var btnRefreshDevices: ImageButton
    private lateinit var layoutPrinterStatus: LinearLayout
    private lateinit var tvSelectedPrinterName: TextView
    private lateinit var tvSelectedPrinterMac: TextView
    private lateinit var btnSavePrinter: Button
    private lateinit var btnTestPrint: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val deviceList = mutableListOf<BluetoothDevice>()

    // Bluetooth permissions launcher for API 31+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            updatePermissionUi(true)
            loadPairedDevices()
        } else {
            Toast.makeText(this, "Izin Bluetooth diperlukan untuk menghubungkan printer.", Toast.LENGTH_SHORT).show()
            updatePermissionUi(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_printer)

        sharedPreferences = getSharedPreferences("PrinterConfig", Context.MODE_PRIVATE)

        initViews()
        setupListeners()
        checkPermissionsAndLoad()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        btnGrantPermission = findViewById(R.id.btnGrantPermission)
        spinnerPrinters = findViewById(R.id.spinnerPrinters)
        btnRefreshDevices = findViewById(R.id.btnRefreshDevices)
        layoutPrinterStatus = findViewById(R.id.layoutPrinterStatus)
        tvSelectedPrinterName = findViewById(R.id.tvSelectedPrinterName)
        tvSelectedPrinterMac = findViewById(R.id.tvSelectedPrinterMac)
        btnSavePrinter = findViewById(R.id.btnSavePrinter)
        btnTestPrint = findViewById(R.id.btnTestPrint)
    }

    private fun setupListeners() {
        btnGrantPermission.setOnClickListener {
            askBluetoothPermissions()
        }

        btnRefreshDevices.setOnClickListener {
            if (BluetoothPrinterHelper.hasBluetoothPermission(this)) {
                loadPairedDevices()
                Toast.makeText(this, "Daftar printer dimuat ulang.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Mohon berikan izin Bluetooth terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }

        spinnerPrinters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < deviceList.size) {
                    val device = deviceList[position]
                    showSelectedPrinterInfo(device)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSavePrinter.setOnClickListener {
            val position = spinnerPrinters.selectedItemPosition
            if (position >= 0 && position < deviceList.size) {
                val device = deviceList[position]
                val name = try { device.name ?: "Thermal Printer" } catch (e: SecurityException) { "Thermal Printer" }
                val mac = device.address

                sharedPreferences.edit()
                    .putString("printer_name", name)
                    .putString("printer_mac", mac)
                    .apply()

                Toast.makeText(this, "Printer $name berhasil disimpan.", Toast.LENGTH_SHORT).show()
                btnTestPrint.isEnabled = true
            } else {
                Toast.makeText(this, "Pilih printer terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }

        btnTestPrint.setOnClickListener {
            val savedMac = sharedPreferences.getString("printer_mac", null)
            if (savedMac != null) {
                btnTestPrint.isEnabled = false
                Toast.makeText(this, "Memulai test print...", Toast.LENGTH_SHORT).show()
                BluetoothPrinterHelper.printTestPage(this, savedMac) { success, message ->
                    runOnUiThread {
                        btnTestPrint.isEnabled = true
                        Toast.makeText(this, message ?: (if (success) "Sukses mencetak!" else "Gagal mencetak."), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Silakan simpan pengaturan printer terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndLoad() {
        if (BluetoothPrinterHelper.hasBluetoothPermission(this)) {
            updatePermissionUi(true)
            loadPairedDevices()
        } else {
            updatePermissionUi(false)
        }
    }

    private fun updatePermissionUi(isGranted: Boolean) {
        if (isGranted) {
            tvPermissionStatus.text = "Izin Bluetooth aktif (Diberikan)"
            tvPermissionStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
            btnGrantPermission.visibility = View.GONE
            spinnerPrinters.isEnabled = true
            btnRefreshDevices.isEnabled = true
            btnSavePrinter.isEnabled = true
            
            val savedMac = sharedPreferences.getString("printer_mac", null)
            btnTestPrint.isEnabled = savedMac != null
        } else {
            tvPermissionStatus.text = "Izin Bluetooth belum diberikan"
            tvPermissionStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            btnGrantPermission.visibility = View.VISIBLE
            spinnerPrinters.isEnabled = false
            btnRefreshDevices.isEnabled = false
            btnSavePrinter.isEnabled = false
            btnTestPrint.isEnabled = false
        }
    }

    private fun askBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
    }

    private fun loadPairedDevices() {
        val devices = BluetoothPrinterHelper.getPairedDevices(this)
        deviceList.clear()
        deviceList.addAll(devices)

        val deviceNames = mutableListOf<String>()
        for (device in devices) {
            val name = try { device.name ?: "Unknown Device" } catch (e: SecurityException) { "Unknown Device" }
            deviceNames.add("$name (${device.address})")
        }

        if (deviceNames.isEmpty()) {
            deviceNames.add("Tidak ada printer Bluetooth dipasangkan")
            btnSavePrinter.isEnabled = false
            btnTestPrint.isEnabled = false
            layoutPrinterStatus.visibility = View.GONE
        } else {
            btnSavePrinter.isEnabled = true
            val savedMac = sharedPreferences.getString("printer_mac", null)
            btnTestPrint.isEnabled = savedMac != null
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrinters.adapter = adapter

        // Select currently saved printer
        val savedMac = sharedPreferences.getString("printer_mac", null)
        if (savedMac != null) {
            val index = devices.indexOfFirst { it.address == savedMac }
            if (index != -1) {
                spinnerPrinters.setSelection(index)
                showSelectedPrinterInfo(devices[index])
            }
        }
    }

    private fun showSelectedPrinterInfo(device: BluetoothDevice) {
        val name = try { device.name ?: "Thermal Printer" } catch (e: SecurityException) { "Thermal Printer" }
        tvSelectedPrinterName.text = name
        tvSelectedPrinterMac.text = "MAC Address: ${device.address}"
        layoutPrinterStatus.visibility = View.VISIBLE
    }
}
