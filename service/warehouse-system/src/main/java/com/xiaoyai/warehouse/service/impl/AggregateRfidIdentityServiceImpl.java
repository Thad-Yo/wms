package com.xiaoyai.warehouse.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.SecurityUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.warehouse.domain.WarehouseGoods;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateRfidBindGoodsDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.AggregateLifecycleVo;
import com.xiaoyai.warehouse.enums.AggregateEventType;
import com.xiaoyai.warehouse.enums.AggregateIdentityState;
import com.xiaoyai.warehouse.mapper.AggregateEventMapper;
import com.xiaoyai.warehouse.mapper.AggregateRfidIdentityMapper;
import com.xiaoyai.warehouse.service.IAggregateMaterialService;
import com.xiaoyai.warehouse.service.IAggregateRfidIdentityService;
import com.xiaoyai.warehouse.service.IWarehouseGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class AggregateRfidIdentityServiceImpl extends ServiceImpl<AggregateRfidIdentityMapper, AggregateRfidIdentity> implements IAggregateRfidIdentityService {
    @Autowired
    private IAggregateMaterialService aggregateMaterialService;

    @Autowired
    private AggregateEventMapper aggregateEventMapper;

    @Autowired
    private IWarehouseGoodsService warehouseGoodsService;

    @Override
    public List<AggregateRfidIdentity> selectAggregateRfidIdentityList(AggregateRfidIdentity aggregateRfidIdentity) {
        applyUserScope(aggregateRfidIdentity);
        return baseMapper.selectAggregateRfidIdentityList(aggregateRfidIdentity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AggregateRfidIdentity createIdentity(AggregateRfidIdentity aggregateRfidIdentity) {
        if (StringUtils.isBlank(aggregateRfidIdentity.getRfidCode())) {
            throw new ServiceException("RFID编码不能为空");
        }
        if (aggregateRfidIdentity.getMaterialId() == null) {
            throw new ServiceException("骨料档案不能为空");
        }
        AggregateRfidIdentity exists = getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getRfidCode, aggregateRfidIdentity.getRfidCode())
                .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
        if (exists != null) {
            throw new ServiceException("RFID编码已建档");
        }
        AggregateMaterial material = aggregateMaterialService.getById(aggregateRfidIdentity.getMaterialId());
        if (material == null || "1".equals(material.getDelFlag())) {
            throw new ServiceException("骨料档案不存在");
        }
        aggregateRfidIdentity.setMaterialCode(material.getMaterialCode());
        aggregateRfidIdentity.setMaterialName(material.getMaterialName());
        aggregateRfidIdentity.setBatchNo(StringUtils.isBlank(aggregateRfidIdentity.getBatchNo()) ? material.getBatchNo() : aggregateRfidIdentity.getBatchNo());
        aggregateRfidIdentity.setOwnerUserId(material.getOwnerUserId());
        aggregateRfidIdentity.setOwnerUserName(material.getOwnerUserName());
        aggregateRfidIdentity.setCurrentState(AggregateIdentityState.CREATED.getCode());
        aggregateRfidIdentity.setBindTime(DateUtils.getNowDate());
        aggregateRfidIdentity.setCreateTime(DateUtils.getNowDate());
        save(aggregateRfidIdentity);

        AggregateEvent event = new AggregateEvent();
        event.setIdentityId(aggregateRfidIdentity.getIdentityId());
        event.setRfidCode(aggregateRfidIdentity.getRfidCode());
        event.setMaterialId(aggregateRfidIdentity.getMaterialId());
        event.setEventType(AggregateEventType.CREATED.getCode());
        event.setEventName(AggregateEventType.CREATED.getName());
        event.setActionName("RFID身份建档");
        event.setRemark(aggregateRfidIdentity.getRemark());
        event.setCreateBy(aggregateRfidIdentity.getCreateBy());
        event.setEventTime(DateUtils.getNowDate());
        event.setCreateTime(DateUtils.getNowDate());
        aggregateEventMapper.insert(event);

        aggregateRfidIdentity.setLastEventTime(event.getEventTime());
        updateById(aggregateRfidIdentity);
        return aggregateRfidIdentity;
    }

    @Override
    public int updateAggregateRfidIdentity(AggregateRfidIdentity aggregateRfidIdentity) {
        aggregateRfidIdentity.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(aggregateRfidIdentity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindGoods(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName) {
        if (bindGoodsDto == null || bindGoodsDto.getIdentityIds() == null || bindGoodsDto.getIdentityIds().isEmpty()) {
            throw new ServiceException("请选择需要绑定的RFID身份");
        }
        if (bindGoodsDto.getBindGoodsId() == null) {
            throw new ServiceException("请选择需要绑定的货品");
        }
        WarehouseGoods goods = warehouseGoodsService.selectWarehouseGoodsByGoodsId(bindGoodsDto.getBindGoodsId());
        if (goods == null || "1".equals(goods.getDelFlag())) {
            throw new ServiceException("绑定货品不存在");
        }

        List<AggregateRfidIdentity> identities = list(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .in(AggregateRfidIdentity::getIdentityId, bindGoodsDto.getIdentityIds())
                .eq(AggregateRfidIdentity::getDelFlag, "0"));
        if (identities.size() != bindGoodsDto.getIdentityIds().size()) {
            throw new ServiceException("部分RFID身份不存在或已删除");
        }

        Date now = DateUtils.getNowDate();
        List<Long> ids = new ArrayList<>();
        for (AggregateRfidIdentity identity : identities) {
            checkOwner(identity);
            if (identity.getBindGoodsId() != null) {
                throw new ServiceException("RFID已绑定货品：" + identity.getRfidCode());
            }
            ids.add(identity.getIdentityId());
        }

        boolean updated = lambdaUpdate()
                .in(AggregateRfidIdentity::getIdentityId, ids)
                .set(AggregateRfidIdentity::getBindGoodsId, goods.getGoodsId())
                .set(AggregateRfidIdentity::getBindGoodsCode, goods.getGoodsCode())
                .set(AggregateRfidIdentity::getBindGoodsName, goods.getGoodsName())
                .set(AggregateRfidIdentity::getBindGoodsTime, now)
                .set(AggregateRfidIdentity::getLastEventTime, now)
                .set(AggregateRfidIdentity::getUpdateBy, operatorName)
                .set(AggregateRfidIdentity::getUpdateTime, now)
                .update();
        if (!updated) {
            return 0;
        }

        for (AggregateRfidIdentity identity : identities) {
            AggregateEvent event = new AggregateEvent();
            event.setIdentityId(identity.getIdentityId());
            event.setRfidCode(identity.getRfidCode());
            event.setMaterialId(identity.getMaterialId());
            event.setEventType(AggregateEventType.BIND_GOODS.getCode());
            event.setEventName(AggregateEventType.BIND_GOODS.getName());
            event.setActionName("绑定货品：" + goods.getGoodsName());
            event.setSourceReceiptType("GOODS");
            event.setSourceReceiptId(goods.getGoodsId());
            event.setSourceReceiptNo(goods.getGoodsCode());
            event.setCreateBy(operatorName);
            event.setEventTime(now);
            event.setCreateTime(now);
            event.setRemark(bindGoodsDto.getRemark());
            aggregateEventMapper.insert(event);
        }
        return ids.size();
    }

    @Override
    public int deleteAggregateRfidIdentityByIds(Long[] identityIds) {
        return lambdaUpdate().in(AggregateRfidIdentity::getIdentityId, Arrays.asList(identityIds)).set(AggregateRfidIdentity::getDelFlag, "1").update() ? 1 : 0;
    }

    @Override
    public AggregateLifecycleVo selectLifecycleByRfidCode(String rfidCode) {
        AggregateRfidIdentity identity = getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
        if (identity == null) {
            throw new ServiceException("RFID身份不存在");
        }
        checkOwner(identity);
        AggregateEvent query = new AggregateEvent();
        query.setRfidCode(rfidCode);
        List<AggregateEvent> events = aggregateEventMapper.selectAggregateEventList(query);
        AggregateLifecycleVo vo = new AggregateLifecycleVo();
        vo.setIdentity(identity);
        vo.setEvents(events);
        return vo;
    }

    private void applyUserScope(AggregateRfidIdentity aggregateRfidIdentity) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId)) {
            aggregateRfidIdentity.setOwnerUserId(userId);
        }
    }

    private void checkOwner(AggregateRfidIdentity identity) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId) && (identity.getOwnerUserId() == null || !identity.getOwnerUserId().equals(userId))) {
            throw new ServiceException("无权查看该RFID身份");
        }
    }
}
