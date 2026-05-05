<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
      <el-form-item label="RFID" prop="rfidCode">
        <el-input v-model="queryParams.rfidCode" placeholder="请输入RFID/EPC编码" clearable style="width: 360px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询时间线</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-descriptions v-if="identity" title="RFID数字身份" :column="3" border class="mb20">
      <el-descriptions-item label="RFID/EPC">{{ identity.rfidCode }}</el-descriptions-item>
      <el-descriptions-item label="TID">{{ identity.tidCode || "-" }}</el-descriptions-item>
      <el-descriptions-item label="当前状态">
        <el-tag :type="stateTagType(identity.currentState)">{{ stateLabel(identity.currentState) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="骨料编号">{{ identity.materialCode }}</el-descriptions-item>
      <el-descriptions-item label="骨料名称">{{ identity.materialName }}</el-descriptions-item>
      <el-descriptions-item label="批次号">{{ identity.batchNo || "-" }}</el-descriptions-item>
      <el-descriptions-item label="当前仓库">{{ identity.currentWarehouseName || "-" }}</el-descriptions-item>
      <el-descriptions-item label="当前位置">{{ identity.currentLocation || "-" }}</el-descriptions-item>
      <el-descriptions-item label="最后事件">{{ identity.lastEventTime || "-" }}</el-descriptions-item>
    </el-descriptions>

    <el-empty v-if="!identity && !loading" description="请输入RFID查询生命周期"></el-empty>

    <el-card v-if="identity" shadow="never">
      <div slot="header">
        <span>生命周期事件链</span>
      </div>
      <el-timeline v-loading="loading">
        <el-timeline-item
          v-for="item in events"
          :key="item.eventId"
          :timestamp="item.eventTime"
          placement="top"
          :type="timelineType(item.eventType)"
        >
          <el-card shadow="never">
            <div class="event-title">
              <el-tag :type="eventTagType(item.eventType)" size="small">{{ eventLabel(item.eventType) }}</el-tag>
              <span>{{ item.actionName || item.eventName }}</span>
            </div>
            <el-descriptions :column="3" size="small" class="event-desc">
              <el-descriptions-item label="地点">{{ item.locationName || "-" }}</el-descriptions-item>
              <el-descriptions-item label="仓库">{{ item.warehouseName || "-" }}</el-descriptions-item>
              <el-descriptions-item label="重量">{{ item.weight || "-" }}</el-descriptions-item>
              <el-descriptions-item label="来源仓库">{{ item.fromWarehouseName || "-" }}</el-descriptions-item>
              <el-descriptions-item label="目标仓库">{{ item.toWarehouseName || "-" }}</el-descriptions-item>
              <el-descriptions-item label="车牌">{{ item.vehicleNo || "-" }}</el-descriptions-item>
              <el-descriptions-item label="设备">{{ item.deviceCode || "-" }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ item.deviceType || "-" }}</el-descriptions-item>
              <el-descriptions-item label="操作人">{{ item.operatorName || "-" }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
import { getAggregateLifecycle } from "@/api/warehouse/aggregateRfid";

export default {
  name: "AggregateLifecycle",
  data() {
    return {
      loading: false,
      queryParams: {
        rfidCode: null,
      },
      identity: null,
      events: [],
      stateOptions: [
        { label: "已建档", value: "CREATED" },
        { label: "已入库", value: "IN_STOCK" },
        { label: "已出库", value: "OUT_STOCK" },
        { label: "已移动", value: "MOVED" },
      ],
    };
  },
  created() {
    if (this.$route.query.rfidCode) {
      this.queryParams.rfidCode = this.$route.query.rfidCode;
      this.handleQuery();
    }
  },
  methods: {
    handleQuery() {
      if (!this.queryParams.rfidCode) {
        this.$modal.msgWarning("请输入RFID/EPC编码");
        return;
      }
      this.loading = true;
      getAggregateLifecycle(this.queryParams.rfidCode).then((response) => {
        this.identity = response.data.identity;
        this.events = response.data.events || [];
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    resetQuery() {
      this.queryParams.rfidCode = null;
      this.identity = null;
      this.events = [];
    },
    stateLabel(value) {
      const item = this.stateOptions.find((state) => state.value === value);
      return item ? item.label : value;
    },
    stateTagType(value) {
      if (value === "IN_STOCK") return "success";
      if (value === "OUT_STOCK") return "info";
      if (value === "MOVED") return "warning";
      return "";
    },
    eventLabel(value) {
      const map = { CREATED: "RFID建档", INBOUND: "入库", OUTBOUND: "出库", TRANSFER: "移动" };
      return map[value] || value;
    },
    eventTagType(value) {
      if (value === "INBOUND") return "success";
      if (value === "OUTBOUND") return "info";
      if (value === "TRANSFER") return "warning";
      return "";
    },
    timelineType(value) {
      if (value === "INBOUND") return "success";
      if (value === "OUTBOUND") return "info";
      if (value === "TRANSFER") return "warning";
      return "primary";
    },
  },
};
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.event-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
}

.event-desc {
  margin-top: 8px;
}
</style>
