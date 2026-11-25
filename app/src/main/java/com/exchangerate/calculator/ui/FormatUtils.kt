package com.exchangerate.calculator.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 数字格式化工具：千分位金额 / 高精度汇率
 */
object FormatUtils {

    private val symbols = DecimalFormatSymbols(Locale.US)

    /** 金额：按币种小数位格式化（JPY/KRW 等 0 位小数） */
    fun formatAmount(value: Double, fractionDigits: Int): String {
        if (!value.isFinite()) return "—"
        val pattern = if (fractionDigits <= 0) "#,##0" else "#,##0." + "0".repeat(fractionDigits)
        return DecimalFormat(pattern, symbols).format(value)
    }

    /** 汇率：最多 8 位小数，去掉多余的 0 */
    fun formatRate(rate: Double): String {
        if (!rate.isFinite()) return "—"
        return DecimalFormat("#,##0.########", symbols).format(rate)
    }

    /** 输入串转金额，非法输入按 0 处理 */
    fun parseAmount(input: String): Double = input.toDoubleOrNull() ?: 0.0
}
