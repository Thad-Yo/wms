package com.xiaoyai.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateMaterialImportDto;

import java.util.List;

public interface IAggregateMaterialService extends IService<AggregateMaterial> {
    List<AggregateMaterial> selectAggregateMaterialList(AggregateMaterial aggregateMaterial);

    int insertAggregateMaterial(AggregateMaterial aggregateMaterial);

    AggregateMaterial importBatch(AggregateMaterialImportDto importDto);

    int updateAggregateMaterial(AggregateMaterial aggregateMaterial);

    int deleteAggregateMaterialByIds(Long[] materialIds);
}
