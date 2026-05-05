package com.xiaoyai.warehouse.controller;

import com.xiaoyai.common.annotation.Log;
import com.xiaoyai.common.core.controller.BaseController;
import com.xiaoyai.common.core.domain.AjaxResult;
import com.xiaoyai.common.core.page.TableDataInfo;
import com.xiaoyai.common.enums.BusinessType;
import com.xiaoyai.warehouse.domain.WarehouseGoods;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.dto.AggregateRfidBindGoodsDto;
import com.xiaoyai.warehouse.domain.dto.WarehouseGoodsQueryDto;
import com.xiaoyai.warehouse.service.IAggregateRfidIdentityService;
import com.xiaoyai.warehouse.service.IWarehouseGoodsService;
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
@RequestMapping("/warehouse/aggregate/rfid")
public class AggregateRfidIdentityController extends BaseController {
    @Autowired
    private IAggregateRfidIdentityService aggregateRfidIdentityService;

    @Autowired
    private IWarehouseGoodsService warehouseGoodsService;

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:list')")
    @GetMapping("/list")
    public TableDataInfo list(AggregateRfidIdentity aggregateRfidIdentity) {
        startPage();
        List<AggregateRfidIdentity> list = aggregateRfidIdentityService.selectAggregateRfidIdentityList(aggregateRfidIdentity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:query')")
    @GetMapping("/{identityId}")
    public AjaxResult getInfo(@PathVariable Long identityId) {
        return AjaxResult.success(aggregateRfidIdentityService.getById(identityId));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:add')")
    @Log(title = "数字骨料RFID身份", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AggregateRfidIdentity aggregateRfidIdentity) {
        aggregateRfidIdentity.setCreateBy(getUsername());
        return AjaxResult.success(aggregateRfidIdentityService.createIdentity(aggregateRfidIdentity));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:edit')")
    @Log(title = "数字骨料RFID身份", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AggregateRfidIdentity aggregateRfidIdentity) {
        aggregateRfidIdentity.setUpdateBy(getUsername());
        return toAjax(aggregateRfidIdentityService.updateAggregateRfidIdentity(aggregateRfidIdentity));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:edit')")
    @GetMapping("/goodsOptions")
    public TableDataInfo goodsOptions(WarehouseGoodsQueryDto warehouseGoods) {
        startPage();
        List<WarehouseGoods> list = warehouseGoodsService.selectWarehouseGoodsList(warehouseGoods);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:edit')")
    @Log(title = "数字骨料RFID身份绑定货品", businessType = BusinessType.UPDATE)
    @PutMapping("/bindGoods")
    public AjaxResult bindGoods(@RequestBody AggregateRfidBindGoodsDto bindGoodsDto) {
        return toAjax(aggregateRfidIdentityService.batchBindGoods(bindGoodsDto, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:rfid:remove')")
    @Log(title = "数字骨料RFID身份", businessType = BusinessType.DELETE)
    @DeleteMapping("/{identityIds}")
    public AjaxResult remove(@PathVariable Long[] identityIds) {
        return toAjax(aggregateRfidIdentityService.deleteAggregateRfidIdentityByIds(identityIds));
    }

    @PreAuthorize("@ss.hasPermi('warehouse:aggregate:lifecycle:query')")
    @GetMapping("/lifecycle/{rfidCode}")
    public AjaxResult lifecycle(@PathVariable String rfidCode) {
        return AjaxResult.success(aggregateRfidIdentityService.selectLifecycleByRfidCode(rfidCode));
    }
}
