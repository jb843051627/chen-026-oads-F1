package com.fc.v2.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TOadsCheckRowMapper;
import com.fc.v2.model.auto.TOadsCheckRow;
import com.fc.v2.service.ITOadsCheckRowService;

/**
 * 集中更换核验行 Service业务层处理（batch-process 形状：整批提交）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TOadsCheckRowServiceImpl implements ITOadsCheckRowService {

    private static final int MAX_ROWS = 500;
    private static final int STATUS_OK = 1;
    private static final int STATUS_FAIL = 2;

    @javax.annotation.Resource
    private TOadsCheckRowMapper oadsCheckRowMapper;

    @Override
    public TOadsCheckRow selectTOadsCheckRowById(Long id) {
        return this.oadsCheckRowMapper.selectById(id);
    }

    @Override
    public int submitBatch(String batchNo, List<TOadsCheckRow> rows) {
        String no = rows.get(0).getBatchNo();
        java.util.List<TOadsCheckRow> errors = new java.util.ArrayList<TOadsCheckRow>();
        int seq = 0;
        for (TOadsCheckRow r : rows) {
            if (r.getItemCode() == null || r.getItemCode().trim().isEmpty()
                    || r.getQty() == null
                    || r.getQty().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                seq++;
                r.setRowNo(Integer.valueOf(seq));
                r.setBatchNo(no);
                r.setStatus(STATUS_FAIL);
                this.oadsCheckRowMapper.insert(r);
                errors.add(r);
            }
        }
        if (!errors.isEmpty()) {
            return 0;
        }
        int ok = 0;
        for (TOadsCheckRow r : rows) {
            r.setBatchNo(no);
            r.setStatus(STATUS_OK);
            this.oadsCheckRowMapper.insert(r);
            ok++;
        }
        return ok;
    }

    @Override
    public List<TOadsCheckRow> listErrors(String batchNo) {
        return this.oadsCheckRowMapper.selectList(new QueryWrapper<TOadsCheckRow>()
                .eq("batch_no", batchNo).eq("status", STATUS_FAIL));
    }
}
