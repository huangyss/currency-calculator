package com.exchangerate.calculator.data

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * API 解析：正常响应、缺省 base、以及非法 JSON 的行为
 */
class RatesApiParseTest {

    @Test
    fun `解析正常响应并补齐基准币种`() {
        val json = """
            {"amount":1.0,"base":"USD","date":"2026-11-25","rates":{"CNY":7.1,"JPY":150.0}}
        """.trimIndent()

        val snapshot = RatesApi.parse(json, fetchedAt = 42L)

        assertEquals("USD", snapshot.base)
        assertEquals("2026-11-25", snapshot.date)
        assertEquals(42L, snapshot.fetchedAt)
        assertEquals(3, snapshot.rates.size)
        // 基准币种自身必须存在，否则基准->基准的换算会失败
        assertEquals(1.0, snapshot.rates["USD"]!!, 1e-12)
        assertEquals(7.1, snapshot.rates["CNY"]!!, 1e-12)
        assertTrue(snapshot.rates.containsKey("JPY"))
    }

    @Test
    fun `缺少base字段时按USD处理`() {
        val json = """{"date":"2026-11-25","rates":{"CNY":7.1}}"""
        val snapshot = RatesApi.parse(json)
        assertEquals("USD", snapshot.base)
    }

    @Test
    fun `缺少rates字段时抛异常由上层兜底`() {
        val json = """{"base":"USD","date":"2026-11-25"}"""
        assertThrows(JSONException::class.java) { RatesApi.parse(json) }
    }

    @Test
    fun `非法JSON抛异常`() {
        assertThrows(JSONException::class.java) { RatesApi.parse("<html>502</html>") }
    }
}
