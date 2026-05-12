package com.xiaoyai.warehouse.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyai.common.exception.ServiceException;
import com.xiaoyai.common.utils.DateUtils;
import com.xiaoyai.common.utils.SecurityUtils;
import com.xiaoyai.common.utils.StringUtils;
import com.xiaoyai.warehouse.domain.WarehouseGoods;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.AggregateMaterial;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectBindRecord;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectField;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectTemplate;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateRfidBindGoodsDto;
import com.xiaoyai.warehouse.domain.aggregate.vo.AggregateLifecycleVo;
import com.xiaoyai.warehouse.domain.dto.WarehouseGoodsQueryDto;
import com.xiaoyai.warehouse.enums.AggregateEventType;
import com.xiaoyai.warehouse.enums.AggregateIdentityState;
import com.xiaoyai.warehouse.mapper.AggregateEventMapper;
import com.xiaoyai.warehouse.mapper.AggregateRfidIdentityMapper;
import com.xiaoyai.warehouse.mapper.AggregateSubjectBindRecordMapper;
import com.xiaoyai.warehouse.mapper.AggregateSubjectFieldMapper;
import com.xiaoyai.warehouse.mapper.AggregateSubjectTemplateMapper;
import com.xiaoyai.warehouse.service.IAggregateMaterialService;
import com.xiaoyai.warehouse.service.IAggregateRfidIdentityService;
import com.xiaoyai.warehouse.service.IWarehouseGoodsService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AggregateRfidIdentityServiceImpl extends ServiceImpl<AggregateRfidIdentityMapper, AggregateRfidIdentity> implements IAggregateRfidIdentityService {
    @Autowired
    private IAggregateMaterialService aggregateMaterialService;

    @Autowired
    private AggregateEventMapper aggregateEventMapper;

    @Autowired
    private IWarehouseGoodsService warehouseGoodsService;

    @Autowired
    private AggregateSubjectTemplateMapper aggregateSubjectTemplateMapper;

    @Autowired
    private AggregateSubjectFieldMapper aggregateSubjectFieldMapper;

    @Autowired
    private AggregateSubjectBindRecordMapper aggregateSubjectBindRecordMapper;

    @Override
    public List<AggregateRfidIdentity> selectAggregateRfidIdentityList(AggregateRfidIdentity aggregateRfidIdentity) {
        applyUserScope(aggregateRfidIdentity);
        List<AggregateRfidIdentity> list = baseMapper.selectAggregateRfidIdentityList(aggregateRfidIdentity);
        enrichSubjectFieldMap(list);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AggregateRfidIdentity createIdentity(AggregateRfidIdentity aggregateRfidIdentity) {
        if (StringUtils.isBlank(aggregateRfidIdentity.getRfidCode())) {
            throw new ServiceException("RFID编码不能为空");
        }
        if (aggregateRfidIdentity.getMaterialId() == null) {
            throw new ServiceException("骨料档案不能为空");
        }
        AggregateRfidIdentity exists = getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getRfidCode, aggregateRfidIdentity.getRfidCode())
                .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
        if (exists != null) {
            throw new ServiceException("RFID编码已建档");
        }
        AggregateMaterial material = aggregateMaterialService.getById(aggregateRfidIdentity.getMaterialId());
        if (material == null || "1".equals(material.getDelFlag())) {
            throw new ServiceException("骨料档案不存在");
        }
        aggregateRfidIdentity.setMaterialCode(material.getMaterialCode());
        aggregateRfidIdentity.setMaterialName(material.getMaterialName());
        aggregateRfidIdentity.setBatchNo(StringUtils.isBlank(aggregateRfidIdentity.getBatchNo()) ? material.getBatchNo() : aggregateRfidIdentity.getBatchNo());
        aggregateRfidIdentity.setOwnerUserId(material.getOwnerUserId());
        aggregateRfidIdentity.setOwnerUserName(material.getOwnerUserName());
        aggregateRfidIdentity.setCurrentState(AggregateIdentityState.CREATED.getCode());
        aggregateRfidIdentity.setBindTime(DateUtils.getNowDate());
        aggregateRfidIdentity.setCreateTime(DateUtils.getNowDate());
        save(aggregateRfidIdentity);

        AggregateEvent event = new AggregateEvent();
        event.setIdentityId(aggregateRfidIdentity.getIdentityId());
        event.setRfidCode(aggregateRfidIdentity.getRfidCode());
        event.setMaterialId(aggregateRfidIdentity.getMaterialId());
        event.setObjectId(aggregateRfidIdentity.getMaterialId());
        event.setEventType(AggregateEventType.CREATED.getCode());
        event.setEventName(AggregateEventType.CREATED.getName());
        event.setActionName("标签身份建档");
        event.setSourceModule("rfid");
        event.setRemark(aggregateRfidIdentity.getRemark());
        event.setCreateBy(aggregateRfidIdentity.getCreateBy());
        event.setEventTime(DateUtils.getNowDate());
        event.setCreateTime(DateUtils.getNowDate());
        aggregateEventMapper.insert(event);

        aggregateRfidIdentity.setLastEventTime(event.getEventTime());
        updateById(aggregateRfidIdentity);
        return aggregateRfidIdentity;
    }

    @Override
    public int updateAggregateRfidIdentity(AggregateRfidIdentity aggregateRfidIdentity) {
        aggregateRfidIdentity.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(aggregateRfidIdentity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchBindObject(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName) {
        if (bindGoodsDto == null || bindGoodsDto.getIdentityIds() == null || bindGoodsDto.getIdentityIds().isEmpty()) {
            throw new ServiceException("请选择需要绑定的标签身份");
        }
        if (bindGoodsDto.getBindObjectId() == null) {
            throw new ServiceException("请选择需要绑定的骨料");
        }
        if (bindGoodsDto.getTemplateId() == null) {
            throw new ServiceException("请选择骨料模板");
        }
        WarehouseGoods goods = warehouseGoodsService.selectWarehouseGoodsByGoodsId(bindGoodsDto.getBindObjectId());
        if (goods == null || "1".equals(goods.getDelFlag())) {
            throw new ServiceException("绑定骨料不存在");
        }
        AggregateSubjectTemplate template = aggregateSubjectTemplateMapper.selectById(bindGoodsDto.getTemplateId());
        if (template == null || "1".equals(template.getDelFlag()) || !"0".equals(template.getStatus())) {
            throw new ServiceException("骨料模板不存在或已停用");
        }
        List<AggregateSubjectField> fieldList = aggregateSubjectFieldMapper.selectList(Wrappers.<AggregateSubjectField>lambdaQuery()
                .eq(AggregateSubjectField::getTemplateId, template.getTemplateId())
                .orderByAsc(AggregateSubjectField::getSortOrder, AggregateSubjectField::getFieldId));
        if (fieldList.isEmpty()) {
            throw new ServiceException("骨料模板未配置字段");
        }
        Integer writeNo = 1;
        validateDynamicForm(fieldList, bindGoodsDto.getFormData(), writeNo);

        List<AggregateRfidIdentity> identities = list(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .in(AggregateRfidIdentity::getIdentityId, bindGoodsDto.getIdentityIds())
                .eq(AggregateRfidIdentity::getDelFlag, "0"));
        if (identities.size() != bindGoodsDto.getIdentityIds().size()) {
            throw new ServiceException("部分标签身份不存在或已删除");
        }

        Date now = DateUtils.getNowDate();
        List<Long> ids = new ArrayList<>();
        for (AggregateRfidIdentity identity : identities) {
            checkOwner(identity);
            if (identity.getBindGoodsId() != null) {
                throw new ServiceException("RFID已绑定骨料：" + identity.getRfidCode());
            }
            ids.add(identity.getIdentityId());
        }

        boolean updated = lambdaUpdate()
                .in(AggregateRfidIdentity::getIdentityId, ids)
                .set(AggregateRfidIdentity::getBindGoodsId, goods.getGoodsId())
                .set(AggregateRfidIdentity::getBindGoodsCode, goods.getGoodsCode())
                .set(AggregateRfidIdentity::getBindGoodsName, goods.getGoodsName())
                .set(AggregateRfidIdentity::getBindGoodsTime, now)
                .set(AggregateRfidIdentity::getLastEventTime, now)
                .set(AggregateRfidIdentity::getUpdateBy, operatorName)
                .set(AggregateRfidIdentity::getUpdateTime, now)
                .update();
        if (!updated) {
            return 0;
        }

        for (AggregateRfidIdentity identity : identities) {
            AggregateSubjectBindRecord bindRecord = buildBindRecord(identity, goods, template, fieldList, bindGoodsDto, writeNo, now, operatorName);
            aggregateSubjectBindRecordMapper.insert(bindRecord);

            AggregateEvent event = new AggregateEvent();
            event.setIdentityId(identity.getIdentityId());
            event.setRfidCode(identity.getRfidCode());
            event.setMaterialId(identity.getMaterialId());
            event.setObjectId(identity.getMaterialId());
            event.setEventType(AggregateEventType.BIND_OBJECT.getCode());
            event.setEventName(AggregateEventType.BIND_OBJECT.getName());
            event.setActionName("绑定骨料：" + goods.getGoodsName());
            event.setSourceModule("rfid");
            event.setSourceReceiptType("GOODS");
            event.setSourceReceiptId(goods.getGoodsId());
            event.setSourceReceiptNo(goods.getGoodsCode());
            event.setRawPayload(bindRecord.getFormDataJson());
            event.setSnapshotData(bindRecord.getFieldSnapshotJson());
            event.setCreateBy(operatorName);
            event.setEventTime(now);
            event.setCreateTime(now);
            event.setRemark("模板：" + template.getSubjectName() + (StringUtils.isNotBlank(bindGoodsDto.getRemark()) ? "；" + bindGoodsDto.getRemark() : ""));
            aggregateEventMapper.insert(event);
        }
        return ids.size();
    }

    @Override
    @Deprecated
    public int batchBindGoods(AggregateRfidBindGoodsDto bindGoodsDto, String operatorName) {
        return batchBindObject(bindGoodsDto, operatorName);
    }

    @Override
    public void exportBindTemplate(HttpServletResponse response, AggregateRfidIdentity aggregateRfidIdentity) {
        AggregateSubjectTemplate template = getEnabledTemplate();
        List<AggregateSubjectField> fieldList = selectTemplateFields(template.getTemplateId());
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("绑定模板");
            CellStyle requiredHeaderStyle = workbook.createCellStyle();
            Font requiredHeaderFont = workbook.createFont();
            requiredHeaderFont.setColor(IndexedColors.RED.getIndex());
            requiredHeaderFont.setBold(true);
            requiredHeaderStyle.setFont(requiredHeaderFont);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("RFID/EPC编码");
            header.createCell(1).setCellValue("骨料编号");
            header.getCell(0).setCellStyle(requiredHeaderStyle);
            header.getCell(1).setCellStyle(requiredHeaderStyle);
            for (int i = 0; i < fieldList.size(); i++) {
                AggregateSubjectField field = fieldList.get(i);
                header.createCell(i + 2).setCellValue(field.getFieldLabel());
                if ("1".equals(field.getRequiredFlag())) {
                    header.getCell(i + 2).setCellStyle(requiredHeaderStyle);
                }
            }
            Row tip = sheet.createRow(1);
            tip.createCell(0).setCellValue("示例：RFID10001");
            tip.createCell(1).setCellValue("示例：OBJ10001");
            for (int i = 0; i < fieldList.size(); i++) {
                AggregateSubjectField field = fieldList.get(i);
                tip.createCell(i + 2).setCellValue(buildExampleValue(field));
            }
            Row note = sheet.createRow(2);
            note.createCell(0).setCellValue("红色表头为必填项；建议仅填写未绑定RFID对应的货物编号及模板字段");
            fillExportRows(sheet, aggregateRfidIdentity, fieldList);
            for (int i = 0; i < fieldList.size() + 2; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1024, 12000));
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = java.net.URLEncoder.encode("rfid_bind_template", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + fileName + ".xlsx");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new ServiceException("导出绑定模板失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importBindData(MultipartFile file, String operatorName) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请上传导入文件");
        }
        AggregateSubjectTemplate template = getEnabledTemplate();
        List<AggregateSubjectField> fieldList = selectTemplateFields(template.getTemplateId());
        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 3) {
                throw new ServiceException("导入文件没有可用数据");
            }
            int successCount = 0;
            List<String> failMessages = new ArrayList<>();
            for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String rfidCode = getCellString(row, 0);
                String objectCode = getCellString(row, 1);
                if (StringUtils.isBlank(rfidCode) && StringUtils.isBlank(objectCode)) {
                    continue;
                }
                try {
                    AggregateRfidIdentity identity = getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                            .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                            .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
                    if (identity == null) {
                        throw new ServiceException("RFID不存在");
                    }
                    WarehouseGoodsQueryDto goodsQuery = new WarehouseGoodsQueryDto();
                    goodsQuery.setGoodsCode(objectCode);
                    List<WarehouseGoods> goodsList = warehouseGoodsService.selectWarehouseGoodsList(goodsQuery);
                    WarehouseGoods goods = goodsList.isEmpty() ? null : goodsList.get(0);
                    if (goods == null) {
                        throw new ServiceException("骨料编号不存在");
                    }
                    Map<String, Object> formData = new LinkedHashMap<>();
                    int columnIndex = 2;
                    for (AggregateSubjectField field : fieldList) {
                        String value = getCellString(row, columnIndex++);
                        if (StringUtils.isNotBlank(value)) {
                            formData.put(field.getFieldCode(), value);
                        }
                    }
                    AggregateRfidBindGoodsDto dto = new AggregateRfidBindGoodsDto();
                    dto.setIdentityIds(Arrays.asList(identity.getIdentityId()));
                    dto.setBindObjectId(goods.getGoodsId());
                    dto.setTemplateId(template.getTemplateId());
                    dto.setFormData(formData);
                    dto.setRemark("导入绑定");
                    batchBindObject(dto, operatorName);
                    successCount++;
                } catch (Exception e) {
                    failMessages.add("第" + (rowIndex + 1) + "行：" + e.getMessage());
                }
            }
            StringBuilder result = new StringBuilder("成功导入 " + successCount + " 条绑定数据");
            if (!failMessages.isEmpty()) {
                result.append("，失败 ").append(failMessages.size()).append(" 条：<br/>");
                result.append(String.join("<br/>", failMessages));
            }
            return result.toString();
        }
    }

    private void fillExportRows(Sheet sheet, AggregateRfidIdentity query, List<AggregateSubjectField> fieldList) {
        AggregateRfidIdentity exportQuery = query == null ? new AggregateRfidIdentity() : query;
        applyUserScope(exportQuery);
        if (StringUtils.isBlank(exportQuery.getUseStatus())) {
            exportQuery.setUseStatus("UNUSED");
        }
        List<AggregateRfidIdentity> exportRows = baseMapper.selectAggregateRfidIdentityList(exportQuery);
        int rowIndex = 3;
        for (AggregateRfidIdentity item : exportRows) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(StringUtils.isBlank(item.getRfidCode()) ? "" : item.getRfidCode());
            dataRow.createCell(1).setCellValue("");
            for (int i = 0; i < fieldList.size(); i++) {
                dataRow.createCell(i + 2).setCellValue("");
            }
        }
    }

    @Override
    public int deleteAggregateRfidIdentityByIds(Long[] identityIds) {
        return lambdaUpdate().in(AggregateRfidIdentity::getIdentityId, Arrays.asList(identityIds)).set(AggregateRfidIdentity::getDelFlag, "1").update() ? 1 : 0;
    }

    @Override
    public AggregateLifecycleVo selectLifecycleByRfidCode(String rfidCode) {
        AggregateRfidIdentity identity = getOne(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getRfidCode, rfidCode)
                .eq(AggregateRfidIdentity::getDelFlag, "0"), false);
        if (identity == null) {
            throw new ServiceException("标签身份不存在");
        }
        checkOwner(identity);
        AggregateEvent query = new AggregateEvent();
        query.setRfidCode(rfidCode);
        List<AggregateEvent> events = aggregateEventMapper.selectAggregateEventList(query);
        for (int i = 0; i < events.size(); i++) {
            AggregateEvent current = events.get(i);
            if (i == 0) {
                current.setPrevSnapshotData(null);
            } else {
                current.setPrevSnapshotData(events.get(i - 1).getSnapshotData());
            }
        }
        List<AggregateSubjectBindRecord> bindRecords = aggregateSubjectBindRecordMapper.selectList(Wrappers.<AggregateSubjectBindRecord>lambdaQuery()
                .eq(AggregateSubjectBindRecord::getIdentityId, identity.getIdentityId())
                .orderByAsc(AggregateSubjectBindRecord::getWriteNo, AggregateSubjectBindRecord::getRecordId));
        AggregateLifecycleVo vo = new AggregateLifecycleVo();
        vo.setIdentity(identity);
        vo.setEvents(events);
        vo.setBindRecords(bindRecords);
        return vo;
    }

    @Override
    public List<AggregateRfidIdentity> selectByMaterialId(Long materialId) {
        if (materialId == null) {
            return new ArrayList<>();
        }
        return list(Wrappers.<AggregateRfidIdentity>lambdaQuery()
                .eq(AggregateRfidIdentity::getMaterialId, materialId)
                .eq(AggregateRfidIdentity::getDelFlag, "0")
                .orderByAsc(AggregateRfidIdentity::getIdentityId));
    }

    private void applyUserScope(AggregateRfidIdentity aggregateRfidIdentity) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId)) {
            aggregateRfidIdentity.setOwnerUserId(userId);
        }
    }

    private void checkOwner(AggregateRfidIdentity identity) {
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId) && (identity.getOwnerUserId() == null || !identity.getOwnerUserId().equals(userId))) {
            throw new ServiceException("无权查看该标签身份");
        }
    }

    private void validateDynamicForm(List<AggregateSubjectField> fieldList, Map<String, Object> formData, Integer writeNo) {
        if (formData == null || formData.isEmpty()) {
            throw new ServiceException("请按骨料模板填写表单");
        }
        for (AggregateSubjectField field : fieldList) {
            if (!allowWrite(field.getWriteScope(), writeNo)) {
                continue;
            }
            if ("1".equals(field.getRequiredFlag())) {
                Object value = formData.get(field.getFieldCode());
                if (value == null || StringUtils.isBlank(String.valueOf(value))) {
                    throw new ServiceException("字段【" + field.getFieldLabel() + "】不能为空");
                }
            }
        }
    }

    private boolean allowWrite(String writeScope, Integer writeNo) {
        if (StringUtils.isBlank(writeScope)) {
            return true;
        }
        String[] arr = writeScope.split(",");
        for (String item : arr) {
            if (String.valueOf(writeNo).equals(item.trim())) {
                return true;
            }
        }
        return false;
    }

    private AggregateSubjectBindRecord buildBindRecord(AggregateRfidIdentity identity, WarehouseGoods goods,
                                                       AggregateSubjectTemplate template, List<AggregateSubjectField> fieldList,
                                                       AggregateRfidBindGoodsDto bindGoodsDto, Integer writeNo, Date now,
                                                       String operatorName) {
        AggregateSubjectBindRecord record = new AggregateSubjectBindRecord();
        record.setIdentityId(identity.getIdentityId());
        record.setRfidCode(identity.getRfidCode());
        record.setBindGoodsId(goods.getGoodsId());
        record.setBindGoodsCode(goods.getGoodsCode());
        record.setBindGoodsName(goods.getGoodsName());
        record.setTemplateId(template.getTemplateId());
        record.setSubjectCode(template.getSubjectCode());
        record.setSubjectName(template.getSubjectName());
        record.setModuleName(template.getModuleName());
        record.setWriteNo(writeNo);
        record.setFormDataJson(JSON.toJSONString(bindGoodsDto.getFormData()));
        record.setFieldSnapshotJson(JSON.toJSONString(fieldList));
        record.setOperatorId(SecurityUtils.getUserId());
        record.setOperatorName(operatorName);
        record.setWriteTime(now);
        record.setRemark(bindGoodsDto.getRemark());
        record.setCreateBy(operatorName);
        record.setCreateTime(now);
        return record;
    }

    private void enrichSubjectFieldMap(List<AggregateRfidIdentity> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> identityIds = new HashSet<>();
        for (AggregateRfidIdentity item : list) {
            identityIds.add(item.getIdentityId());
        }
        List<AggregateSubjectBindRecord> records = aggregateSubjectBindRecordMapper.selectList(Wrappers.<AggregateSubjectBindRecord>lambdaQuery()
                .in(AggregateSubjectBindRecord::getIdentityId, identityIds)
                .eq(AggregateSubjectBindRecord::getDelFlag, "0")
                .orderByDesc(AggregateSubjectBindRecord::getRecordId));
        Map<Long, Map<String, Object>> latestFieldMap = new HashMap<>();
        for (AggregateSubjectBindRecord record : records) {
            if (latestFieldMap.containsKey(record.getIdentityId())) {
                continue;
            }
            if (StringUtils.isBlank(record.getFormDataJson())) {
                latestFieldMap.put(record.getIdentityId(), new HashMap<>());
                continue;
            }
            Map<String, Object> parsed = JSON.parseObject(record.getFormDataJson(), new TypeReference<Map<String, Object>>() {});
            latestFieldMap.put(record.getIdentityId(), parsed == null ? new HashMap<>() : parsed);
        }
        for (AggregateRfidIdentity item : list) {
            item.setSubjectFieldMap(latestFieldMap.getOrDefault(item.getIdentityId(), new HashMap<>()));
        }
    }

    private AggregateSubjectTemplate getEnabledTemplate() {
        AggregateSubjectTemplate template = aggregateSubjectTemplateMapper.selectOne(Wrappers.<AggregateSubjectTemplate>lambdaQuery()
                .eq(AggregateSubjectTemplate::getDelFlag, "0")
                .eq(AggregateSubjectTemplate::getStatus, "0")
                .last("limit 1"));
        if (template == null) {
            throw new ServiceException("当前没有启用的骨料模板");
        }
        return template;
    }

    private List<AggregateSubjectField> selectTemplateFields(Long templateId) {
        List<AggregateSubjectField> fieldList = aggregateSubjectFieldMapper.selectList(Wrappers.<AggregateSubjectField>lambdaQuery()
                .eq(AggregateSubjectField::getTemplateId, templateId)
                .orderByAsc(AggregateSubjectField::getSortOrder, AggregateSubjectField::getFieldId));
        if (fieldList.isEmpty()) {
            throw new ServiceException("启用模板未配置字段");
        }
        return fieldList;
    }

    private String getCellString(Row row, int index) {
        if (row.getCell(index) == null) {
            return "";
        }
        row.getCell(index).setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        return StringUtils.trim(row.getCell(index).getStringCellValue());
    }

    private String buildOptionTip(String optionsJson) {
        try {
            List<Map<String, Object>> options = JSON.parseObject(optionsJson, new TypeReference<List<Map<String, Object>>>() {});
            List<String> values = new ArrayList<>();
            if (options != null) {
                for (Map<String, Object> option : options) {
                    values.add(String.valueOf(option.get("label")));
                }
            }
            return String.join("、", values);
        } catch (Exception e) {
            return "";
        }
    }

    private String buildExampleValue(AggregateSubjectField field) {
        if ("select".equals(field.getFieldType()) && StringUtils.isNotBlank(field.getOptionsJson())) {
            String optionTip = buildOptionTip(field.getOptionsJson());
            if (StringUtils.isNotBlank(optionTip)) {
                String[] values = optionTip.split("、");
                return values[0];
            }
            return "示例选项";
        }
        if ("date".equals(field.getFieldType())) {
            return "2026-05-06";
        }
        if ("number".equals(field.getFieldType())) {
            return "1";
        }
        if ("textarea".equals(field.getFieldType())) {
            return field.getFieldLabel() + "示例";
        }
        return field.getFieldLabel() + "示例";
    }
}
