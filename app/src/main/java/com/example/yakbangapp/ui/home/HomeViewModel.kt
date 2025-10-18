package com.example.yakbangapp.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yakbangapp.repository.Category
import com.example.yakbangapp.repository.YakRepository
import com.example.yakbangapp.ui.data.YakData
import com.example.yakbangapp.ui.data.toYakDataList
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "HomeViewModel"

class HomeViewModel(private val repository: YakRepository = YakRepository()) : ViewModel() {
    private val _yakDataList = MutableLiveData<List<YakData>?>()
    val yakDataList get() = _yakDataList

    fun getYakList(category: Category, query: String) {
        Log.d("HomeViewModel", "🔍 getYakList called: category=${category}, query=$query")

        viewModelScope.launch {
            runCatching {
                repository.getYakInfo(category, query)
            }.onSuccess {
                _yakDataList.value = it.toYakDataList()
            }.onFailure {
                Log.e(TAG, "getYakList() failed! : $it")
                if (it is HttpException) {
                    val errorJsonString = it.response()?.errorBody()?.string()
                    Log.e(TAG, "getYakList() failed! : $errorJsonString")
                }
            }
        }
    }
}