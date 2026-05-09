package com.xiaoyai.warehouse.service;

import com.xiaoyai.warehouse.domain.aggregate.BoneObject;
import com.xiaoyai.warehouse.domain.aggregate.BoneRfid;
import com.xiaoyai.warehouse.domain.aggregate.dto.BoneObjectBindDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.BoneObjectTimelineVo;

import java.util.List;

public interface IBoneObjectService {
    List<BoneObject> selectBoneObjectList(BoneObject boneObject);

    BoneObject selectBoneObjectById(Long objectId);

    int insertBoneObject(BoneObject boneObject);

    int updateBoneObject(BoneObject boneObject);

    int bindBoneObject(BoneObjectBindDto bindDto, String operatorName);

    List<BoneRfid> selectAvailableBoneRfidList(String keyword);

    int syncBoneRfidFromAggregate(String operatorName);

    BoneObjectTimelineVo selectTimelineByObjectId(Long objectId);
}
