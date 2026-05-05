package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoyai.common.annotation.Excel;
import com.xiaoyai.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 数字骨料RFID身份 aggregate_rfid_identity
 */
@TableName("aggregate_rfid_identity")
public class AggregateRfidIdentity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long identityId;

    @Excel(name = "RFID/EPC编码")
    private String rfidCode;

    @Excel(name = "TID编码")
    private String tidCode;

    private Long materialId;

    @Excel(name = "骨料编号")
    private String materialCode;

    @Excel(name = "骨料名称")
    private String materialName;

    @Excel(name = "批次号")
    private String batchNo;

    private String identityLevel;

    @Excel(name = "当前状态")
    private String currentState;

    private Long currentWarehouseId;

    @Excel(name = "当前仓库")
    private String currentWarehouseName;

    private Long ownerUserId;

    @Excel(name = "分配用户")
    private String ownerUserName;

    private Long bindGoodsId;

    @Excel(name = "绑定货品编号")
    private String bindGoodsCode;

    @Excel(name = "绑定货品名称")
    private String bindGoodsName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindGoodsTime;

    @TableField(exist = false)
    private String useStatus;

    @Excel(name = "当前位置")
    private String currentLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastEventTime;

    public Long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(Long identityId) {
        this.identityId = identityId;
    }

    public String getRfidCode() {
        return rfidCode;
    }

    public void setRfidCode(String rfidCode) {
        this.rfidCode = rfidCode;
    }

    public String getTidCode() {
        return tidCode;
    }

    public void setTidCode(String tidCode) {
        this.tidCode = tidCode;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getIdentityLevel() {
        return identityLevel;
    }

    public void setIdentityLevel(String identityLevel) {
        this.identityLevel = identityLevel;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Long getCurrentWarehouseId() {
        return currentWarehouseId;
    }

    public void setCurrentWarehouseId(Long currentWarehouseId) {
        this.currentWarehouseId = currentWarehouseId;
    }

    public String getCurrentWarehouseName() {
        return currentWarehouseName;
    }

    public void setCurrentWarehouseName(String currentWarehouseName) {
        this.currentWarehouseName = currentWarehouseName;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public Long getBindGoodsId() {
        return bindGoodsId;
    }

    public void setBindGoodsId(Long bindGoodsId) {
        this.bindGoodsId = bindGoodsId;
    }

    public String getBindGoodsCode() {
        return bindGoodsCode;
    }

    public void setBindGoodsCode(String bindGoodsCode) {
        this.bindGoodsCode = bindGoodsCode;
    }

    public String getBindGoodsName() {
        return bindGoodsName;
    }

    public void setBindGoodsName(String bindGoodsName) {
        this.bindGoodsName = bindGoodsName;
    }

    public Date getBindGoodsTime() {
        return bindGoodsTime;
    }

    public void setBindGoodsTime(Date bindGoodsTime) {
        this.bindGoodsTime = bindGoodsTime;
    }

    public String getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(String useStatus) {
        this.useStatus = useStatus;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public Date getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        this.lastEventTime = lastEventTime;
    }
}
