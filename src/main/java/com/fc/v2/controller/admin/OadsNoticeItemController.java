package com.fc.v2.controller.admin;

import java.util.Date;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.custom.OadsNoticeRunResult;
import com.fc.v2.service.ITOadsNoticeItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 届期催办与巡查条目 Controller（scheduling-job 形状：只有执行键，无增删改查入口）
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
     * 执行键：任何人都可以按下，程序照样应答；
     * 夜间窗口（02:00-05:00）之外按下一行都不改动，原话回"本次未改动清单"。
     */
    @Log(title = "届期催办与巡查", action = "run")
    @ApiOperation(value = "执行一轮", notes = "窗口外按下只应答不改动")
    @PostMapping("/run")
    @ResponseBody
    public AjaxResult run() {
        OadsNoticeRunResult result = oadsNoticeItemService.runOnce(new Date());
        return AjaxResult.successData(200, result).put("msg", result.getMessage());
    }
}
