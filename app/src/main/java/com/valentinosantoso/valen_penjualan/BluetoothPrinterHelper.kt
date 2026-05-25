package com.valentinosantoso.valen_penjualan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

object BluetoothPrinterHelper {

    private const val TAG = "BluetoothPrinterHelper"
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Command constants
    private val ESC_INIT = byteArrayOf(0x1B, 0x40)
    private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ESC_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
    private val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val ESC_DOUBLE_SIZE = byteArrayOf(0x1D, 0x21, 0x11)
    private val ESC_NORMAL_SIZE = byteArrayOf(0x1D, 0x21, 0x00)
    private val ESC_FEED_AND_CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x10) // Feed 16 dots and cut
    private val CHAR_SET_DEFAULT = "Cp1252" // Standard encoding for thermal printers

    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPairedDevices(context: Context): List<BluetoothDevice> {
        if (!hasBluetoothPermission(context)) {
            return emptyList()
        }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!bluetoothAdapter.isEnabled) {
            return emptyList()
        }
        return try {
            bluetoothAdapter.bondedDevices.toList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    fun printReceipt(
        context: Context,
        macAddress: String,
        cabang: String,
        kasir: String,
        waktu: String,
        itemsText: String,
        totalHarga: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (!hasBluetoothPermission(context)) {
            onResult(false, "Izin Bluetooth tidak diberikan.")
            return
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            onResult(false, "Bluetooth tidak aktif atau tidak didukung.")
            return
        }

        Thread {
            var socket: BluetoothSocket? = null
            var outputStream: OutputStream? = null
            try {
                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                // Create RFCOMM socket
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                outputStream = socket.outputStream

                // Build receipt commands
                val bytes = generateReceiptBytes(cabang, kasir, waktu, itemsText, totalHarga)
                outputStream.write(bytes)
                outputStream.flush()

                // Wait a bit for printer buffer to clear, then close
                Thread.sleep(800)
                onResult(true, "Berhasil mencetak nota.")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException: Bluetooth permission missing at runtime.", e)
                onResult(false, "Kesalahan izin Bluetooth: ${e.message}")
            } catch (e: IOException) {
                Log.e(TAG, "IOException connecting/writing to Bluetooth printer.", e)
                onResult(false, "Gagal terhubung dengan printer. Pastikan printer menyala.")
            } finally {
                try {
                    outputStream?.close()
                    socket?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing socket/stream", e)
                }
            }
        }.start()
    }

    fun printTestPage(
        context: Context,
        macAddress: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (!hasBluetoothPermission(context)) {
            onResult(false, "Izin Bluetooth tidak diberikan.")
            return
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            onResult(false, "Bluetooth tidak aktif.")
            return
        }

        Thread {
            var socket: BluetoothSocket? = null
            var outputStream: OutputStream? = null
            try {
                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                outputStream = socket.outputStream

                val byteBuffer = mutableListOf<Byte>()
                byteBuffer.addAll(ESC_INIT.toList())
                byteBuffer.addAll(ESC_ALIGN_CENTER.toList())
                byteBuffer.addAll(ESC_DOUBLE_SIZE.toList())
                byteBuffer.addAll(ESC_BOLD_ON.toList())
                byteBuffer.addAll("TEST PRINT\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll(ESC_NORMAL_SIZE.toList())
                byteBuffer.addAll(ESC_BOLD_OFF.toList())
                byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll("Printer Thermal Bluetooth\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll("Berhasil Terhubung!\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll(ESC_ALIGN_LEFT.toList())
                byteBuffer.addAll("Waktu: ${Date()}\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll("\n\n\n\n\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                
                outputStream.write(byteBuffer.toByteArray())
                outputStream.flush()
                Thread.sleep(800)
                onResult(true, "Test print berhasil.")
            } catch (e: Exception) {
                Log.e(TAG, "Test print failed", e)
                onResult(false, "Gagal test print: ${e.message}")
            } finally {
                try {
                    outputStream?.close()
                    socket?.close()
                } catch (e: Exception) {}
            }
        }.start()
    }

    private fun generateReceiptBytes(
        cabang: String,
        kasir: String,
        waktu: String,
        itemsText: String,
        totalHarga: Double
    ): ByteArray {
        val byteBuffer = mutableListOf<Byte>()

        // 1. Initialize
        byteBuffer.addAll(ESC_INIT.toList())

        // 2. Header (Cabang name)
        byteBuffer.addAll(ESC_ALIGN_CENTER.toList())
        byteBuffer.addAll(ESC_DOUBLE_SIZE.toList())
        byteBuffer.addAll(ESC_BOLD_ON.toList())
        byteBuffer.addAll((cabang.uppercase() + "\n").toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        // 3. Sub-header (App name / static text)
        byteBuffer.addAll(ESC_NORMAL_SIZE.toList())
        byteBuffer.addAll(ESC_BOLD_OFF.toList())
        byteBuffer.addAll("VALEN PENJUALAN\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        // 4. Metadata (Kasir, Waktu)
        byteBuffer.addAll(ESC_ALIGN_LEFT.toList())
        byteBuffer.addAll("Kasir : $kasir\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        byteBuffer.addAll("Waktu : $waktu\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        // 5. Items list
        // itemsText contains lines formatted as: "• $nama x$jumlah = Rp$subtotal"
        val lines = itemsText.split("\n")
        for (line in lines) {
            val cleanLine = line.replace("•", "").replace("\u2022", "").trim()
            if (cleanLine.isEmpty()) continue

            // Let's parse product name and total
            if (cleanLine.contains(" = ")) {
                val parts = cleanLine.split(" = ")
                val productAndQty = parts[0].trim()
                val subtotal = parts[1].trim()

                // Extract product name and quantity
                // e.g. "Bakso Wonogiri x1"
                var productName = productAndQty
                var qty = ""
                if (productAndQty.contains(" x")) {
                    val lastIndex = productAndQty.lastIndexOf(" x")
                    productName = productAndQty.substring(0, lastIndex).trim()
                    qty = productAndQty.substring(lastIndex).trim() // e.g. "x1"
                }

                // Print product name on its own line (left aligned)
                byteBuffer.addAll(ESC_ALIGN_LEFT.toList())
                byteBuffer.addAll(ESC_BOLD_ON.toList())
                byteBuffer.addAll(("$productName\n").toByteArray(charset(CHAR_SET_DEFAULT)).toList())
                byteBuffer.addAll(ESC_BOLD_OFF.toList())

                // Print quantity details and subtotal on the next line
                val leftDetails = "  $qty"
                val rightDetails = subtotal
                val detailsRow = makeRow(leftDetails, rightDetails) + "\n"
                byteBuffer.addAll(detailsRow.toByteArray(charset(CHAR_SET_DEFAULT)).toList())
            } else {
                // Fallback: print raw line if it doesn't match standard format
                byteBuffer.addAll(ESC_ALIGN_LEFT.toList())
                byteBuffer.addAll((cleanLine + "\n").toByteArray(charset(CHAR_SET_DEFAULT)).toList())
            }
        }

        byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        // 6. Total
        val totalStr = "Rp${String.format("%,.0f", totalHarga)}"
        val totalRow = makeRow("TOTAL", totalStr) + "\n"
        byteBuffer.addAll(ESC_BOLD_ON.toList())
        byteBuffer.addAll(totalRow.toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        byteBuffer.addAll(ESC_BOLD_OFF.toList())
        byteBuffer.addAll("--------------------------------\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        // 7. Footer
        byteBuffer.addAll(ESC_ALIGN_CENTER.toList())
        byteBuffer.addAll("Terima Kasih\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        byteBuffer.addAll("Atas Kunjungan Anda\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())
        
        // Feed some lines to make it easy to tear
        byteBuffer.addAll("\n\n\n\n\n".toByteArray(charset(CHAR_SET_DEFAULT)).toList())

        return byteBuffer.toByteArray()
    }

    private fun makeRow(left: String, right: String, totalWidth: Int = 32): String {
        val spaces = totalWidth - left.length - right.length
        return if (spaces > 0) {
            left + " ".repeat(spaces) + right
        } else {
            left + " " + right
        }
    }
}
