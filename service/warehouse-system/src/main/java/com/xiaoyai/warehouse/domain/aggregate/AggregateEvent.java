package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoyai.common.annotation.Excel;
import com.xiaoyai.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 骨料RFID事件流水 aggregate_event
 */
@TableName("aggregate_event")
public class AggregateEvent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long eventId;
    private Long identityId;
    @Excel(name = "RFID/EPC编码")
    private String rfidCode;
    private Long materialId;
    private Long objectId;
    @Excel(name = "事件类型")
    private String eventType;
    @Excel(name = "事件名称")
    private String eventName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;
    private String locationName;
    private String sourceModule;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Long operatorId;
    private String operatorName;
    private String actionName;
    private Long deviceId;
    private String deviceCode;
    private String deviceType;
    private Long warehouseId;
    private String warehouseName;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private BigDecimal weight;
    private String vehicleNo;
    private String sourceReceiptType;
    private Long sourceReceiptId;
    private String sourceReceiptNo;
    private String rawPayload;
    private String snapshotData;
    @TableField(exist = false)
    private String prevSnapshotData;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getIdentityId() { return identityId; }
    public void setIdentityId(Long identityId) { this.identityId = identityId; }
    public String getRfidCode() { return rfidCode; }
    public void setRfidCode(String rfidCode) { this.rfidCode = rfidCode; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public Date getEventTime() { return eventTime; }
    public void setEventTime(Date eventTime) { this.eventTime = eventTime; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public String getFromWarehouseName() { return fromWarehouseName; }
    public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public String getToWarehouseName() { return toWarehouseName; }
    public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
    public String getSourceReceiptType() { return sourceReceiptType; }
    public void setSourceReceiptType(String sourceReceiptType) { this.sourceReceiptType = sourceReceiptType; }
    public Long getSourceReceiptId() { return sourceReceiptId; }
    public void setSourceReceiptId(Long sourceReceiptId) { this.sourceReceiptId = sourceReceiptId; }
    public String getSourceReceiptNo() { return sourceReceiptNo; }
    public void setSourceReceiptNo(String sourceReceiptNo) { this.sourceReceiptNo = sourceReceiptNo; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }
    public String getPrevSnapshotData() { return prevSnapshotData; }
    public void setPrevSnapshotData(String prevSnapshotData) { this.prevSnapshotData = prevSnapshotData; }
}
