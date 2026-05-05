<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8" :xs="24">
        <el-card shadow="never">
          <div slot="header">
            <span>RFID事件采集</span>
          </div>
          <el-form ref="form" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="RFID" prop="rfidCode">
              <el-input v-model="form.rfidCode" placeholder="请输入或扫描RFID/EPC编码" clearable />
            </el-form-item>
            <el-form-item label="事件类型" prop="eventType">
              <el-select v-model="form.eventType" placeholder="请选择事件类型" style="width: 100%" @change="eventTypeChange">
                <el-option label="入库事件" value="INBOUND" />
                <el-option label="出库事件" value="OUTBOUND" />
                <el-option label="移动事件" value="TRANSFER" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="form.eventType === 'INBOUND' || form.eventType === 'OUTBOUND'" label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width: 100%">
                <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="form.eventType === 'TRANSFER'" label="来源仓库" prop="fromWarehouseId">
              <el-select v-model="form.fromWarehouseId" filterable clearable placeholder="请选择来源仓库" style="width: 100%">
                <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="form.eventType === 'TRANSFER'" label="目标仓库" prop="toWarehouseId">
              <el-select v-model="form.toWarehouseId" filterable clearable placeholder="请选择目标仓库" style="width: 100%">
                <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
              </el-select>
            </el-form-item>
            <el-form-item label="地点" prop="locationName">
              <el-input v-model="form.locationName" placeholder="请输入采集地点" />
            </el-form-item>
            <el-form-item label="操作行为" prop="actionName">
              <el-input v-model="form.actionName" placeholder="如手持入库、装车出库、库区移动" />
            </el-form-item>
            <el-form-item label="重量(吨)" prop="weight">
              <el-input-number v-model="form.weight" :precision="3" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="车牌号" prop="vehicleNo">
              <el-input v-model="form.vehicleNo" placeholder="请输入车牌号" />
            </el-form-item>
            <el-form-item label="设备编号" prop="deviceCode">
              <el-input v-model="form.deviceCode" placeholder="手持终端/地磅/闸机编号" />
            </el-form-item>
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类型" clearable style="width: 100%">
                <el-option label="手持终端" value="HANDHELD" />
                <el-option label="地磅" value="WEIGHBRIDGE" />
                <el-option label="闸机" value="GATE" />
                <el-option label="RFID读写器" value="READER" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-check" @click="submitForm">提交事件</el-button>
              <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="16" :xs="24">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="RFID" prop="rfidCode">
            <el-input v-model="queryParams.rfidCode" placeholder="请输入RFID/EPC编码" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="事件类型" prop="eventType">
            <el-select v-model="queryParams.eventType" placeholder="事件类型" clearable>
              <el-option label="建档" value="CREATED" />
              <el-option label="入库" value="INBOUND" />
              <el-option label="出库" value="OUTBOUND" />
              <el-option label="移动" value="TRANSFER" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="eventList">
          <el-table-column label="事件时间" align="center" prop="eventTime" width="160" />
          <el-table-column label="RFID" align="center" prop="rfidCode" min-width="160" />
          <el-table-column label="事件" align="center" prop="eventType" width="100">
            <template slot-scope="scope">
              <el-tag :type="eventTagType(scope.row.eventType)">{{ eventLabel(scope.row.eventType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="仓库" align="center" prop="warehouseName" />
          <el-table-column label="来源仓库" align="center" prop="fromWarehouseName" />
          <el-table-column label="目标仓库" align="center" prop="toWarehouseName" />
          <el-table-column label="地点" align="center" prop="locationName" />
          <el-table-column label="重量" align="center" prop="weight" width="90" />
          <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
        </el-table>
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { addAggregateEvent, listAggregateEvent } from "@/api/warehouse/aggregateEvent";
import { listWarehouse } from "@/api/warehouse/warehouse";

export default {
  name: "AggregateEvent",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      eventList: [],
      warehouseOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        rfidCode: null,
        eventType: null,
      },
      form: {},
      rules: {
        rfidCode: [{ required: true, message: "RFID不能为空", trigger: "blur" }],
        eventType: [{ required: true, message: "请选择事件类型", trigger: "change" }],
        warehouseId: [{ required: true, message: "请选择仓库", trigger: "change" }],
        toWarehouseId: [{ required: true, message: "请选择目标仓库", trigger: "change" }],
      },
    };
  },
  created() {
    this.reset();
    if (this.$route.query.rfidCode) {
      this.form.rfidCode = this.$route.query.rfidCode;
      this.queryParams.rfidCode = this.$route.query.rfidCode;
    }
    this.getWarehouseOptions();
    this.getList();
  },
  methods: {
    getWarehouseOptions() {
      listWarehouse({ pageNum: 1, pageSize: 999 }).then((response) => {
        this.warehouseOptions = response.rows || [];
      });
    },
    getList() {
      this.loading = true;
      listAggregateEvent(this.queryParams).then((response) => {
        this.eventList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    reset() {
      const rfidCode = this.form && this.form.rfidCode ? this.form.rfidCode : null;
      this.form = {
        rfidCode,
        eventType: "INBOUND",
        warehouseId: null,
        fromWarehouseId: null,
        toWarehouseId: null,
        locationName: null,
        actionName: "手持入库",
        weight: undefined,
        vehicleNo: null,
        deviceCode: null,
        deviceType: "HANDHELD",
        remark: null,
      };
      this.resetForm("form");
    },
    eventTypeChange(type) {
      const map = {
        INBOUND: "手持入库",
        OUTBOUND: "手持出库",
        TRANSFER: "库区移动",
      };
      this.form.actionName = map[type];
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (!valid) return;
        addAggregateEvent(this.form).then(() => {
          this.$modal.msgSuccess("事件采集成功");
          this.queryParams.rfidCode = this.form.rfidCode;
          this.getList();
        });
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    eventLabel(value) {
      const map = { CREATED: "建档", INBOUND: "入库", OUTBOUND: "出库", TRANSFER: "移动" };
      return map[value] || value;
    },
    eventTagType(value) {
      if (value === "INBOUND") return "success";
      if (value === "OUTBOUND") return "info";
      if (value === "TRANSFER") return "warning";
      return "";
    },
  },
};
</script>
