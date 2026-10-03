package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TOadsGovFlow;
import com.fc.v2.service.ITOadsGovFlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 点位监管办件流转单 Controller（state-machine 形状：流转入口）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Api(value = "点位监管办件流转单")
@Controller
@RequestMapping("/oadsGovFlow")
public class OadsGovFlowController extends BaseController {

    private final String prefix = "admin/oadsGovFlow";

    @Autowired
    private ITOadsGovFlowService oadsGovFlowService;

    @ApiOperation(value = "流转台账跳转", notes = "流转台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("oadsGovFlow:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "点位监管办件流转单流转台账", action = "list")
    @ApiOperation(value = "流转台账", notes = "流转台账")
    @GetMapping("/list")
    @RequiresPermissions("oadsGovFlow:list")
    @ResponseBody
    public ResultTable list(TOadsGovFlow record) {
        QueryWrapper<TOadsGovFlow> queryWrapper = new QueryWrapper<TOadsGovFlow>();
        startPage();
        com.github.pagehelper.PageInfo<TOadsGovFlow> page =
                new com.github.pagehelper.PageInfo<TOadsGovFlow>(oadsGovFlowService.selectTOadsGovFlowList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "点位监管办件流转单推进", action = "advance")
    @ApiOperation(value = "推进一档", notes = "推进一档")
    @PostMapping("/advance")
    @RequiresPermissions("oadsGovFlow:advance")
    @ResponseBody
    public AjaxResult advance(Long id, String remark) {
        return toAjax(oadsGovFlowService.advance(id, remark) != null ? 1 : 0);
    }

    @Log(title = "点位监管办件流转单回退", action = "rollback")
    @ApiOperation(value = "回退一档", notes = "回退一档")
    @PostMapping("/rollback")
    @RequiresPermissions("oadsGovFlow:rollback")
    @ResponseBody
    public AjaxResult rollback(Long id, String remark) {
        return toAjax(oadsGovFlowService.rollback(id, remark) != null ? 1 : 0);
    }
}
