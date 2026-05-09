<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="88px">
      <el-form-item label="对象编号" prop="objectCode">
        <el-input v-model="queryParams.objectCode" placeholder="请输入对象编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="对象名称" prop="objectName">
        <el-input v-model="queryParams.objectName" placeholder="请输入对象名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="对象类型" prop="objectType">
        <el-input v-model="queryParams.objectType" placeholder="请输入对象类型" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="绑定状态" prop="bindStatus">
        <el-select v-model="queryParams.bindStatus" placeholder="请选择绑定状态" clearable>
          <el-option label="未绑定" value="UNBOUND" />
          <el-option label="已绑定" value="BOUND" />
          <el-option label="重绑" value="REBOUND" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增对象</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-refresh" size="mini" @click="handleSyncBonePool">同步骨料池</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="objectList">
      <el-table-column label="对象编号" align="center" prop="objectCode" min-width="140" />
      <el-table-column label="对象名称" align="center" prop="objectName" min-width="160" />
      <el-table-column label="对象类型" align="center" prop="objectType" width="120" />
      <el-table-column label="模板" align="center" prop="templateName" min-width="160" />
      <el-table-column label="绑定状态" align="center" prop="bindStatus" width="110">
        <template slot-scope="scope">
          <el-tag :type="scope.row.bindStatus === 'BOUND' ? 'success' : 'info'" effect="plain">
            {{ bindStatusText(scope.row.bindStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生命周期" align="center" prop="lifecycleStatus" width="120" />
      <el-table-column label="绑定骨料" align="center" prop="boneRfidCode" min-width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.boneRfidCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="归属用户" align="center" prop="ownerUserName" width="120" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-if="scope.row.bindStatus !== 'BOUND'" size="mini" type="text" icon="el-icon-connection" @click="handleBind(scope.row)">绑定骨料</el-button>
          <el-button size="mini" type="text" icon="el-icon-tickets" @click="handleTimeline(scope.row)">时间线</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="对象编号" prop="objectCode">
              <el-input v-model="form.objectCode" placeholder="请输入对象编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对象名称" prop="objectName">
              <el-input v-model="form.objectName" placeholder="请输入对象名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="对象类型" prop="objectType">
              <el-input v-model="form.objectType" placeholder="如货物、建材、设备" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属用户" prop="ownerUserId">
              <el-select v-model="form.ownerUserId" filterable clearable placeholder="请选择归属用户" style="width: 100%" @change="ownerChange">
                <el-option v-for="item in userOptions" :key="item.userId" :label="userOptionLabel(item)" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板编码" prop="templateCode">
              <el-input v-model="form.templateCode" placeholder="请输入模板编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="form.templateName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生命周期" prop="lifecycleStatus">
              <el-select v-model="form.lifecycleStatus" placeholder="请选择生命周期状态" style="width: 100%">
                <el-option label="已创建" value="CREATED" />
                <el-option label="已绑定" value="BOUND" />
                <el-option label="已入库" value="INBOUND" />
                <el-option label="已出库" value="OUTBOUND" />
                <el-option label="运输中" value="TRANSFER" />
                <el-option label="已签收" value="SIGNED" />
                <el-option label="已安装" value="INSTALLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源系统" prop="sourceSystem">
              <el-input v-model="form.sourceSystem" placeholder="如 WMS / MES / ERP" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="固定属性JSON" prop="fixedDataJson">
              <el-input v-model="form.fixedDataJson" type="textarea" :rows="4" placeholder='如 {"specification":"5-10mm","unit":"吨"}' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="动态属性JSON" prop="dynamicDataJson">
              <el-input v-model="form.dynamicDataJson" type="textarea" :rows="5" placeholder='如 {"manufacturer":"某厂家","qualityGrade":"一级"}' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="追溯属性JSON" prop="traceDataJson">
              <el-input v-model="form.traceDataJson" type="textarea" :rows="4" placeholder='如 {"origin":"山东","batchNo":"B202605"}' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="绑定骨料" :visible.sync="bindOpen" width="560px" append-to-body>
      <el-form ref="bindForm" :model="bindForm" :rules="bindRules" label-width="90px">
        <el-form-item label="对象编号">
          <el-input :value="bindForm.objectCode" readonly />
        </el-form-item>
        <el-form-item label="对象名称">
          <el-input :value="bindForm.objectName" readonly />
        </el-form-item>
        <el-form-item label="骨料选择" prop="boneRfidId">
          <el-select
            v-model="bindForm.boneRfidId"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="请输入骨料编号或 RFID 搜索"
            :remote-method="searchBoneOptions"
            :loading="boneLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in boneOptions"
              :key="item.boneRfidId"
              :label="boneOptionLabel(item)"
              :value="item.boneRfidId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bindForm.remark" type="textarea" placeholder="请输入绑定说明" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBind">确 定</el-button>
        <el-button @click="bindOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="对象时间线" :visible.sync="timelineOpen" width="900px" append-to-body>
      <div v-if="timeline.object" class="timeline-summary">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="对象编号">{{ timeline.object.objectCode }}</el-descriptions-item>
          <el-descriptions-item label="对象名称">{{ timeline.object.objectName }}</el-descriptions-item>
          <el-descriptions-item label="对象类型">{{ timeline.object.objectType }}</el-descriptions-item>
          <el-descriptions-item label="模板">{{ timeline.object.templateName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="骨料RFID">{{ timeline.object.boneRfidCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生命周期">{{ timeline.object.lifecycleStatus || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-timeline v-if="timeline.events && timeline.events.length" class="mt20">
        <el-timeline-item v-for="item in timeline.events" :key="item.eventId" :timestamp="item.eventTime" placement="top">
          <el-card shadow="never">
            <div class="timeline-title">
              <el-tag size="small" type="success">{{ item.eventName || item.eventType }}</el-tag>
              <span>{{ item.operatorName || '-' }}</span>
            </div>
            <el-row :gutter="12" class="timeline-meta">
              <el-col :span="12">来源模块：{{ item.sourceModule || '-' }}</el-col>
              <el-col :span="12">哈希：{{ item.eventHash || '-' }}</el-col>
            </el-row>
            <el-divider content-position="left">对象快照</el-divider>
            <pre class="json-block">{{ formatJson(item.snapshotData) }}</pre>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无时间线事件" />
    </el-dialog>
  </div>
</template>

<script>
import { listUser } from '@/api/system/user'
import {
  listBoneObject,
  getBoneObject,
  addBoneObject,
  updateBoneObject,
  bindBoneToObject,
  listBoneOptions,
  syncBonePool,
  getBoneObjectTimeline
} from '@/api/warehouse/boneObject'

export default {
  name: 'BoneObject',
  data() {
    const validateJson = (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }
      try {
        JSON.parse(value)
        callback()
      } catch (e) {
        callback(new Error('请输入合法的 JSON'))
      }
    }
    return {
      loading: true,
      showSearch: true,
      total: 0,
      objectList: [],
      userOptions: [],
      title: '',
      open: false,
      bindOpen: false,
      timelineOpen: false,
      boneLoading: false,
      boneOptions: [],
      form: {},
      bindForm: {
        objectId: null,
        objectCode: '',
        objectName: '',
        boneRfidId: null,
        remark: ''
      },
      timeline: {
        object: null,
        events: []
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        objectCode: null,
        objectName: null,
        objectType: null,
        bindStatus: null
      },
      rules: {
        objectCode: [{ required: true, message: '对象编号不能为空', trigger: 'blur' }],
        objectName: [{ required: true, message: '对象名称不能为空', trigger: 'blur' }],
        objectType: [{ required: true, message: '对象类型不能为空', trigger: 'blur' }],
        fixedDataJson: [{ validator: validateJson, trigger: 'blur' }],
        dynamicDataJson: [{ validator: validateJson, trigger: 'blur' }],
        traceDataJson: [{ validator: validateJson, trigger: 'blur' }]
      },
      bindRules: {
        boneRfidId: [{ required: true, message: '请选择骨料', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
    this.getUserOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listBoneObject(this.queryParams).then((response) => {
        this.objectList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getUserOptions() {
      listUser({ pageNum: 1, pageSize: 999, status: '0' }).then((response) => {
        this.userOptions = response.rows || []
      })
    },
    userOptionLabel(item) {
      return `${item.userName || ''}${item.nickName ? ' / ' + item.nickName : ''}`
    },
    ownerChange(userId) {
      const user = this.userOptions.find((item) => item.userId === userId)
      this.form.ownerUserName = user ? user.userName : null
    },
    bindStatusText(status) {
      const map = {
        UNBOUND: '未绑定',
        BOUND: '已绑定',
        REBOUND: '重绑'
      }
      return map[status] || status || '-'
    },
    formatJson(text) {
      if (!text) return '{}'
      try {
        return JSON.stringify(JSON.parse(text), null, 2)
      } catch (e) {
        return text
      }
    },
    reset() {
      this.form = {
        objectId: null,
        objectCode: null,
        objectName: null,
        objectType: null,
        templateId: null,
        templateCode: null,
        templateName: null,
        bindStatus: 'UNBOUND',
        lifecycleStatus: 'CREATED',
        ownerUserId: undefined,
        ownerUserName: null,
        sourceSystem: 'WMS',
        fixedDataJson: '{}',
        dynamicDataJson: '{}',
        traceDataJson: '{}',
        extDataJson: '{}',
        remark: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.title = '新增对象'
      this.open = true
    },
    handleUpdate(row) {
      this.reset()
      getBoneObject(row.objectId).then((response) => {
        this.form = {
          ...this.form,
          ...response.data
        }
        this.open = true
        this.title = '修改对象'
      })
    },
    handleBind(row) {
      this.bindForm = {
        objectId: row.objectId,
        objectCode: row.objectCode,
        objectName: row.objectName,
        boneRfidId: null,
        remark: ''
      }
      this.boneOptions = []
      this.bindOpen = true
      this.searchBoneOptions('')
    },
    handleTimeline(row) {
      getBoneObjectTimeline(row.objectId).then((response) => {
        this.timeline = response.data || { object: null, events: [] }
        this.timelineOpen = true
      })
    },
    handleSyncBonePool() {
      this.$modal.confirm('是否同步 aggregate 身份数据到新版骨料池？').then(() => {
        return syncBonePool()
      }).then((response) => {
        this.$modal.msgSuccess(response.msg || '同步成功')
      })
    },
    searchBoneOptions(keyword) {
      this.boneLoading = true
      listBoneOptions(keyword).then((response) => {
        this.boneOptions = response.data || []
        this.boneLoading = false
      }).catch(() => {
        this.boneLoading = false
      })
    },
    boneOptionLabel(item) {
      return `${item.boneCode || '-'} / ${item.boneRfidCode || '-'} / ${item.boneName || '-'}`
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (!valid) return
        const request = this.form.objectId ? updateBoneObject(this.form) : addBoneObject(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.objectId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    submitBind() {
      this.$refs.bindForm.validate((valid) => {
        if (!valid) return
        bindBoneToObject(this.bindForm).then(() => {
          this.$modal.msgSuccess('绑定成功')
          this.bindOpen = false
          this.getList()
        })
      })
    },
    cancel() {
      this.open = false
      this.reset()
    }
  }
}
</script>

<style scoped>
.timeline-summary {
  margin-bottom: 12px;
}

.timeline-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.timeline-meta {
  margin-top: 12px;
  color: #606266;
}

.json-block {
  margin: 0;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
}
</style>
