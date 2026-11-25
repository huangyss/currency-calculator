package com.exchangerate.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.exchangerate.calculator.ConverterState
import com.exchangerate.calculator.R
import com.exchangerate.calculator.RatesUiState
import com.exchangerate.calculator.data.Currency
import com.exchangerate.calculator.data.CurrencyCatalog
import com.exchangerate.calculator.ui.components.CurrencyDropdown

/**
 * 换算页：金额输入 + 币种选择 + 互换 + 结果展示
 * @param modifier 外层容器修饰符（平板双栏/单栏限宽等场景由调用方控制尺寸）
 */
@Composable
fun ConverterScreen(
    converter: ConverterState,
    ratesState: RatesUiState,
    onAmountChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fromCurrency = CurrencyCatalog.find(converter.fromCode)
    val toCurrency = CurrencyCatalog.find(converter.toCode)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- 输入卡片 ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = converter.amountInput,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.label_amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium,
                    supportingText = { Text("支持小数，最多 12 位") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // 快捷金额
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("100", "500", "1000", "5000").forEach { quick ->
                        AssistChip(
                            onClick = { onAmountChange(quick) },
                            label = { Text(quick) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 币种选择 + 互换
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val options = CurrencyCatalog.availableIn(
                        (ratesState as? RatesUiState.Success)?.snapshot?.rates ?: emptyMap()
                    )
                    CurrencyDropdown(
                        label = stringResource(R.string.label_from),
                        selected = fromCurrency,
                        options = options,
                        onSelected = { onFromChange(it.code) },
                        modifier = Modifier.weight(1f)
                    )

                    FilledTonalIconButton(
                        onClick = onSwap,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_swap),
                            contentDescription = stringResource(R.string.action_swap)
                        )
                    }

                    CurrencyDropdown(
                        label = stringResource(R.string.label_to),
                        selected = toCurrency,
                        options = options,
                        onSelected = { onToChange(it.code) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ---------- 结果区域 ----------
        when (ratesState) {
            is RatesUiState.Loading -> LoadingCard()

            is RatesUiState.Error -> ErrorCard(
                message = ratesState.message,
                onRetry = onRetry
            )

            is RatesUiState.Success -> ResultCard(
                converter = converter,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                ratesState = ratesState
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.msg_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.msg_error),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun ResultCard(
    converter: ConverterState,
    fromCurrency: Currency,
    toCurrency: Currency,
    ratesState: RatesUiState.Success
) {
    val snapshot = ratesState.snapshot
    val rate = snapshot.rateBetween(converter.fromCode, converter.toCode)
    val amount = FormatUtils.parseAmount(converter.amountInput)
    val converted = if (rate != null) amount * rate else Double.NaN

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.label_result),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = FormatUtils.formatAmount(converted, toCurrency.fractionDigits),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = toCurrency.code,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${toCurrency.nameZh} ${toCurrency.symbol}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_rate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "1 ${fromCurrency.code} = ${FormatUtils.formatRate(rate ?: Double.NaN)} ${toCurrency.code}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            // 数据来源与日期
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "数据日期 ${snapshot.date} · 欧洲央行参考汇率",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (ratesState.fromCache) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.msg_cached),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
