package com.exchangerate.calculator.data

import android.content.Context
import org.json.JSONObject

/**
 * 汇率仓库：网络拉取 + 本地缓存（SharedPreferences）
 * 网络不可用时自动回退到最近一次缓存数据
 */
class RatesRepository(context: Context) {

    private val api = RatesApi()
    private val prefs = context.getSharedPreferences("rates_cache", Context.MODE_PRIVATE)

    /**
     * 获取汇率快照
     * @param forceRefresh true 时跳过缓存有效期强制刷新
     * @return 汇率快照（可能来自缓存，通过 fetchedAt 判断新旧）
     */
    suspend fun getRates(forceRefresh: Boolean = false): RateSnapshot {
        if (!forceRefresh) {
            loadCache()?.let { cached ->
                if (!cached.isStale()) return cached
            }
        }
        try {
            val fresh = api.fetchUsdRates()
            saveCache(fresh)
            return fresh
        } catch (e: Exception) {
            // 网络失败时兜底返回缓存（即便已过期）
            loadCache()?.let { return it }
            throw e
        }
    }

    private fun RateSnapshot.isStale(): Boolean =
        System.currentTimeMillis() - fetchedAt > STALE_THRESHOLD_MS

    private fun saveCache(snapshot: RateSnapshot) {
        val ratesJson = JSONObject()
        snapshot.rates.forEach { (k, v) -> ratesJson.put(k, v) }
        prefs.edit()
            .putString(KEY_BODY, JSONObject().apply {
                put("base", snapshot.base)
                put("date", snapshot.date)
                put("rates", ratesJson)
            }.toString())
            .putLong(KEY_FETCHED_AT, snapshot.fetchedAt)
            .apply()
    }

    private fun loadCache(): RateSnapshot? {
        val body = prefs.getString(KEY_BODY, null) ?: return null
        val fetchedAt = prefs.getLong(KEY_FETCHED_AT, 0L)
        return try {
            RatesApi.parse(body, fetchedAt)
        } catch (e: Exception) {
            null
        }
    }

    /** 是否已有缓存（用于展示"离线缓存"标识） */
    fun hasCache(): Boolean = prefs.contains(KEY_BODY)

    companion object {
        private const val KEY_BODY = "body"
        private const val KEY_FETCHED_AT = "fetched_at"
        private const val STALE_THRESHOLD_MS = 30 * 60 * 1000L // 30 分钟
    }
}
