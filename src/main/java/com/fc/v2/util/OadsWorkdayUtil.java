package com.fc.v2.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

/**
 * 届期催办与巡查：墙钟与办工日算法。
 *
 * 全类只用一种解析方式——本机默认时区的墙钟（JDBC 的 serverTimezone 与本机
 * 时区不对称，直接把 java.util.Date 作参数会整体偏移）。挑行与回写两边同一把尺，
 * 早先进库的老记录与今年新添的也用同一种算法，不另给一套说法。
 *
 * @author fuce
 * @date 2026-10-04
 */
public final class OadsWorkdayUtil {

    private OadsWorkdayUtil() {
    }

    /** 夜间执行窗口起点（含）：02:00 */
    public static final LocalTime WIN_FROM = LocalTime.of(2, 0);
    /** 夜间执行窗口终点（不含）：05:00，过点立即收工 */
    public static final LocalTime WIN_TO = LocalTime.of(5, 0);

    /** 可提前几日的上界：超过即视为大得没有边界 */
    public static final int MAX_ADVANCE_DAYS = 366;

    /** 联系栏空窗达到这个月数，记成派不出（空着好几季没人补） */
    public static final int CONTACT_STALE_MONTHS = 6;

    /** 统一墙钟格式 */
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 统一墙钟：Date -> 本机默认时区的年月日时分秒 */
    public static LocalDateTime toLocal(Date d) {
        return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    /** 统一墙钟字符串（与 toLocal 同一把尺） */
    public static String ts(Date d) {
        LocalDateTime l = toLocal(d);
        return l == null ? null : FMT.format(l);
    }

    /** 此刻是否落在夜间执行窗口内：[02:00, 05:00) */
    public static boolean inWindow(Date at) {
        LocalDateTime l = toLocal(at);
        if (l == null) {
            return false;
        }
        LocalTime t = l.toLocalTime();
        return !t.isBefore(WIN_FROM) && t.isBefore(WIN_TO);
    }

    /**
     * 可提前几日取整：凑不满一整日的零头不算，只往满里凑。
     * 调用前须已排除无值、负数、大得没有边界三种站不住的情形。
     */
    public static int advanceDays(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.FLOOR).intValue();
    }

    /**
     * 该发出提醒的最后时刻：从到期日的前一日起（到期当日只是零头，不算一整日），
     * 一日一日往满里凑够 workdays 个真正办得成事的日子——法定节假日不算、市里下文
     * 停办的日子不算（只认休办日历表）；凑到的最末一日带上到期当日的时分，即为
     * 最后时刻。该时刻早于本次执行的那一刻、或正压在这一瞬，都算本轮要办的。
     */
    public static LocalDateTime trigger(Date dueAt, int workdays, Set<LocalDate> restDays) {
        LocalDateTime due = toLocal(dueAt);
        LocalDate day = due.toLocalDate();
        int gathered = 0;
        while (gathered < workdays) {
            day = day.minusDays(1);
            if (restDays == null || !restDays.contains(day)) {
                gathered++;
            }
        }
        return LocalDateTime.of(day, due.toLocalTime());
    }

    /** 季度码，如 2026Q4 */
    public static String quarterCode(Date at) {
        LocalDate d = toLocal(at).toLocalDate();
        return d.getYear() + "Q" + ((d.getMonthValue() - 1) / 3 + 1);
    }
}
