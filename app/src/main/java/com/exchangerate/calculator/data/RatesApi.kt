package com.exchangerate.calculator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 一次汇率快照：以 base 为基准（默认 USD），rates[code] 表示 1 base = rates[code] code
 */
data class RateSnapshot(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
    val fetchedAt: Long = System.currentTimeMillis()
) {
    /** from -> to 的汇率：1 from = ? to */
    fun rateBetween(from: String, to: String): Double? {
        val rFrom = rates[from] ?: return null
        val rTo = rates[to] ?: return null
        if (rFrom <= 0.0) return null
        return rTo / rFrom
    }
}

/**
 * 汇率 API：Frankfurter（欧洲央行每日参考汇率，免费无需密钥）
 * 主备两个域名，任一可用即可
 */
class RatesApi {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val endpoints = listOf(
        "https://api.frankfurter.dev/v1/latest?base=USD",
        "https://api.frankfurter.app/latest?base=USD"
    )

    suspend fun fetchUsdRates(): RateSnapshot = withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        for (url in endpoints) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
                    val body = resp.body?.string() ?: throw IOException("服务器返回空响应")
                    return@withContext parse(body)
                }
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw IOException("无法获取汇率数据，请检查网络连接", lastError)
    }

    companion object {
        fun parse(body: String, fetchedAt: Long = System.currentTimeMillis()): RateSnapshot {
            val json = JSONObject(body)
            val base = json.optString("base", "USD")
            val date = json.optString("date", "")
            val ratesJson = json.getJSONObject("rates")
            val rates = HashMap<String, Double>(ratesJson.length() + 1)
            rates[base] = 1.0
            for (key in ratesJson.keys()) {
                rates[key] = ratesJson.getDouble(key)
            }
            return RateSnapshot(base, date, rates, fetchedAt)
        }
    }
}
