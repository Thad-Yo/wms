<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="RFID" prop="rfidCode">
        <el-input v-model="queryParams.rfidCode" placeholder="请输入RFID/EPC编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="骨料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入骨料名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="当前状态" prop="currentState">
        <el-select v-model="queryParams.currentState" placeholder="请选择状态" clearable>
          <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
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
        <el-button v-hasPermi="['warehouse:aggregate:rfid:edit']" type="primary" plain icon="el-icon-connection" size="mini" :disabled="multiple" @click="handleBind">批量绑定货品</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['warehouse:aggregate:rfid:remove']" type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rfidList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="RFID/EPC编码" align="center" prop="rfidCode" min-width="170">
        <template slot-scope="scope">
          <el-button type="text" @click="openLifecycle(scope.row)">{{ scope.row.rfidCode }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="TID编码" align="center" prop="tidCode" min-width="140" />
      <el-table-column label="骨料编号" align="center" prop="materialCode" />
      <el-table-column label="骨料名称" align="center" prop="materialName" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="分配用户" align="center" prop="ownerUserName" />
      <el-table-column label="身份粒度" align="center" prop="identityLevel" />
      <el-table-column label="使用状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.bindGoodsId ? 'success' : 'warning'" effect="plain">{{ scope.row.bindGoodsId ? '已使用' : '未使用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定货品" align="center" prop="bindGoodsName" min-width="140">
        <template slot-scope="scope">
          <span>{{ scope.row.bindGoodsName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前状态" align="center" prop="currentState" width="110">
        <template slot-scope="scope">
          <el-tag :type="stateTagType(scope.row.currentState)">{{ stateLabel(scope.row.currentState) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前仓库" align="center" prop="currentWarehouseName" />
      <el-table-column label="最后事件" align="center" prop="lastEventTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-position" @click="openEvent(scope.row)">采集事件</el-button>
          <el-button v-if="!scope.row.bindGoodsId" size="mini" type="text" icon="el-icon-connection" @click="handleBind(scope.row)">绑定货品</el-button>
          <el-button size="mini" type="text" icon="el-icon-tickets" @click="openLifecycle(scope.row)">时间线</el-button>
          <el-button v-hasPermi="['warehouse:aggregate:rfid:remove']" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="批量绑定货品" :visible.sync="bindOpen" width="560px" append-to-body>
      <el-alert :title="'已选择 ' + bindForm.identityIds.length + ' 个RFID身份'" type="info" show-icon class="mb16" />
      <el-form ref="bindForm" :model="bindForm" :rules="bindRules" label-width="90px">
        <el-form-item label="绑定货品" prop="bindGoodsId">
          <el-select
            v-model="bindForm.bindGoodsId"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="请输入货品编号或名称搜索"
            :remote-method="searchGoods"
            :loading="goodsLoading"
            style="width: 100%"
          >
            <el-option v-for="item in goodsOptions" :key="item.goodsId" :label="goodsOptionLabel(item)" :value="item.goodsId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="bindForm.remark" type="textarea" placeholder="请输入绑定说明" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBind">确 定</el-button>
        <el-button @click="bindOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAggregateRfid, delAggregateRfid, bindAggregateRfidGoods, listAggregateRfidGoodsOptions } from '@/api/warehouse/aggregateRfid'

export default {
  name: 'AggregateRfid',
  data() {
    return {
      loading: true,
      goodsLoading: false,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      rfidList: [],
      goodsOptions: [],
      selectedRows: [],
      bindOpen: false,
      bindForm: {
        identityIds: [],
        bindGoodsId: null,
        remark: null
      },
      stateOptions: [
        { label: '已建档', value: 'CREATED' },
        { label: '已入库', value: 'IN_STOCK' },
        { label: '已出库', value: 'OUT_STOCK' },
        { label: '已移动', value: 'MOVED' }
      ],
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
        currentState: null,
        useStatus: null
      },
      bindRules: {
        bindGoodsId: [{ required: true, message: '绑定货品不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAggregateRfid(this.queryParams).then((response) => {
        this.rfidList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    stateLabel(value) {
      const item = this.stateOptions.find((state) => state.value === value)
      return item ? item.label : value
    },
    stateTagType(value) {
      if (value === 'IN_STOCK') return 'success'
      if (value === 'OUT_STOCK') return 'info'
      if (value === 'MOVED') return 'warning'
      return ''
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
      this.selectedRows = selection
      this.ids = selection.map((item) => item.identityId)
      this.multiple = !selection.length
    },
    handleBind(row) {
      const rows = row.identityId ? [row] : this.selectedRows
      const used = rows.find((item) => item.bindGoodsId)
      if (used) {
        this.$modal.msgWarning('已使用的RFID不能重复绑定：' + used.rfidCode)
        return
      }
      this.bindForm = {
        identityIds: rows.map((item) => item.identityId),
        bindGoodsId: null,
        remark: null
      }
      this.goodsOptions = []
      this.bindOpen = true
    },
    searchGoods(query) {
      if (!query) {
        this.goodsOptions = []
        return
      }
      this.goodsLoading = true
      Promise.all([
        listAggregateRfidGoodsOptions({ pageNum: 1, pageSize: 20, goodsName: query }),
        listAggregateRfidGoodsOptions({ pageNum: 1, pageSize: 20, goodsCode: query })
      ]).then(([nameResponse, codeResponse]) => {
        const goodsMap = new Map()
        const goodsRows = [...(nameResponse.rows || []), ...(codeResponse.rows || [])]
        goodsRows.forEach((item) => {
          goodsMap.set(item.goodsId, item)
        })
        this.goodsOptions = Array.from(goodsMap.values())
        this.goodsLoading = false
      }).catch(() => {
        this.goodsLoading = false
      })
    },
    goodsOptionLabel(item) {
      return `${item.goodsCode || ''}${item.goodsName ? ' / ' + item.goodsName : ''}`
    },
    submitBind() {
      this.$refs['bindForm'].validate((valid) => {
        if (!valid) return
        bindAggregateRfidGoods(this.bindForm).then(() => {
          this.$modal.msgSuccess('绑定成功')
          this.bindOpen = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const identityIds = row.identityId || this.ids
      this.$modal.confirm('是否确认删除RFID身份编号为"' + identityIds + '"的数据项？').then(function() {
        return delAggregateRfid(identityIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    openEvent(row) {
      this.$router.push({ path: '/aggregate/event', query: { rfidCode: row.rfidCode }})
    },
    openLifecycle(row) {
      this.$router.push({ path: '/aggregate/lifecycle', query: { rfidCode: row.rfidCode }})
    }
  }
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
</style>
