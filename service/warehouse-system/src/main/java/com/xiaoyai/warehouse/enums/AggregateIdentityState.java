package com.xiaoyai.warehouse.enums;

/**
 * 骨料RFID身份状态
 */
public enum AggregateIdentityState {
    CREATED("CREATED", "已建档"),
    IN_STOCK("IN_STOCK", "已入库"),
    OUT_STOCK("OUT_STOCK", "已出库"),
    MOVED("MOVED", "已移动");

    private final String code;
    private final String name;

    AggregateIdentityState(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
