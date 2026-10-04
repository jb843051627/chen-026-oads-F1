package com.fc.v2.common.oads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fc.v2.model.auto.TOadsNoticeItem;

/**
 * 夜间轮次纯规则的边界用例：窗口、数日子、单行站不住、该不该点、季度。
 */
class OadsNoticeRulesTest {

    private static Date at(String s) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static TOadsNoticeItem row(String dueAt, String amount, String itemNo) {
        TOadsNoticeItem r = new TOadsNoticeItem();
        r.setDueAt(dueAt == null ? null : at(dueAt));
        r.setAmount(amount == null ? null : new BigDecimal(amount));
        r.setItemNo(itemNo);
        return r;
    }

    // ---- 规则一：02:00（含）至 05:00（不含） ----

    @Test
    void windowBoundaries() {
        assertFalse(OadsNoticeRules.isInWindow(at("2026-10-04 01:59:59")));
        assertTrue(OadsNoticeRules.isInWindow(at("2026-10-04 02:00:00")));
        assertTrue(OadsNoticeRules.isInWindow(at("2026-10-04 03:30:00")));
        assertTrue(OadsNoticeRules.isInWindow(at("2026-10-04 04:59:59")));
        assertFalse(OadsNoticeRules.isInWindow(at("2026-10-04 05:00:00"))); // 过 05:00 立即收工
        assertFalse(OadsNoticeRules.isInWindow(at("2026-10-04 12:00:00")));
    }

    // ---- 规则四：从该发时刻往回数办得成事的整日 ----

    @Test
    void windowOpenWalksBackFullWorkableDays() {
        Date due = at("2026-10-07 09:00:00");
        // 可提前 0 日：开窗时刻就是该发时刻本身，一秒也不许提前
        assertEquals("2026-10-07 09:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 0, null)));
        // 每凑满 24 小时算一日
        assertEquals("2026-10-06 09:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 1, null)));
        assertEquals("2026-10-04 09:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 3, null)));
    }

    @Test
    void windowOpenSkipsClosedDays() {
        Date due = at("2026-10-07 09:00:00");
        Set<String> closed = new HashSet<String>();
        closed.add("2026-10-06"); // 法定节假日：落点压在这一天的一整日段不算数
        closed.add("2026-10-03"); // 停办日：不在回数路径上，无影响
        // 可提前 2 日：10-06 停办不算，须再往前凑一日 → 10-04 09:00 开窗
        assertEquals("2026-10-04 09:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 2, closed)));
        // 没有对表时，可提前 2 日 → 10-05 09:00 开窗
        assertEquals("2026-10-05 09:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 2, null)));
    }

    // ---- 规则七：单行站不住 ----

    @Test
    void invalidReasonCoversEveryBadCell() {
        assertNotNull(OadsNoticeRules.invalidReason(row(null, "3", "A-1")));            // 截止时刻空
        assertNotNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", null, "A-1"))); // 可提前几日没值
        assertNotNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "-1", "A-1"))); // 负数
        assertNotNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "99999999", "A-1"))); // 大得没有边界
        assertNotNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "3", null)));   // 代号没填
        assertNotNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "3", "  ")));   // 代号空白
        assertNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "3", "A-1")));     // 站得住
        assertNull(OadsNoticeRules.invalidReason(row("2026-10-04 03:00:00", "0", "A-1")));     // 零日也站得住
    }

    // ---- 规则二＋四：本轮该不该点 ----

    @Test
    void dueWhenLastMomentPassedOrExactlyNow() {
        Date now = at("2026-10-04 03:00:00");
        assertTrue(OadsNoticeRules.isDue(row("2026-10-04 02:59:59", "0", "A-1"), now, null)); // 早于本次执行
        assertTrue(OadsNoticeRules.isDue(row("2026-10-04 03:00:00", "0", "A-1"), now, null)); // 正压着这一瞬
        assertFalse(OadsNoticeRules.isDue(row("2026-10-04 03:00:01", "0", "A-1"), now, null)); // 还没到
    }

    @Test
    void dueWithinAdvanceWindow() {
        Date now = at("2026-10-04 03:00:00");
        // 剩 3 个整日，可提前 3 日：点
        assertTrue(OadsNoticeRules.isDue(row("2026-10-07 03:00:00", "3", "A-1"), now, null));
        // 剩 4 个整日，可提前 3 日：凑不进，不点
        assertFalse(OadsNoticeRules.isDue(row("2026-10-08 03:00:00", "3", "A-1"), now, null));
        // 可提前 0 日：只有压着或过了才点
        assertFalse(OadsNoticeRules.isDue(row("2026-10-04 23:59:59", "0", "A-1"), now, null));
    }

    @Test
    void fractionalRemainderBuysNoEarliness() {
        // 可提前 1 日：开窗时刻 = 该发时刻往回凑满 24 小时，零头不顶数
        Date due = at("2026-10-05 15:00:00");
        assertEquals("2026-10-04 15:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 1, null)));
        assertFalse(OadsNoticeRules.isDue(row("2026-10-05 15:00:00", "1", "A-1"), at("2026-10-04 14:59:59"), null));
        assertTrue(OadsNoticeRules.isDue(row("2026-10-05 15:00:00", "1", "A-1"), at("2026-10-04 15:00:00"), null));
    }

    @Test
    void holidayPushesWindowEarlier() {
        Date now = at("2026-10-04 03:00:00");
        Date due = at("2026-10-07 03:00:00");
        Set<String> closed = new HashSet<String>(Collections.singletonList("2026-10-06"));
        // 10-06 停办：往回数时落点压在 10-06 的整日段不算，可提前 2 日须多让一日
        // → 10-04 03:00 就开窗，本轮点得着
        assertEquals("2026-10-04 03:00:00", OadsNoticeRules.ts(OadsNoticeRules.windowOpenAt(due, 2, closed)));
        assertTrue(OadsNoticeRules.isDue(row("2026-10-07 03:00:00", "2", "A-1"), now, closed));
        // 没有停办日：可提前 2 日 → 10-05 03:00 才开窗，10-04 03:00 还点不着
        assertFalse(OadsNoticeRules.isDue(row("2026-10-07 03:00:00", "2", "A-1"), now, null));
    }

    // ---- 季度定额的起止 ----

    @Test
    void quarterBounds() {
        Date[] q = OadsNoticeRules.quarterOf(at("2026-10-04 03:00:00"));
        assertEquals("2026-10-01 00:00:00", OadsNoticeRules.ts(q[0]));
        assertEquals("2027-01-01 00:00:00", OadsNoticeRules.ts(q[1]));
        Date[] q1 = OadsNoticeRules.quarterOf(at("2026-02-01 00:00:00"));
        assertEquals("2026-01-01 00:00:00", OadsNoticeRules.ts(q1[0]));
        assertEquals("2026-04-01 00:00:00", OadsNoticeRules.ts(q1[1]));
    }
}
