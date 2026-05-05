package com.xiaoyai.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;

import java.util.List;

public interface AggregateMaterialMapper extends BaseMapper<AggregateMaterial> {
    List<AggregateMaterial> selectAggregateMaterialList(AggregateMaterial aggregateMaterial);
}
