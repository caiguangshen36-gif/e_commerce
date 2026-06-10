<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>规格值管理</h2>
        <!-- 显示当前规格信息 -->
        <p v-if="attrInfo" class="attr-info">
          当前规格：<strong>{{ attrInfo.attrName }}</strong>
          （所属分类：{{ attrInfo.categoryName || '无' }}）
        </p>
      </div>
      <el-button type="primary" @click="openAddDialog">新增规格值</el-button>
    </div>

    <el-table :data="paginatedList" border stripe v-loading="loading">
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="规格值" prop="attrValue" align="center" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      style="margin-top:20px; text-align:right"
      layout="total, sizes, prev, pager, next, jumper"
    />

    <el-dialog v-model="dialogVisible" title="规格值信息" width="500px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="规格值" prop="attrValue">
          <el-input v-model="form.attrValue" placeholder="请输入规格值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="form.sort" placeholder="数字越小越靠前" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'

import {
  listValuesByAttrService,
  getAttributeValueDetailService,
  addAttributeValueService,
  updateAttributeValueService,
  updateAttributeValueStatusService,
  deleteAttributeValueService,
  getAttributeDetailService
} from '@/api/admin/attributes.js'

const route = useRoute()
const attrId = ref(route.query.attrId)

const valueList = ref([])
const loading = ref(false)
const attrInfo = ref(null)

const pageNum = ref(1)
const pageSize = ref(10)
const total = computed(() => valueList.value.length)
const paginatedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  return valueList.value.slice(start, end)
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  attrId: null,
  attrValue: '',
  sort: 0,
  status: 1
})

const rules = {
  attrValue: [{ required: true, message: '请输入规格值', trigger: 'blur' }]
}

// 获取当前规格详情
const getAttrInfo = async () => {
  if (!attrId.value) return
  try {
    const res = await getAttributeDetailService({ id: attrId.value })
    attrInfo.value = res.data
  } catch (e) {
    ElMessage.error('加载规格信息失败')
  }
}

// 加载列表
const getList = async () => {
  if (!attrId.value) {
    ElMessage.warning('请从规格管理页面进入')
    return
  }
  console.log('规格attrId:', attrId.value)

  loading.value = true
  try {
    const res = await listValuesByAttrService({ attrId: attrId.value })
    valueList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  form.value = {
    id: null,
    attrId: attrId.value,
    attrValue: '',
    sort: 0,
    status: 1
  }
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  const res = await getAttributeValueDetailService({ id: row.id })
  form.value = res.data
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await updateAttributeValueService(form.value)
      } else {
        await addAttributeValueService(form.value)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      getList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    await updateAttributeValueStatusService({
      id: row.id,
      status: row.status
    })
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？')
  try {
    await deleteAttributeValueService({ id })
    ElMessage.success('删除成功')
    getList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  getAttrInfo() 
  getList()
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
  margin-bottom: 20px;
  align-items: center;
}
.attr-info {
  margin: 8px 0 0 0;
  color: #666;
  font-size: 14px;
}
</style>