package com.xiaoyai.warehouse.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.SecurityUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.warehouse.domain.Warehouse;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateEventDto;
import com.xiaoyai.warehouse.enums.AggregateEventType;
import com.xiaoyai.warehouse.enums.AggregateIdentityState;
import com.xiaoyai.warehouse.mapper.AggregateEventMapper;
import com.xiaoyai.warehouse.service.IAggregateEventService;
import com.xiaoyai.warehouse.service.IAggregateRfidIdentityService;
import com.xiaoyai.warehouse.service.IWarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AggregateEventServiceImpl extends ServiceImpl<AggregateEventMapper, AggregateEvent> implements IAggregateEventService {
    @Autowired
    private IAggregateRfidIdentityService aggregateRfidIdentityService;

    @Autowired
    private IWarehouseService warehouseService;

    @Override
    public List<AggregateEvent> selectAggregateEventList(AggregateEvent aggregateEvent) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId) && StringUtils.isBlank(aggregateEvent.getRfidCode())) {
            throw new ServiceException("请输入RFID编码查询事件");
        }
        if (!SecurityUtils.isAdmin(userId) && StringUtils.isNotBlank(aggregateEvent.getRfidCode())) {
            AggregateRfidIdentity identity = aggregateRfidIdentityService.getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                    .eq(AggregateRfidIdentity::getRfidCode, aggregateEvent.getRfidCode())
                    .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
            checkOwner(identity);
        }
        return baseMapper.selectAggregateEventList(aggregateEvent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AggregateEvent recordEvent(AggregateEventDto aggregateEventDto) {
        if (StringUtils.isBlank(aggregateEventDto.getRfidCode())) {
            throw new ServiceException("RFID编码不能为空");
        }
        AggregateEventType eventType = AggregateEventType.of(aggregateEventDto.getEventType());
        if (eventType == null) {
            throw new ServiceException("不支持的事件类型");
        }
        AggregateRfidIdentity identity = aggregateRfidIdentityService.getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getRfidCode, aggregateEventDto.getRfidCode())
                .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
        if (identity == null) {
            throw new ServiceException("RFID身份不存在，请先建档");
        }
        checkOwner(identity);
        validateEvent(identity, aggregateEventDto, eventType);
        fillWarehouseNames(aggregateEventDto);

        AggregateEvent aggregateEvent = new AggregateEvent();
        BeanUtils.copyProperties(aggregateEventDto, aggregateEvent);
        aggregateEvent.setIdentityId(identity.getIdentityId());
        aggregateEvent.setMaterialId(identity.getMaterialId());
        aggregateEvent.setEventName(StringUtils.isBlank(aggregateEvent.getEventName()) ? eventType.getName() : aggregateEvent.getEventName());
        aggregateEvent.setEventTime(aggregateEvent.getEventTime() == null ? DateUtils.getNowDate() : aggregateEvent.getEventTime());
        aggregateEvent.setCreateTime(DateUtils.getNowDate());
        save(aggregateEvent);

        updateIdentityState(identity, aggregateEvent, eventType);
        return aggregateEvent;
    }

    private void validateEvent(AggregateRfidIdentity identity, AggregateEventDto event, AggregateEventType eventType) {
        String state = identity.getCurrentState();
        if (AggregateEventType.INBOUND.equals(eventType) && AggregateIdentityState.IN_STOCK.getCode().equals(state)) {
            throw new ServiceException("当前RFID已在库，不能重复入库");
        }
        if (AggregateEventType.OUTBOUND.equals(eventType) && !AggregateIdentityState.IN_STOCK.getCode().equals(state)
                && !AggregateIdentityState.MOVED.getCode().equals(state)) {
            throw new ServiceException("当前RFID未在库，不能出库");
        }
        if (AggregateEventType.TRANSFER.equals(eventType) && event.getToWarehouseId() == null) {
            throw new ServiceException("移动事件必须指定目标仓库");
        }
    }

    private void fillWarehouseNames(AggregateEventDto event) {
        if (event.getWarehouseId() != null && StringUtils.isBlank(event.getWarehouseName())) {
            Warehouse warehouse = warehouseService.getById(event.getWarehouseId());
            if (warehouse != null) {
                event.setWarehouseName(warehouse.getWarehouseName());
            }
        }
        if (event.getFromWarehouseId() != null && StringUtils.isBlank(event.getFromWarehouseName())) {
            Warehouse warehouse = warehouseService.getById(event.getFromWarehouseId());
            if (warehouse != null) {
                event.setFromWarehouseName(warehouse.getWarehouseName());
            }
        }
        if (event.getToWarehouseId() != null && StringUtils.isBlank(event.getToWarehouseName())) {
            Warehouse warehouse = warehouseService.getById(event.getToWarehouseId());
            if (warehouse != null) {
                event.setToWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void updateIdentityState(AggregateRfidIdentity identity, AggregateEvent event, AggregateEventType eventType) {
        if (AggregateEventType.CREATED.equals(eventType)) {
            identity.setCurrentState(AggregateIdentityState.CREATED.getCode());
        } else if (AggregateEventType.INBOUND.equals(eventType)) {
            identity.setCurrentState(AggregateIdentityState.IN_STOCK.getCode());
            identity.setCurrentWarehouseId(event.getWarehouseId());
            identity.setCurrentWarehouseName(event.getWarehouseName());
            identity.setCurrentLocation(event.getLocationName());
        } else if (AggregateEventType.OUTBOUND.equals(eventType)) {
            identity.setCurrentState(AggregateIdentityState.OUT_STOCK.getCode());
            identity.setCurrentWarehouseId(null);
            identity.setCurrentWarehouseName(null);
            identity.setCurrentLocation(event.getLocationName());
        } else if (AggregateEventType.TRANSFER.equals(eventType)) {
            identity.setCurrentState(AggregateIdentityState.MOVED.getCode());
            identity.setCurrentWarehouseId(event.getToWarehouseId());
            identity.setCurrentWarehouseName(event.getToWarehouseName());
            identity.setCurrentLocation(event.getLocationName());
        }
        identity.setLastEventTime(event.getEventTime() == null ? new Date() : event.getEventTime());
        identity.setUpdateTime(DateUtils.getNowDate());
        aggregateRfidIdentityService.updateById(identity);
    }

    private void checkOwner(AggregateRfidIdentity identity) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId) && (identity.getOwnerUserId() == null || !identity.getOwnerUserId().equals(userId))) {
            throw new ServiceException("无权操作该RFID身份");
        }
    }
}
