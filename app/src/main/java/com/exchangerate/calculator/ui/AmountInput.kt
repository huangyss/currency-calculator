package com.exchangerate.calculator.ui

/**
 * 金额输入的纯函数处理：只保留数字与一个小数点，规范化前导零。
 * 抽成纯函数便于单元测试（不含 Android 依赖）。
 */
object AmountInput {

    /** 允许的最大字符数（含小数点） */
    const val MAX_LENGTH = 12

    /**
     * 规范化一次输入。
     * @return 规范化后的文本；超出长度上限时返回 null，表示忽略这次输入
     */
    fun sanitize(raw: String): String? {
        // 1. 只允许数字和小数点
        val filtered = raw.filter { it.isDigit() || it == '.' }

        // 2. 只保留第一个小数点
        val firstDot = filtered.indexOf('.')
        val singleDot = if (firstDot < 0) {
            filtered
        } else {
            filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
        }

        // 3. 规范化整数部分的前导零（"007" -> "7"，"0" -> "0"，"0000.5" -> "0.5"）
        val intEnd = singleDot.indexOf('.').let { if (it < 0) singleDot.length else it }
        val intPart = singleDot.substring(0, intEnd)
        val rest = singleDot.substring(intEnd)
        val trimmedInt = intPart.trimStart('0')
        val normalizedInt = if (trimmedInt.isEmpty() && intPart.isNotEmpty()) "0" else trimmedInt
        var normalized = normalizedInt + rest

        // 4. 单独一个小数点补成 "0."
        if (normalized == ".") normalized = "0."

        return if (normalized.length <= MAX_LENGTH) normalized else null
    }
}
