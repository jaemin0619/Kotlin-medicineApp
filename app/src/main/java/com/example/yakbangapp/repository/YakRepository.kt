package com.example.yakbangapp.repository

import com.example.yakbangapp.network.RetrofitInstance
import com.example.yakbangapp.ui.data.toYakDataList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YakRepository {
    suspend fun getYakInfo(category: Category, query: String) = withContext(Dispatchers.IO) {
        val model = when (category) {
            is Category.CompanyName  -> RetrofitInstance.service.getYakInfo(companyName = query)
            is Category.ProductName  -> RetrofitInstance.service.getYakInfo(productName = query)
            is Category.Precautions  -> RetrofitInstance.service.getYakInfo(precautions = query)
            is Category.Warning      -> RetrofitInstance.service.getYakInfo(warning = query)
            is Category.StorageMethod-> RetrofitInstance.service.getYakInfo(storage = query)
            is Category.Efficacy     -> RetrofitInstance.service.getYakInfo(efficacy = query)
            is Category.Interactions -> RetrofitInstance.service.getYakInfo(interactions = query)
            is Category.ProductCode  -> RetrofitInstance.service.getYakInfo(productCode = query)
            is Category.SideEffects  -> RetrofitInstance.service.getYakInfo(sideEffects = query)
            is Category.UsageMethod  -> RetrofitInstance.service.getYakInfo(usage = query)
        }
        // ✅ 여기서 바로 변환
        model.toYakDataList().orEmpty()
    }
}
