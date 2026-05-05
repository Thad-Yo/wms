package com.xiaoyai.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyai.warehouse.domain.aggregate.AggregateDevice;

import java.util.List;

public interface IAggregateDeviceService extends IService<AggregateDevice> {
    List<AggregateDevice> selectAggregateDeviceList(AggregateDevice aggregateDevice);

    int insertAggregateDevice(AggregateDevice aggregateDevice);

    int updateAggregateDevice(AggregateDevice aggregateDevice);

    int deleteAggregateDeviceByIds(Long[] deviceIds);
}
