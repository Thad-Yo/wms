package com.xiaoyai.warehouse.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectField;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateSubjectTemplateDto;
import com.xiaoyai.warehouse.mapper.AggregateSubjectFieldMapper;
import com.xiaoyai.warehouse.mapper.AggregateSubjectTemplateMapper;
import com.xiaoyai.warehouse.service.IAggregateSubjectTemplateService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class AggregateSubjectTemplateServiceImpl implements IAggregateSubjectTemplateService {
    @Autowired
    private AggregateSubjectTemplateMapper aggregateSubjectTemplateMapper;

    @Autowired
    private AggregateSubjectFieldMapper aggregateSubjectFieldMapper;

    @Override
    public List<AggregateSubjectTemplate> selectAggregateSubjectTemplateList(AggregateSubjectTemplate aggregateSubjectTemplate) {
        return aggregateSubjectTemplateMapper.selectAggregateSubjectTemplateList(aggregateSubjectTemplate);
    }

    @Override
    public AggregateSubjectTemplateDto selectAggregateSubjectTemplateById(Long templateId) {
        AggregateSubjectTemplate template = aggregateSubjectTemplateMapper.selectById(templateId);
        if (template == null || "1".equals(template.getDelFlag())) {
            throw new ServiceException("骨料模板不存在");
        }
        AggregateSubjectTemplateDto dto = new AggregateSubjectTemplateDto();
        BeanUtils.copyProperties(template, dto);
        dto.setFieldList(selectFieldList(templateId));
        return dto;
    }

    @Override
    public List<AggregateSubjectTemplate> selectEnabledTemplateOptions() {
        return aggregateSubjectTemplateMapper.selectList(Wrappers.<AggregateSubjectTemplate>lambdaQuery()
                .eq(AggregateSubjectTemplate::getDelFlag, "0")
                .eq(AggregateSubjectTemplate::getStatus, "0")
                .orderByAsc(AggregateSubjectTemplate::getTemplateId));
    }

    @Override
    public String previewNextSubjectCode() {
        String prefix = "TPL-" + DateUtils.parseDateToStr("yyyyMMdd", DateUtils.getNowDate());
        int suffix = 1;
        while (aggregateSubjectTemplateMapper.selectOne(Wrappers.<AggregateSubjectTemplate>lambdaQuery()
                .eq(AggregateSubjectTemplate::getSubjectCode, prefix + String.format("%02d", suffix))
                .eq(AggregateSubjectTemplate::getDelFlag, "0")) != null) {
            suffix++;
        }
        return prefix + String.format("%02d", suffix);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAggregateSubjectTemplate(AggregateSubjectTemplateDto templateDto) {
        validateTemplate(templateDto, null);
        normalizeUseFlag(templateDto);
        if (StringUtils.isBlank(templateDto.getSubjectCode())) {
            templateDto.setSubjectCode(previewNextSubjectCode());
        }
        templateDto.setCreateTime(DateUtils.getNowDate());
        if (shouldActivate(templateDto)) {
            disableAllTemplates();
        }
        int rows = aggregateSubjectTemplateMapper.insert(templateDto);
        saveFields(templateDto);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAggregateSubjectTemplate(AggregateSubjectTemplateDto templateDto) {
        AggregateSubjectTemplate exists = aggregateSubjectTemplateMapper.selectById(templateDto.getTemplateId());
        if (exists == null || "1".equals(exists.getDelFlag())) {
            throw new ServiceException("骨料模板不存在");
        }
        validateTemplate(templateDto, templateDto.getTemplateId());
        normalizeUseFlag(templateDto);
        templateDto.setSubjectCode(exists.getSubjectCode());
        templateDto.setUpdateTime(DateUtils.getNowDate());
        if (shouldActivate(templateDto)) {
            disableAllTemplates();
        }
        int rows = aggregateSubjectTemplateMapper.updateById(templateDto);
        aggregateSubjectFieldMapper.delete(Wrappers.<AggregateSubjectField>lambdaQuery()
                .eq(AggregateSubjectField::getTemplateId, templateDto.getTemplateId()));
        saveFields(templateDto);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int activateTemplate(Long templateId) {
        AggregateSubjectTemplate template = aggregateSubjectTemplateMapper.selectById(templateId);
        if (template == null || "1".equals(template.getDelFlag())) {
            throw new ServiceException("骨料模板不存在");
        }
        disableAllTemplates();
        template.setStatus("0");
        template.setUpdateTime(DateUtils.getNowDate());
        return aggregateSubjectTemplateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AggregateSubjectTemplateDto copyTemplate(Long templateId, String operatorName) {
        AggregateSubjectTemplateDto source = selectAggregateSubjectTemplateById(templateId);
        AggregateSubjectTemplateDto target = new AggregateSubjectTemplateDto();
        target.setSubjectCode(buildCopyCode(source.getSubjectCode()));
        target.setSubjectName(source.getSubjectName() + "-副本");
        target.setModuleName(source.getModuleName());
        target.setStatus("1");
        target.setRemark(source.getRemark());
        target.setCreateBy(operatorName);
        List<AggregateSubjectField> copiedFields = new ArrayList<>();
        for (AggregateSubjectField sourceField : source.getFieldList()) {
            AggregateSubjectField field = new AggregateSubjectField();
            field.setFieldCode(sourceField.getFieldCode());
            field.setFieldLabel(sourceField.getFieldLabel());
            field.setFieldType(sourceField.getFieldType());
            field.setPlaceholder(sourceField.getPlaceholder());
            field.setDefaultValue(sourceField.getDefaultValue());
            field.setOptionsJson(sourceField.getOptionsJson());
            field.setValidationRule(sourceField.getValidationRule());
            field.setRequiredFlag(sourceField.getRequiredFlag());
            field.setIndexedFlag(sourceField.getIndexedFlag());
            field.setTraceFlag(sourceField.getTraceFlag());
            field.setSearchFlag(sourceField.getSearchFlag());
            field.setExportFlag(sourceField.getExportFlag());
            field.setWriteScope(sourceField.getWriteScope());
            field.setEditableEventTypes(sourceField.getEditableEventTypes());
            field.setSortOrder(sourceField.getSortOrder());
            field.setRemark(sourceField.getRemark());
            copiedFields.add(field);
        }
        target.setFieldList(copiedFields);
        insertAggregateSubjectTemplate(target);
        return selectAggregateSubjectTemplateById(target.getTemplateId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAggregateSubjectTemplateByIds(Long[] templateIds) {
        aggregateSubjectFieldMapper.delete(Wrappers.<AggregateSubjectField>lambdaQuery()
                .in(AggregateSubjectField::getTemplateId, Arrays.asList(templateIds)));
        return aggregateSubjectTemplateMapper.update(null, Wrappers.<AggregateSubjectTemplate>lambdaUpdate()
                .in(AggregateSubjectTemplate::getTemplateId, Arrays.asList(templateIds))
                .set(AggregateSubjectTemplate::getDelFlag, "1"));
    }

    private List<AggregateSubjectField> selectFieldList(Long templateId) {
        return aggregateSubjectFieldMapper.selectList(Wrappers.<AggregateSubjectField>lambdaQuery()
                .eq(AggregateSubjectField::getTemplateId, templateId)
                .orderByAsc(AggregateSubjectField::getSortOrder, AggregateSubjectField::getFieldId));
    }

    private void saveFields(AggregateSubjectTemplateDto templateDto) {
        List<AggregateSubjectField> fieldList = templateDto.getFieldList();
        if (fieldList == null) {
            fieldList = new ArrayList<>();
        }
        fieldList.sort(Comparator.comparing(item -> item.getSortOrder() == null ? 999 : item.getSortOrder()));
        for (AggregateSubjectField field : fieldList) {
            field.setTemplateId(templateDto.getTemplateId());
            field.setSortOrder(field.getSortOrder() == null ? 1 : field.getSortOrder());
            field.setRequiredFlag("1".equals(field.getRequiredFlag()) ? "1" : "0");
            field.setIndexedFlag("1".equals(field.getIndexedFlag()) ? "1" : "0");
            field.setTraceFlag("1".equals(field.getTraceFlag()) ? "1" : "0");
            field.setSearchFlag("1".equals(field.getSearchFlag()) ? "1" : "0");
            field.setExportFlag("1".equals(field.getExportFlag()) ? "1" : "0");
            field.setCreateBy(StringUtils.isNotBlank(templateDto.getUpdateBy()) ? templateDto.getUpdateBy() : templateDto.getCreateBy());
            field.setCreateTime(DateUtils.getNowDate());
            aggregateSubjectFieldMapper.insert(field);
        }
    }

    private void validateTemplate(AggregateSubjectTemplateDto templateDto, Long currentId) {
        if (StringUtils.isBlank(templateDto.getSubjectName())) {
            throw new ServiceException("模板名称不能为空");
        }
        List<AggregateSubjectField> fieldList = templateDto.getFieldList();
        if (fieldList == null || fieldList.isEmpty()) {
            throw new ServiceException("请至少配置一个字段");
        }
        for (AggregateSubjectField field : fieldList) {
            if (StringUtils.isBlank(field.getFieldCode())) {
                throw new ServiceException("字段编码不能为空");
            }
            if (StringUtils.isBlank(field.getFieldLabel())) {
                throw new ServiceException("字段名称不能为空");
            }
            if (StringUtils.isBlank(field.getFieldType())) {
                throw new ServiceException("字段类型不能为空");
            }
        }
    }

    private void normalizeUseFlag(AggregateSubjectTemplateDto templateDto) {
        if (templateDto.getUseCurrent() == null) {
            templateDto.setUseCurrent(false);
        }
        if (templateDto.getUseCurrent()) {
            templateDto.setStatus("0");
        }
        if (!"0".equals(templateDto.getStatus())) {
            templateDto.setStatus("1");
        }
    }

    private boolean shouldActivate(AggregateSubjectTemplateDto templateDto) {
        return Boolean.TRUE.equals(templateDto.getUseCurrent()) || "0".equals(templateDto.getStatus());
    }

    private void disableAllTemplates() {
        aggregateSubjectTemplateMapper.update(null, Wrappers.<AggregateSubjectTemplate>lambdaUpdate()
                .eq(AggregateSubjectTemplate::getDelFlag, "0")
                .set(AggregateSubjectTemplate::getStatus, "1"));
    }

    private String buildCopyCode(String sourceCode) {
        String code = sourceCode + "_COPY";
        int index = 1;
        while (aggregateSubjectTemplateMapper.selectOne(Wrappers.<AggregateSubjectTemplate>lambdaQuery()
                .eq(AggregateSubjectTemplate::getSubjectCode, code)
                .eq(AggregateSubjectTemplate::getDelFlag, "0")) != null) {
            code = sourceCode + "_COPY" + index;
            index++;
        }
        return code;
    }
}
