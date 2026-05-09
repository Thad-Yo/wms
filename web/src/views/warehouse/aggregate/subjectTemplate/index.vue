<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="模板编码" prop="subjectCode">
        <el-input v-model="queryParams.subjectCode" placeholder="请输入模板编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模板名称" prop="subjectName">
        <el-input v-model="queryParams.subjectName" placeholder="请输入模板名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="使用中" value="0" />
          <el-option label="未使用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:subjectTemplate:add']" type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增骨料模板</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="templateList">
      <el-table-column label="模板编码" align="center" prop="subjectCode" min-width="120" />
      <el-table-column label="模板名称" align="center" prop="subjectName" min-width="140" />
      <el-table-column label="启用" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            :value="scope.row.status === '0'"
            active-color="#13ce66"
            inactive-color="#dcdfe6"
            @change="(value) => handleStatusSwitch(scope.row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button v-hasPermi="['warehouse:aggregate:subjectTemplate:add']" size="mini" type="text" icon="el-icon-document-copy" @click="handleCopy(scope.row)">复制模板</el-button>
          <el-button v-hasPermi="['warehouse:aggregate:subjectTemplate:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['warehouse:aggregate:subjectTemplate:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="980px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="模板编码" prop="subjectCode">
              <el-input v-model="form.subjectCode" placeholder="请输入模板编码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板名称" prop="subjectName">
              <el-input v-model="form.subjectName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.useCurrent" active-text="使用中" inactive-text="未使用" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="field-header">
        <span>字段配置</span>
        <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addField">新增字段</el-button>
      </div>

      <div class="field-engine-tip">
        模板不仅定义字段，还决定字段是否必填、是否参与检索/追溯/导出，以及在哪些事件节点允许编辑。
      </div>

      <el-table :data="form.fieldList" border class="field-table">
        <el-table-column label="排序" width="70" align="center">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.sortOrder" :min="1" :max="999" size="mini" />
          </template>
        </el-table-column>
        <el-table-column label="字段编码" min-width="130">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.fieldCode"
              placeholder="根据字段名称自动生成"
              size="mini"
              @input="handleFieldCodeInput(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="字段名称" min-width="130">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.fieldLabel"
              placeholder="请输入字段名称"
              size="mini"
              @input="handleFieldLabelInput(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="控件类型" width="110">
          <template slot-scope="scope">
            <el-select v-model="scope.row.fieldType" size="mini" @change="handleFieldTypeChange(scope.row)">
              <el-option label="文本" value="input" />
              <el-option label="多行文本" value="textarea" />
              <el-option label="下拉" value="select" />
              <el-option label="日期" value="date" />
              <el-option label="数字" value="number" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="80" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.requiredFlag" active-value="1" inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column label="默认值" min-width="120">
          <template slot-scope="scope">
            <el-input v-model="scope.row.defaultValue" placeholder="默认值" size="mini" />
          </template>
        </el-table-column>
        <el-table-column label="校验规则" min-width="150">
          <template slot-scope="scope">
            <el-input v-model="scope.row.validationRule" placeholder="如 regex:^\\d{4}-\\d{2}-\\d{2}$" size="mini" />
          </template>
        </el-table-column>
        <el-table-column label="下拉选项" min-width="320">
          <template slot-scope="scope">
            <div v-if="scope.row.fieldType === 'select'">
              <div v-for="(option, optionIndex) in scope.row.optionList" :key="optionIndex" class="option-row">
                <el-input v-model="option.label" placeholder="选项名称" size="mini" class="option-input" />
                <el-input v-model="option.value" placeholder="选项值" size="mini" class="option-input" />
                <el-button type="text" size="mini" icon="el-icon-minus" @click="removeOption(scope.row, optionIndex)" />
              </div>
              <el-button type="text" size="mini" icon="el-icon-plus" @click="addOption(scope.row)">新增选项</el-button>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="引擎能力" min-width="220">
          <template slot-scope="scope">
            <div class="flag-grid">
              <el-checkbox v-model="scope.row.indexedFlag" true-label="1" false-label="0">检索</el-checkbox>
              <el-checkbox v-model="scope.row.searchFlag" true-label="1" false-label="0">查询</el-checkbox>
              <el-checkbox v-model="scope.row.traceFlag" true-label="1" false-label="0">追溯</el-checkbox>
              <el-checkbox v-model="scope.row.exportFlag" true-label="1" false-label="0">导出</el-checkbox>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="可编辑事件" min-width="220">
          <template slot-scope="scope">
            <el-select v-model="scope.row.editableEventTypeList" multiple collapse-tags size="mini" placeholder="选择事件节点">
              <el-option v-for="item in editableEventOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="removeField(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAggregateSubjectTemplate,
  getAggregateSubjectTemplate,
  addAggregateSubjectTemplate,
  updateAggregateSubjectTemplate,
  activateAggregateSubjectTemplate,
  delAggregateSubjectTemplate,
  copyAggregateSubjectTemplate
} from '@/api/warehouse/aggregateSubjectTemplate'

export default {
  name: 'AggregateSubjectTemplate',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      templateList: [],
      title: '',
      open: false,
      editableEventOptions: [
        { label: '创建对象', value: 'CREATED' },
        { label: '绑定标签', value: 'BIND_OBJECT' },
        { label: '入库', value: 'INBOUND' },
        { label: '出库', value: 'OUTBOUND' },
        { label: '运输', value: 'TRANSPORT' },
        { label: '交接', value: 'HANDOVER' },
        { label: '签收', value: 'SIGN' },
        { label: '质检', value: 'QUALITY_CHECK' },
        { label: '安装', value: 'INSTALL' },
        { label: '售后', value: 'AFTER_SALE' },
        { label: '销毁', value: 'DESTROY' }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        subjectCode: null,
        subjectName: null,
        status: null
      },
      form: {},
      rules: {
        subjectCode: [{ required: true, message: '模板编码不能为空', trigger: 'blur' }],
        subjectName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAggregateSubjectTemplate(this.queryParams).then((response) => {
        this.templateList = response.rows || []
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        templateId: null,
        subjectCode: null,
        subjectName: null,
        useCurrent: false,
        remark: null,
        fieldList: [this.createField(1)]
      }
      this.resetForm('form')
    },
    createField(sortOrder) {
      return {
        fieldCode: null,
        fieldLabel: null,
        fieldType: 'input',
        defaultValue: null,
        optionsJson: null,
        optionList: [],
        fieldCodeManual: false,
        validationRule: null,
        requiredFlag: '0',
        indexedFlag: '0',
        traceFlag: '0',
        searchFlag: '0',
        exportFlag: '0',
        sortOrder: sortOrder,
        editableEventTypes: null,
        editableEventTypeList: []
      }
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
      this.open = true
      this.title = '新增骨料模板'
    },
    handleUpdate(row) {
      const templateId = row.templateId
      this.reset()
      getAggregateSubjectTemplate(templateId).then((response) => {
        this.form = response.data
        this.form.useCurrent = this.form.status === '0'
        if (!this.form.fieldList || !this.form.fieldList.length) {
          this.form.fieldList = [this.createField(1)]
        }
        this.form.fieldList = this.form.fieldList.map((item, index) => ({
          ...this.createField(index + 1),
          ...item,
          optionList: this.formatOptionList(item.optionsJson),
          editableEventTypeList: this.parseEditableEventTypes(item.editableEventTypes),
          fieldCodeManual: Boolean(item.fieldCode)
        }))
        this.open = true
        this.title = '修改骨料模板'
      })
    },
    handleDelete(row) {
      const templateIds = row.templateId
      this.$modal.confirm('是否确认删除骨料模板编号为"' + templateIds + '"的数据项？').then(() => {
        return delAggregateSubjectTemplate(templateIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleCopy(row) {
      this.$modal.confirm('是否复制模板【' + row.subjectName + '】？').then(() => {
        return copyAggregateSubjectTemplate(row.templateId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('复制成功')
      }).catch(() => {})
    },
    handleStatusSwitch(row, value) {
      if (!value) {
        this.$modal.msgWarning('启用状态不能直接关闭，请切换到其他模板启用')
        this.getList()
        return
      }
      if (row.status === '0') {
        return
      }
      activateAggregateSubjectTemplate(row.templateId).then(() => {
        this.$modal.msgSuccess('启用成功，其他模板已自动关闭')
        this.getList()
      }).catch(() => {
        this.getList()
      })
    },
    handleFieldTypeChange(row) {
      if (row.fieldType !== 'select') {
        row.optionsJson = null
        row.optionList = []
        return
      }
      if (!row.optionList || !row.optionList.length) {
        row.optionList = [{ label: '', value: '' }]
      }
    },
    handleFieldLabelInput(row) {
      if (!row.fieldCodeManual) {
        row.fieldCode = this.generateFieldCode(row.fieldLabel)
      }
    },
    handleFieldCodeInput(row) {
      const autoCode = this.generateFieldCode(row.fieldLabel)
      row.fieldCodeManual = Boolean(row.fieldCode) && row.fieldCode !== autoCode
      if (!row.fieldCode) {
        row.fieldCodeManual = false
      }
    },
    addField() {
      this.form.fieldList.push(this.createField(this.form.fieldList.length + 1))
    },
    addOption(row) {
      if (!row.optionList) {
        row.optionList = []
      }
      row.optionList.push({ label: '', value: '' })
    },
    removeOption(row, index) {
      row.optionList.splice(index, 1)
      if (!row.optionList.length) {
        row.optionList.push({ label: '', value: '' })
      }
    },
    removeField(index) {
      this.form.fieldList.splice(index, 1)
      if (!this.form.fieldList.length) {
        this.form.fieldList.push(this.createField(1))
      }
    },
    cancel() {
      this.open = false
    },
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (!valid) return
        if (!this.form.fieldList || !this.form.fieldList.length) {
          this.$modal.msgError('请至少配置一个字段')
          return
        }
        const payload = {
          ...this.form,
          moduleName: this.form.subjectName,
          status: this.form.useCurrent ? '0' : '1',
          fieldList: this.form.fieldList.map((item, index) => {
            const field = { ...item }
            field.sortOrder = field.sortOrder || index + 1
            field.placeholder = null
            field.optionsJson = field.fieldType === 'select' ? this.buildOptionsJson(field.optionList) : null
            field.editableEventTypes = (field.editableEventTypeList || []).join(',')
            return field
          })
        }
        const request = this.form.templateId ? updateAggregateSubjectTemplate(payload) : addAggregateSubjectTemplate(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.form.templateId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    formatOptionList(optionsJson) {
      if (!optionsJson) return [{ label: '', value: '' }]
      try {
        const options = JSON.parse(optionsJson)
        if (!Array.isArray(options) || !options.length) {
          return [{ label: '', value: '' }]
        }
        return options.map((item) => ({
          label: item.label || '',
          value: item.value || ''
        }))
      } catch (e) {
        return [{ label: '', value: '' }]
      }
    },
    buildOptionsJson(optionList) {
      if (!optionList || !optionList.length) return null
      const options = optionList.map((item) => {
        const label = (item.label || '').trim()
        const value = (item.value || '').trim() || label
        return { label, value }
      }).filter((item) => item.label)
      return options.length ? JSON.stringify(options) : null
    },
    parseEditableEventTypes(value) {
      if (!value) return []
      return value.split(',').map((item) => item.trim()).filter(Boolean)
    },
    generateFieldCode(fieldLabel) {
      if (!fieldLabel) return ''
      const trimmed = fieldLabel.trim()
      if (!trimmed) return ''
      const normalized = trimmed
        .replace(/[A-Z]/g, (match) => `_${match.toLowerCase()}`)
        .replace(/[\s\-]+/g, '_')
        .replace(/[^\w\u4e00-\u9fa5]/g, '_')
        .replace(/_+/g, '_')
        .replace(/^_+|_+$/g, '')
      if (!normalized) return ''
      if (/[\u4e00-\u9fa5]/.test(normalized)) {
        return `field_${normalized}`
      }
      return normalized.toLowerCase()
    }
  }
}
</script>

<style scoped>
.field-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 12px;
  font-weight: 600;
}

.field-table {
  margin-top: 8px;
}

.field-engine-tip {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.option-input {
  flex: 1;
}

.flag-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  row-gap: 4px;
}

.field-tip {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
</style>
