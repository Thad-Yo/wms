package com.xiaoyai.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateRfidBindGoodsDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.AggregateLifecycleVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.util.List;

public interface IAggregateRfidIdentityService extends IService<AggregateRfidIdentity> {
    List<AggregateRfidIdentity> selectAggregateRfidIdentityList(AggregateRfidIdentity aggregateRfidIdentity);

    AggregateRfidIdentity createIdentity(AggregateRfidIdentity aggregateRfidIdentity);

    int updateAggregateRfidIdentity(AggregateRfidIdentity aggregateRfidIdentity);

    int batchBindObject(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName);

    @Deprecated
    default int batchBindGoods(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName) {
        return batchBindObject(bindGoodsDto, operatorName);
    }

    void exportBindTemplate(HttpServletResponse response);

    String importBindData(MultipartFile file, String operatorName) throws Exception;

    int deleteAggregateRfidIdentityByIds(Long[] identityIds);

    AggregateLifecycleVo selectLifecycleByRfidCode(String rfidCode);

    List<AggregateRfidIdentity> selectByMaterialId(Long materialId);
}
