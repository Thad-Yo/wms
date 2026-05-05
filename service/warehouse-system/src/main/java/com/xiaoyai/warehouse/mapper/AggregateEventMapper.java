package com.xiaoyai.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;

import java.util.List;

public interface AggregateEventMapper extends BaseMapper<AggregateEvent> {
    List<AggregateEvent> selectAggregateEventList(AggregateEvent aggregateEvent);
}
