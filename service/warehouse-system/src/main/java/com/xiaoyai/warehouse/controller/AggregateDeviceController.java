package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.aggregate.AggregateDevice;
import com.xiaoyai.warehouse.service.IAggregateDeviceService;
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
@RequestMapping("/warehouse/aggregate/device")
public class AggregateDeviceController extends BaseController {
    @Autowired
    private IAggregateDeviceService aggregateDeviceService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(AggregateDevice aggregateDevice) {
        startPage();
        List<AggregateDevice> list = aggregateDeviceService.selectAggregateDeviceList(aggregateDevice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:device:query')")
    @GetMapping("/{deviceId}")
    public AjaxResult getInfo(@PathVariable Long deviceId) {
        return AjaxResult.success(aggregateDeviceService.getById(deviceId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:device:add')")
    @Log(title = "数字骨料采集设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AggregateDevice aggregateDevice) {
        aggregateDevice.setCreateBy(getUsername());
        return toAjax(aggregateDeviceService.insertAggregateDevice(aggregateDevice));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:device:edit')")
    @Log(title = "数字骨料采集设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AggregateDevice aggregateDevice) {
        aggregateDevice.setUpdateBy(getUsername());
        return toAjax(aggregateDeviceService.updateAggregateDevice(aggregateDevice));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:device:remove')")
    @Log(title = "数字骨料采集设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds) {
        return toAjax(aggregateDeviceService.deleteAggregateDeviceByIds(deviceIds));
    }
}
