package com.fc.v2.controller.admin;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.common.oads.OadsNoticeRunResult;
import com.fc.v2.service.ITOadsNoticeItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 届期催办与巡查条目 Controller——夜间轮次的执行键
 *
 * @author fuce
 * @date 2026-10-04
 */
@Api(value = "届期催办与巡查条目")
@Controller
@RequestMapping("/OadsNoticeItemController")
public class OadsNoticeItemController extends BaseController {

    @Autowired
    private ITOadsNoticeItemService oadsNoticeItemService;

    /**
     * 执行键：任何人都可以按下。夜间 02:00-05:00 之内跑一轮；
     * 这一时段之外程序照样应答，但清单上一行都不许改动，
     * 回固定那句"本次未改动清单"——连按两次，一字不差。
     * 页面看到的三个数与库里数出的出自同一次计算（就是返回的这个结果）。
     */
    @Log(title = "到期催办与现场巡查清单夜间轮次", action = "runOnce")
    @ApiOperation(value = "执行一轮", notes = "夜间02:00-05:00执行，其余时刻一行不改")
    @PostMapping("/runOnce")
    @ResponseBody
    public AjaxResult runOnce() {
        OadsNoticeRunResult result = oadsNoticeItemService.runOnce(new Date());
        return AjaxResult.success(200, result.getMessage(), result);
    }
}
