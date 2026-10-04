package com.fc.v2.service;

import java.util.Date;
import java.util.List;

import com.fc.v2.common.oads.OadsNoticeRunResult;
import com.fc.v2.model.auto.TOadsNoticeItem;

/**
 * 届期催办与巡查条目 Service接口（scheduling-job 形状：周期执行，无增删改查入口）
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITOadsNoticeItemService {

    /** 按主键回查条目 */
    TOadsNoticeItem selectTOadsNoticeItemById(Long id);

    /**
     * 该时刻本轮该办的条目：到期（含正压着这一瞬）或剩下的整工作日已凑进本行
     * 可提前几日之内；行本身站得住；按截止时刻先后、同一时刻按代号由小到大排序；
     * 至多五百行，其余挪到下一轮。
     */
    List<TOadsNoticeItem> listDue(Date at);

    /**
     * 夜里跑一遍：02:00-05:00 之外一行不动，回固定那句"本次未改动清单"；
     * 窗口内按规矩派发、记派不出，收尾交三个数（尚未派发/本轮派发/本轮派不出）。
     */
    OadsNoticeRunResult runOnce(Date at);
}
