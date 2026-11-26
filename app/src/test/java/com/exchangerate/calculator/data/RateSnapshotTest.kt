package com.exchangerate.calculator.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 汇率换算：缺失币种与除零必须返回 null，由界面显示 "—" 而不是 NaN/Infinity
 */
class RateSnapshotTest {

    private val snapshot = RateSnapshot(
        base = "USD",
        date = "2026-11-25",
        rates = mapOf(
            "USD" to 1.0,
            "CNY" to 7.10,
            "JPY" to 150.0,
            "BAD" to 0.0
        ),
        fetchedAt = 1_000L
    )

    @Test
    fun `基准币种换算到目标币种`() {
        assertEquals(7.10, snapshot.rateBetween("USD", "CNY")!!, 1e-9)
    }

    @Test
    fun `目标币种换算回基准币种取倒数`() {
        assertEquals(1.0 / 7.10, snapshot.rateBetween("CNY", "USD")!!, 1e-12)
    }

    @Test
    fun `交叉汇率由两次换算推导`() {
        assertEquals(150.0 / 7.10, snapshot.rateBetween("CNY", "JPY")!!, 1e-12)
    }

    @Test
    fun `同币种换算为一`() {
        assertEquals(1.0, snapshot.rateBetween("CNY", "CNY")!!, 1e-12)
    }

    @Test
    fun `缺失币种返回null`() {
        assertNull(snapshot.rateBetween("USD", "KRW"))
        assertNull(snapshot.rateBetween("KRW", "USD"))
    }

    @Test
    fun `汇率为零时返回null而不是无穷大`() {
        assertNull(snapshot.rateBetween("BAD", "CNY"))
    }
}
