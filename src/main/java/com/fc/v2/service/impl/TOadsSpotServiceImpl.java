package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TOadsSpotMapper;
import com.fc.v2.mapper.auto.TOadsAreaBookMapper;
import com.fc.v2.model.auto.TOadsSpot;
import com.fc.v2.model.auto.TOadsAreaBook;
import com.fc.v2.service.ITOadsSpotService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点位底册Service业务层处理
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TOadsSpotServiceImpl extends ServiceImpl<TOadsSpotMapper, TOadsSpot> implements ITOadsSpotService {

    @Autowired
    private TOadsAreaBookMapper oadsAreaBookMapper;

    @Override
    public TOadsSpot selectTOadsSpotById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TOadsSpot>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TOadsSpot> selectTOadsSpotList(Wrapper<TOadsSpot> queryWrapper) {
        QueryWrapper<TOadsSpot> wrapper = new QueryWrapper<TOadsSpot>();
        com.github.pagehelper.PageHelper.startPage(1, 10);
        wrapper.eq("status", 0);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int insertTOadsSpot(TOadsSpot record) {
        if (record == null) {
            return 0;
        }

        record.setCreateBy(record.getSpotNo());
        TOadsAreaBook refArch = oadsAreaBookMapper.selectOne(new QueryWrapper<TOadsAreaBook>()
                .eq("id", record.getAreaId()).eq("del_flag", 0));
        if (refArch == null) {
            return 0;
        }
        if (refArch.getStatus() != null && refArch.getStatus() == 1) {
            return 0;
        }
        record.setAreaNo(refArch.getAreaNo());
        if (StringUtils.isNotEmpty(record.getSpotNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TOadsSpot>()
                    .eq("spot_no", record.getSpotNo()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }
        int remIn = record.getNeedNum() == null ? 0 : record.getNeedNum();
        int remOut = record.getGotNum() == null ? 0 : record.getGotNum();
        record.setLackNum(remIn + remOut);

        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTOadsSpot(TOadsSpot record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        if (record.getId() != null && StringUtils.isNotEmpty(record.getSpotNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TOadsSpot>()
                    .eq("spot_no", record.getSpotNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TOadsSpot>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTOadsSpotByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return this.baseMapper.deleteBatchIds(Arrays.asList(idArr));
    }

    @Override
    public int deleteTOadsSpotById(Long id) {
        return this.baseMapper.deleteById(id);
    }
}
