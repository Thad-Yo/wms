package com.xiaoyai.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoyai.common.core.domain.entity.SysUser;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.SecurityUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateMaterialImportDto;
import com.xiaoyai.warehouse.enums.AggregateEventType;
import com.xiaoyai.warehouse.enums.AggregateIdentityState;
import com.xiaoyai.warehouse.mapper.AggregateEventMapper;
import com.xiaoyai.warehouse.mapper.AggregateMaterialMapper;
import com.xiaoyai.warehouse.mapper.AggregateRfidIdentityMapper;
import com.xiaoyai.warehouse.service.IAggregateMaterialService;
import com.xiaoyai.system.mapper.SysUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AggregateMaterialServiceImpl extends ServiceImpl<AggregateMaterialMapper, AggregateMaterial> implements IAggregateMaterialService {
    private static final Pattern RFID_SPLITTER = Pattern.compile("[,，;；\\s]+");

    @Autowired
    private AggregateRfidIdentityMapper aggregateRfidIdentityMapper;

    @Autowired
    private AggregateEventMapper aggregateEventMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<AggregateMaterial> selectAggregateMaterialList(AggregateMaterial aggregateMaterial) {
        applyUserScope(aggregateMaterial);
        return baseMapper.selectAggregateMaterialList(aggregateMaterial);
    }

    @Override
    public int insertAggregateMaterial(AggregateMaterial aggregateMaterial) {
        fillOwnerName(aggregateMaterial);
        aggregateMaterial.setCreateTime(DateUtils.getNowDate());
        return baseMapper.insert(aggregateMaterial);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AggregateMaterial importBatch(AggregateMaterialImportDto importDto) {
        List<String> rfidCodes = parseRfidCodes(importDto.getRfidCodes());
        if (rfidCodes.isEmpty()) {
            throw new ServiceException("RFID列表不能为空");
        }
        if (importDto.getOwnerUserId() == null) {
            throw new ServiceException("分配用户不能为空");
        }
        fillOwnerName(importDto);
        AggregateMaterial material = new AggregateMaterial();
        BeanUtils.copyProperties(importDto, material);
        material.setRfidCount((long) rfidCodes.size());
        material.setCreateTime(DateUtils.getNowDate());
        baseMapper.insert(material);

        List<AggregateRfidIdentity> identities = new ArrayList<>();
        for (String rfidCode : rfidCodes) {
            AggregateRfidIdentity exists = aggregateRfidIdentityMapper.selectOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                    .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                    .eq(AggregateRfidIdentity::getDelFlag, "0"));
            if (exists != null) {
                throw new ServiceException("RFID编码已存在：" + rfidCode);
            }

            AggregateRfidIdentity identity = new AggregateRfidIdentity();
            identity.setRfidCode(rfidCode);
            identity.setMaterialId(material.getMaterialId());
            identity.setMaterialCode(material.getMaterialCode());
            identity.setMaterialName(material.getMaterialName());
            identity.setBatchNo(material.getBatchNo());
            identity.setIdentityLevel("BATCH");
            identity.setCurrentState(AggregateIdentityState.CREATED.getCode());
            identity.setOwnerUserId(material.getOwnerUserId());
            identity.setOwnerUserName(material.getOwnerUserName());
            identity.setBindTime(DateUtils.getNowDate());
            identity.setLastEventTime(DateUtils.getNowDate());
            identity.setCreateBy(material.getCreateBy());
            identity.setCreateTime(DateUtils.getNowDate());
            identities.add(identity);
        }

        for (AggregateRfidIdentity identity : identities) {
            aggregateRfidIdentityMapper.insert(identity);
            AggregateEvent event = new AggregateEvent();
            event.setIdentityId(identity.getIdentityId());
            event.setRfidCode(identity.getRfidCode());
            event.setMaterialId(identity.getMaterialId());
            event.setEventType(AggregateEventType.CREATED.getCode());
            event.setEventName(AggregateEventType.CREATED.getName());
            event.setActionName("批量发行RFID身份");
            event.setCreateBy(material.getCreateBy());
            event.setEventTime(DateUtils.getNowDate());
            event.setCreateTime(DateUtils.getNowDate());
            aggregateEventMapper.insert(event);
        }
        return material;
    }

    @Override
    public int updateAggregateMaterial(AggregateMaterial aggregateMaterial) {
        fillOwnerName(aggregateMaterial);
        aggregateMaterial.setUpdateTime(DateUtils.getNowDate());
        int rows = baseMapper.updateById(aggregateMaterial);
        if (rows > 0) {
            aggregateRfidIdentityMapper.update(null, Wrappers.<AggregateRfidIdentity>lambdaUpdate()
                    .eq(AggregateRfidIdentity::getMaterialId, aggregateMaterial.getMaterialId())
                    .set(AggregateRfidIdentity::getMaterialCode, aggregateMaterial.getMaterialCode())
                    .set(AggregateRfidIdentity::getMaterialName, aggregateMaterial.getMaterialName())
                    .set(AggregateRfidIdentity::getBatchNo, aggregateMaterial.getBatchNo())
                    .set(AggregateRfidIdentity::getOwnerUserId, aggregateMaterial.getOwnerUserId())
                    .set(AggregateRfidIdentity::getOwnerUserName, aggregateMaterial.getOwnerUserName())
                    .set(AggregateRfidIdentity::getUpdateTime, DateUtils.getNowDate()));
        }
        return rows;
    }

    @Override
    public int deleteAggregateMaterialByIds(Long[] materialIds) {
        return lambdaUpdate().in(AggregateMaterial::getMaterialId, Arrays.asList(materialIds)).set(AggregateMaterial::getDelFlag, "1").update() ? 1 : 0;
    }

    private void applyUserScope(AggregateMaterial aggregateMaterial) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId)) {
            aggregateMaterial.setOwnerUserId(userId);
        }
    }

    private void fillOwnerName(AggregateMaterial aggregateMaterial) {
        if (aggregateMaterial.getOwnerUserId() != null && StringUtils.isBlank(aggregateMaterial.getOwnerUserName())) {
            SysUser user = sysUserMapper.selectUserById(aggregateMaterial.getOwnerUserId());
            if (user != null) {
                aggregateMaterial.setOwnerUserName(user.getUserName());
            }
        }
    }

    private List<String> parseRfidCodes(String rfidCodesText) {
        if (StringUtils.isBlank(rfidCodesText)) {
            return new ArrayList<>();
        }
        Set<String> codes = Arrays.stream(RFID_SPLITTER.split(rfidCodesText))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(codes);
    }
}
