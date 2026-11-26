package com.exchangerate.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exchangerate.calculator.data.RateSnapshot
import com.exchangerate.calculator.data.RateSource
import com.exchangerate.calculator.data.RatesRepository
import com.exchangerate.calculator.ui.AmountInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 页面加载状态 */
sealed interface RatesUiState {
    data object Loading : RatesUiState

    /**
     * @param stale true 表示网络失败、当前展示的是兜底缓存，可能已过期
     */
    data class Success(val snapshot: RateSnapshot, val stale: Boolean = false) : RatesUiState

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

    /** 已有数据时的后台刷新标记（用于顶部进度指示，不打断当前内容） */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var refreshJob: Job? = null

    /** 刷新序号：取消旧任务时旧任务的收尾不得清掉新任务的刷新标记 */
    private var refreshSeq = 0

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) {
        // 新请求取代旧请求，避免慢的旧响应覆盖新数据
        refreshJob?.cancel()
        val seq = ++refreshSeq
        _isRefreshing.value = true

        refreshJob = viewModelScope.launch {
            // 已有数据时保留旧数据直到新数据到达（避免闪屏），否则先显示加载态
            if (_uiState.value !is RatesUiState.Success) {
                _uiState.value = RatesUiState.Loading
            }
            try {
                val result = repository.getRates(forceRefresh = force)
                _uiState.value = RatesUiState.Success(
                    snapshot = result.snapshot,
                    stale = result.source == RateSource.FALLBACK
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = RatesUiState.Error(e.message ?: "获取汇率失败")
            } finally {
                if (seq == refreshSeq) {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun onAmountChange(input: String) {
        AmountInput.sanitize(input)?.let { sanitized ->
            _converter.update { it.copy(amountInput = sanitized) }
        }
    }

    fun onFromChange(code: String) = _converter.update { it.copy(fromCode = code) }

    fun onToChange(code: String) = _converter.update { it.copy(toCode = code) }

    /** 互换源币种与目标币种 */
    fun swap() = _converter.update {
        it.copy(fromCode = it.toCode, toCode = it.fromCode)
    }
}
