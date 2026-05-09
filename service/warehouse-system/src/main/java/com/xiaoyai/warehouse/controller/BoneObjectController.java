package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.aggregate.BoneObject;
import com.xiaoyai.warehouse.domain.aggregate.dto.BoneObjectBindDto;
import com.xiaoyai.warehouse.service.IBoneObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse/aggregate/object")
public class BoneObjectController extends BaseController {
    @Autowired
    private IBoneObjectService boneObjectService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:list')")
    @GetMapping("/list")
    public TableDataInfo list(BoneObject boneObject) {
        startPage();
        List<BoneObject> list = boneObjectService.selectBoneObjectList(boneObject);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:query')")
    @GetMapping("/{objectId}")
    public AjaxResult getInfo(@PathVariable Long objectId) {
        return AjaxResult.success(boneObjectService.selectBoneObjectById(objectId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:add')")
    @Log(title = "对象管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BoneObject boneObject) {
        boneObject.setCreateBy(getUsername());
        return toAjax(boneObjectService.insertBoneObject(boneObject));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:edit')")
    @Log(title = "对象管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BoneObject boneObject) {
        boneObject.setUpdateBy(getUsername());
        return toAjax(boneObjectService.updateBoneObject(boneObject));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:edit')")
    @Log(title = "对象绑定骨料", businessType = BusinessType.UPDATE)
    @PutMapping("/bindBone")
    public AjaxResult bindBone(@RequestBody BoneObjectBindDto bindDto) {
        return toAjax(boneObjectService.bindBoneObject(bindDto, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:query')")
    @GetMapping("/boneOptions")
    public AjaxResult boneOptions(@RequestParam(required = false) String keyword) {
        return AjaxResult.success(boneObjectService.selectAvailableBoneRfidList(keyword));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:add')")
    @Log(title = "同步骨料池", businessType = BusinessType.INSERT)
    @PostMapping("/syncBonePool")
    public AjaxResult syncBonePool() {
        return AjaxResult.success("同步成功", boneObjectService.syncBoneRfidFromAggregate(getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:object:query')")
    @GetMapping("/timeline/{objectId}")
    public AjaxResult timeline(@PathVariable Long objectId) {
        return AjaxResult.success(boneObjectService.selectTimelineByObjectId(objectId));
    }
}
