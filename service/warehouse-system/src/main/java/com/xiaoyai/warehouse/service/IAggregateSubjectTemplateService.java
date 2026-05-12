package com.xiaoyai.warehouse.service;

import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateSubjectTemplateDto;

import java.util.List;

public interface IAggregateSubjectTemplateService {
    List<AggregateSubjectTemplate> selectAggregateSubjectTemplateList(AggregateSubjectTemplate aggregateSubjectTemplate);

    AggregateSubjectTemplateDto selectAggregateSubjectTemplateById(Long templateId);

    String previewNextSubjectCode();

    List<AggregateSubjectTemplate> selectEnabledTemplateOptions();

    int insertAggregateSubjectTemplate(AggregateSubjectTemplateDto templateDto);

    int updateAggregateSubjectTemplate(AggregateSubjectTemplateDto templateDto);

    int activateTemplate(Long templateId);

    AggregateSubjectTemplateDto copyTemplate(Long templateId, String operatorName);

    int deleteAggregateSubjectTemplateByIds(Long[] templateIds);
}
