package com.exchangerate.calculator.data

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 缓存新鲜度判定。
 *
 * 汇率数据是欧洲央行参考汇率，每个工作日约 16:00（CET/CEST）发布一次，
 * 一天之内不会变化。因此用「挂钟 TTL」判断新旧没有意义——30 分钟前抓的数据
 * 和刚抓的数据在内容上完全一样。这里改为以**最近一次发布时刻**为界：
 * 抓取时间晚于该时刻，说明缓存已经是当前这批汇率，无需重新请求；
 * 早于该时刻，说明央行已发布新数据，应当刷新。
 *
 * 说明：本实现只跳过周末，不含欧洲央行节假日日历。遇到节假日会多一次网络请求，
 * 返回的数据仍与缓存同日期，属于可接受的代价。
 */
object RateFreshness {

    private val ECB_ZONE: ZoneId = ZoneId.of("Europe/Berlin")

    /** 欧洲央行发布参考汇率的时间点（当地时间 16:00） */
    private const val PUBLISH_HOUR = 16

    /**
     * 最近一次已经发生的发布时刻。
     * 例：周一 10:00 → 上周五 16:00；周一 17:00 → 本周一 16:00；周六任意时刻 → 周五 16:00。
     */
    fun lastPublishInstant(now: ZonedDateTime = ZonedDateTime.now(ECB_ZONE)): ZonedDateTime {
        var candidate = now
            .withHour(PUBLISH_HOUR)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (now.isBefore(candidate)) {
            candidate = candidate.minusDays(1)
        }
        while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY) {
            candidate = candidate.minusDays(1)
        }
        return candidate
    }

    /** 抓取时刻是否仍属于当前这批汇率（即：在最近一次发布之后抓的） */
    fun isFresh(fetchedAt: Long, now: ZonedDateTime = ZonedDateTime.now(ECB_ZONE)): Boolean =
        fetchedAt >= lastPublishInstant(now).toInstant().toEpochMilli()
}
