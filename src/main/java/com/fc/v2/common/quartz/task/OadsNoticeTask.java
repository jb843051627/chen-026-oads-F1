package com.fc.v2.common.quartz.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fc.v2.common.oads.OadsNoticeRunResult;
import com.fc.v2.service.ITOadsNoticeItemService;

/**
 * 到期催办与现场巡查清单——夜班任务。
 *
 * <p>在调度中心把 cron 配在 02:00-05:00 之间（如 {@code 0 30 2 * * ?}），
 * 调用目标填 {@code oadsNoticeTask.runNightly()}。窗口判断在服务里把守，
 * 任务本身不再卡钟点：即便被提前或延后触发，窗外也一行不动。</p>
 *
 * @author fuce
 * @date 2026-10-04
 */
@Component("oadsNoticeTask")
public class OadsNoticeTask {

    private static final Logger log = LoggerFactory.getLogger(OadsNoticeTask.class);

    @Autowired
    private ITOadsNoticeItemService oadsNoticeItemService;

    /** 无参夜班任务：夜里跑一遍清单 */
    public void runNightly() {
        OadsNoticeRunResult result = oadsNoticeItemService.runOnce(new Date());
        log.info("到期催办与现场巡查清单夜班：{}", result);
    }
}
