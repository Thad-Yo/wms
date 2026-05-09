package com.xiaoyai.warehouse.domain.aggregate;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 骨料对象事件
 */
public class BoneObjectEvent {
    private Long eventId;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private Long boneRfidId;
    private String boneRfidCode;
    private String eventType;
    private String eventName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;
    private Long operatorId;
    private String operatorName;
    private String location;
    private String sourceModule;
    private String sourceSystem;
    private String sourceDeviceCode;
    private Long warehouseId;
    private String warehouseName;
    private String snapshotData;
    private String beforeData;
    private String changedFields;
    private String extData;
    private String prevEventHash;
    private String eventHash;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceDeviceCode() {
        return sourceDeviceCode;
    }

    public void setSourceDeviceCode(String sourceDeviceCode) {
        this.sourceDeviceCode = sourceDeviceCode;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public String getPrevEventHash() {
        return prevEventHash;
    }

    public void setPrevEventHash(String prevEventHash) {
        this.prevEventHash = prevEventHash;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }
}
