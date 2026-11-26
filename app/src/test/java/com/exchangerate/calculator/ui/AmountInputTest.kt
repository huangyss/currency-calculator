package com.exchangerate.calculator.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 金额输入过滤：只留数字与一个小数点、规范化前导零、超长忽略
 */
class AmountInputTest {

    @Test
    fun `过滤非数字字符`() {
        assertEquals("123", AmountInput.sanitize("1a2b3"))
        assertEquals("", AmountInput.sanitize("abc"))
    }

    @Test
    fun `只保留第一个小数点`() {
        assertEquals("1.23", AmountInput.sanitize("1.2.3"))
        assertEquals("0.", AmountInput.sanitize("."))
    }

    @Test
    fun `规范化前导零`() {
        assertEquals("7", AmountInput.sanitize("007"))
        assertEquals("0", AmountInput.sanitize("000"))
        assertEquals("0.5", AmountInput.sanitize("0000.5"))
        assertEquals("0.05", AmountInput.sanitize("0.05"))
    }

    @Test
    fun `保留小数尾零`() {
        assertEquals("100.00", AmountInput.sanitize("100.00"))
    }

    @Test
    fun `恰好十二位仍可输入`() {
        assertEquals("999999999.99", AmountInput.sanitize("999999999.99"))
    }

    @Test
    fun `超过十二位则忽略本次输入`() {
        assertNull(AmountInput.sanitize("1234567890123"))
    }
}
