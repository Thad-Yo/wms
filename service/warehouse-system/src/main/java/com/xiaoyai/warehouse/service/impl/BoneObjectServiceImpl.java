package com.xiaoyai.warehouse.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoyai.common.core.domain.entity.SysUser;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.SecurityUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.system.mapper.SysUserMapper;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.BoneObject;
import com.xiaoyai.warehouse.domain.aggregate.BoneObjectEvent;
import com.xiaoyai.warehouse.domain.aggregate.BoneRfid;
import com.xiaoyai.warehouse.domain.aggregate.dto.BoneObjectBindDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.BoneObjectTimelineVo;
import com.xiaoyai.warehouse.mapper.AggregateRfidIdentityMapper;
import com.xiaoyai.warehouse.mapper.BoneObjectMapper;
import com.xiaoyai.warehouse.service.IBoneObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BoneObjectServiceImpl implements IBoneObjectService {
    @Autowired
    private BoneObjectMapper boneObjectMapper;

    @Autowired
    private AggregateRfidIdentityMapper aggregateRfidIdentityMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<BoneObject> selectBoneObjectList(BoneObject boneObject) {
        applyUserScope(boneObject);
        return boneObjectMapper.selectBoneObjectList(boneObject);
    }

    @Override
    public BoneObject selectBoneObjectById(Long objectId) {
        BoneObject boneObject = boneObjectMapper.selectBoneObjectById(objectId);
        if (boneObject == null || "1".equals(boneObject.getDelFlag())) {
            throw new ServiceException("对象不存在");
        }
        return boneObject;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBoneObject(BoneObject boneObject) {
        validateBoneObject(boneObject, null);
        fillOwnerName(boneObject);
        normalizeJsonFields(boneObject);
        boneObject.setBindStatus(StringUtils.isBlank(boneObject.getBindStatus()) ? "UNBOUND" : boneObject.getBindStatus());
        boneObject.setLifecycleStatus(StringUtils.isBlank(boneObject.getLifecycleStatus()) ? "CREATED" : boneObject.getLifecycleStatus());
        boneObject.setCreateTime(DateUtils.getNowDate());
        int rows = boneObjectMapper.insertBoneObject(boneObject);
        if (rows > 0) {
            insertEvent(boneObject, null, "CREATE", "创建对象", "object", boneObject.getCreateBy(), boneObject.getRemark());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBoneObject(BoneObject boneObject) {
        BoneObject exists = selectBoneObjectById(boneObject.getObjectId());
        validateBoneObject(boneObject, boneObject.getObjectId());
        fillOwnerName(boneObject);
        normalizeJsonFields(boneObject);
        boneObject.setUpdateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(boneObject.getBindStatus())) {
            boneObject.setBindStatus(exists.getBindStatus());
        }
        if (StringUtils.isBlank(boneObject.getLifecycleStatus())) {
            boneObject.setLifecycleStatus(exists.getLifecycleStatus());
        }
        if (boneObject.getBoneRfidId() == null) {
            boneObject.setBoneRfidId(exists.getBoneRfidId());
            boneObject.setBoneRfidCode(exists.getBoneRfidCode());
            boneObject.setBindTime(exists.getBindTime());
        }
        int rows = boneObjectMapper.updateBoneObject(boneObject);
        if (rows > 0) {
            BoneObject latest = boneObjectMapper.selectBoneObjectById(boneObject.getObjectId());
            insertEvent(latest, exists, "UPDATE", "更新对象", "object", boneObject.getUpdateBy(), boneObject.getRemark());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int bindBoneObject(BoneObjectBindDto bindDto, String operatorName) {
        if (bindDto == null || bindDto.getObjectId() == null) {
            throw new ServiceException("请选择对象");
        }
        if (bindDto.getBoneRfidId() == null) {
            throw new ServiceException("请选择骨料");
        }
        BoneObject boneObject = selectBoneObjectById(bindDto.getObjectId());
        if ("BOUND".equals(boneObject.getBindStatus())) {
            throw new ServiceException("该对象已绑定骨料");
        }
        BoneRfid boneRfid = boneObjectMapper.selectBoneRfidById(bindDto.getBoneRfidId());
        if (boneRfid == null) {
            throw new ServiceException("骨料不存在");
        }
        if (boneRfid.getCurrentObjectId() != null) {
            throw new ServiceException("该骨料已绑定对象");
        }
        Date now = DateUtils.getNowDate();
        int rows = boneObjectMapper.bindBoneRfidToObject(boneObject.getObjectId(), boneRfid.getBoneRfidId(), boneRfid.getBoneRfidCode(), operatorName);
        boneObjectMapper.occupyBoneRfid(boneRfid.getBoneRfidId(), boneObject.getObjectId(), boneObject.getObjectCode(), boneObject.getObjectName(), operatorName);
        if (rows > 0) {
            BoneObject latest = boneObjectMapper.selectBoneObjectById(boneObject.getObjectId());
            latest.setBindTime(now);
            insertEvent(latest, boneObject, "BIND", "绑定骨料", "object", operatorName, bindDto.getRemark());
        }
        return rows;
    }

    @Override
    public List<BoneRfid> selectAvailableBoneRfidList(String keyword) {
        return boneObjectMapper.selectAvailableBoneRfidList(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncBoneRfidFromAggregate(String operatorName) {
        List<AggregateRfidIdentity> identities = aggregateRfidIdentityMapper.selectList(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getDelFlag, "0"));
        int count = 0;
        for (AggregateRfidIdentity identity : identities) {
            BoneRfid exists = boneObjectMapper.selectBoneRfidByCode(identity.getRfidCode());
            if (exists != null) {
                continue;
            }
            BoneRfid boneRfid = new BoneRfid();
            boneRfid.setBoneRfidCode(identity.getRfidCode());
            boneRfid.setTidCode(identity.getTidCode());
            boneRfid.setBoneCode(StringUtils.isNotBlank(identity.getMaterialCode()) ? identity.getMaterialCode() : "BONE-" + identity.getIdentityId());
            boneRfid.setBoneName(StringUtils.isNotBlank(identity.getMaterialName()) ? identity.getMaterialName() : "骨料身份-" + identity.getIdentityId());
            boneRfid.setStatus("UNASSIGNED");
            boneRfid.setCreateBy(operatorName);
            boneRfid.setCreateTime(DateUtils.getNowDate());
            boneRfid.setUpdateBy(operatorName);
            boneRfid.setUpdateTime(DateUtils.getNowDate());
            boneRfid.setRemark("由 aggregate_rfid_identity 同步生成");
            count += boneObjectMapper.insertBoneRfid(boneRfid);
        }
        return count;
    }

    @Override
    public BoneObjectTimelineVo selectTimelineByObjectId(Long objectId) {
        BoneObjectTimelineVo vo = new BoneObjectTimelineVo();
        BoneObject boneObject = selectBoneObjectById(objectId);
        vo.setObject(boneObject);
        vo.setEvents(boneObjectMapper.selectBoneObjectEventList(objectId));
        return vo;
    }

    private void validateBoneObject(BoneObject boneObject, Long objectId) {
        if (StringUtils.isBlank(boneObject.getObjectCode())) {
            throw new ServiceException("对象编号不能为空");
        }
        if (StringUtils.isBlank(boneObject.getObjectName())) {
            throw new ServiceException("对象名称不能为空");
        }
        if (StringUtils.isBlank(boneObject.getObjectType())) {
            throw new ServiceException("对象类型不能为空");
        }
        List<BoneObject> existsList = boneObjectMapper.selectBoneObjectList(new BoneObject());
        for (BoneObject item : existsList) {
            if (boneObject.getObjectCode().equals(item.getObjectCode()) && (objectId == null || !objectId.equals(item.getObjectId()))) {
                throw new ServiceException("对象编号已存在");
            }
        }
    }

    private void applyUserScope(BoneObject boneObject) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId)) {
            boneObject.setOwnerUserId(userId);
        }
    }

    private void fillOwnerName(BoneObject boneObject) {
        if (boneObject.getOwnerUserId() != null && StringUtils.isBlank(boneObject.getOwnerUserName())) {
            SysUser user = sysUserMapper.selectUserById(boneObject.getOwnerUserId());
            if (user != null) {
                boneObject.setOwnerUserName(user.getUserName());
            }
        }
    }

    private void normalizeJsonFields(BoneObject boneObject) {
        boneObject.setFixedDataJson(normalizeJsonObject(boneObject.getFixedDataJson()));
        boneObject.setDynamicDataJson(normalizeJsonObject(boneObject.getDynamicDataJson()));
        boneObject.setTraceDataJson(normalizeJsonObject(boneObject.getTraceDataJson()));
        boneObject.setExtDataJson(normalizeJsonObject(boneObject.getExtDataJson()));
    }

    private String normalizeJsonObject(String jsonText) {
        if (StringUtils.isBlank(jsonText)) {
            return "{}";
        }
        try {
            return JSON.toJSONString(JSON.parseObject(jsonText));
        } catch (Exception e) {
            throw new ServiceException("JSON格式不正确");
        }
    }

    private void insertEvent(BoneObject current, BoneObject before, String eventType, String eventName,
                             String sourceModule, String operatorName, String remark) {
        BoneObjectEvent event = new BoneObjectEvent();
        event.setObjectId(current.getObjectId());
        event.setObjectCode(current.getObjectCode());
        event.setObjectName(current.getObjectName());
        event.setBoneRfidId(current.getBoneRfidId());
        event.setBoneRfidCode(current.getBoneRfidCode());
        event.setEventType(eventType);
        event.setEventName(eventName);
        event.setEventTime(DateUtils.getNowDate());
        event.setOperatorId(SecurityUtils.getUserId());
        event.setOperatorName(operatorName);
        event.setSourceModule(sourceModule);
        event.setSnapshotData(buildSnapshot(current));
        event.setBeforeData(before == null ? "{}" : buildSnapshot(before));
        event.setChangedFields(buildChangedFields(current, before));
        event.setExtData("{}");
        boneObjectMapper.insertBoneObjectEvent(event);
    }

    private String buildSnapshot(BoneObject boneObject) {
        JSONObject snapshot = new JSONObject();
        snapshot.put("objectCode", boneObject.getObjectCode());
        snapshot.put("objectName", boneObject.getObjectName());
        snapshot.put("objectType", boneObject.getObjectType());
        snapshot.put("templateId", boneObject.getTemplateId());
        snapshot.put("templateName", boneObject.getTemplateName());
        snapshot.put("boneRfidCode", boneObject.getBoneRfidCode());
        snapshot.put("bindStatus", boneObject.getBindStatus());
        snapshot.put("lifecycleStatus", boneObject.getLifecycleStatus());
        snapshot.put("ownerUserName", boneObject.getOwnerUserName());
        snapshot.put("sourceSystem", boneObject.getSourceSystem());
        snapshot.put("fixedData", parseJsonObject(boneObject.getFixedDataJson()));
        snapshot.put("dynamicData", parseJsonObject(boneObject.getDynamicDataJson()));
        snapshot.put("traceData", parseJsonObject(boneObject.getTraceDataJson()));
        return snapshot.toJSONString();
    }

    private JSONObject parseJsonObject(String jsonText) {
        if (StringUtils.isBlank(jsonText)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(jsonText);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private String buildChangedFields(BoneObject current, BoneObject before) {
        if (before == null) {
            return "[\"objectCode\",\"objectName\",\"objectType\"]";
        }
        Set<String> fields = new LinkedHashSet<>();
        addChangedField(fields, "objectCode", current.getObjectCode(), before.getObjectCode());
        addChangedField(fields, "objectName", current.getObjectName(), before.getObjectName());
        addChangedField(fields, "objectType", current.getObjectType(), before.getObjectType());
        addChangedField(fields, "templateId", current.getTemplateId(), before.getTemplateId());
        addChangedField(fields, "boneRfidCode", current.getBoneRfidCode(), before.getBoneRfidCode());
        addChangedField(fields, "bindStatus", current.getBindStatus(), before.getBindStatus());
        addChangedField(fields, "lifecycleStatus", current.getLifecycleStatus(), before.getLifecycleStatus());
        addChangedField(fields, "dynamicData", normalizeJsonObject(current.getDynamicDataJson()), normalizeJsonObject(before.getDynamicDataJson()));
        return JSON.toJSONString(new ArrayList<>(fields));
    }

    private void addChangedField(Set<String> fields, String fieldName, Object current, Object before) {
        if (!String.valueOf(current).equals(String.valueOf(before))) {
            fields.add(fieldName);
        }
    }
}
