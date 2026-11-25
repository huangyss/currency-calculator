package com.exchangerate.calculator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.exchangerate.calculator.R
import com.exchangerate.calculator.RatesUiState
import com.exchangerate.calculator.data.Currency
import com.exchangerate.calculator.data.CurrencyCatalog
import com.exchangerate.calculator.ui.components.CurrencyDropdown

/**
 * 汇率表页：以基准币种查看各币种汇率，点选即设为换算目标币种
 * @param modifier 外层容器修饰符（平板双栏/单栏限宽等场景由调用方控制尺寸）
 */
@Composable
fun RatesTableScreen(
    ratesState: RatesUiState,
    baseCode: String,
    onBaseChange: (String) -> Unit,
    onPickTarget: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseCurrency = CurrencyCatalog.find(baseCode)

    Column(modifier = modifier.fillMaxSize()) {
        // 基准币种选择
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.label_base),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                val options = CurrencyCatalog.availableIn(
                    (ratesState as? RatesUiState.Success)?.snapshot?.rates ?: emptyMap()
                )
                CurrencyDropdown(
                    label = stringResource(R.string.label_base),
                    selected = baseCurrency,
                    options = options,
                    onSelected = { onBaseChange(it.code) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "1 ${baseCurrency.code}（${baseCurrency.nameZh}）兑换各币种数量",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when (ratesState) {
            is RatesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RatesUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.msg_error),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ratesState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            is RatesUiState.Success -> {
                val snapshot = ratesState.snapshot
                val currencies = CurrencyCatalog.availableIn(snapshot.rates)
                    .filter { it.code != baseCode }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(currencies, key = { it.code }) { currency ->
                        val rate = snapshot.rateBetween(baseCode, currency.code)
                        RateRow(
                            currency = currency,
                            rate = rate,
                            onClick = { onPickTarget(currency.code) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RateRow(
    currency: Currency,
    rate: Double?,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.nameZh,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${currency.code} · ${currency.symbol}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = FormatUtils.formatRate(rate ?: Double.NaN),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
