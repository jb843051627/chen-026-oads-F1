package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TOadsSpot;
import com.fc.v2.service.ITOadsSpotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 点位底册 Controller
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "点位底册")
@Controller
@RequestMapping("/OadsSpotController")
public class OadsSpotController extends BaseController {

    private final String prefix = "admin/oadsSpot";

    @Autowired
    private ITOadsSpotService oadsSpotService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("oads:oadsSpot:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "点位底册集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("oads:oadsSpot:list")
    @ResponseBody
    public ResultTable list(TOadsSpot record) {
        QueryWrapper<TOadsSpot> queryWrapper = new QueryWrapper<TOadsSpot>();
        startPage();
        com.github.pagehelper.PageInfo<TOadsSpot> page =
                new com.github.pagehelper.PageInfo<TOadsSpot>(oadsSpotService.selectTOadsSpotList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "点位底册新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("/add")
    @RequiresPermissions("oads:oadsSpot:add")
    @ResponseBody
    public AjaxResult add(TOadsSpot record) {
        return toAjax(oadsSpotService.insertTOadsSpot(record));
    }

    @Log(title = "点位底册修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("oads:oadsSpot:edit")
    @ResponseBody
    public AjaxResult editSave(TOadsSpot record) {
        return toAjax(oadsSpotService.updateTOadsSpot(record));
    }

    @Log(title = "点位底册删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("oads:oadsSpot:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(oadsSpotService.deleteTOadsSpotByIds(ids));
    }
}
