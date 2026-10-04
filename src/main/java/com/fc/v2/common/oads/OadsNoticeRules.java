package com.fc.v2.common.oads;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import com.fc.v2.model.auto.TOadsNoticeItem;

/**
 * 到期催办与现场巡查清单——夜间轮次的纯规则（不碰库，便于单测）。
 *
 * <p>解析口径只有一条（规则二）：凡是"时刻"，一律按 JVM 默认时区的墙钟
 * {@code yyyy-MM-dd HH:mm:ss} 读与写。挑行与回写同走 {@link #ts(Date)}，
 * 两边不各搞一套——年末两张清单对不住，都是从各用一套解析开始的。</p>
 *
 * <p>早先进库的老记录与今年新添的走同一套算法（规则四）：这里只看起止两个时刻
 * 和行自己的格子，不认 create_time 的早晚。</p>
 */
public final class OadsNoticeRules {

    private OadsNoticeRules() {
    }

    /** 执行窗口起点：夜间 02:00（含） */
    public static final int WINDOW_FROM_HOUR = 2;

    /** 执行窗口终点：夜间 05:00（不含，过点立即收工） */
    public static final int WINDOW_TO_HOUR = 5;

    /** 可提前几日的 sanity 上限（十年）。再大的数视为"大得没有边界"（规则七） */
    public static final int MAX_ADVANCE_DAYS = 3650;

    /** 一轮最多处理的行数（规则九），其余挪到下一轮 */
    public static final int MAX_ROWS_PER_ROUND = 500;

    /** 时段之外的固定回话（规则一：连按两次，一字不差） */
    public static final String MSG_NO_CHANGE = "本次未改动清单";

    /** 一行也没挑中的固定回话（平常日子，不当事故上报） */
    public static final String MSG_NONE_DUE = "本轮无到期";

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    /** 墙钟字符串——挑行与回写共用这一种解析 */
    public static String ts(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }

    /** 日历日 yyyy-MM-dd（法定节假日、停办日都按日历日对表） */
    public static String day(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    /** 此刻是否落在执行窗口内：02:00（含）至 05:00（不含） */
    public static boolean isInWindow(Date at) {
        Calendar c = Calendar.getInstance();
        c.setTime(at);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour >= WINDOW_FROM_HOUR && hour < WINDOW_TO_HOUR;
    }

    /**
     * 单行站不住的情形（规则七）：返回毛病出在哪一格；站得住返回 null。
     * 检查次序固定：截止时刻 → 可提前几日 → 代号。
     */
    public static String invalidReason(TOadsNoticeItem row) {
        if (row.getDueAt() == null) {
            return "截止时刻(due_at)那一格是空的";
        }
        if (row.getAmount() == null) {
            return "可提前几日(amount)没有值";
        }
        if (row.getAmount().signum() < 0) {
            return "可提前几日(amount)是负数";
        }
        if (row.getAmount().compareTo(BigDecimal.valueOf(MAX_ADVANCE_DAYS)) > 0) {
            return "可提前几日(amount)大得没有边界";
        }
        if (row.getItemNo() == null || row.getItemNo().trim().isEmpty()) {
            return "代号(item_no)压根没填";
        }
        return null;
    }

    /** 该行的可提前几日：只认本行写定的那个数，取整数日（零头不算） */
    public static int advanceDaysOf(TOadsNoticeItem row) {
        return row.getAmount().intValue();
    }

    /**
     * 开窗时刻：从该发催办的最后时刻往回，一日一日往满里凑，每凑满一个
     * "办得成事"的整日（24 小时）算一日；这一整日段的落点压在法定节假日
     * 或市里下文停办的日子上的，不算数，但日子照样走过去。凑满 amount
     * 个整工作日所到的那一瞬间，就是本轮可以动手发催办的最早时刻。
     *
     * <p>amount 为 0 时开窗时刻就是该发时刻本身——一日也不许提前。
     * amount 大得没有边界的行在规则七已被判成站不住，走不到这里来。</p>
     */
    public static Date windowOpenAt(Date dueAt, int amount, Set<String> closedDays) {
        long cursor = dueAt.getTime();
        int needed = amount;
        while (needed > 0) {
            // 这一整日段 (cursor-24h, cursor] 的落点在 cursor 所在的日历日
            if (closedDays == null || !closedDays.contains(day(new Date(cursor)))) {
                needed--;
            }
            cursor -= DAY_MS;
        }
        return new Date(cursor);
    }

    /**
     * 本行本轮该不该点（规则二＋规则四）：本次执行的那一刻已经走到或越过
     * 开窗时刻就点——该发时刻早于本次执行、或正压着这一瞬，自然都在其内；
     * 日子还没走到的原样搁着，让它自己到期。
     */
    public static boolean isDue(TOadsNoticeItem row, Date at, Set<String> closedDays) {
        Date openAt = windowOpenAt(row.getDueAt(), advanceDaysOf(row), closedDays);
        return ts(at).compareTo(ts(openAt)) >= 0;
    }

    /** at 所在季度的起止（含头不含尾），供岗位季度定额数件数 */
    public static Date[] quarterOf(Date at) {
        Calendar c = Calendar.getInstance();
        c.setTime(at);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) / 3 * 3);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();
        c.add(Calendar.MONTH, 3);
        return new Date[]{start, c.getTime()};
    }
}
