package com.xiaoyai.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;

import java.util.List;

public interface AggregateRfidIdentityMapper extends BaseMapper<AggregateRfidIdentity> {
    List<AggregateRfidIdentity> selectAggregateRfidIdentityList(AggregateRfidIdentity aggregateRfidIdentity);
}
