package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TOadsSpot;

import java.util.List;

/**
 * 点位底册 Service接口
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITOadsSpotService {

    /** 按主键查询 */
    TOadsSpot selectTOadsSpotById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TOadsSpot> selectTOadsSpotList(Wrapper<TOadsSpot> queryWrapper);

    /** 新增 */
    int insertTOadsSpot(TOadsSpot record);

    /** 修改 */
    int updateTOadsSpot(TOadsSpot record);

    /** 批量删除 */
    int deleteTOadsSpotByIds(String ids);

    /** 按主键删除 */
    int deleteTOadsSpotById(Long id);
}
