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
 * 数字骨料基础档案 aggregate_material
 */
@TableName("aggregate_material")
public class AggregateMaterial extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long materialId;

    @Excel(name = "骨料编号")
    private String materialCode;

    @Excel(name = "骨料名称")
    private String materialName;

    @Excel(name = "骨料类型")
    private String materialType;

    @Excel(name = "规格/粒径")
    private String specification;

    @Excel(name = "产地")
    private String originPlace;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "单据状态")
    private String state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private Long auditId;

    @Excel(name = "审核人")
    private String auditName;

    private Long supplierId;

    @Excel(name = "供应商")
    private String supplierName;

    @Excel(name = "计量单位")
    private String unit;

    @Excel(name = "质量/强度等级")
    private String qualityGrade;

    @Excel(name = "分配用户ID")
    private Long ownerUserId;

    @Excel(name = "分配用户")
    private String ownerUserName;

    @Excel(name = "RFID数量")
    private Long rfidCount;

    @Excel(name = "已使用数量")
    private Long usedRfidCount;

    @Excel(name = "未使用数量")
    private Long unusedRfidCount;

    @TableField(exist = false)
    private String rfidCodes;

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

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getOriginPlace() {
        return originPlace;
    }

    public void setOriginPlace(String originPlace) {
        this.originPlace = originPlace;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
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

    public Long getRfidCount() {
        return rfidCount;
    }

    public void setRfidCount(Long rfidCount) {
        this.rfidCount = rfidCount;
    }

    public Long getUsedRfidCount() {
        return usedRfidCount;
    }

    public void setUsedRfidCount(Long usedRfidCount) {
        this.usedRfidCount = usedRfidCount;
    }

    public Long getUnusedRfidCount() {
        return unusedRfidCount;
    }

    public void setUnusedRfidCount(Long unusedRfidCount) {
        this.unusedRfidCount = unusedRfidCount;
    }

    public String getRfidCodes() {
        return rfidCodes;
    }

    public void setRfidCodes(String rfidCodes) {
        this.rfidCodes = rfidCodes;
    }
}
