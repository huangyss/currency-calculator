package com.exchangerate.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exchangerate.calculator.ui.ConverterScreen
import com.exchangerate.calculator.ui.RatesTableScreen
import com.exchangerate.calculator.ui.WindowWidthClass
import com.exchangerate.calculator.ui.currentWindowWidthClass
import com.exchangerate.calculator.ui.theme.CurrencyCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyCalcTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val converter by viewModel.converter.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val widthClass = currentWindowWidthClass()

    val refresh: () -> Unit = { viewModel.refresh(force = true) }

    // 两个功能屏内容（各布局按需携带尺寸修饰符）
    val converterScreen: @Composable (Modifier) -> Unit = { m ->
        ConverterScreen(
            converter = converter,
            ratesState = uiState,
            onAmountChange = viewModel::onAmountChange,
            onFromChange = viewModel::onFromChange,
            onToChange = viewModel::onToChange,
            onSwap = viewModel::swap,
            onRetry = refresh,
            modifier = m
        )
    }
    val ratesScreen: @Composable (Modifier) -> Unit = { m ->
        RatesTableScreen(
            ratesState = uiState,
            baseCode = converter.fromCode,
            onBaseChange = viewModel::onFromChange,
            onPickTarget = { code ->
                viewModel.onToChange(code)
                selectedTab = 0
            },
            onRetry = refresh,
            modifier = m
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // 已有数据时的后台刷新：给一个轻量进度提示，不清空当前内容
                    if (isRefreshing && uiState is RatesUiState.Success) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(onClick = refresh, enabled = !isRefreshing) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.action_refresh)
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        }
    ) { innerPadding ->
        when (widthClass) {
            // ---------- 手机竖屏：顶部 Tab + 单栏 ----------
            WindowWidthClass.COMPACT -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.tab_convert)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.tab_rates)) }
                    )
                }
                HorizontalDivider(thickness = 0.5.dp)

                when (selectedTab) {
                    0 -> converterScreen(Modifier.fillMaxSize())
                    1 -> ratesScreen(Modifier.fillMaxSize())
                }
            }

            // ---------- 手机横屏 / 平板竖屏：侧边导航栏 + 单栏居中 ----------
            WindowWidthClass.MEDIUM -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavigationRail {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_swap),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.tab_convert)) }
                    )
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_list),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(R.string.tab_rates)) }
                    )
                }
                VerticalDivider(thickness = 0.5.dp)

                // 内容居中限宽，避免超宽屏上控件被拉伸
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                    when (selectedTab) {
                        0 -> converterScreen(contentModifier)
                        1 -> ratesScreen(contentModifier)
                    }
                }
            }

            // ---------- 平板横屏 / 折叠屏展开：双栏并排 ----------
            WindowWidthClass.EXPANDED -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 左栏：换算（固定宽度，独立滚动）
                converterScreen(
                    Modifier
                        .width(440.dp)
                        .fillMaxHeight()
                )
                VerticalDivider(thickness = 1.dp)
                // 右栏：汇率表（弹性宽度，独立滚动）
                ratesScreen(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}
