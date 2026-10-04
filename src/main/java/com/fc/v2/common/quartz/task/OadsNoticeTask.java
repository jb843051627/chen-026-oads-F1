package com.fc.v2.common.quartz.task;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fc.v2.model.custom.OadsNoticeRunResult;
import com.fc.v2.service.ITOadsNoticeItemService;

/**
 * 届期催办与巡查：夜里翻清单的定时任务。
 * 调度中心把调用目标配成 oadsNoticeTask.runNightly() 即可；
 * 程序自己把守 02:00-05:00 的窗口，窗口外只应答不改动。
 *
 * @author fuce
 * @date 2026-10-04
 */
@Component("oadsNoticeTask")
public class OadsNoticeTask {

    @Autowired
    private ITOadsNoticeItemService oadsNoticeItemService;

    /**
     * 翻一遍《到期催办与现场巡查清单》，跑一轮。
     *
     * @return 本轮的交代（三个数只讲本次这一轮）
     */
    public OadsNoticeRunResult runNightly() {
        return oadsNoticeItemService.runOnce(new Date());
    }
}
