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
              <el-input v-model="form.subjectCode" disabled placeholder="系统自动生成" />
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

      <div>
        <div class="field-header">
          <span>字段配置</span>
          <div class="field-header__actions">
            <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addField">新增字段</el-button>
          </div>
        </div>

        <div class="field-engine-tip">
          按业务字段来配置即可，例如“生产日期”选择日期，“是否质检”选择下拉框，“数量”选择数字。
        </div>

        <draggable v-model="form.fieldList" handle=".drag-handle" animation="200" @end="syncFieldSortOrder">
          <transition-group type="transition" name="flip-list">
            <div v-for="(field, index) in form.fieldList" :key="field._rowKey" class="field-card">
              <div class="field-card__header">
                <div class="field-card__title">
                  <span class="drag-handle">
                    <i class="el-icon-rank" />
                  </span>
                  <div class="field-title-editor">
                    <el-input
                      v-if="field.editingLabel"
                      ref="fieldTitleInput"
                      v-model="field.fieldLabel"
                      size="mini"
                      placeholder="请输入字段名称"
                      @input="handleFieldLabelInput(field)"
                      @blur="finishFieldTitleEdit(field, index)"
                      @keyup.enter.native="finishFieldTitleEdit(field, index)"
                    />
                    <span
                      v-else
                      :class="['field-title-editor__text', { 'is-placeholder': !field.fieldLabel, 'is-saved': field.justUpdated }]"
                      title="点击编辑"
                      @click="startFieldTitleEdit(field, index)"
                    >
                      {{ field.fieldLabel || `点击编辑字段 ${index + 1}` }}
                    </span>
                  </div>
                </div>
                <div class="field-card__actions">
                  <el-button type="text" size="mini" icon="el-icon-plus" @click="insertField(index)">下方增加</el-button>
                  <el-button type="text" size="mini" icon="el-icon-delete" @click="removeField(index)">删除当前行</el-button>
                </div>
              </div>

              <div class="field-editor">
                <div class="field-editor__row">
                  <div class="field-editor__item is-full">
                    <div class="field-editor__label">字段类型</div>
                    <el-radio-group v-model="field.fieldType" size="mini" @change="handleFieldTypeChange(field)">
                      <el-radio-button label="input">文本</el-radio-button>
                      <el-radio-button label="date">日期</el-radio-button>
                      <el-radio-button label="select">下拉框</el-radio-button>
                      <el-radio-button label="number">数字</el-radio-button>
                      <el-radio-button label="textarea">多行文本</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>

                <div class="field-editor__row">
                  <div class="field-editor__item is-full">
                    <el-checkbox v-model="field.requiredFlag" true-label="1" false-label="0">是否必填</el-checkbox>
                  </div>
                </div>

                <div v-if="field.fieldType === 'select'" class="field-select-box">
                  <div class="field-select-box__title">下拉选项</div>
                  <div v-if="!field.optionList || !field.optionList.length" class="field-select-empty" @click="addFirstOption(field)">
                    点击新增选项
                  </div>
                  <div v-else>
                    <div v-for="(option, optionIndex) in field.optionList" :key="optionIndex" class="option-row">
                      <el-input v-model="option.label" placeholder="选项名称" size="mini" class="option-input" />
                      <el-input v-model="option.value" placeholder="选项值，默认同名称" size="mini" class="option-input" />
                      <el-button type="text" size="mini" icon="el-icon-plus" @click="insertOption(field, optionIndex)">新增</el-button>
                      <el-button type="text" size="mini" icon="el-icon-minus" @click="removeOption(field, optionIndex)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </transition-group>
        </draggable>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import {
  listAggregateSubjectTemplate,
  getAggregateSubjectTemplate,
  getNextAggregateSubjectTemplateCode,
  addAggregateSubjectTemplate,
  updateAggregateSubjectTemplate,
  activateAggregateSubjectTemplate,
  delAggregateSubjectTemplate,
  copyAggregateSubjectTemplate
} from '@/api/warehouse/aggregateSubjectTemplate'

export default {
  name: 'AggregateSubjectTemplate',
  components: {
    draggable
  },
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      templateList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        subjectCode: null,
        subjectName: null,
        status: null
      },
      form: {},
      rules: {
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
        _rowKey: `${Date.now()}_${Math.random().toString(16).slice(2)}`,
        editingLabel: true,
        justUpdated: false,
        fieldCode: null,
        fieldLabel: null,
        fieldType: 'input',
        defaultValue: null,
        optionsJson: null,
        optionList: [],
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
      getNextAggregateSubjectTemplateCode().then((response) => {
        this.form.subjectCode = response.msg || ''
        this.open = true
        this.title = '新增骨料模板'
      })
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
          _rowKey: `${item.fieldId || index}_${Date.now()}_${index}`,
          editingLabel: false,
          justUpdated: false,
          optionList: this.formatOptionList(item.optionsJson)
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
      if (!row.optionList) {
        row.optionList = []
      }
    },
    handleFieldLabelInput(row) {
      row.fieldCode = this.generateFieldCode(row.fieldLabel)
    },
    startFieldTitleEdit(row, index) {
      this.form.fieldList.forEach((item) => {
        item.editingLabel = false
      })
      row.editingLabel = true
      this.$nextTick(() => {
        const refs = this.$refs.fieldTitleInput
        const inputRef = Array.isArray(refs) ? refs[index] : refs
        if (inputRef && inputRef.focus) {
          inputRef.focus()
        }
      })
    },
    finishFieldTitleEdit(row) {
      row.editingLabel = false
      row.fieldLabel = (row.fieldLabel || '').trim()
      this.handleFieldLabelInput(row)
      row.justUpdated = true
      setTimeout(() => {
        row.justUpdated = false
      }, 1200)
    },
    normalizeOptionList(optionList) {
      return (optionList || []).map((item) => {
        const label = (item.label || '').trim()
        const value = (item.value || '').trim() || label
        return { label, value }
      }).filter(item => item.label)
    },
    addField() {
      this.form.fieldList.push(this.createField(this.form.fieldList.length + 1))
      this.$nextTick(() => {
        const index = this.form.fieldList.length - 1
        this.startFieldTitleEdit(this.form.fieldList[index], index)
      })
    },
    insertField(index) {
      this.form.fieldList.splice(index + 1, 0, this.createField(index + 2))
      this.syncFieldSortOrder()
      this.$nextTick(() => {
        this.startFieldTitleEdit(this.form.fieldList[index + 1], index + 1)
      })
    },
    insertOption(row, index) {
      if (!row.optionList) {
        row.optionList = []
      }
      row.optionList.splice(index + 1, 0, { label: '', value: '' })
    },
    addFirstOption(row) {
      row.optionList = [{ label: '', value: '' }]
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
      this.syncFieldSortOrder()
    },
    syncFieldSortOrder() {
      this.form.fieldList = (this.form.fieldList || []).map((item, index) => ({
        ...item,
        sortOrder: index + 1
      }))
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
            field.fieldCode = this.generateFieldCode(field.fieldLabel) || `field_${index + 1}`
            field.sortOrder = field.sortOrder || index + 1
            field.placeholder = null
            field.optionsJson = field.fieldType === 'select' ? this.buildOptionsJson(field.optionList) : null
            field.validationRule = null
            field.editableEventTypes = null
            field.indexedFlag = '0'
            field.searchFlag = '0'
            field.traceFlag = '0'
            field.exportFlag = '0'
            return field
          })
        }
        if (!payload.templateId && !payload.subjectCode) {
          this.$modal.msgError('模板编码生成失败')
          return
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

.field-header__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.field-card-list {
  margin-top: 8px;
}

.field-card {
  margin-bottom: 12px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.05);
}

.field-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.field-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.field-title-editor {
  min-width: 220px;
}

.field-title-editor__text {
  display: inline-block;
  min-width: 220px;
  padding: 4px 6px;
  border-radius: 4px;
  cursor: text;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.field-title-editor__text:hover {
  background: #f5f7fa;
}

.field-title-editor__text.is-placeholder {
  color: #b8c0cc;
  font-weight: 400;
}

.field-title-editor__text.is-saved {
  background: #ecfdf3;
  color: #23a36d;
  box-shadow: inset 0 0 0 1px #b7ebd1;
}

.drag-handle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  cursor: move;
  color: #409eff;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
}

.field-card__actions {
  display: flex;
  gap: 8px;
}

.field-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-editor__row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.field-editor__item {
  min-width: 220px;
  flex: 1;
}

.field-editor__item.is-wide {
  flex: 2;
}

.field-editor__item.is-full {
  flex: 1 1 100%;
}

.field-editor__label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
  font-weight: 600;
}

.field-ability-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding: 2px 0 4px;
}

.field-select-box {
  padding: 10px 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  background: #fafbfd;
}

.field-select-box__title {
  margin-bottom: 10px;
  font-weight: 600;
  color: #606266;
}

.field-select-empty {
  padding: 12px 14px;
  border: 1px dashed #d9ecff;
  border-radius: 6px;
  color: #409eff;
  background: #f8fbff;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s ease;
}

.field-select-empty:hover {
  background: #ecf5ff;
  border-color: #409eff;
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
