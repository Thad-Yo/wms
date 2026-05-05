package com.xiaoyai.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateEventDto;

import java.util.List;

public interface IAggregateEventService extends IService<AggregateEvent> {
    List<AggregateEvent> selectAggregateEventList(AggregateEvent aggregateEvent);

    AggregateEvent recordEvent(AggregateEventDto aggregateEventDto);
}
