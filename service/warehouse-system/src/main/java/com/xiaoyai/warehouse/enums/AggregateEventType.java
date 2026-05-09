package com.xiaoyai.warehouse.enums;

/**
 * 骨料事件类型
 */
public enum AggregateEventType {
    CREATED("CREATED", "RFID建档"),
    INBOUND("INBOUND", "入库"),
    OUTBOUND("OUTBOUND", "出库"),
    TRANSFER("TRANSFER", "移动"),
    BIND_OBJECT("BIND_OBJECT", "绑定骨料"),
    BIND_GOODS("BIND_GOODS", "绑定骨料");

    private final String code;
    private final String name;

    AggregateEventType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AggregateEventType of(String code) {
        for (AggregateEventType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
