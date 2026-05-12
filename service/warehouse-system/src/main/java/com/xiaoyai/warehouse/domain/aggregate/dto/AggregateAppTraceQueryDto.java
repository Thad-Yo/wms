package com.xiaoyai.warehouse.domain.aggregate.dto;

/**
 * APP骨料溯源查询参数
 */
public class AggregateAppTraceQueryDto {
    private String rfidCode;

    public String getRfidCode() {
        return rfidCode;
    }

    public void setRfidCode(String rfidCode) {
        this.rfidCode = rfidCode;
    }
}
