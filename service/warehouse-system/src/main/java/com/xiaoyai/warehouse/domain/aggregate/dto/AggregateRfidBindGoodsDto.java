package com.xiaoyai.warehouse.domain.aggregate.dto;

import java.util.Map;
import java.util.List;

/**
 * RFID批量绑定骨料参数
 */
public class AggregateRfidBindGoodsDto {
    private List<Long> identityIds;
    private Long bindGoodsId;
    private Long bindObjectId;
    private Long templateId;
    private Integer writeNo;
    private Map<String, Object> formData;
    private String remark;

    public List<Long> getIdentityIds() {
        return identityIds;
    }

    public void setIdentityIds(List<Long> identityIds) {
        this.identityIds = identityIds;
    }

    public Long getBindGoodsId() {
        return bindGoodsId;
    }

    public void setBindGoodsId(Long bindGoodsId) {
        this.bindGoodsId = bindGoodsId;
        this.bindObjectId = bindGoodsId;
    }

    public Long getBindObjectId() {
        return bindObjectId != null ? bindObjectId : bindGoodsId;
    }

    public void setBindObjectId(Long bindObjectId) {
        this.bindObjectId = bindObjectId;
        this.bindGoodsId = bindObjectId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getWriteNo() {
        return writeNo;
    }

    public void setWriteNo(Integer writeNo) {
        this.writeNo = writeNo;
    }

    public Map<String, Object> getFormData() {
        return formData;
    }

    public void setFormData(Map<String, Object> formData) {
        this.formData = formData;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
