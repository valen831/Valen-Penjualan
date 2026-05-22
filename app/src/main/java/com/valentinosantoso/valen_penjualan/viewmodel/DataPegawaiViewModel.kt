package com.valentinosantoso.valen_penjualan.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.pegawai.DataPegawaiModel

class DataPegawaiViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance(
        "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val myRef = database.getReference("pegawai")

    val pegawaiList = MutableLiveData<List<DataPegawaiModel>>()
    private val originalPegawaiList = ArrayList<DataPegawaiModel>()

    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    private var valueEventListener: ValueEventListener? = null

    init {
        getData()
    }

    fun getData() {
        isLoading.postValue(true)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.postValue(false)
                if (snapshot.exists()) {
                    val list = ArrayList<DataPegawaiModel>()
                    for (dataSnapshot in snapshot.children) {
                        val id = dataSnapshot.key ?: ""
                        val nama = dataSnapshot.child("namaPegawai").getValue(String::class.java) ?: ""
                        val jabatan = dataSnapshot.child("jabatan").getValue(String::class.java) ?: ""
                        val telepon = dataSnapshot.child("teleponPegawai").getValue(String::class.java) ?: ""
                        val email = dataSnapshot.child("emailPegawai").getValue(String::class.java) ?: ""
                        val status = dataSnapshot.child("statusAktif").getValue(Boolean::class.java) ?: false
                        list.add(DataPegawaiModel(id, nama, jabatan, telepon, email, status))
                    }
                    originalPegawaiList.clear()
                    originalPegawaiList.addAll(list)
                    isSearchEmpty.postValue(false)
                    pegawaiList.postValue(ArrayList(originalPegawaiList))
                } else {
                    originalPegawaiList.clear()
                    isSearchEmpty.postValue(true)
                    pegawaiList.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.postValue(false)
                Log.e("DataPegawaiViewModel", "onCancelled: ${error.message}", error.toException())
            }
        }
        valueEventListener?.let { myRef.addValueEventListener(it) }
    }

    fun searchPegawai(keyword: String) {
        if (keyword.isEmpty()) {
            isSearchEmpty.postValue(originalPegawaiList.isEmpty())
            pegawaiList.postValue(ArrayList(originalPegawaiList))
            return
        }
        val filtered = originalPegawaiList.filter { pegawai ->
            pegawai.namaPegawai.contains(keyword, ignoreCase = true) ||
                    pegawai.jabatan.contains(keyword, ignoreCase = true)
        }
        isSearchEmpty.postValue(filtered.isEmpty())
        pegawaiList.postValue(filtered)
    }

    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let { myRef.removeEventListener(it) }
    }
}
