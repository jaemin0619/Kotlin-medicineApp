package com.example.yakbangapp.ui.mymeds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.data.MedRepository
import com.example.yakbangapp.model.MyMed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyMedsViewModel(app: Application) : AndroidViewModel(app) {

    private val session = UserSession(app)
    private val repo = MedRepository(app, session)

    // ✅ Room Flow → LiveData로 노출
    val meds = repo.myMedsFlow.asLiveData()

    fun addMed(name: String, doseText: String, scheduleText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.add(name, doseText, scheduleText)
        }
    }

    fun toggleTaken(item: MyMed, checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleTaken(item, checked)
        }
    }

    fun delete(item: MyMed) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(item)
        }
    }
}
