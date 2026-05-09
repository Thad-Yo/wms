<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="骨料批次" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入骨料批次" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="入库时间">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-tabs v-model="stateTab">
      <el-tab-pane v-for="item in stateOptions" :key="item.value" :label="item.label" :name="item.value" />
      <el-tab-pane label="全部" name="ALL" />
    </el-tabs>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:material:import']" type="primary" plain icon="el-icon-plus" size="mini" @click="handleIssue">创建骨料入库</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="materialList">
      <el-table-column label="骨料批次" align="center" prop="batchNo" min-width="160" />
      <el-table-column label="入库时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据状态" align="center" width="110">
        <template slot-scope="scope">
          <el-tag :type="stateMeta(scope.row.state).type" effect="plain">{{ stateMeta(scope.row.state).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产地" align="center" prop="originPlace" min-width="120" />
      <el-table-column label="总数" align="center" prop="rfidCount" width="90">
        <template slot-scope="scope">
          <el-button type="text" class="count-number count-total count-link" @click="openRfidList(scope.row, '')">{{ scope.row.rfidCount || 0 }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="已使用" align="center" prop="usedRfidCount" width="90">
        <template slot-scope="scope">
          <el-button type="text" class="count-number count-used count-link" @click="openRfidList(scope.row, 'USED')">{{ scope.row.usedRfidCount || 0 }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="未使用" align="center" prop="unusedRfidCount" width="90">
        <template slot-scope="scope">
          <el-button type="text" class="count-number count-unused count-link" @click="openRfidList(scope.row, 'UNUSED')">{{ scope.row.unusedRfidCount || 0 }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="分配用户" align="center" prop="ownerUserName" width="120" />
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template slot-scope="scope">
          <el-button v-if="isPending(scope.row.state)" v-hasPermi="['warehouse:aggregate:material:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-collection-tag" @click="openRfidList(scope.row, '')">标签明细</el-button>
          <el-button v-if="isPending(scope.row.state)" v-hasPermi="['warehouse:aggregate:material:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          <el-button v-if="isPending(scope.row.state)" v-hasPermi="['warehouse:aggregate:material:edit']" size="mini" type="text" icon="el-icon-s-check" @click="handleApprove(scope.row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-alert v-if="!form.materialId" title="创建骨料入库时会自动生成骨料批次，并为该批次批量写入 RFID 标签身份。" type="info" show-icon class="mb16" />
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="骨料批次" prop="batchNo">
              <el-input v-model="form.batchNo" disabled placeholder="自动生成，如 GL-2026050701" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库时间" prop="createTime">
              <el-date-picker
                v-model="form.createTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择入库时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="originPlace">
              <el-input v-model="form.originPlace" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分配用户" prop="ownerUserId">
              <el-select v-model="form.ownerUserId" filterable clearable placeholder="请选择下游用户" style="width: 100%" @change="ownerChange">
                <el-option v-for="item in userOptions" :key="item.userId" :label="userOptionLabel(item)" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RFID列表" prop="rfidCodes">
              <div class="rfid-editor">
                <div class="rfid-toolbar">
                  <div class="rfid-toolbar__left">
                    <el-tag size="mini" type="info">已识别 {{ rfidSummary.total }} 个</el-tag>
                    <el-tag size="mini" :type="rfidSummary.duplicateCount ? 'warning' : 'success'">重复 {{ rfidSummary.duplicateCount }} 个</el-tag>
                    <el-tag size="mini" :type="rfidSummary.invalidCount ? 'danger' : 'success'">异常 {{ rfidSummary.invalidCount }} 个</el-tag>
                  </div>
                  <div class="rfid-toolbar__right">
                    <span class="scan-mode-label">扫码模式</span>
                    <el-switch v-model="scanMode" @change="handleScanModeChange" />
                    <el-button size="mini" plain @click="formatRfidCodes">按行整理</el-button>
                    <el-button size="mini" plain @click="dedupeRfidCodes">一键去重</el-button>
                    <el-button size="mini" plain @click="clearRfidCodes">清空</el-button>
                  </div>
                </div>
                <el-input
                  ref="rfidInput"
                  v-model="form.rfidCodes"
                  type="textarea"
                  :rows="10"
                  placeholder="支持 Excel 整列粘贴、扫码枪连续录入；可按换行、逗号、分号或空格自动识别"
                  @focus="rfidInputFocused = true"
                  @blur="rfidInputFocused = false"
                  @input="handleRfidInput"
                />
                <div class="rfid-hint">
                  <span>红色异常项不会自动提交，建议先整理后保存。</span>
                  <span v-if="scanMode">扫码模式已开启，输入框会尽量保持聚焦，适合扫码枪或 PDA 连续录入。</span>
                </div>
                <div v-if="form.materialId" class="rfid-change-summary">
                  <el-tag size="mini" type="success">新增 {{ rfidChangeSummary.addedCount }} 个</el-tag>
                  <el-tag size="mini" type="danger">删除 {{ rfidChangeSummary.removedCount }} 个</el-tag>
                  <el-tag size="mini" type="info">保留 {{ rfidChangeSummary.keptCount }} 个</el-tag>
                </div>
                <div v-if="rfidSummary.previewList.length" class="rfid-preview">
                  <div class="rfid-preview__title">解析预览</div>
                  <div class="rfid-preview__list">
                    <span
                      v-for="item in rfidSummary.previewList"
                      :key="item.value + item.type"
                      :class="['rfid-preview__item', `is-${item.type}`]"
                    >
                      {{ item.value }}
                    </span>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!form.materialId" type="primary" @click="submitForm">确 定</el-button>
        <el-button v-else type="warning" @click="submitForm">保存修改</el-button>
        <el-button v-if="form.materialId && isPending(form.state)" v-hasPermi="['warehouse:aggregate:material:edit']" type="primary" @click="submitApprove">提交审核</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAggregateMaterial, getAggregateMaterial, getNextAggregateMaterialBatchNo, updateAggregateMaterial, delAggregateMaterial, importAggregateMaterialBatch, approveAggregateMaterial } from '@/api/warehouse/aggregateMaterial'
import { listAggregateRfid } from '@/api/warehouse/aggregateRfid'
import { listUser } from '@/api/system/user'

export default {
  name: 'AggregateMaterial',
  data() {
    const validateRfidCodes = (rule, value, callback) => {
      const summary = this.analyzeRfidCodes(value)
      if (!this.form.materialId && !summary.total) {
        callback(new Error('RFID列表不能为空'))
      } else if (summary.invalidCount > 0) {
        callback(new Error('RFID列表中存在异常内容，请先整理后再保存'))
      } else {
        callback()
      }
    }
    return {
      loading: true,
      showSearch: true,
      total: 0,
      materialList: [],
      userOptions: [],
      title: '',
      open: false,
      scanMode: false,
      rfidInputFocused: false,
      originalRfidCodes: '',
      daterangeCreateTime: [],
      stateTab: '3',
      stateOptions: [
        { label: '待审核', value: '3' },
        { label: '已审核', value: '4' }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        state: '3',
        params: {}
      },
      form: {},
      rules: {
        ownerUserId: [{ required: true, message: '分配用户不能为空', trigger: 'change' }],
        rfidCodes: [{ validator: validateRfidCodes, trigger: 'blur' }]
      }
    }
  },
  computed: {
    rfidSummary() {
      return this.analyzeRfidCodes(this.form.rfidCodes)
    },
    rfidChangeSummary() {
      const currentList = this.rfidSummary.uniqueValidList || []
      const originalList = this.analyzeRfidCodes(this.originalRfidCodes).uniqueValidList || []
      const currentSet = new Set(currentList)
      const originalSet = new Set(originalList)
      return {
        addedCount: currentList.filter(item => !originalSet.has(item)).length,
        removedCount: originalList.filter(item => !currentSet.has(item)).length,
        keptCount: currentList.filter(item => originalSet.has(item)).length
      }
    }
  },
  watch: {
    stateTab(val) {
      this.queryParams.pageNum = 1
      this.queryParams.state = val === 'ALL' ? null : val
      this.getList()
    }
  },
  created() {
    this.getList()
    this.getUserOptions()
  },
  methods: {
    getList() {
      this.loading = true
      this.queryParams.params = this.queryParams.params || {}
      if (this.daterangeCreateTime && this.daterangeCreateTime.length === 2) {
        this.queryParams.params.beginCreateTime = this.daterangeCreateTime[0]
        this.queryParams.params.endCreateTime = this.daterangeCreateTime[1]
      } else {
        this.queryParams.params.beginCreateTime = null
        this.queryParams.params.endCreateTime = null
      }
      listAggregateMaterial(this.queryParams).then((response) => {
        this.materialList = response.rows
        this.total = response.total
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
    analyzeRfidCodes(text) {
      const rawList = String(text || '')
        .split(/[\n,，;；\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
      const counter = {}
      rawList.forEach((item) => {
        counter[item] = (counter[item] || 0) + 1
      })
      const validList = []
      const invalidList = []
      rawList.forEach((item) => {
        if (this.isValidRfidCode(item)) {
          validList.push(item)
        } else {
          invalidList.push(item)
        }
      })
      const duplicateList = Object.keys(counter).filter(key => counter[key] > 1)
      const uniqueValidList = []
      const uniqueMap = {}
      validList.forEach((item) => {
        if (!uniqueMap[item]) {
          uniqueMap[item] = true
          uniqueValidList.push(item)
        }
      })
      const previewList = []
      uniqueValidList.slice(0, 12).forEach((item) => {
        previewList.push({ value: item, type: duplicateList.includes(item) ? 'duplicate' : 'normal' })
      })
      invalidList.slice(0, Math.max(0, 12 - previewList.length)).forEach((item) => {
        previewList.push({ value: item, type: 'invalid' })
      })
      return {
        total: uniqueValidList.length,
        duplicateCount: duplicateList.length,
        invalidCount: invalidList.length,
        uniqueValidList,
        duplicateList,
        invalidList,
        previewList
      }
    },
    isValidRfidCode(value) {
      return /^[A-Za-z0-9_-]+$/.test(value)
    },
    normalizeRfidCodes() {
      const summary = this.analyzeRfidCodes(this.form.rfidCodes)
      this.$set(this.form, 'rfidCodes', summary.uniqueValidList.join('\n'))
      return summary
    },
    handleRfidInput() {
      if (!this.scanMode) {
        return
      }
      this.$nextTick(() => {
        this.focusRfidInput()
      })
    },
    handleScanModeChange(val) {
      if (!val) {
        return
      }
      this.$nextTick(() => {
        this.focusRfidInput()
      })
    },
    focusRfidInput() {
      const input = this.$refs.rfidInput
      if (input && input.$refs && input.$refs.textarea) {
        input.$refs.textarea.focus()
      }
    },
    formatRfidCodes() {
      const summary = this.normalizeRfidCodes()
      if (summary.invalidCount > 0) {
        this.$modal.msgWarning('已自动整理有效 RFID，异常内容未保留')
      } else {
        this.$modal.msgSuccess('RFID 已按一行一个整理')
      }
      this.$nextTick(() => {
        this.focusRfidInput()
      })
    },
    dedupeRfidCodes() {
      const summary = this.analyzeRfidCodes(this.form.rfidCodes)
      this.$set(this.form, 'rfidCodes', summary.uniqueValidList.join('\n'))
      if (summary.duplicateCount > 0) {
        this.$modal.msgSuccess('重复 RFID 已去重')
      } else {
        this.$modal.msgSuccess('当前没有重复 RFID')
      }
      this.$nextTick(() => {
        this.focusRfidInput()
      })
    },
    clearRfidCodes() {
      this.$set(this.form, 'rfidCodes', '')
      this.$nextTick(() => {
        this.focusRfidInput()
      })
    },
    getCurrentDateTime() {
      const date = new Date()
      const pad = (value) => String(value).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    openRfidList(row, useStatus) {
      this.$router.push({
        path: '/aggregate/rfid',
        query: {
          batchNo: row.batchNo,
          useStatus: useStatus || ''
        }
      })
    },
    stateMeta(state) {
      if (this.isPending(state)) {
        return { label: '待审核', type: 'primary' }
      }
      if (state === '4') {
        return { label: '已审核', type: 'success' }
      }
      return { label: '待审核', type: 'primary' }
    },
    isPending(state) {
      return state === '1' || state === '3'
    },
    reset() {
      this.form = {
        materialId: null,
        materialCode: null,
        createTime: this.getCurrentDateTime(),
        originPlace: null,
        batchNo: null,
        state: '3',
        ownerUserId: undefined,
        ownerUserName: null,
        rfidCodes: null,
        remark: null
      }
      this.originalRfidCodes = ''
      this.scanMode = false
      this.rfidInputFocused = false
      this.resetForm('form')
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.daterangeCreateTime = []
      this.resetForm('queryForm')
      this.stateTab = '3'
      this.queryParams.state = '3'
      this.queryParams.params = { beginCreateTime: null, endCreateTime: null }
      this.handleQuery()
    },
    async handleIssue() {
      this.reset()
      const response = await getNextAggregateMaterialBatchNo()
      this.$set(this.form, 'batchNo', response && response.msg ? response.msg : '')
      this.$set(this.form, 'materialCode', this.form.batchNo)
      this.originalRfidCodes = ''
      this.open = true
      this.title = '创建骨料入库'
    },
    handleUpdate(row) {
      this.reset()
      const materialId = row.materialId
      Promise.all([getAggregateMaterial(materialId), listAggregateRfid({ pageNum: 1, pageSize: 999, materialId })]).then(([response, rfidResponse]) => {
        this.form = response.data
        const rows = rfidResponse.rows || []
        const rfidCodes = rows.map(item => item.rfidCode).filter(Boolean).join('\n')
        this.$set(this.form, 'rfidCodes', rfidCodes)
        this.originalRfidCodes = rfidCodes
        this.open = true
        this.title = '修改骨料入库单'
      })
    },
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (!valid) return
        const normalizedSummary = this.normalizeRfidCodes()
        if (!this.form.materialId && !normalizedSummary.total) {
          return
        }
        if (this.form.materialId != null) {
          const payload = {
            materialId: this.form.materialId,
            materialCode: this.form.materialCode,
            materialName: this.form.materialName,
            batchNo: this.form.batchNo,
            createTime: this.form.createTime,
            originPlace: this.form.originPlace,
            state: this.form.state,
            ownerUserId: this.form.ownerUserId,
            ownerUserName: this.form.ownerUserName,
            rfidCodes: this.form.rfidCodes,
            remark: this.form.remark
          }
          updateAggregateMaterial(payload).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          importAggregateMaterialBatch(this.form).then(() => {
            this.$modal.msgSuccess('创建成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    submitApprove() {
      this.$refs['form'].validate((valid) => {
        if (!valid) return
        this.normalizeRfidCodes()
        const payload = {
          materialId: this.form.materialId,
          materialCode: this.form.materialCode,
          materialName: this.form.materialName,
          batchNo: this.form.batchNo,
          createTime: this.form.createTime,
          originPlace: this.form.originPlace,
          state: this.form.state,
          ownerUserId: this.form.ownerUserId,
          ownerUserName: this.form.ownerUserName,
          rfidCodes: this.form.rfidCodes,
          remark: this.form.remark
        }
        this.$modal.confirm('是否确认提交审核？提交之后将不可修改！').then(() => {
          return updateAggregateMaterial(payload)
        }).then(() => {
          return approveAggregateMaterial({ materialId: this.form.materialId })
        }).then(() => {
          this.$modal.msgSuccess('审核成功')
          this.open = false
          this.getList()
        }).catch(() => {})
      })
    },
    handleApprove(row) {
      this.$modal.confirm('是否确认审核该骨料批次？审核之后将不可撤回！').then(() => {
        return approveAggregateMaterial({ materialId: row.materialId })
      }).then(() => {
        this.$modal.msgSuccess('审核成功')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const total = row.rfidCount || 0
      this.$modal.confirm('该批次包含 ' + total + ' 个骨料，确定一起删除吗？').then(function() {
        return delAggregateMaterial(row.materialId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.rfid-editor {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  background: #fafbfd;
}

.rfid-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.rfid-toolbar__left,
.rfid-toolbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.scan-mode-label {
  font-size: 12px;
  color: #606266;
}

.rfid-hint {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.rfid-change-summary {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.rfid-preview {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.rfid-preview__title {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.rfid-preview__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.rfid-preview__item {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.4;
  background: #f4f4f5;
  color: #606266;
}

.rfid-preview__item.is-normal {
  background: #ecf5ff;
  color: #409eff;
}

.rfid-preview__item.is-duplicate {
  background: #fdf6ec;
  color: #e6a23c;
}

.rfid-preview__item.is-invalid {
  background: #fef0f0;
  color: #f56c6c;
}

.count-number {
  font-weight: 600;
  padding: 0;
}

.count-total {
  color: #409eff;
}

.count-used {
  color: #67c23a;
}

.count-unused {
  color: #e6a23c;
}

.count-link:hover {
  text-decoration: underline;
}
</style>
