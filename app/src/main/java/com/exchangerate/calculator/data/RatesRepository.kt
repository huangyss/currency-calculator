package com.exchangerate.calculator.data

import android.content.Context
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

/** 数据来源：决定界面要不要提示「离线缓存」 */
enum class RateSource {
    /** 本次刚从网络取回 */
    NETWORK,

    /** 磁盘缓存，且仍属于当前这批汇率（内容与网络一致） */
    CACHE,

    /** 网络失败后的兜底缓存，可能已过期 */
    FALLBACK
}

/** 仓库返回结果：快照 + 来源标记（来源由仓库判定，界面不再自己猜） */
data class RateResult(val snapshot: RateSnapshot, val source: RateSource)

/**
 * 汇率仓库：网络拉取 + 本地缓存（SharedPreferences）
 * 网络不可用时自动回退到最近一次缓存数据
 */
class RatesRepository(context: Context) {

    private val api = RatesApi()
    private val prefs = context.getSharedPreferences("rates_cache", Context.MODE_PRIVATE)

    /**
     * 获取汇率。
     * @param forceRefresh true 时跳过缓存直接请求网络
     * @return 快照与其来源；调用方据此决定是否展示「离线缓存」标识
     */
    suspend fun getRates(forceRefresh: Boolean = false): RateResult {
        if (!forceRefresh) {
            loadCache()?.let { cached ->
                if (RateFreshness.isFresh(cached.fetchedAt)) {
                    return RateResult(cached, RateSource.CACHE)
                }
            }
        }
        try {
            val fresh = api.fetchUsdRates()
            saveCache(fresh)
            return RateResult(fresh, RateSource.NETWORK)
        } catch (e: CancellationException) {
            // 协程取消必须继续往上抛，否则会被当成网络失败而返回缓存
            throw e
        } catch (e: Exception) {
            // 网络失败时兜底返回缓存（即便已过期），由来源标记告知界面
            loadCache()?.let { return RateResult(it, RateSource.FALLBACK) }
            throw e
        }
    }

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

    companion object {
        private const val KEY_BODY = "body"
        private const val KEY_FETCHED_AT = "fetched_at"
    }
}
