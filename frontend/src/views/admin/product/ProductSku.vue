<template>
  <div class="page-container">
    <div class="page-header">
      <h2>SKU管理</h2>
      <el-button type="primary" @click="handleAddSku">新增SKU</el-button>
    </div>

    <!-- 查询条件 -->
    <el-form :model="queryParams" inline class="query-form">
      <el-form-item label="SKU编码">
        <el-input v-model="queryParams.skuCode" placeholder="请输入SKU编码" clearable style="width: 200px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="所属商品">
        <el-select
          v-model="queryParams.productId"
          placeholder="请选择商品"
          clearable
          filterable
          style="width: 260px"
        >
          <el-option
            v-for="item in productList"
            :key="item.id"
            :label="item.productName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    
    <el-table :data="skuList" border stripe v-loading="tableLoading">
      <el-table-column label="SKU信息" width="350">
        <template #default="{ row }">
          <div class="sku-info">
            <div class="sku-code">编码：{{ row.skuCode }}</div>
            <div class="sku-product">所属商品ID：{{ row.productId }}</div>
            <div class="sku-time">创建时间：{{ row.createTime }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="图片" align="center" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.pic || 'https://via.placeholder.com/60'"
            style="width: 60px; height: 60px; border-radius: 4px"
            fit="cover"
            preview-teleported
            :preview-src-list="row.pic ? [row.pic] : []"
          />
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center">
        <template #default="{ row }">
          <div style="color: #f56c6c; font-weight: bold">¥{{ Number(row.price || 0).toFixed(2) }}</div>
          <div style="color: #999; font-size: 12px">成本：¥{{ Number(row.costPrice || 0).toFixed(2) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="库存" align="center" prop="stock" />
      <el-table-column label="规格属性" align="center" width="250">
        <template #default="{ row }">
          <div v-if="row.skuAttrList && row.skuAttrList.length">
            <div v-for="attr in row.skuAttrList" :key="attr.id" class="attr-item">
              <span class="attr-name">{{ attr.attrName }}:</span>
              <span class="attr-value">{{ attr.attrValue }}</span>
            </div>
          </div>
          <span v-else style="color: #999; font-size: 12px">无规格</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            :loading="actionLoading[row.id + '_status']"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEditSku(row)">编辑</el-button>
          <el-button type="danger" link :loading="actionLoading[row.id + '_delete']" @click="handleDeleteSku(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      style="margin-top:20px; text-align:right"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="handleSizeChange"
      @current-change="getSkuList"
    />

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="所属商品" prop="productId">
          <el-select v-model="formData.productId" style="width: 100%" filterable placeholder="请选择所属商品">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.productName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU编码" prop="skuCode">
          <el-input v-model="formData.skuCode" placeholder="请输入SKU编码" />
        </el-form-item>
        <el-form-item label="销售价" prop="price">
          <el-input-number v-model="formData.price" :precision="2" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本价" prop="costPrice">
          <el-input-number v-model="formData.costPrice" :precision="2" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片" prop="pic">
          <el-input v-model="formData.pic" placeholder="请输入图片地址" />
          <el-image v-if="formData.pic" :src="formData.pic" style="width: 80px; height: 80px; margin-top: 8px;" fit="cover" />
        </el-form-item>
        <el-form-item label="重量(kg)" prop="weight">
          <el-input-number v-model="formData.weight" :precision="3" :min="0" :step="0.001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体积(m³)" prop="volume">
          <el-input-number v-model="formData.volume" :precision="4" :min="0" :step="0.0001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSkuPageListService,
  getSkuDetailService,
  addSkuService,
  updateSkuService,
  updateSkuStatusService,
  deleteSkuService
} from '@/api/admin/productSku.js'
import { getProductListService } from '@/api/admin/product.js'

const tableLoading = ref(false)
const saveLoading = ref(false)
const actionLoading = reactive({})

const productList = ref([])
const skuList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  skuCode: '',
  productId: undefined,
  status: undefined
})

const getSkuList = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      skuCode: queryParams.value.skuCode || null,
      productId: queryParams.value.productId ?? null,
      status: queryParams.value.status ?? null
    }

    const res = await getSkuPageListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      skuList.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      skuList.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('查询SKU列表失败', e)
    ElMessage.error('查询SKU列表失败')
    skuList.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

// 加载商品下拉列表
const loadProductList = async () => {
  try {
    const res = await getProductListService({ pageNum: 1, pageSize: 9999, keyword: '', status: 1 })
    if (res?.code === 200) {
      const pageData = res.data || {}
      productList.value = pageData.records || pageData.list || pageData.rows || []
    } else {
      productList.value = []
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
    productList.value = []
  }
}

const handleSearch = () => {
  queryParams.value.pageNum = 1
  getSkuList()
}

const handleSizeChange = () => {
  queryParams.value.pageNum = 1
  getSkuList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    skuCode: '',
    productId: undefined,
    status: undefined
  }
  getSkuList()
}

// 弹窗表单
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const getDefaultForm = () => ({
  id: null,
  productId: null,
  skuCode: '',
  price: null,
  costPrice: null,
  stock: null,
  pic: '',
  weight: null,
  volume: null,
  status: 1,
  skuAttrList: []
})

const formData = ref(getDefaultForm())

const formRules = ref({
  productId: [{ required: true, message: '请选择所属商品', trigger: 'change' }],
  skuCode: [{ required: true, message: '请输入SKU编码', trigger: 'blur' }],
  price: [{ required: true, message: '请输入销售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
})

// 新增
const handleAddSku = () => {
  isEdit.value = false
  dialogTitle.value = '新增SKU'
  formData.value = getDefaultForm()
  dialogVisible.value = true
}

// 编辑
const handleEditSku = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑SKU'
  try {
    const res = await getSkuDetailService({ id: row.id })
    formData.value = res.data || { ...row }
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取SKU详情失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      if (isEdit.value) {
        await updateSkuService(formData.value)
        ElMessage.success('更新成功')
      } else {
        await addSkuService(formData.value)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getSkuList()
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      saveLoading.value = false
    }
  })
}

const handleStatusChange = async (row) => {
  const oldStatus = row.status === 1 ? 0 : 1
  const loadingKey = row.id + '_status'

  actionLoading[loadingKey] = true
  try {
    await updateSkuStatusService({ id: row.id, status: row.status })
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = oldStatus
    ElMessage.error('状态更新失败')
  } finally {
    actionLoading[loadingKey] = false
  }
}

const handleDeleteSku = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除SKU「${row.skuCode}」？`, '提示', { type: 'warning' })

    const loadingKey = row.id + '_delete'
    actionLoading[loadingKey] = true

    try {
      await deleteSkuService({ id: row.id })
      ElMessage.success('删除成功')
      if (skuList.value.length === 1 && queryParams.value.pageNum > 1) {
        queryParams.value.pageNum--
      }
      getSkuList()
    } finally {
      actionLoading[loadingKey] = false
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败', e)
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  Promise.all([loadProductList(), getSkuList()])
})
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
.query-form {
  margin-bottom: 20px;
}
.sku-info {
  line-height: 1.8;
}
.sku-code {
  font-weight: bold;
}
.sku-product,
.sku-time {
  color: #666;
  font-size: 13px;
}
.attr-item {
  font-size: 13px;
  line-height: 1.6;
}
.attr-name {
  color: #999;
  margin-right: 4px;
}
.attr-value {
  color: #333;
}
</style>