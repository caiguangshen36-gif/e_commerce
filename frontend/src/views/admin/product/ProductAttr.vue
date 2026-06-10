<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商品规格管理</h2>
      <el-button type="primary" @click="openAddDialog">新增规格</el-button>
    </div>

    <el-form inline :model="query" style="margin-bottom: 20px">
      <el-form-item label="规格名称">
        <el-input
          v-model="query.attrName"
          placeholder="请输入规格名称"
          clearable
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="所属分类">
        <el-select
          v-model="query.categoryId"
          placeholder="请选择分类"
          clearable
          style="width: 220px"
        >
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.categoryName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="paginatedList" border stripe v-loading="loading">
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="规格名称" prop="attrName" align="center" />
      <el-table-column label="所属分类" prop="categoryName" align="center" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="val => handleStatusChange(val, row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="260" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button type="info" link @click="lookAttrValue(row)">规格值管理</el-button>
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

    <el-dialog v-model="dialogVisible" title="规格信息" width="550px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="规格名称" prop="attrName">
          <el-input v-model="form.attrName" placeholder="请输入规格名称" />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序">
          <el-input v-model.number="form.sort" placeholder="数字越小越靠前" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getAttributeListService,
  addAttributeService,
  updateAttributeService,
  getAttributeDetailService,
  updateAttributeStatusService,
  deleteAttributeService
} from '@/api/admin/attributes.js'

import { getAllCategoryListService } from '@/api/admin/category.js'
const router = useRouter()

const query = reactive({
  attrName: '',
  categoryId: null,
  status: null
})

const tableList = ref([])
const categoryList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const pageNum = ref(1)
const pageSize = ref(10)
const total = computed(() => tableList.value.length)
const paginatedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableList.value.slice(start, end)
})

const form = ref({
  id: '',
  attrName: '',
  categoryId: null,
  sort: 0,
  status: 1
})

const rules = {
  attrName: [{ required: true, message: '请输入规格名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }]
}

const getCategoryList = async () => {
  const res = await getAllCategoryListService()
  let list = res.data || []
  categoryList.value = list.map(item => ({
    ...item,
    id: String(item.id),
    parentId: item.parentId ? String(item.parentId) : null
  }))
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getAttributeListService(query)
    tableList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.attrName = ''
  query.categoryId = null
  query.status = null
  getList()
}

const openAddDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  form.value = {
    id: '',
    attrName: '',
    categoryId: null,
    sort: 0,
    status: 1
  }
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  formRef.value?.resetFields()
  const res = await getAttributeDetailService({ id: row.id })
  form.value = {
    ...res.data,
    categoryId: res.data.categoryId ? String(res.data.categoryId) : null
  }
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const params = { ...form.value }
      if (isEdit.value) {
        await updateAttributeService(params)
      } else {
        await addAttributeService(params)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      getList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

const handleStatusChange = async (newStatus, row) => {
  try {
    await updateAttributeStatusService({ id: row.id, status: newStatus })
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？')
  try {
    await deleteAttributeService({ id })
    getList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const lookAttrValue = (row) => {
  router.push({
    path: '/admin/product/attrValue',
    query: { attrId: row.id }
  })
}

onMounted(() => {
  getCategoryList()
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
</style>