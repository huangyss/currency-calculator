package com.exchangerate.calculator.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 数字格式化：千分位、按币种小数位、非有限值降级
 */
class FormatUtilsTest {

    @Test
    fun `金额按两位小数并加千分位`() {
        assertEquals("1,234.57", FormatUtils.formatAmount(1234.5678, 2))
        assertEquals("0.00", FormatUtils.formatAmount(0.0, 2))
    }

    @Test
    fun `零小数币种不显示小数位`() {
        assertEquals("1,235", FormatUtils.formatAmount(1234.5678, 0))
    }

    @Test
    fun `汇率最多八位小数并去掉尾随零`() {
        assertEquals("7.1", FormatUtils.formatRate(7.10))
        assertEquals("7.12345679", FormatUtils.formatRate(7.123456789))
        assertEquals("1", FormatUtils.formatRate(1.0))
    }

    @Test
    fun `非有限值显示占位符`() {
        assertEquals("—", FormatUtils.formatAmount(Double.NaN, 2))
        assertEquals("—", FormatUtils.formatAmount(Double.POSITIVE_INFINITY, 2))
        assertEquals("—", FormatUtils.formatRate(Double.NaN))
    }

    @Test
    fun `非法输入按零处理`() {
        assertEquals(0.0, FormatUtils.parseAmount(""), 1e-12)
        assertEquals(0.0, FormatUtils.parseAmount("abc"), 1e-12)
        assertEquals(12.5, FormatUtils.parseAmount("12.5"), 1e-12)
    }
}
