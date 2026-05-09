package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaoyai.common.core.domain.BaseEntity;

/**
 * 主体模板字段 aggregate_subject_field
 */
@TableName("aggregate_subject_field")
public class AggregateSubjectField extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long fieldId;

    private Long templateId;
    private String fieldCode;
    private String fieldLabel;
    private String fieldType;
    private String placeholder;
    private String defaultValue;
    private String optionsJson;
    private String validationRule;
    private String requiredFlag;
    private String indexedFlag;
    private String traceFlag;
    private String searchFlag;
    private String exportFlag;
    private String writeScope;
    private String editableEventTypes;
    private Integer sortOrder;

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public String getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(String requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public String getIndexedFlag() {
        return indexedFlag;
    }

    public void setIndexedFlag(String indexedFlag) {
        this.indexedFlag = indexedFlag;
    }

    public String getTraceFlag() {
        return traceFlag;
    }

    public void setTraceFlag(String traceFlag) {
        this.traceFlag = traceFlag;
    }

    public String getSearchFlag() {
        return searchFlag;
    }

    public void setSearchFlag(String searchFlag) {
        this.searchFlag = searchFlag;
    }

    public String getExportFlag() {
        return exportFlag;
    }

    public void setExportFlag(String exportFlag) {
        this.exportFlag = exportFlag;
    }

    public String getWriteScope() {
        return writeScope;
    }

    public void setWriteScope(String writeScope) {
        this.writeScope = writeScope;
    }

    public String getEditableEventTypes() {
        return editableEventTypes;
    }

    public void setEditableEventTypes(String editableEventTypes) {
        this.editableEventTypes = editableEventTypes;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
