package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateEventDto;
import com.xiaoyai.warehouse.service.IAggregateEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse/aggregate/event")
public class AggregateEventController extends BaseController {
    @Autowired
    private IAggregateEventService aggregateEventService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:event:list')")
    @GetMapping("/list")
    public TableDataInfo list(AggregateEvent aggregateEvent) {
        startPage();
        List<AggregateEvent> list = aggregateEventService.selectAggregateEventList(aggregateEvent);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:event:add')")
    @Log(title = "数字骨料RFID事件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AggregateEventDto aggregateEventDto) {
        aggregateEventDto.setCreateBy(getUsername());
        aggregateEventDto.setOperatorName(aggregateEventDto.getOperatorName() == null ? getUsername() : aggregateEventDto.getOperatorName());
        return AjaxResult.success(aggregateEventService.recordEvent(aggregateEventDto));
    }
}
