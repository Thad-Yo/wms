package com.xiaoyai.warehouse.mapper;

import com.xiaoyai.warehouse.domain.aggregate.BoneObject;
import com.xiaoyai.warehouse.domain.aggregate.BoneObjectEvent;
import com.xiaoyai.warehouse.domain.aggregate.BoneRfid;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BoneObjectMapper {
    List<BoneObject> selectBoneObjectList(BoneObject boneObject);

    BoneObject selectBoneObjectById(@Param("objectId") Long objectId);

    int insertBoneObject(BoneObject boneObject);

    int updateBoneObject(BoneObject boneObject);

    List<BoneRfid> selectAvailableBoneRfidList(@Param("keyword") String keyword);

    BoneRfid selectBoneRfidById(@Param("boneRfidId") Long boneRfidId);

    BoneRfid selectBoneRfidByCode(@Param("boneRfidCode") String boneRfidCode);

    int insertBoneRfid(BoneRfid boneRfid);

    int bindBoneRfidToObject(@Param("objectId") Long objectId,
                             @Param("boneRfidId") Long boneRfidId,
                             @Param("boneRfidCode") String boneRfidCode,
                             @Param("updateBy") String updateBy);

    int occupyBoneRfid(@Param("boneRfidId") Long boneRfidId,
                       @Param("objectId") Long objectId,
                       @Param("objectCode") String objectCode,
                       @Param("objectName") String objectName,
                       @Param("updateBy") String updateBy);

    int insertBoneObjectEvent(BoneObjectEvent event);

    List<BoneObjectEvent> selectBoneObjectEventList(@Param("objectId") Long objectId);
}
