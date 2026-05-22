package com.valentinosantoso.valen_penjualan.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.valentinosantoso.valen_penjualan.cabang.DataCabangModel

class DataCabangViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance(
        "https://aplikasipertama-2cbc4b5e-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val myRef = database.getReference("cabang")

    val cabangList = MutableLiveData<List<DataCabangModel>>()
    private val originalCabangList = ArrayList<DataCabangModel>()

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
                    val list = ArrayList<DataCabangModel>()
                    for (dataSnapshot in snapshot.children) {
                        val id = dataSnapshot.key ?: ""
                        val nama = dataSnapshot.child("namaCabang").getValue(String::class.java) ?: ""
                        val alamat = dataSnapshot.child("alamatCabang").getValue(String::class.java) ?: ""
                        val telepon = dataSnapshot.child("teleponCabang").getValue(String::class.java) ?: ""
                        val status = dataSnapshot.child("statusAktif").getValue(Boolean::class.java) ?: false
                        
                        list.add(DataCabangModel(id, nama, alamat, telepon, status))
                    }
                    originalCabangList.clear()
                    originalCabangList.addAll(list)
                    isSearchEmpty.postValue(false)
                    cabangList.postValue(ArrayList(originalCabangList))
                } else {
                    originalCabangList.clear()
                    isSearchEmpty.postValue(true)
                    cabangList.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.postValue(false)
                Log.e("DataCabangViewModel", "onCancelled: ${error.message}", error.toException())
            }
        }
        valueEventListener?.let { myRef.addValueEventListener(it) }
    }

    fun searchCabang(keyword: String) {
        if (keyword.isEmpty()) {
            isSearchEmpty.postValue(originalCabangList.isEmpty())
            cabangList.postValue(ArrayList(originalCabangList))
            return
        }
        val filtered = originalCabangList.filter { cabang ->
            cabang.namaCabang.contains(keyword, ignoreCase = true) ||
                    cabang.alamatCabang.contains(keyword, ignoreCase = true)
        }
        isSearchEmpty.postValue(filtered.isEmpty())
        cabangList.postValue(filtered)
    }

    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let { myRef.removeEventListener(it) }
    }
}
