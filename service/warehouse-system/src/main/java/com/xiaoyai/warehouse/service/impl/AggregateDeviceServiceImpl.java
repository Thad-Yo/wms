package com.xiaoyai.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.warehouse.domain.aggregate.AggregateDevice;
import com.xiaoyai.warehouse.mapper.AggregateDeviceMapper;
import com.xiaoyai.warehouse.service.IAggregateDeviceService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AggregateDeviceServiceImpl extends ServiceImpl<AggregateDeviceMapper, AggregateDevice> implements IAggregateDeviceService {
    @Override
    public List<AggregateDevice> selectAggregateDeviceList(AggregateDevice aggregateDevice) {
        return baseMapper.selectAggregateDeviceList(aggregateDevice);
    }

    @Override
    public int insertAggregateDevice(AggregateDevice aggregateDevice) {
        aggregateDevice.setCreateTime(DateUtils.getNowDate());
        return baseMapper.insert(aggregateDevice);
    }

    @Override
    public int updateAggregateDevice(AggregateDevice aggregateDevice) {
        aggregateDevice.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(aggregateDevice);
    }

    @Override
    public int deleteAggregateDeviceByIds(Long[] deviceIds) {
        return lambdaUpdate().in(AggregateDevice::getDeviceId, Arrays.asList(deviceIds)).set(AggregateDevice::getDelFlag, "1").update() ? 1 : 0;
    }
}
