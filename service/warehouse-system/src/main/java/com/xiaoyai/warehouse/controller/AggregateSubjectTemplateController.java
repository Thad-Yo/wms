package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateSubjectTemplateDto;
import com.xiaoyai.warehouse.service.IAggregateSubjectTemplateService;
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
@RequestMapping("/warehouse/aggregate/subjectTemplate")
public class AggregateSubjectTemplateController extends BaseController {
    @Autowired
    private IAggregateSubjectTemplateService aggregateSubjectTemplateService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(AggregateSubjectTemplate aggregateSubjectTemplate) {
        startPage();
        List<AggregateSubjectTemplate> list = aggregateSubjectTemplateService.selectAggregateSubjectTemplateList(aggregateSubjectTemplate);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:query')")
    @GetMapping("/{templateId}")
    public AjaxResult getInfo(@PathVariable Long templateId) {
        return AjaxResult.success(aggregateSubjectTemplateService.selectAggregateSubjectTemplateById(templateId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:query')")
    @GetMapping("/options/enabled")
    public AjaxResult enabledOptions() {
        return AjaxResult.success(aggregateSubjectTemplateService.selectEnabledTemplateOptions());
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:add')")
    @GetMapping("/nextCode")
    public AjaxResult nextCode() {
        return AjaxResult.success(aggregateSubjectTemplateService.previewNextSubjectCode());
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:add')")
    @Log(title = "骨料模板管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AggregateSubjectTemplateDto templateDto) {
        templateDto.setCreateBy(getUsername());
        return toAjax(aggregateSubjectTemplateService.insertAggregateSubjectTemplate(templateDto));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:edit')")
    @Log(title = "骨料模板管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AggregateSubjectTemplateDto templateDto) {
        templateDto.setUpdateBy(getUsername());
        return toAjax(aggregateSubjectTemplateService.updateAggregateSubjectTemplate(templateDto));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:edit')")
    @Log(title = "骨料模板启用", businessType = BusinessType.UPDATE)
    @PutMapping("/activate/{templateId}")
    public AjaxResult activate(@PathVariable Long templateId) {
        return toAjax(aggregateSubjectTemplateService.activateTemplate(templateId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:add')")
    @Log(title = "骨料模板复制", businessType = BusinessType.INSERT)
    @PostMapping("/copy/{templateId}")
    public AjaxResult copy(@PathVariable Long templateId) {
        return AjaxResult.success(aggregateSubjectTemplateService.copyTemplate(templateId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:subjectTemplate:remove')")
    @Log(title = "骨料模板管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        return toAjax(aggregateSubjectTemplateService.deleteAggregateSubjectTemplateByIds(templateIds));
    }
}
