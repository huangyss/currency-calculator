package com.exchangerate.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exchangerate.calculator.data.RateSnapshot
import com.exchangerate.calculator.data.RatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 页面加载状态 */
sealed interface RatesUiState {
    data object Loading : RatesUiState
    data class Success(val snapshot: RateSnapshot, val fromCache: Boolean = false) : RatesUiState
    data class Error(val message: String) : RatesUiState
}

/** 换算页输入状态 */
data class ConverterState(
    val amountInput: String = "100",
    val fromCode: String = "USD",
    val toCode: String = "CNY"
)

/**
 * 主界面 ViewModel：持有换算输入与汇率数据，负责拉取/缓存/互换逻辑
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RatesRepository(application)

    private val _uiState = MutableStateFlow<RatesUiState>(RatesUiState.Loading)
    val uiState: StateFlow<RatesUiState> = _uiState.asStateFlow()

    private val _converter = MutableStateFlow(ConverterState())
    val converter: StateFlow<ConverterState> = _converter.asStateFlow()

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) {
        viewModelScope.launch {
            // 强制刷新时保留旧数据直到新数据到达（避免闪屏）
            if (_uiState.value !is RatesUiState.Success || force.not()) {
                if (_uiState.value !is RatesUiState.Success) {
                    _uiState.value = RatesUiState.Loading
                }
            }
            try {
                val snapshot = repository.getRates(forceRefresh = force)
                val fromCache = isFromCache(snapshot)
                _uiState.value = RatesUiState.Success(snapshot, fromCache)
            } catch (e: Exception) {
                _uiState.value = RatesUiState.Error(e.message ?: "获取汇率失败")
            }
        }
    }

    private fun isFromCache(snapshot: RateSnapshot): Boolean =
        System.currentTimeMillis() - snapshot.fetchedAt > 5 * 60 * 1000L

    fun onAmountChange(input: String) {
        // 只允许数字和一个小数点
        val filtered = input.filter { it.isDigit() || it == '.' }
            .let { s ->
                val firstDot = s.indexOf('.')
                if (firstDot >= 0) {
                    s.substring(0, firstDot + 1) + s.substring(firstDot + 1).replace(".", "")
                } else s
            }
        if (filtered.length <= 12) {
            _converter.update { it.copy(amountInput = filtered) }
        }
    }

    fun onFromChange(code: String) = _converter.update { it.copy(fromCode = code) }

    fun onToChange(code: String) = _converter.update { it.copy(toCode = code) }

    /** 互换源币种与目标币种 */
    fun swap() = _converter.update {
        it.copy(fromCode = it.toCode, toCode = it.fromCode)
    }

    val amountValue: Double
        get() = _converter.value.amountInput.toDoubleOrNull() ?: 0.0
}
