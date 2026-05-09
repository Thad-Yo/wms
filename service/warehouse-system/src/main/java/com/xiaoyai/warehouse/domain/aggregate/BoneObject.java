package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaoyai.common.annotation.Excel;
import com.xiaoyai.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 骨料可信对象
 */
public class BoneObject extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long objectId;

    @Excel(name = "对象编号")
    private String objectCode;

    @Excel(name = "对象名称")
    private String objectName;

    @Excel(name = "对象类型")
    private String objectType;

    private Long templateId;

    @Excel(name = "模板编码")
    private String templateCode;

    @Excel(name = "模板名称")
    private String templateName;

    private Long boneRfidId;

    @Excel(name = "骨料RFID")
    private String boneRfidCode;

    @Excel(name = "绑定状态")
    private String bindStatus;

    @Excel(name = "生命周期状态")
    private String lifecycleStatus;

    private Long ownerUserId;

    @Excel(name = "归属用户")
    private String ownerUserName;

    private String sourceSystem;

    @TableField(exist = false)
    private String searchText;

    private String fixedDataJson;

    private String dynamicDataJson;

    private String traceDataJson;

    private String extDataJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date unbindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;

    @TableField(exist = false)
    private String boneStatus;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
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

    public String getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
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

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getFixedDataJson() {
        return fixedDataJson;
    }

    public void setFixedDataJson(String fixedDataJson) {
        this.fixedDataJson = fixedDataJson;
    }

    public String getDynamicDataJson() {
        return dynamicDataJson;
    }

    public void setDynamicDataJson(String dynamicDataJson) {
        this.dynamicDataJson = dynamicDataJson;
    }

    public String getTraceDataJson() {
        return traceDataJson;
    }

    public void setTraceDataJson(String traceDataJson) {
        this.traceDataJson = traceDataJson;
    }

    public String getExtDataJson() {
        return extDataJson;
    }

    public void setExtDataJson(String extDataJson) {
        this.extDataJson = extDataJson;
    }

    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public Date getUnbindTime() {
        return unbindTime;
    }

    public void setUnbindTime(Date unbindTime) {
        this.unbindTime = unbindTime;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    public String getBoneStatus() {
        return boneStatus;
    }

    public void setBoneStatus(String boneStatus) {
        this.boneStatus = boneStatus;
    }
}
