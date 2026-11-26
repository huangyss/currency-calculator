package com.exchangerate.calculator.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 缓存新鲜度：以欧洲央行发布时刻（工作日 16:00 欧洲时间）为界
 */
class RateFreshnessTest {

    private val berlin = ZoneId.of("Europe/Berlin")

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): ZonedDateTime =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, berlin)

    /** 2026-11-23 是周一 */
    private val monday = at(2026, 11, 23, 0)

    @Test
    fun `发布时刻取当天16点`() {
        val last = RateFreshness.lastPublishInstant(at(2026, 11, 23, 17))
        assertEquals(at(2026, 11, 23, 16), last)
    }

    @Test
    fun `16点之前仍以上一个工作日的发布为准`() {
        val last = RateFreshness.lastPublishInstant(at(2026, 11, 23, 15, 59))
        assertEquals(at(2026, 11, 20, 16), last) // 上周五
    }

    @Test
    fun `周末回退到周五16点`() {
        assertEquals(at(2026, 11, 20, 16), RateFreshness.lastPublishInstant(at(2026, 11, 21, 10))) // 周六上午
        assertEquals(at(2026, 11, 20, 16), RateFreshness.lastPublishInstant(at(2026, 11, 22, 20))) // 周日晚上
    }

    @Test
    fun `周一上午周五收盘后抓的缓存仍然新鲜`() {
        val fetchedAt = at(2026, 11, 20, 17).toInstant().toEpochMilli()
        assertTrue(RateFreshness.isFresh(fetchedAt, at(2026, 11, 23, 10)))
    }

    @Test
    fun `周一下午16点后周五的缓存已过期`() {
        val fetchedAt = at(2026, 11, 20, 17).toInstant().toEpochMilli()
        assertFalse(RateFreshness.isFresh(fetchedAt, at(2026, 11, 23, 17)))
    }

    @Test
    fun `发布时刻整点视为已发布`() {
        val fetchedAt = at(2026, 11, 23, 16).toInstant().toEpochMilli()
        assertTrue(RateFreshness.isFresh(fetchedAt, at(2026, 11, 23, 16)))
    }

    @Test
    fun `周一零点等于上周五的发布窗口`() {
        assertEquals(at(2026, 11, 20, 16), RateFreshness.lastPublishInstant(monday))
    }
}
