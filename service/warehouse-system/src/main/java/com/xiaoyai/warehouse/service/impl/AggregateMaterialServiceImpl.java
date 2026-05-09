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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AggregateMaterialServiceImpl extends ServiceImpl<AggregateMaterialMapper, AggregateMaterial> implements IAggregateMaterialService {
    private static final Pattern RFID_SPLITTER = Pattern.compile("[,，;；\\s]+");
    private static final DateTimeFormatter BATCH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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
    public AggregateMaterial getById(java.io.Serializable id) {
        AggregateMaterial material = super.getById(id);
        if (material != null) {
            material.setRfidCodes(loadRfidCodesText(material.getMaterialId()));
        }
        return material;
    }

    @Override
    public int insertAggregateMaterial(AggregateMaterial aggregateMaterial) {
        fillOwnerName(aggregateMaterial);
        if (aggregateMaterial.getCreateTime() == null) {
            aggregateMaterial.setCreateTime(DateUtils.getNowDate());
        }
        prepareMaterialForSave(aggregateMaterial, true);
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
        if (material.getCreateTime() == null) {
            material.setCreateTime(DateUtils.getNowDate());
        }
        prepareMaterialForSave(material, true);
        baseMapper.insert(material);

        List<AggregateRfidIdentity> identities = new ArrayList<>();
        for (String rfidCode : rfidCodes) {
            AggregateRfidIdentity exists = aggregateRfidIdentityMapper.selectOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                    .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                    .last("limit 1"));
            if (exists != null && "0".equals(exists.getDelFlag())) {
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
            if (exists != null) {
                identity.setIdentityId(exists.getIdentityId());
            }
            identities.add(identity);
        }

        for (AggregateRfidIdentity identity : identities) {
            saveOrRestoreIdentity(identity);
            AggregateEvent event = new AggregateEvent();
            event.setIdentityId(identity.getIdentityId());
            event.setRfidCode(identity.getRfidCode());
            event.setMaterialId(identity.getMaterialId());
            event.setEventType(AggregateEventType.CREATED.getCode());
            event.setEventName(AggregateEventType.CREATED.getName());
            event.setActionName("批量创建标签身份");
            event.setCreateBy(material.getCreateBy());
            event.setEventTime(DateUtils.getNowDate());
            event.setCreateTime(DateUtils.getNowDate());
            aggregateEventMapper.insert(event);
        }
        return material;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAggregateMaterial(AggregateMaterial aggregateMaterial) {
        fillOwnerName(aggregateMaterial);
        AggregateMaterial dbMaterial = getById(aggregateMaterial.getMaterialId());
        if (dbMaterial == null) {
            throw new ServiceException("骨料入库单不存在");
        }
        ensurePendingState(dbMaterial, "当前单据不是待审核状态，不能修改");
        aggregateMaterial.setBatchNo(dbMaterial.getBatchNo());
        if (StringUtils.isBlank(aggregateMaterial.getMaterialCode())) {
            aggregateMaterial.setMaterialCode(dbMaterial.getMaterialCode());
        }
        if (StringUtils.isBlank(aggregateMaterial.getMaterialName())) {
            aggregateMaterial.setMaterialName(dbMaterial.getMaterialName());
        }
        if (StringUtils.isBlank(aggregateMaterial.getState())) {
            aggregateMaterial.setState(dbMaterial.getState());
        }
        if (aggregateMaterial.getOwnerUserId() == null) {
            aggregateMaterial.setOwnerUserId(dbMaterial.getOwnerUserId());
        }
        if (StringUtils.isBlank(aggregateMaterial.getOwnerUserName())) {
            aggregateMaterial.setOwnerUserName(dbMaterial.getOwnerUserName());
        }
        aggregateMaterial.setAuditTime(dbMaterial.getAuditTime());
        aggregateMaterial.setAuditId(dbMaterial.getAuditId());
        aggregateMaterial.setAuditName(dbMaterial.getAuditName());
        syncMaterialRfidList(dbMaterial, aggregateMaterial);
        aggregateMaterial.setUpdateTime(DateUtils.getNowDate());
        int rows = baseMapper.updateById(aggregateMaterial);
        if (rows > 0) {
            aggregateRfidIdentityMapper.update(null, Wrappers.<AggregateRfidIdentity>update()
                    .eq("material_id", aggregateMaterial.getMaterialId())
                    .eq("del_flag", "0")
                    .set("material_code", aggregateMaterial.getMaterialCode())
                    .set("material_name", aggregateMaterial.getMaterialName())
                    .set("batch_no", aggregateMaterial.getBatchNo())
                    .set("owner_user_id", aggregateMaterial.getOwnerUserId())
                    .set("owner_user_name", aggregateMaterial.getOwnerUserName())
                    .set("update_time", DateUtils.getNowDate()));
        }
        return rows;
    }

    @Override
    public int deleteAggregateMaterialByIds(Long[] materialIds) {
        List<AggregateMaterial> materials = listByIds(Arrays.asList(materialIds));
        if (materials.isEmpty()) {
            return 0;
        }
        for (AggregateMaterial material : materials) {
            ensurePendingState(material, "仅待审核单据允许删除");
        }
        Date now = DateUtils.getNowDate();
        boolean materialUpdated = update(Wrappers.<AggregateMaterial>update()
                .in("material_id", Arrays.asList(materialIds))
                .set("del_flag", "1")
                .set("update_time", now))
                ;
        aggregateRfidIdentityMapper.update(null, Wrappers.<AggregateRfidIdentity>update()
                .in("material_id", Arrays.asList(materialIds))
                .set("del_flag", "1")
                .set("update_time", now));
        return materialUpdated ? materials.size() : 0;
    }

    @Override
    public String previewNextBatchNo() {
        return generateBatchNo(DateUtils.getNowDate());
    }

    @Override
    public int approveAggregateMaterial(AggregateMaterial aggregateMaterial) {
        AggregateMaterial dbMaterial = getById(aggregateMaterial.getMaterialId());
        if (dbMaterial == null || "1".equals(dbMaterial.getDelFlag())) {
            throw new ServiceException("骨料入库单不存在");
        }
        ensurePendingState(dbMaterial, "当前单据状态不是待审核状态，请检查");
        AggregateMaterial updateEntity = new AggregateMaterial();
        updateEntity.setMaterialId(dbMaterial.getMaterialId());
        updateEntity.setState("4");
        updateEntity.setAuditId(aggregateMaterial.getAuditId());
        updateEntity.setAuditName(aggregateMaterial.getAuditName());
        updateEntity.setAuditTime(DateUtils.getNowDate());
        updateEntity.setUpdateBy(aggregateMaterial.getUpdateBy());
        updateEntity.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(updateEntity);
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

    private synchronized String generateBatchNo(java.util.Date createTime) {
        LocalDate date = createTime == null
                ? LocalDate.now()
                : createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String prefix = "GL-" + date.format(BATCH_DATE_FORMATTER);
        List<AggregateMaterial> sameDayList = lambdaQuery()
                .likeRight(AggregateMaterial::getBatchNo, prefix)
                .orderByDesc(AggregateMaterial::getBatchNo)
                .last("limit 1")
                .list();
        int nextSeq = 1;
        if (!sameDayList.isEmpty() && StringUtils.isNotBlank(sameDayList.get(0).getBatchNo())) {
            String currentBatchNo = sameDayList.get(0).getBatchNo();
            if (currentBatchNo.length() >= prefix.length() + 2) {
                String seqText = currentBatchNo.substring(prefix.length());
                if (StringUtils.isNumeric(seqText)) {
                    nextSeq = Integer.parseInt(seqText) + 1;
                }
            }
        }
        return prefix + String.format("%02d", nextSeq);
    }

    private void prepareMaterialForSave(AggregateMaterial aggregateMaterial, boolean generateBatchNo) {
        if (generateBatchNo || StringUtils.isBlank(aggregateMaterial.getBatchNo())) {
            aggregateMaterial.setBatchNo(generateBatchNo(aggregateMaterial.getCreateTime()));
        }
        if (StringUtils.isBlank(aggregateMaterial.getMaterialCode())) {
            aggregateMaterial.setMaterialCode(aggregateMaterial.getBatchNo());
        }
        if (StringUtils.isBlank(aggregateMaterial.getMaterialName())) {
            aggregateMaterial.setMaterialName(aggregateMaterial.getBatchNo());
        }
        if (StringUtils.isBlank(aggregateMaterial.getState())) {
            aggregateMaterial.setState("3");
        }
    }

    private String loadRfidCodesText(Long materialId) {
        if (materialId == null) {
            return null;
        }
        List<AggregateRfidIdentity> identities = aggregateRfidIdentityMapper.selectList(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getMaterialId, materialId)
                .eq(AggregateRfidIdentity::getDelFlag, "0")
                .orderByAsc(AggregateRfidIdentity::getIdentityId));
        return identities.stream()
                .map(AggregateRfidIdentity::getRfidCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

    private void ensurePendingState(AggregateMaterial material, String message) {
        if (material == null) {
            throw new ServiceException("骨料入库单不存在");
        }
        if (!Arrays.asList("1", "3").contains(material.getState())) {
            throw new ServiceException(message);
        }
    }

    private void syncMaterialRfidList(AggregateMaterial dbMaterial, AggregateMaterial updateMaterial) {
        if (StringUtils.isBlank(updateMaterial.getRfidCodes())) {
            return;
        }
        List<String> latestCodes = parseRfidCodes(updateMaterial.getRfidCodes());
        if (latestCodes.isEmpty()) {
            throw new ServiceException("RFID列表不能为空");
        }
        List<AggregateRfidIdentity> currentList = aggregateRfidIdentityMapper.selectList(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getMaterialId, dbMaterial.getMaterialId())
                .eq(AggregateRfidIdentity::getDelFlag, "0")
                .orderByAsc(AggregateRfidIdentity::getIdentityId));
        Map<String, AggregateRfidIdentity> currentMap = currentList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getRfidCode()))
                .collect(Collectors.toMap(AggregateRfidIdentity::getRfidCode, item -> item, (left, right) -> left, LinkedHashMap::new));

        for (String rfidCode : latestCodes) {
            AggregateRfidIdentity exists = aggregateRfidIdentityMapper.selectOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                    .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                    .eq(AggregateRfidIdentity::getDelFlag, "0")
                    .last("limit 1"));
            if (exists != null && !dbMaterial.getMaterialId().equals(exists.getMaterialId())) {
                throw new ServiceException("RFID编码已存在：" + rfidCode);
            }
        }

        Set<String> latestCodeSet = new LinkedHashSet<>(latestCodes);
        List<AggregateRfidIdentity> removeList = currentList.stream()
                .filter(item -> !latestCodeSet.contains(item.getRfidCode()))
                .collect(Collectors.toList());
        for (AggregateRfidIdentity removeItem : removeList) {
            if (removeItem.getBindGoodsId() != null) {
                throw new ServiceException("RFID已绑定对象，不能移除：" + removeItem.getRfidCode());
            }
        }

        Date now = DateUtils.getNowDate();
        for (AggregateRfidIdentity removeItem : removeList) {
            AggregateRfidIdentity deleteEntity = new AggregateRfidIdentity();
            deleteEntity.setIdentityId(removeItem.getIdentityId());
            deleteEntity.setDelFlag("1");
            deleteEntity.setUpdateBy(updateMaterial.getUpdateBy());
            deleteEntity.setUpdateTime(now);
            aggregateRfidIdentityMapper.updateById(deleteEntity);
        }

        for (String rfidCode : latestCodes) {
            if (currentMap.containsKey(rfidCode)) {
                continue;
            }
            AggregateRfidIdentity exists = aggregateRfidIdentityMapper.selectOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                    .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                    .last("limit 1"));
            if (exists != null && "0".equals(exists.getDelFlag()) && !dbMaterial.getMaterialId().equals(exists.getMaterialId())) {
                throw new ServiceException("RFID编码已存在：" + rfidCode);
            }
            AggregateRfidIdentity identity = new AggregateRfidIdentity();
            identity.setRfidCode(rfidCode);
            identity.setMaterialId(dbMaterial.getMaterialId());
            identity.setMaterialCode(updateMaterial.getMaterialCode());
            identity.setMaterialName(updateMaterial.getMaterialName());
            identity.setBatchNo(updateMaterial.getBatchNo());
            identity.setIdentityLevel("BATCH");
            identity.setCurrentState(AggregateIdentityState.CREATED.getCode());
            identity.setOwnerUserId(updateMaterial.getOwnerUserId());
            identity.setOwnerUserName(updateMaterial.getOwnerUserName());
            identity.setBindTime(now);
            identity.setLastEventTime(now);
            identity.setCreateBy(updateMaterial.getUpdateBy());
            identity.setCreateTime(now);
            if (exists != null) {
                identity.setIdentityId(exists.getIdentityId());
            }
            saveOrRestoreIdentity(identity);

            AggregateEvent event = new AggregateEvent();
            event.setIdentityId(identity.getIdentityId());
            event.setRfidCode(identity.getRfidCode());
            event.setMaterialId(identity.getMaterialId());
            event.setEventType(AggregateEventType.CREATED.getCode());
            event.setEventName(AggregateEventType.CREATED.getName());
            event.setActionName("修改批次时新增标签身份");
            event.setCreateBy(updateMaterial.getUpdateBy());
            event.setEventTime(now);
            event.setCreateTime(now);
            aggregateEventMapper.insert(event);
        }

        updateMaterial.setRfidCount((long) latestCodes.size());
    }

    private void saveOrRestoreIdentity(AggregateRfidIdentity identity) {
        if (identity.getIdentityId() == null) {
            aggregateRfidIdentityMapper.insert(identity);
            return;
        }
        AggregateRfidIdentity restoreEntity = new AggregateRfidIdentity();
        restoreEntity.setIdentityId(identity.getIdentityId());
        restoreEntity.setRfidCode(identity.getRfidCode());
        restoreEntity.setTidCode(null);
        restoreEntity.setMaterialId(identity.getMaterialId());
        restoreEntity.setMaterialCode(identity.getMaterialCode());
        restoreEntity.setMaterialName(identity.getMaterialName());
        restoreEntity.setBatchNo(identity.getBatchNo());
        restoreEntity.setIdentityLevel(identity.getIdentityLevel());
        restoreEntity.setCurrentState(identity.getCurrentState());
        restoreEntity.setOwnerUserId(identity.getOwnerUserId());
        restoreEntity.setOwnerUserName(identity.getOwnerUserName());
        restoreEntity.setBindGoodsId(null);
        restoreEntity.setBindGoodsCode(null);
        restoreEntity.setBindGoodsName(null);
        restoreEntity.setBindGoodsTime(null);
        restoreEntity.setCurrentWarehouseId(null);
        restoreEntity.setCurrentWarehouseName(null);
        restoreEntity.setCurrentLocation(null);
        restoreEntity.setBindTime(identity.getBindTime());
        restoreEntity.setLastEventTime(identity.getLastEventTime());
        restoreEntity.setDelFlag("0");
        restoreEntity.setUpdateBy(identity.getCreateBy());
        restoreEntity.setUpdateTime(identity.getCreateTime());
        aggregateRfidIdentityMapper.updateById(restoreEntity);
        identity.setIdentityId(restoreEntity.getIdentityId());
    }
}
