package com.xiaoyai.warehouse.domain.aggregate;

import com.xiaoyai.common.core.domain.BaseEntity;

/**
 * 骨料RFID选项
 */
public class BoneRfid extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long boneRfidId;
    private String boneRfidCode;
    private String tidCode;
    private String boneCode;
    private String boneName;
    private String status;
    private Long currentObjectId;
    private String currentObjectCode;
    private String currentObjectName;

    public Long getBoneRfidId() {
        return boneRfidId;
    }

    public void setBoneRfidId(Long boneRfidId) {
        this.boneRfidId = boneRfidId;
    }

    public String getBoneRfidCode() {
        return boneRfidCode;
    }

    public void setBoneRfidCode(String boneRfidCode) {
        this.boneRfidCode = boneRfidCode;
    }

    public String getTidCode() {
        return tidCode;
    }

    public void setTidCode(String tidCode) {
        this.tidCode = tidCode;
    }

    public String getBoneCode() {
        return boneCode;
    }

    public void setBoneCode(String boneCode) {
        this.boneCode = boneCode;
    }

    public String getBoneName() {
        return boneName;
    }

    public void setBoneName(String boneName) {
        this.boneName = boneName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCurrentObjectId() {
        return currentObjectId;
    }

    public void setCurrentObjectId(Long currentObjectId) {
        this.currentObjectId = currentObjectId;
    }

    public String getCurrentObjectCode() {
        return currentObjectCode;
    }

    public void setCurrentObjectCode(String currentObjectCode) {
        this.currentObjectCode = currentObjectCode;
    }

    public String getCurrentObjectName() {
        return currentObjectName;
    }

    public void setCurrentObjectName(String currentObjectName) {
        this.currentObjectName = currentObjectName;
    }
}
