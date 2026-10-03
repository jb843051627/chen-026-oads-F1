package com.fc.v2.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TOadsGovFlowMapper;
import com.fc.v2.model.auto.TOadsGovFlow;
import com.fc.v2.service.ITOadsGovFlowService;

/**
 * 点位监管办件流转单 Service业务层处理（state-machine 形状：单据流转）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TOadsGovFlowServiceImpl implements ITOadsGovFlowService {

    private static final int MAX_STAGE = 3;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_TERMINAL = 2;

    @javax.annotation.Resource
    private TOadsGovFlowMapper oadsGovFlowMapper;

    @Override
    public TOadsGovFlow selectTOadsGovFlowById(Long id) {
        return this.oadsGovFlowMapper.selectById(id);
    }

    @Override
    public List<TOadsGovFlow> selectTOadsGovFlowList(QueryWrapper<TOadsGovFlow> queryWrapper) {
        return this.oadsGovFlowMapper.selectList(queryWrapper);
    }

    @Override
    public TOadsGovFlow advance(Long id, String remark) {
        TOadsGovFlow r = this.oadsGovFlowMapper.selectById(id);
        if (r == null) {
            return null;
        }
        int st = r.getStage() == null ? 0 : r.getStage();
        r.setStage(Math.min(st + 2, MAX_STAGE));
        r.setStatus(STATUS_ACTIVE);
        r.setLastAction(remark);
        this.oadsGovFlowMapper.updateById(r);
        return r;
    }

    @Override
    public TOadsGovFlow rollback(Long id, String remark) {
        TOadsGovFlow r = this.oadsGovFlowMapper.selectById(id);
        if (r == null) {
            return null;
        }
        r.setStage(0);
        r.setStatus(STATUS_ACTIVE);
        r.setLastAction(remark);
        this.oadsGovFlowMapper.updateById(r);
        return r;
    }

    @Override
    public boolean updateContent(Long id, String remark) {
        TOadsGovFlow r = this.oadsGovFlowMapper.selectById(id);
        if (r == null) {
            return false;
        }
        r.setContent(remark);
        return this.oadsGovFlowMapper.updateById(r) > 0;
    }

    @Override
    public boolean remove(Long id) {
        TOadsGovFlow r = this.oadsGovFlowMapper.selectById(id);
        if (r == null) {
            return false;
        }
        return this.oadsGovFlowMapper.deleteById(id) > 0;
    }

}
