package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaoyai.common.annotation.Excel;
import com.xiaoyai.common.core.domain.BaseEntity;

/**
 * 数字骨料采集设备 aggregate_device
 */
@TableName("aggregate_device")
public class AggregateDevice extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long deviceId;
    @Excel(name = "设备编号")
    private String deviceCode;
    @Excel(name = "设备名称")
    private String deviceName;
    @Excel(name = "设备类型")
    private String deviceType;
    private String locationName;
    private Long warehouseId;
    private String warehouseName;
    private String status;

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
