package com.exchangerate.calculator.data

/**
 * 币种目录：代码 + 中文名 + 符号 + 金额小数位
 * 覆盖欧洲央行（ECB）公布汇率的主要货币
 */
data class Currency(
    val code: String,
    val nameZh: String,
    val symbol: String,
    val fractionDigits: Int = 2
)

object CurrencyCatalog {

    val all: List<Currency> = listOf(
        Currency("USD", "美元", "$"),
        Currency("CNY", "人民币", "¥"),
        Currency("EUR", "欧元", "€"),
        Currency("JPY", "日元", "¥", 0),
        Currency("GBP", "英镑", "£"),
        Currency("HKD", "港元", "HK$"),
        Currency("KRW", "韩元", "₩", 0),
        Currency("AUD", "澳元", "A$"),
        Currency("CAD", "加元", "C$"),
        Currency("CHF", "瑞士法郎", "Fr"),
        Currency("SGD", "新加坡元", "S$"),
        Currency("THB", "泰铢", "฿"),
        Currency("MYR", "马来西亚林吉特", "RM"),
        Currency("INR", "印度卢比", "₹"),
        Currency("PHP", "菲律宾比索", "₱"),
        Currency("IDR", "印尼卢比", "Rp", 0),
        Currency("NZD", "新西兰元", "NZ$"),
        Currency("SEK", "瑞典克朗", "kr"),
        Currency("NOK", "挪威克朗", "kr"),
        Currency("DKK", "丹麦克朗", "kr"),
        Currency("MXN", "墨西哥比索", "Mex$"),
        Currency("BRL", "巴西雷亚尔", "R$"),
        Currency("ZAR", "南非兰特", "R"),
        Currency("TRY", "土耳其里拉", "₺"),
        Currency("PLN", "波兰兹罗提", "zł"),
        Currency("CZK", "捷克克朗", "Kč"),
        Currency("HUF", "匈牙利福林", "Ft", 0),
        Currency("RON", "罗马尼亚列伊", "lei"),
        Currency("ILS", "以色列新谢克尔", "₪"),
        Currency("ISK", "冰岛克朗", "kr", 0)
    )

    private val byCode: Map<String, Currency> = all.associateBy { it.code }

    fun find(code: String): Currency =
        byCode[code] ?: Currency(code, code, code)

    /** 只保留汇率数据中实际存在的币种，保证列表可用 */
    fun availableIn(rates: Map<String, Double>): List<Currency> =
        if (rates.isEmpty()) all else all.filter { rates.containsKey(it.code) }
}
