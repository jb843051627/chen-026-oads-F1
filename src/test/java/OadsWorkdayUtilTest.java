import com.fc.v2.util.OadsWorkdayUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 届期催办与巡查：墙钟与办工日算法的纯逻辑用例（不落库）。
 */
public class OadsWorkdayUtilTest {

    private static Date at(String s) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
    }

    @Test
    public void windowBoundaries() throws Exception {
        assertFalse(OadsWorkdayUtil.inWindow(at("2026-10-05 01:59:59")), "02:00 前不执行");
        assertTrue(OadsWorkdayUtil.inWindow(at("2026-10-05 02:00:00")), "02:00 正点起算");
        assertTrue(OadsWorkdayUtil.inWindow(at("2026-10-05 04:59:59")), "05:00 前一刻仍在窗口");
        assertFalse(OadsWorkdayUtil.inWindow(at("2026-10-05 05:00:00")), "过 05:00 立即收工");
        assertFalse(OadsWorkdayUtil.inWindow(at("2026-10-05 14:00:00")), "白天不执行");
        assertFalse(OadsWorkdayUtil.inWindow(null), "空时刻不执行");
    }

    @Test
    public void advanceDaysDropsFraction() {
        assertEquals(3, OadsWorkdayUtil.advanceDays(new BigDecimal("3.99")), "零头不算");
        assertEquals(0, OadsWorkdayUtil.advanceDays(new BigDecimal("0.00")));
        assertEquals(5, OadsWorkdayUtil.advanceDays(new BigDecimal("5")));
    }

    @Test
    public void triggerZeroDaysIsDueMomentItself() throws Exception {
        Date due = at("2026-10-10 15:30:00");
        LocalDateTime t = OadsWorkdayUtil.trigger(due, 0, new HashSet<LocalDate>());
        assertEquals(LocalDateTime.of(2026, 10, 10, 15, 30, 0), t);
    }

    @Test
    public void triggerWalksBackFullWorkdaysOnly() throws Exception {
        Date due = at("2026-10-10 15:30:00");
        // 10-09 法定节假日、10-08 市里停办：都不算；往前凑 3 个能办事的日子 -> 10-07/10-06/10-05
        Set<LocalDate> rest = new HashSet<LocalDate>();
        rest.add(LocalDate.of(2026, 10, 9));
        rest.add(LocalDate.of(2026, 10, 8));
        LocalDateTime t = OadsWorkdayUtil.trigger(due, 3, rest);
        assertEquals(LocalDateTime.of(2026, 10, 5, 15, 30, 0), t, "休办日跳过，时分不动");
    }

    @Test
    public void triggerWithoutRestDays() throws Exception {
        Date due = at("2026-10-10 08:00:00");
        LocalDateTime t = OadsWorkdayUtil.trigger(due, 2, null);
        assertEquals(LocalDateTime.of(2026, 10, 8, 8, 0, 0), t);
    }

    @Test
    public void quarterCodeRolls() throws Exception {
        assertEquals("2026Q1", OadsWorkdayUtil.quarterCode(at("2026-01-01 03:00:00")));
        assertEquals("2026Q1", OadsWorkdayUtil.quarterCode(at("2026-03-31 03:00:00")));
        assertEquals("2026Q2", OadsWorkdayUtil.quarterCode(at("2026-04-01 03:00:00")));
        assertEquals("2026Q4", OadsWorkdayUtil.quarterCode(at("2026-10-04 03:00:00")));
    }

    @Test
    public void tsRoundTripsInOneFrame() throws Exception {
        Date d = at("2026-10-05 03:00:00");
        assertEquals("2026-10-05 03:00:00", OadsWorkdayUtil.ts(d));
        assertEquals(LocalDateTime.of(2026, 10, 5, 3, 0, 0), OadsWorkdayUtil.toLocal(d));
    }
}
