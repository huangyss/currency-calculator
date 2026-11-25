package com.exchangerate.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * 窗口宽度分级（Material 3 官方断点）：
 * - COMPACT  (< 600dp)  ：手机竖屏 → 顶部 Tab + 单栏
 * - MEDIUM   (600–840dp)：手机横屏 / 平板竖屏 → 侧边导航栏 + 单栏（内容居中限宽）
 * - EXPANDED (≥ 840dp)  ：平板横屏 / 折叠屏展开 → 双栏（换算 + 汇率表并排）
 *
 * 以窗口宽度而非屏幕宽度为准，分屏/悬浮窗场景同样正确。
 */
enum class WindowWidthClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun currentWindowWidthClass(): WindowWidthClass {
    val windowWidthDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    return when {
        windowWidthDp < 600.dp -> WindowWidthClass.COMPACT
        windowWidthDp < 840.dp -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }
}
