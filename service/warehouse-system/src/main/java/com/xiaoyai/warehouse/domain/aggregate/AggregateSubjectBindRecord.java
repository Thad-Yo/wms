package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoyai.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 主体表单填写记录 aggregate_subject_bind_record
 */
@TableName("aggregate_subject_bind_record")
public class AggregateSubjectBindRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long identityId;
    private String rfidCode;
    private Long bindGoodsId;
    private String bindGoodsCode;
    private String bindGoodsName;
    private Long templateId;
    private String subjectCode;
    private String subjectName;
    private String moduleName;
    private Integer writeNo;
    private String formDataJson;
    private String fieldSnapshotJson;
    private Long operatorId;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date writeTime;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

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

    public Long getBindGoodsId() {
        return bindGoodsId;
    }

    public void setBindGoodsId(Long bindGoodsId) {
        this.bindGoodsId = bindGoodsId;
    }

    public Long getBindObjectId() {
        return bindGoodsId;
    }

    public void setBindObjectId(Long bindObjectId) {
        this.bindGoodsId = bindObjectId;
    }

    public String getBindGoodsCode() {
        return bindGoodsCode;
    }

    public void setBindGoodsCode(String bindGoodsCode) {
        this.bindGoodsCode = bindGoodsCode;
    }

    public String getBindObjectCode() {
        return bindGoodsCode;
    }

    public void setBindObjectCode(String bindObjectCode) {
        this.bindGoodsCode = bindObjectCode;
    }

    public String getBindGoodsName() {
        return bindGoodsName;
    }

    public void setBindGoodsName(String bindGoodsName) {
        this.bindGoodsName = bindGoodsName;
    }

    public String getBindObjectName() {
        return bindGoodsName;
    }

    public void setBindObjectName(String bindObjectName) {
        this.bindGoodsName = bindObjectName;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getWriteNo() {
        return writeNo;
    }

    public void setWriteNo(Integer writeNo) {
        this.writeNo = writeNo;
    }

    public String getFormDataJson() {
        return formDataJson;
    }

    public void setFormDataJson(String formDataJson) {
        this.formDataJson = formDataJson;
    }

    public String getFieldSnapshotJson() {
        return fieldSnapshotJson;
    }

    public void setFieldSnapshotJson(String fieldSnapshotJson) {
        this.fieldSnapshotJson = fieldSnapshotJson;
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

    public Date getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(Date writeTime) {
        this.writeTime = writeTime;
    }
}
