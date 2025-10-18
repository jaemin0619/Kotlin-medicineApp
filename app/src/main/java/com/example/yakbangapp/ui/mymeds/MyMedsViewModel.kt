package com.example.yakbangapp.ui.mymeds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yakbangapp.model.MyMed

class MyMedsViewModel : ViewModel() {

    private val _meds = MutableLiveData<List<MyMed>>(emptyList())
    val meds: LiveData<List<MyMed>> = _meds

    fun addMed(name: String, doseText: String, scheduleText: String) {
        val newList = _meds.value.orEmpty().toMutableList()
        newList.add(MyMed(name = name, doseText = doseText, scheduleText = scheduleText))
        _meds.value = newList
    }

    fun toggleTaken(item: MyMed, checked: Boolean) {
        _meds.value = _meds.value.orEmpty().map {
            if (it.id == item.id) it.copy(takenToday = checked) else it
        }
    }

    fun delete(item: MyMed) {
        _meds.value = _meds.value.orEmpty().filterNot { it.id == item.id }
    }
}
