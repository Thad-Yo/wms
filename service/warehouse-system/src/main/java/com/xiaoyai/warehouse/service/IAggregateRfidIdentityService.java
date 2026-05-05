package com.xiaoyai.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateRfidBindGoodsDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.AggregateLifecycleVo;

import java.util.List;

public interface IAggregateRfidIdentityService extends IService<AggregateRfidIdentity> {
    List<AggregateRfidIdentity> selectAggregateRfidIdentityList(AggregateRfidIdentity aggregateRfidIdentity);

    AggregateRfidIdentity createIdentity(AggregateRfidIdentity aggregateRfidIdentity);

    int updateAggregateRfidIdentity(AggregateRfidIdentity aggregateRfidIdentity);

    int batchBindGoods(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName);

    int deleteAggregateRfidIdentityByIds(Long[] identityIds);

    AggregateLifecycleVo selectLifecycleByRfidCode(String rfidCode);
}
