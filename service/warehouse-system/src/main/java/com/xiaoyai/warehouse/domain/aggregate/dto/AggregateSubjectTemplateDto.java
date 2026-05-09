package com.xiaoyai.warehouse.domain.aggregate.dto;

import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectField;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;

import java.util.List;

public class AggregateSubjectTemplateDto extends AggregateSubjectTemplate {
    private List<AggregateSubjectField> fieldList;
    private Boolean useCurrent;

    public List<AggregateSubjectField> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<AggregateSubjectField> fieldList) {
        this.fieldList = fieldList;
    }

    public Boolean getUseCurrent() {
        return useCurrent;
    }

    public void setUseCurrent(Boolean useCurrent) {
        this.useCurrent = useCurrent;
    }
}
