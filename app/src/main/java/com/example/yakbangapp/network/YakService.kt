package com.example.yakbangapp.network

import com.example.yakbangapp.model.YakModel
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY =
    "byvVXYUpgwAsVPZJU6SzX6nu8DrNXMuM5Fl25YwPXiK19yq%2B1LL8ARR7%2BRnZbWCONCShHk6DMd8006V3Ko%2BSLQ%3D%3D"


// YakService.kt (이미 올리신 것처럼 유지)
interface YakService {
    @GET("getDrbEasyDrugList?serviceKey=$API_KEY")
    suspend fun getYakInfo(
        @Query("entpName") companyName: String = "", // 업체명
        @Query("itemName") productName: String = "", // 제품명
        @Query("itemSeq") productCode: String = "", // 품목기준코드
        @Query("efcyQesitm") efficacy: String = "", // 효능
        @Query("useMethodQesitm") usage: String = "",// 사용법
        @Query("atpnWarnQesitm") warning: String = "", // 주의 사항 경고
        @Query("atpnQesitm") precautions: String = "", // 주의사항
        @Query("intrcQesitm") interactions: String = "",// 상호 작용
        @Query("seQesitm") sideEffects: String = "",// 부작용
        @Query("depositMethodQesitm") storage: String = "",// 보관법
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("type") responseType: String = "json"
    ): YakModel // ← Response<>가 아니라 YakModel 직접 반환으로 고정
}
