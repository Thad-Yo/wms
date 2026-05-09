<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input v-model="batchName" placeholder="请输入骨料批次" clearable size="small" prefix-icon="el-icon-search" style="margin-bottom: 20px" />
        </div>
        <div class="head-container">
          <el-tree
            ref="batchTree"
            node-key="batchNo"
            show-checkbox
            :data="batchOptions"
            :props="batchTreeProps"
            :expand-on-click-node="false"
            :filter-node-method="filterBatchNode"
            default-expand-all
            @check="handleBatchCheck"
          />
        </div>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
          <el-form-item label="RFID" prop="rfidCode">
            <el-input v-model="queryParams.rfidCode" placeholder="请输入RFID编码" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="骨料名称" prop="materialName">
            <el-input v-model="queryParams.materialName" placeholder="请输入骨料名称" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="使用状态" prop="useStatus">
            <el-select v-model="queryParams.useStatus" placeholder="请选择使用状态" clearable>
              <el-option v-for="item in useStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['warehouse:aggregate:rfid:edit']" type="primary" plain icon="el-icon-download" size="mini" @click="handleExportTemplate">导出绑定模板</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['warehouse:aggregate:rfid:edit']" type="warning" plain icon="el-icon-upload2" size="mini" @click="handleImport">导入绑定</el-button>
          </el-col>
          <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="rfidList">
          <el-table-column label="RFID编码" align="center" prop="rfidCode" min-width="170">
            <template slot-scope="scope">
              <el-button type="text" @click="openLifecycle(scope.row)">{{ scope.row.rfidCode }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="分配用户" align="center" prop="ownerUserName" />
          <el-table-column label="使用状态" align="center" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.bindObjectId ? 'success' : 'warning'" effect="plain">{{ scope.row.bindObjectId ? '已使用' : '未使用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定骨料" align="center" prop="bindObjectName" min-width="140">
            <template slot-scope="scope">
              <span>{{ scope.row.bindObjectName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column
            v-for="field in templateFields"
            :key="field.fieldCode"
            :label="field.fieldLabel"
            align="center"
            min-width="140"
          >
            <template slot-scope="scope">
              <span>{{ formatDynamicValue(scope.row.subjectFieldMap, field) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="当前仓库" align="center" prop="currentWarehouseName" />
          <el-table-column label="最后事件" align="center" prop="lastEventTime" width="160" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-position" @click="openEvent(scope.row)">采集事件</el-button>
              <el-button v-if="!scope.row.bindObjectId" size="mini" type="text" icon="el-icon-connection" @click="handleBind(scope.row)">绑定骨料</el-button>
              <el-button size="mini" type="text" icon="el-icon-tickets" @click="openLifecycle(scope.row)">时间线</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <el-dialog title="绑定骨料档案" :visible.sync="bindOpen" width="760px" append-to-body>
      <el-form ref="bindForm" :model="bindForm" :rules="bindRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="RFID编码">
              <el-input :value="bindForm.rfidCode" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前模板" prop="templateId">
              <el-input :value="currentTemplate ? currentTemplate.subjectName : '当前暂无启用模板'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绑定骨料" prop="bindObjectId">
              <el-select
                v-model="bindForm.bindObjectId"
                filterable
                remote
                reserve-keyword
                clearable
                placeholder="请输入骨料编号或名称搜索"
                :remote-method="searchObjects"
                :loading="objectLoading"
                style="width: 100%"
              >
                <el-option v-for="item in objectOptions" :key="item.goodsId" :label="objectOptionLabel(item)" :value="item.goodsId" />
              </el-select>
            </el-form-item>
          </el-col>
          <template v-for="field in templateFields">
            <el-col :key="field.fieldCode" :span="12">
              <el-form-item :label="field.fieldLabel" :prop="'formData.' + field.fieldCode">
                <el-input
                  v-if="field.fieldType === 'input'"
                  v-model="bindForm.formData[field.fieldCode]"
                  :placeholder="'请输入' + field.fieldLabel"
                />
                <el-input
                  v-else-if="field.fieldType === 'textarea'"
                  v-model="bindForm.formData[field.fieldCode]"
                  type="textarea"
                  :placeholder="'请输入' + field.fieldLabel"
                />
                <el-input-number
                  v-else-if="field.fieldType === 'number'"
                  v-model="bindForm.formData[field.fieldCode]"
                  style="width: 100%"
                />
                <el-date-picker
                  v-else-if="field.fieldType === 'date'"
                  v-model="bindForm.formData[field.fieldCode]"
                  type="date"
                  value-format="yyyy-MM-dd"
                  placeholder="请选择日期"
                  style="width: 100%"
                />
                <el-select
                  v-else-if="field.fieldType === 'select'"
                  v-model="bindForm.formData[field.fieldCode]"
                  :placeholder="'请选择' + field.fieldLabel"
                  style="width: 100%"
                >
                  <el-option v-for="opt in parseOptions(field.optionsJson)" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </template>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="bindForm.remark" type="textarea" placeholder="请输入骨料绑定说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBind">确 定</el-button>
        <el-button @click="bindOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div slot="tip" class="el-upload__tip text-center">
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="handleExportTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { listAggregateRfid, bindAggregateRfidObject, listAggregateRfidObjectOptions } from '@/api/warehouse/aggregateRfid'
import { listEnabledAggregateSubjectTemplateOptions, getAggregateSubjectTemplate } from '@/api/warehouse/aggregateSubjectTemplate'
import { listAggregateMaterial } from '@/api/warehouse/aggregateMaterial'

export default {
  name: 'AggregateRfid',
  data() {
    return {
      loading: true,
      objectLoading: false,
      showSearch: true,
      total: 0,
      batchName: undefined,
      batchOptions: [],
      batchTreeProps: {
        children: 'children',
        label: 'label'
      },
      rfidList: [],
      objectOptions: [],
      currentTemplate: null,
      templateFields: [],
      bindOpen: false,
      bindForm: {
        identityIds: [],
        rfidCode: '',
        bindObjectId: null,
        templateId: null,
        formData: {},
        remark: null
      },
      upload: {
        open: false,
        title: '导入绑定',
        isUploading: false,
        headers: { Authorization: 'Bearer ' + getToken() },
        url: process.env.VUE_APP_BASE_API + '/warehouse/aggregate/rfid/importBindData'
      },
      useStatusOptions: [
        { label: '未使用', value: 'UNUSED' },
        { label: '已使用', value: 'USED' }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        rfidCode: null,
        materialName: null,
        batchNo: null,
        useStatus: null
      },
      bindRules: {
        templateId: [{ required: true, message: '当前模板不能为空', trigger: 'change' }],
        bindObjectId: [{ required: true, message: '绑定骨料不能为空', trigger: 'change' }]
      }
    }
  },
  watch: {
    batchName(val) {
      if (this.$refs.batchTree) {
        this.$refs.batchTree.filter(val)
      }
    },
    $route: {
      handler() {
        this.initQueryFromRoute()
        this.syncBatchTreeSelection()
        this.getList()
      }
    }
  },
  created() {
    this.initQueryFromRoute()
    this.getCurrentTemplate()
    this.getBatchOptions()
  },
  methods: {
    getBatchOptions() {
      listAggregateMaterial({ pageNum: 1, pageSize: 9999, state: null }).then((response) => {
        this.batchOptions = [{
          batchNo: null,
          label: '全部',
          children: (response.rows || []).map((item) => ({
            batchNo: item.batchNo,
            label: item.batchNo,
            children: []
          }))
        }]
        this.syncBatchTreeSelection()
        this.getList()
      })
    },
    initQueryFromRoute() {
      const { batchNo, useStatus } = this.$route.query
      this.queryParams.batchNo = batchNo || null
      this.batchName = batchNo || undefined
      this.queryParams.useStatus = useStatus || null
    },
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (params.batchNo && Array.isArray(params.batchNo)) {
        params.batchNo = params.batchNo[0] || null
      }
      listAggregateRfid(params).then((response) => {
        this.rfidList = response.rows || []
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getCurrentTemplate() {
      listEnabledAggregateSubjectTemplateOptions().then((response) => {
        const enabledTemplate = (response.data || [])[0]
        if (!enabledTemplate) {
          this.currentTemplate = null
          this.templateFields = []
          return
        }
        getAggregateSubjectTemplate(enabledTemplate.templateId).then((templateResponse) => {
          this.currentTemplate = templateResponse.data
          this.templateFields = this.currentTemplate.fieldList || []
          this.getList()
        })
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.batchNo = null
      this.queryParams.useStatus = null
      this.batchName = undefined
      if (this.$refs.batchTree) {
        this.$refs.batchTree.setCheckedKeys([])
      }
      this.handleQuery()
    },
    handleBind(row) {
      if (!this.currentTemplate || !this.currentTemplate.templateId) {
        this.$modal.msgWarning('请先在模板管理中启用一个骨料模板')
        return
      }
      if (row.bindObjectId) {
        this.$modal.msgWarning('该 RFID 已绑定骨料')
        return
      }
      this.bindForm = {
        identityIds: [row.identityId],
        rfidCode: row.rfidCode,
        bindObjectId: null,
        templateId: this.currentTemplate.templateId,
        formData: this.initFormData(),
        remark: null
      }
      this.objectOptions = []
      this.bindOpen = true
    },
    initFormData() {
      const formData = {}
      this.templateFields.forEach((field) => {
        formData[field.fieldCode] = field.defaultValue || ''
      })
      return formData
    },
    parseOptions(optionsJson) {
      if (!optionsJson) return []
      try {
        return JSON.parse(optionsJson)
      } catch (e) {
        return []
      }
    },
    formatDynamicValue(subjectFieldMap, field) {
      if (!subjectFieldMap || subjectFieldMap[field.fieldCode] === undefined || subjectFieldMap[field.fieldCode] === null || subjectFieldMap[field.fieldCode] === '') {
        return '-'
      }
      const value = subjectFieldMap[field.fieldCode]
      if (field.fieldType === 'select') {
        const target = this.parseOptions(field.optionsJson).find((item) => String(item.value) === String(value))
        return target ? target.label : value
      }
      return value
    },
    searchObjects(query) {
      if (!query) {
        this.objectOptions = []
        return
      }
      this.objectLoading = true
      Promise.all([
        listAggregateRfidObjectOptions({ pageNum: 1, pageSize: 20, goodsName: query }),
        listAggregateRfidObjectOptions({ pageNum: 1, pageSize: 20, goodsCode: query })
      ]).then(([nameResponse, codeResponse]) => {
        const objectMap = new Map()
        const objectRows = [...(nameResponse.rows || []), ...(codeResponse.rows || [])]
        objectRows.forEach((item) => {
          objectMap.set(item.goodsId, item)
        })
        this.objectOptions = Array.from(objectMap.values())
        this.objectLoading = false
      }).catch(() => {
        this.objectLoading = false
      })
    },
    objectOptionLabel(item) {
      return `${item.goodsCode || ''}${item.goodsName ? ' / ' + item.goodsName : ''}`
    },
    submitBind() {
      this.$refs.bindForm.validate((valid) => {
        if (!valid) return
        bindAggregateRfidObject(this.bindForm).then(() => {
          this.$modal.msgSuccess('绑定成功')
          this.bindOpen = false
          this.getList()
        })
      })
    },
    handleExportTemplate() {
      if (!this.currentTemplate || !this.currentTemplate.templateId) {
        this.$modal.msgWarning('请先在模板管理中启用一个骨料模板')
        return
      }
      this.download('/warehouse/aggregate/rfid/exportBindTemplate', {}, `rfid_bind_template_${new Date().getTime()}.xlsx`)
    },
    handleImport() {
      if (!this.currentTemplate || !this.currentTemplate.templateId) {
        this.$modal.msgWarning('请先在模板管理中启用一个骨料模板')
        return
      }
      this.upload.open = true
      this.upload.title = '导入绑定'
    },
    handleFileUploadProgress() {
      this.upload.isUploading = true
    },
    handleFileSuccess(response) {
      this.upload.open = false
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + '</div>', '导入结果', { dangerouslyUseHTMLString: true })
      this.getList()
    },
    submitFileForm() {
      this.$refs.upload.submit()
    },
    openEvent(row) {
      this.$router.push({ path: '/aggregate/event', query: { rfidCode: row.rfidCode }})
    },
    openLifecycle(row) {
      this.$router.push({ path: '/aggregate/lifecycle', query: { rfidCode: row.rfidCode }})
    },
    filterBatchNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    handleBatchCheck() {
      const checkedKeys = (this.$refs.batchTree && this.$refs.batchTree.getCheckedKeys()) || []
      const realKeys = checkedKeys.filter(Boolean)
      this.queryParams.batchNo = realKeys.length > 0 ? realKeys[0] : null
      if (!this.queryParams.batchNo && checkedKeys.includes(null)) {
        this.queryParams.batchNo = null
      }
      this.handleQuery()
    },
    syncBatchTreeSelection() {
      this.$nextTick(() => {
        if (this.$refs.batchTree) {
          this.$refs.batchTree.setCheckedKeys(this.queryParams.batchNo ? [this.queryParams.batchNo] : [])
        }
      })
    }
  }
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.head-container {
  margin-bottom: 16px;
}
</style>
