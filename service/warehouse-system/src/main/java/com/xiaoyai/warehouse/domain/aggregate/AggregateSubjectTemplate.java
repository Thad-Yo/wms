package com.xiaoyai.warehouse.domain.aggregate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaoyai.common.annotation.Excel;
import com.xiaoyai.common.core.domain.BaseEntity;

import java.util.List;

/**
 * 主体模板配置 aggregate_subject_template
 */
@TableName("aggregate_subject_template")
public class AggregateSubjectTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long templateId;

    @Excel(name = "主体编码")
    private String subjectCode;

    @Excel(name = "主体名称")
    private String subjectName;

    @Excel(name = "模块名称")
    private String moduleName;

    @Excel(name = "状态")
    private String status;

    @TableField(exist = false)
    private List<AggregateSubjectField> fieldList;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AggregateSubjectField> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<AggregateSubjectField> fieldList) {
        this.fieldList = fieldList;
    }
}
