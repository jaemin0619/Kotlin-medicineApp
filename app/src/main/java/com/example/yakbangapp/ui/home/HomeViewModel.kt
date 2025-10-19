package com.example.yakbangapp.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yakbangapp.repository.Category
import com.example.yakbangapp.repository.YakRepository
import com.example.yakbangapp.ui.data.YakData
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: YakRepository = YakRepository()
) : ViewModel() {

    val yakDataList = MutableLiveData<List<YakData>>()

    fun getYakList(category: Category, q: String) {
        viewModelScope.launch {
            runCatching {
                repo.getYakInfo(category, q) // ✅ 이미 List<YakData>
            }.onSuccess { list ->
                yakDataList.postValue(list)           // 리스트 크기 0/1 로깅해도 좋음
            }.onFailure { e ->
                // 에러시 빈 리스트로
                yakDataList.postValue(emptyList())
            }
        }
    }
}
