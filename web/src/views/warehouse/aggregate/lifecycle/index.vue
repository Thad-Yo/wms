<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="RFID" prop="rfidCode">
        <el-input v-model="queryParams.rfidCode" placeholder="请输入RFID/EPC编码" clearable style="width: 360px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询追踪时间线</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-descriptions v-if="identity" title="骨料标签身份" :column="3" border class="mb20">
      <el-descriptions-item label="RFID/EPC">{{ identity.rfidCode }}</el-descriptions-item>
      <el-descriptions-item label="TID">{{ identity.tidCode || "-" }}</el-descriptions-item>
      <el-descriptions-item label="骨料批次">{{ identity.batchNo || "-" }}</el-descriptions-item>
      <el-descriptions-item label="骨料名称">{{ identity.materialName }}</el-descriptions-item>
      <el-descriptions-item label="当前仓库">{{ identity.currentWarehouseName || "-" }}</el-descriptions-item>
      <el-descriptions-item label="当前位置">{{ identity.currentLocation || "-" }}</el-descriptions-item>
      <el-descriptions-item label="最后事件">{{ identity.lastEventTime || "-" }}</el-descriptions-item>
    </el-descriptions>

    <el-empty v-if="!identity && !loading" description="请输入 RFID 查询骨料追踪时间线" />

    <el-card v-if="identity" shadow="never">
      <div slot="header">
        <span>骨料事件链</span>
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
            <el-divider content-position="left">动态快照</el-divider>
            <el-descriptions :column="2" size="small" class="event-desc">
              <template v-if="snapshotFields(item).length">
                <el-descriptions-item
                  v-for="field in snapshotFields(item)"
                  :key="field.key"
                  :label="field.key"
                >
                  <span class="snapshot-old">{{ field.before }}</span>
                  <span class="snapshot-arrow">→</span>
                  <span class="snapshot-new">{{ field.after }}</span>
                </el-descriptions-item>
              </template>
              <el-descriptions-item v-else label="快照">暂无变化</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-card v-if="bindRecords && bindRecords.length" shadow="never" class="mt20">
      <div slot="header">
        <span>模板字段填写记录</span>
      </div>
      <el-timeline>
        <el-timeline-item v-for="item in bindRecords" :key="item.recordId" :timestamp="item.writeTime" placement="top">
          <el-card shadow="never">
            <div class="event-title">
              <el-tag type="success" size="small">{{ item.subjectName }}</el-tag>
              <span>{{ item.writeTime || '-' }}</span>
            </div>
            <el-descriptions :column="3" size="small" class="event-desc">
              <el-descriptions-item label="模块">{{ item.moduleName || "-" }}</el-descriptions-item>
              <el-descriptions-item label="模板">{{ item.subjectCode || "-" }}</el-descriptions-item>
              <el-descriptions-item label="操作人">{{ item.operatorName || "-" }}</el-descriptions-item>
            </el-descriptions>
            <el-divider content-position="left">字段快照</el-divider>
            <el-descriptions :column="2" size="small" class="event-desc">
              <el-descriptions-item v-for="field in bindSnapshotFields(item)" :key="field.key" :label="field.key">
                {{ field.value }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
import { getAggregateLifecycle } from '@/api/warehouse/aggregateRfid'

export default {
  name: 'AggregateLifecycle',
  data() {
    return {
      loading: false,
      queryParams: {
        rfidCode: null
      },
      identity: null,
      events: [],
      bindRecords: []
    }
  },
  created() {
    if (this.$route.query.rfidCode) {
      this.queryParams.rfidCode = this.$route.query.rfidCode
      this.handleQuery()
    }
  },
  methods: {
    handleQuery() {
      if (!this.queryParams.rfidCode) {
        this.$modal.msgWarning('请输入RFID/EPC编码')
        return
      }
      this.loading = true
      getAggregateLifecycle(this.queryParams.rfidCode).then((response) => {
        this.identity = response.data.identity
        this.events = response.data.events || []
        this.bindRecords = response.data.bindRecords || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    resetQuery() {
      this.queryParams.rfidCode = null
      this.identity = null
      this.events = []
      this.bindRecords = []
    },
    eventLabel(value) {
      const map = { CREATED: '骨料建档', BIND_OBJECT: '绑定骨料', BIND_GOODS: '绑定骨料', INBOUND: '入库', OUTBOUND: '出库', TRANSFER: '移动' }
      return map[value] || value
    },
    eventTagType(value) {
      if (value === 'INBOUND') return 'success'
      if (value === 'OUTBOUND') return 'info'
      if (value === 'TRANSFER') return 'warning'
      return ''
    },
    timelineType(value) {
      if (value === 'INBOUND') return 'success'
      if (value === 'OUTBOUND') return 'info'
      if (value === 'TRANSFER') return 'warning'
      return 'primary'
    },
    parseJson(value) {
      if (!value) return {}
      try {
        return JSON.parse(value)
      } catch (e) {
        return {}
      }
    },
    snapshotFields(item) {
      const current = this.parseJson(item.snapshotData)
      const prev = this.parseJson(item.prevSnapshotData)
      return Object.keys(current).map((key) => ({
        key,
        before: prev[key] !== undefined ? prev[key] : '-',
        after: current[key] !== undefined ? current[key] : '-'
      })).filter((field) => String(field.before) !== String(field.after))
    },
    bindSnapshotFields(item) {
      const data = this.parseJson(item.fieldSnapshotJson || item.formDataJson)
      return Object.keys(data).map((key) => ({ key, value: data[key] }))
    }
  }
}
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

.snapshot-old {
  color: #909399;
}

.snapshot-arrow {
  margin: 0 6px;
  color: #c0c4cc;
}

.snapshot-new {
  color: #303133;
  font-weight: 600;
}
</style>
