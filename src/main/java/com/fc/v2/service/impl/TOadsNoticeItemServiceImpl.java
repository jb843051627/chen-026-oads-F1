package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fc.v2.common.oads.OadsNoticeProperties;
import com.fc.v2.common.oads.OadsNoticeRules;
import com.fc.v2.common.oads.OadsNoticeRunResult;
import com.fc.v2.mapper.auto.TOadsNoticeItemMapper;
import com.fc.v2.mapper.auto.TOadsSpotMapper;
import com.fc.v2.model.auto.TOadsNoticeItem;
import com.fc.v2.model.auto.TOadsSpot;
import com.fc.v2.service.ITOadsNoticeItemService;

/**
 * 届期催办与巡查条目 Service业务层处理（scheduling-job 形状：周期执行）
 *
 * <p>夜里跑一遍《到期催办与现场巡查清单》，落到谁名下归谁去做。规矩：</p>
 * <ol>
 * <li>只在 02:00（含）至 05:00（不含）之间动清单；此外一律回"本次未改动清单"。</li>
 * <li>到期以该发出提醒的最后时刻为准：早于本次执行或正压着这一瞬都算本轮；
 *     读时刻只有一种解析（{@link OadsNoticeRules#ts}），挑行与回写同用。</li>
 * <li>上一轮已派出去的行（情形=已发出）不再当新事办。</li>
 * <li>可提前几日只认本行 amount 一格；数日子只数办得成事的整日：
 *     零头不算、法定节假日不算、市里停办日不算；凑不够就不点。</li>
 * <li>挑中的行按（路段目录 + 门牌段）并成一拨，一拨办完才起下一拨。</li>
 * <li>岗位本季定额装满，摊到他名下的行当场记派不出，原因写在本行上。</li>
 * <li>单行站不住（截止时刻空／可提前几日没值、为负、大得没边界／代号没填）
 *     当场记派不出并写明毛病格；标完它剩下的行照旧办完，整轮不中断、不抛异常。</li>
 * <li>办成一行留两边记号：情形挪到已发出 + 派发那一刻与接手岗位，一次更新落齐；
 *     点位已注销、联系栏空着的，直接记派不出，省得每轮空跑。</li>
 * <li>收尾交三个数：尚未派发／本轮派发／本轮派不出，只讲本轮，回清单逐行数出；
 *     挑中过五百行时按截止时刻、代号排序只办前五百，不一次读穿整张清单。</li>
 * </ol>
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TOadsNoticeItemServiceImpl implements ITOadsNoticeItemService {

    private static final Logger log = LoggerFactory.getLogger(TOadsNoticeItemServiceImpl.class);

    /** 条目情形：搁着 */
    private static final int STATUS_WAIT = 0;
    /** 条目情形：已发出 */
    private static final int STATUS_DONE = 1;
    /** 条目情形：发不成 */
    private static final int STATUS_FAIL = 2;

    /** 挑行与扫毛病都分页步长：不一口气读穿整张清单（规则九） */
    private static final int PAGE_SIZE = 500;

    @javax.annotation.Resource
    private TOadsNoticeItemMapper oadsNoticeItemMapper;

    @javax.annotation.Resource
    private TOadsSpotMapper oadsSpotMapper;

    @Autowired
    private OadsNoticeProperties noticeProperties;

    @Override
    public TOadsNoticeItem selectTOadsNoticeItemById(Long id) {
        return this.oadsNoticeItemMapper.selectById(id);
    }

    /** 办不成事的日子（yyyy-MM-dd）：法定节假日 ∪ 市里下文停办的日子 */
    private Set<String> closedDays() {
        Set<String> closed = new HashSet<String>();
        if (noticeProperties.getHolidays() != null) {
            closed.addAll(noticeProperties.getHolidays());
        }
        if (noticeProperties.getSuspensions() != null) {
            closed.addAll(noticeProperties.getSuspensions());
        }
        return closed;
    }

    /**
     * 该时刻本轮该办的条目（规则二、三、四、九）。
     *
     * <p>先按"截止时刻 ≤ 本次执行 + 可提前量上界"粗筛（上界外的一行也轮不到），
     * 再逐行按本行 amount 数整工作日定夺；排序与回写同一种墙钟解析；
     * 至多 {@link OadsNoticeRules#MAX_ROWS_PER_ROUND} 行，其余挪到下一轮。</p>
     */
    @Override
    public List<TOadsNoticeItem> listDue(Date at) {
        Set<String> closedDays = closedDays();
        // 粗筛上界：可提前量再大也不越 MAX_ADVANCE_DAYS 个整日，外加日历上停办的日子兜底；
        // 超出此界的行，剩下的整工作日必多于任何合法 amount，本轮点不着。
        Date bound = new Date(at.getTime()
                + (OadsNoticeRules.MAX_ADVANCE_DAYS + closedDays.size()) * 24L * 3600L * 1000L);
        List<TOadsNoticeItem> picked = new ArrayList<TOadsNoticeItem>();
        int offset = 0;
        while (picked.size() < OadsNoticeRules.MAX_ROWS_PER_ROUND) {
            QueryWrapper<TOadsNoticeItem> wrapper = new QueryWrapper<TOadsNoticeItem>();
            wrapper.eq("status", STATUS_WAIT)          // 规则三：已派出的不再当新事办
                    .eq("del_flag", 0)
                    .isNotNull("due_at")
                    .apply("due_at <= {0}", OadsNoticeRules.ts(bound))
                    .orderByAsc("due_at")
                    .orderByAsc("item_no")
                    .last("LIMIT " + offset + ", " + PAGE_SIZE);
            List<TOadsNoticeItem> page = this.oadsNoticeItemMapper.selectList(wrapper);
            if (page == null || page.isEmpty()) {
                break;
            }
            for (TOadsNoticeItem row : page) {
                if (OadsNoticeRules.invalidReason(row) != null) {
                    continue; // 单行站不住的归扫甲（sweepInvalidRows）记派不出，这里不挑
                }
                if (OadsNoticeRules.isDue(row, at, closedDays)) {
                    picked.add(row);
                    if (picked.size() >= OadsNoticeRules.MAX_ROWS_PER_ROUND) {
                        break;
                    }
                }
            }
            offset += page.size();
            if (page.size() < PAGE_SIZE) {
                break;
            }
        }
        // 排序口径与回写同一种解析（规则二）：截止时刻墙钟字符串，同一时刻再按代号由小到大
        picked.sort(Comparator.comparing((TOadsNoticeItem r) -> OadsNoticeRules.ts(r.getDueAt()))
                .thenComparing(r -> r.getItemNo()));
        return picked;
    }

    @Override
    public OadsNoticeRunResult runOnce(Date at) {
        OadsNoticeRunResult result = new OadsNoticeRunResult();

        // 规则一：时段闸门。窗外任何人按下执行键都照样应答，但清单一行不动，
        // 回话固定那一句——连按两次，一字不差。
        if (!OadsNoticeRules.isInWindow(at)) {
            result.setChanged(false);
            result.setMessage(OadsNoticeRules.MSG_NO_CHANGE);
            result.setPending(countPending());
            return result;
        }

        int invalidMarked = 0;
        int dispatched = 0;
        int failed = 0;
        try {
            // 扫甲：单行站不住的（规则七），当场记派不出并写明毛病格；与到期不到期无关
            invalidMarked = sweepInvalidRows(at);

            // 扫乙：挑出本轮该办的行，按拨派发
            List<TOadsNoticeItem> due = listDue(at);
            if (due.isEmpty() && invalidMarked == 0) {
                // 一行也没挑中：平常日子，回固定那句即收工，不当事故上报
                result.setChanged(false);
                result.setMessage(OadsNoticeRules.MSG_NONE_DUE);
                result.setPending(countPending());
                return result;
            }

            int[] counts = dispatchInGroups(due, at);
            dispatched = counts[0];
            failed = counts[1] + invalidMarked;

            // 规则九：三个数只讲本轮，收尾回清单逐行数出
            result.setChanged(true);
            result.setDispatched(dispatched);
            result.setFailed(failed);
            result.setPending(countPending());
            result.setMessage("本轮派发" + dispatched + "行，记成派不出" + failed
                    + "行，尚未派发" + result.getPending() + "行");
            return result;
        } catch (Exception e) {
            // 规则七：不许让整轮抛一句异常了事——已落下的记号保留，回话交代清楚
            log.error("到期催办与现场巡查清单：本轮执行中断，已办部分保留", e);
            result.setChanged(invalidMarked + dispatched + failed > 0);
            result.setDispatched(dispatched);
            result.setFailed(failed + invalidMarked);
            result.setPending(countPendingQuietly());
            result.setMessage("本轮执行中断，已办出的记号保留：" + e.getMessage());
            return result;
        }
    }

    /**
     * 扫甲：把单行站不住的搁着行当场记成派不出（规则七）。
     * 定点查毛病行，不读穿整张清单；每行独立标记，一行出事不拖垮整轮。
     */
    private int sweepInvalidRows(Date at) {
        int marked = 0;
        Set<Long> attempted = new HashSet<Long>();
        while (true) {
            QueryWrapper<TOadsNoticeItem> wrapper = new QueryWrapper<TOadsNoticeItem>();
            wrapper.eq("status", STATUS_WAIT)
                    .eq("del_flag", 0)
                    .and(w -> w.isNull("due_at")
                            .or().isNull("amount")
                            .or().lt("amount", 0)
                            .or().gt("amount", OadsNoticeRules.MAX_ADVANCE_DAYS)
                            .or().isNull("item_no")
                            .or().apply("TRIM(item_no) = ''"))
                    .last("LIMIT " + PAGE_SIZE);
            List<TOadsNoticeItem> page = this.oadsNoticeItemMapper.selectList(wrapper);
            if (page == null || page.isEmpty()) {
                break;
            }
            boolean fresh = false;
            for (TOadsNoticeItem row : page) {
                if (!attempted.add(row.getId())) {
                    continue; // 并发下没标成的行不重复试，防空转
                }
                fresh = true;
                try {
                    String reason = OadsNoticeRules.invalidReason(row);
                    if (reason != null && markFailed(row, "单行校验卡住：" + reason, at)) {
                        marked++;
                    }
                } catch (Exception e) {
                    log.error("记派不出失败，跳过该行继续, itemNo={}", row.getItemNo(), e);
                }
            }
            if (!fresh || page.size() < PAGE_SIZE) {
                break;
            }
        }
        return marked;
    }

    /**
     * 扫乙：挑中的行按（路段目录 + 门牌段）并成一拨，一拨从头到尾办完才起下一拨（规则五）；
     * 同一（路段目录＋门牌段）的行绝不拆进两拨。
     *
     * @return [本轮派发几行, 本轮记成派不出几行]
     */
    private int[] dispatchInGroups(List<TOadsNoticeItem> due, Date at) {
        int dispatched = 0;
        int failed = 0;
        Map<String, Integer> usedThisRun = new HashMap<String, Integer>();
        Map<String, Integer> quotaBase = new HashMap<String, Integer>();

        Map<String, List<TOadsNoticeItem>> groups = new LinkedHashMap<String, List<TOadsNoticeItem>>();
        for (TOadsNoticeItem row : due) {
            String key = (row.getRoadNo() == null ? "" : row.getRoadNo())
                    + "|" + (row.getDoorSeg() == null ? "" : row.getDoorSeg());
            groups.computeIfAbsent(key, k -> new ArrayList<TOadsNoticeItem>()).add(row);
        }

        for (List<TOadsNoticeItem> group : groups.values()) {
            for (TOadsNoticeItem row : group) {
                try {
                    String blockReason = dispatchBlockReason(row, at, usedThisRun, quotaBase);
                    if (blockReason != null) {
                        if (markFailed(row, blockReason, at)) {
                            failed++;
                        }
                        continue;
                    }
                    if (markDone(row, at)) {
                        dispatched++;
                        String post = row.getPostNo().trim();
                        usedThisRun.merge(post, Integer.valueOf(1), Integer::sum);
                    }
                } catch (Exception e) {
                    // 规则七：单行出事不中断整轮，记成派不出接着办
                    log.error("派发单行出事，记派不出继续, itemNo={}", row.getItemNo(), e);
                    try {
                        if (markFailed(row, "派发卡住：处理异常 " + e.getClass().getSimpleName(), at)) {
                            failed++;
                        }
                    } catch (Exception ex) {
                        log.error("补记派不出也失败，跳过该行, itemNo={}", row.getItemNo(), ex);
                    }
                }
            }
        }
        return new int[]{dispatched, failed};
    }

    /**
     * 这一行当场派不派得出去：派不出去返回原因（卡住的那一步连同毛病），
     * 派得出去返回 null。次序：天生办不成（规则八）→ 承接岗位 → 岗位定额（规则六）。
     */
    private String dispatchBlockReason(TOadsNoticeItem row, Date at,
                                       Map<String, Integer> usedThisRun,
                                       Map<String, Integer> quotaBase) {
        // 规则八·天生办不成之一：那处点位已从名册上注销
        if (spotDeregistered(row.getSpotNo())) {
            return "派发卡住：所对点位已从名册注销";
        }
        // 规则八·天生办不成之二：联系栏空着好几季没人补
        if (row.getContact() == null || row.getContact().trim().isEmpty()) {
            return "派发卡住：联系栏空着没人补";
        }
        // 接手岗位没着落，办成的两边记号补不齐（规则八），只能记派不出
        if (row.getPostNo() == null || row.getPostNo().trim().isEmpty()) {
            return "派发卡住：承接岗位未落定";
        }
        // 规则六：岗位本季派发件数已经装满
        String post = row.getPostNo().trim();
        int quota = quotaFor(post);
        int used = quotaUsedSoFar(post, at, quotaBase)
                + usedThisRun.getOrDefault(post, Integer.valueOf(0)).intValue();
        if (used >= quota) {
            return "定额核验卡住：岗位" + post + "本季派发定额" + quota + "件已装满";
        }
        return null;
    }

    /** 点位是否已从名册上注销：查无此点位或删除标记已立的，都算注销 */
    private boolean spotDeregistered(String spotNo) {
        if (spotNo == null || spotNo.trim().isEmpty()) {
            return false; // 行上没对点位，无从查起，不拿这条卡它
        }
        QueryWrapper<TOadsSpot> wrapper = new QueryWrapper<TOadsSpot>();
        wrapper.eq("spot_no", spotNo.trim()).last("LIMIT 1");
        List<TOadsSpot> spots = this.oadsSpotMapper.selectList(wrapper);
        if (spots == null || spots.isEmpty()) {
            return true;
        }
        Integer delFlag = spots.get(0).getDelFlag();
        return delFlag != null && delFlag.intValue() != 0;
    }

    /** 该岗位本季派发定额：单列的认单列，其余认公用默认 */
    private int quotaFor(String post) {
        Integer quota = noticeProperties.getQuotaByPost() == null
                ? null : noticeProperties.getQuotaByPost().get(post);
        return quota != null ? quota.intValue() : noticeProperties.getQuotaDefault();
    }

    /**
     * 该岗位本季已派发的件数（库里数出，每岗位本轮只查一次）：
     * 情形=已发出、派发那一刻落在本季之内。时刻比较与回写同一种墙钟解析。
     */
    private int quotaUsedSoFar(String post, Date at, Map<String, Integer> quotaBase) {
        Integer cached = quotaBase.get(post);
        if (cached != null) {
            return cached.intValue();
        }
        Date[] quarter = OadsNoticeRules.quarterOf(at);
        QueryWrapper<TOadsNoticeItem> wrapper = new QueryWrapper<TOadsNoticeItem>();
        wrapper.eq("status", STATUS_DONE)
                .eq("del_flag", 0)
                .eq("post_no", post)
                .apply("sent_at >= {0}", OadsNoticeRules.ts(quarter[0]))
                .apply("sent_at < {0}", OadsNoticeRules.ts(quarter[1]));
        Integer count = this.oadsNoticeItemMapper.selectCount(wrapper);
        int used = count == null ? 0 : count.intValue();
        quotaBase.put(post, Integer.valueOf(used));
        return used;
    }

    /**
     * 办成一行（规则八）：两边记号一次更新落齐——情形挪到已发出，
     * 补上派发那一刻与接手的岗位；缺任何一处都不算办完。
     * 只挪还搁着的行（status=0），并发下别人动过的行不抢。
     */
    private boolean markDone(TOadsNoticeItem row, Date at) {
        UpdateWrapper<TOadsNoticeItem> wrapper = new UpdateWrapper<TOadsNoticeItem>();
        wrapper.eq("id", row.getId())
                .eq("status", STATUS_WAIT)
                .set("status", STATUS_DONE)
                .set("sent_at", OadsNoticeRules.ts(at)) // 回写与挑行同一种解析（规则二）
                .set("post_no", row.getPostNo().trim())
                .set("update_time", OadsNoticeRules.ts(at));
        return this.oadsNoticeItemMapper.update(null, wrapper) == 1;
    }

    /**
     * 记成派不出：情形挪到发不成，把卡住的那一步连同原因写在本行上，
     * 日后照着这个原因还能把这一批重排一次。只挪还搁着的行。
     */
    private boolean markFailed(TOadsNoticeItem row, String reason, Date at) {
        UpdateWrapper<TOadsNoticeItem> wrapper = new UpdateWrapper<TOadsNoticeItem>();
        wrapper.eq("id", row.getId())
                .eq("status", STATUS_WAIT)
                .set("status", STATUS_FAIL)
                .set("fail_reason", reason)
                .set("update_time", OadsNoticeRules.ts(at));
        return this.oadsNoticeItemMapper.update(null, wrapper) == 1;
    }

    /** 尚未派发的还有几行：回清单逐行数出（页面看到的与库里数出的出自同一次计算） */
    private int countPending() {
        QueryWrapper<TOadsNoticeItem> wrapper = new QueryWrapper<TOadsNoticeItem>();
        wrapper.eq("status", STATUS_WAIT).eq("del_flag", 0);
        Integer count = this.oadsNoticeItemMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    /** 收尾数行数也不许把整轮炸掉：数不出来就当零，回话照给 */
    private int countPendingQuietly() {
        try {
            return countPending();
        } catch (Exception e) {
            log.error("数尚未派发行数失败", e);
            return 0;
        }
    }
}
