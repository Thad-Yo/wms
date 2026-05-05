package com.xiaoyai.warehouse.domain.aggregate.dto;

import java.util.List;

/**
 * RFID批量绑定货品参数
 */
public class AggregateRfidBindGoodsDto {
    private List<Long> identityIds;
    private Long bindGoodsId;
    private String remark;

    public List<Long> getIdentityIds() {
        return identityIds;
    }

    public void setIdentityIds(List<Long> identityIds) {
        this.identityIds = identityIds;
    }

    public Long getBindGoodsId() {
        return bindGoodsId;
    }

    public void setBindGoodsId(Long bindGoodsId) {
        this.bindGoodsId = bindGoodsId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
