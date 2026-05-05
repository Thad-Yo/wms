<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="骨料编号" prop="materialCode">
        <el-input v-model="queryParams.materialCode" placeholder="请输入骨料编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="骨料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入骨料名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="骨料类型" prop="materialType">
        <el-input v-model="queryParams.materialType" placeholder="请输入骨料类型" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:material:import']" type="primary" plain icon="el-icon-upload2" size="mini" @click="handleIssue">发行批次</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:material:edit']" type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:material:remove']" type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="materialList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="骨料编号" align="center" prop="materialCode" min-width="120" />
      <el-table-column label="骨料名称" align="center" prop="materialName" min-width="140" />
      <el-table-column label="骨料类型" align="center" prop="materialType" />
      <el-table-column label="规格/粒径" align="center" prop="specification" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="总数" align="center" prop="rfidCount" width="90">
        <template slot-scope="scope">
          <span class="count-number count-total">{{ scope.row.rfidCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已使用" align="center" prop="usedRfidCount" width="90">
        <template slot-scope="scope">
          <span class="count-number count-used">{{ scope.row.usedRfidCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="未使用" align="center" prop="unusedRfidCount" width="90">
        <template slot-scope="scope">
          <span class="count-number count-unused">{{ scope.row.unusedRfidCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分配用户" align="center" prop="ownerUserName" width="120" />
      <el-table-column label="供应商" align="center" prop="supplierName" />
      <el-table-column label="计量单位" align="center" prop="unit" />
      <el-table-column label="质量等级" align="center" prop="qualityGrade" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button v-hasPermi="['warehouse:aggregate:material:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['warehouse:aggregate:material:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-alert v-if="!form.materialId" title="发行批次会创建一个骨料批次，并批量写入该批次下的RFID数字身份。" type="info" show-icon class="mb16" />
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="骨料编号" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="请输入骨料编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="骨料名称" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入骨料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="骨料类型" prop="materialType">
              <el-input v-model="form.materialType" placeholder="如碎石、机制砂" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格/粒径" prop="specification">
              <el-input v-model="form.specification" placeholder="如5-10mm" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="originPlace">
              <el-input v-model="form.originPlace" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分配用户" prop="ownerUserId">
              <el-select v-model="form.ownerUserId" filterable clearable placeholder="请选择下游用户" style="width: 100%" @change="ownerChange">
                <el-option v-for="item in userOptions" :key="item.userId" :label="userOptionLabel(item)" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="吨" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="质量等级" prop="qualityGrade">
              <el-input v-model="form.qualityGrade" placeholder="请输入质量等级" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item v-if="!form.materialId" label="RFID列表" prop="rfidCodes">
              <el-input v-model="form.rfidCodes" type="textarea" :rows="10" placeholder="一行一个RFID，也支持逗号、分号或空格分隔" />
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAggregateMaterial, getAggregateMaterial, updateAggregateMaterial, delAggregateMaterial, importAggregateMaterialBatch } from '@/api/warehouse/aggregateMaterial'
import { listUser } from '@/api/system/user'

export default {
  name: 'AggregateMaterial',
  data() {
    const validateRfidCodes = (rule, value, callback) => {
      if (!this.form.materialId && !value) {
        callback(new Error('RFID列表不能为空'))
      } else {
        callback()
      }
    }
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      materialList: [],
      userOptions: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialCode: null,
        materialName: null,
        materialType: null,
        batchNo: null
      },
      form: {},
      rules: {
        materialCode: [{ required: true, message: '骨料编号不能为空', trigger: 'blur' }],
        materialName: [{ required: true, message: '骨料名称不能为空', trigger: 'blur' }],
        batchNo: [{ required: true, message: '批次号不能为空', trigger: 'blur' }],
        ownerUserId: [{ required: true, message: '分配用户不能为空', trigger: 'change' }],
        rfidCodes: [{ validator: validateRfidCodes, trigger: 'blur' }]
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
      listAggregateMaterial(this.queryParams).then((response) => {
        this.materialList = response.rows
        this.total = response.total
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
    reset() {
      this.form = {
        materialId: null,
        materialCode: null,
        materialName: null,
        materialType: null,
        specification: null,
        originPlace: null,
        batchNo: null,
        supplierName: null,
        unit: '吨',
        qualityGrade: null,
        ownerUserId: undefined,
        ownerUserName: null,
        rfidCodes: null,
        remark: null
      }
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
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.materialId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleIssue() {
      this.reset()
      this.open = true
      this.title = '发行骨料批次'
    },
    handleUpdate(row) {
      this.reset()
      const materialId = row.materialId || this.ids
      getAggregateMaterial(materialId).then((response) => {
        this.form = response.data
        this.open = true
        this.title = '修改骨料批次'
      })
    },
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (!valid) return
        if (this.form.materialId != null) {
          updateAggregateMaterial(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          importAggregateMaterialBatch(this.form).then(() => {
            this.$modal.msgSuccess('发行成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const materialIds = row.materialId || this.ids
      this.$modal.confirm('是否确认删除骨料档案编号为"' + materialIds + '"的数据项？').then(function() {
        return delAggregateMaterial(materialIds)
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

.count-number {
  font-weight: 600;
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
</style>
