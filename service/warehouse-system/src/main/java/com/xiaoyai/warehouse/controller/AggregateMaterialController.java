package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateMaterialImportDto;
import com.xiaoyai.warehouse.service.IAggregateMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse/aggregate/material")
public class AggregateMaterialController extends BaseController {
    @Autowired
    private IAggregateMaterialService aggregateMaterialService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(AggregateMaterial aggregateMaterial) {
        startPage();
        List<AggregateMaterial> list = aggregateMaterialService.selectAggregateMaterialList(aggregateMaterial);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:query')")
    @GetMapping("/{materialId}")
    public AjaxResult getInfo(@PathVariable Long materialId) {
        return AjaxResult.success(aggregateMaterialService.getById(materialId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:add')")
    @Log(title = "数字骨料档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AggregateMaterial aggregateMaterial) {
        aggregateMaterial.setCreateBy(getUsername());
        return toAjax(aggregateMaterialService.insertAggregateMaterial(aggregateMaterial));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:import')")
    @Log(title = "数字骨料批量发行", businessType = BusinessType.IMPORT)
    @PostMapping("/importBatch")
    public AjaxResult importBatch(@RequestBody AggregateMaterialImportDto importDto) {
        importDto.setCreateBy(getUsername());
        return AjaxResult.success(aggregateMaterialService.importBatch(importDto));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:edit')")
    @Log(title = "数字骨料档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AggregateMaterial aggregateMaterial) {
        aggregateMaterial.setUpdateBy(getUsername());
        return toAjax(aggregateMaterialService.updateAggregateMaterial(aggregateMaterial));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:material:remove')")
    @Log(title = "数字骨料档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds) {
        return toAjax(aggregateMaterialService.deleteAggregateMaterialByIds(materialIds));
    }
}
