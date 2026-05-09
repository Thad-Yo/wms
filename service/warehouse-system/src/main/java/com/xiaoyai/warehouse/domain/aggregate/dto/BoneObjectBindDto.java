package com.xiaoyai.warehouse.domain.aggregate.dto;

/**
 * 对象绑定骨料参数
 */
public class BoneObjectBindDto {
    private Long objectId;
    private Long boneRfidId;
    private String remark;

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getBoneRfidId() {
        return boneRfidId;
    }

    public void setBoneRfidId(Long boneRfidId) {
        this.boneRfidId = boneRfidId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
