package com.xiaoyai.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateDevice;

import java.util.List;

public interface AggregateDeviceMapper extends BaseMapper<AggregateDevice> {
    List<AggregateDevice> selectAggregateDeviceList(AggregateDevice aggregateDevice);
}
