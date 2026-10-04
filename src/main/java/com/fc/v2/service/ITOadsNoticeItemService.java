package com.fc.v2.service;

import java.util.Date;

import com.fc.v2.model.auto.TOadsNoticeItem;
import com.fc.v2.model.custom.OadsNoticeRunResult;

/**
 * 届期催办与巡查条目 Service接口（scheduling-job 形状：周期执行，无增删改查入口）
 *
 * 夜里替人翻《到期催办与现场巡查清单》：只在 02:00-05:00 之间执行，
 * 窗口之外按下执行键只应答不改动；落到谁名下归谁去做。
 *
 * @author fuce
 * @date 2026-10-04
 */
public interface ITOadsNoticeItemService {

    /** 按主键回查条目 */
    TOadsNoticeItem selectTOadsNoticeItemById(Long id);

    /**
     * 翻一遍清单，跑一轮。
     * 窗口（02:00-05:00）之外：一行都不许改动，原话回"本次未改动清单"；
     * 一行也没挑中：回"本轮无到期"即收工；
     * 否则收尾交三个数：尚未派发的还有几行、本轮派发几行、记成派不出几行。
     *
     * @param at 本次执行的那一刻
     * @return 本轮的交代
     */
    OadsNoticeRunResult runOnce(Date at);
}
