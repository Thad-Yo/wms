package com.xiaoyai.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;

import java.util.List;

public interface AggregateSubjectTemplateMapper extends BaseMapper<AggregateSubjectTemplate> {
    List<AggregateSubjectTemplate> selectAggregateSubjectTemplateList(AggregateSubjectTemplate aggregateSubjectTemplate);
}
