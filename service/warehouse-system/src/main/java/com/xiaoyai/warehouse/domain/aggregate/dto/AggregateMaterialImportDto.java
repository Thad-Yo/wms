package com.xiaoyai.warehouse.domain.aggregate.dto;

import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;

/**
 * 数字骨料批次及RFID批量导入参数
 */
public class AggregateMaterialImportDto extends AggregateMaterial {
    private static final long serialVersionUID = 1L;

    /**
     * RFID列表文本，支持一行一个、逗号、分号或空白分隔。
     */
    private String rfidCodes;

    public String getRfidCodes() {
        return rfidCodes;
    }

    public void setRfidCodes(String rfidCodes) {
        this.rfidCodes = rfidCodes;
    }
}
