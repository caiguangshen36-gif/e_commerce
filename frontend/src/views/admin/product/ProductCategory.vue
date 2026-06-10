<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商品分类管理</h2>
      <el-button type="primary" @click="openAddDialog">新增分类</el-button>
    </div>

    <!-- 查询条件表单 -->
    <el-form :model="queryForm" inline @submit.prevent="loadCategoryList" class="query-form">
      <el-form-item label="分类名称">
        <el-input v-model="queryForm.categoryName" placeholder="请输入分类名称" clearable />
      </el-form-item>
      <el-form-item label="上级ID">
        <el-input-number v-model="queryForm.parentId" :min="0" placeholder="顶级为0" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadCategoryList">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 树形表格 -->
    <el-table
      :data="paginatedList"
      border
      stripe
      row-key="id"
      :loading="loading"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="分类名称" prop="categoryName" align="center" />
      <el-table-column label="上级分类ID" prop="parentId" width="120" align="center" />
      <el-table-column label="层级" prop="level" width="80" align="center" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="图标" align="center" width="100">
        <template #default="{ row }">
          <el-icon color="#333" size="18">
            <Folder v-if="row.level === 1" />
            <GoodsFilled v-else />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            :loading="loadingIds.has(row.id)"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button 
            type="danger" 
            link 
            :loading="loadingIds.has(row.id)"
            @click="handleDelete(row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      style="margin-top:20px; text-align:right"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handlePageChange"
      @current-change="handlePageChange"
    />

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" title="分类信息" width="500px">
      <el-form :model="formData" label-width="100px" ref="formRef">
        <el-form-item 
          label="分类名称" 
          prop="categoryName" 
          required
          :rules="[{ required: true, message: '请输入分类名称', trigger: 'blur' }]"
        >
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item 
          label="上级ID" 
          prop="parentId"
          :rules="[{ required: true, message: '请输入上级ID', trigger: 'blur' }]"
        >
          <el-input-number 
            v-model="formData.parentId" 
            :min="0" 
            :max="9999999999"
            placeholder="顶级为0" 
            style="width:100%" 
          />
        </el-form-item>
        <el-form-item 
          label="层级" 
          prop="level"
          :rules="[{ required: true, message: '请输入层级', trigger: 'blur' }]"
        >
          <el-input-number 
            v-model="formData.level" 
            :min="1" 
            :max="3"
            placeholder="1级/2级/3级" 
            style="width:100%" 
          />
        </el-form-item>
        <el-form-item 
          label="排序" 
          prop="sort"
          :rules="[{ required: true, message: '请输入排序', trigger: 'blur' }]"
        >
          <el-input-number 
            v-model="formData.sort" 
            :min="0" 
            :max="9999"
            placeholder="数字越大越靠前" 
            style="width:100%" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitLoading"
          @click="submitForm"
        >确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryListService,
  addCategoryService,
  updateCategoryService,
  updateCategoryStatusService,
  deleteCategoryService
} from '@/api/admin/category.js'

import { GoodsFilled, Folder } from '@element-plus/icons-vue'

// 列表
const categoryList = ref([])
const loading = ref(false)
const loadingIds = ref(new Set())

// 查询表单
const queryForm = ref({
  categoryName: '',
  parentId: null,
  status: null
})

// 弹窗
const dialogVisible = ref(false)
const formRef = ref()
const formData = ref({
  id: '',
  categoryName: '',
  parentId: 0,
  level: 1,
  sort: 0,
  status: 1
})
const isEdit = ref(false)
const submitLoading = ref(false)

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = computed(() => categoryList.value.length)
const paginatedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  return categoryList.value.slice(start, end)
})

const handlePageChange = () => {}

// 树形格式化
const formatTreeData = (list) => {
  return list.map(item => {
    const formatted = { ...item, children: item.children || [] }
    if (formatted.children.length > 0) {
      formatted.children = formatTreeData(formatted.children)
    }
    return formatted
  })
}

// 加载分类列表（带条件查询）
const loadCategoryList = async () => {
  loading.value = true
  try {
    const res = await getCategoryListService(queryForm.value)
    categoryList.value = formatTreeData(res.data || [])
  } catch (e) {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    categoryName: '',
    parentId: null,
    status: null
  }
  loadCategoryList()
}

// 打开新增
const openAddDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  formData.value = { id: '', categoryName: '', parentId: 0, level: 1, sort: 0, status: 1 }
  dialogVisible.value = true
}

// 打开编辑
const openEditDialog = (row) => {
  isEdit.value = true
  formRef.value?.resetFields()
  formData.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
}

// 提交
const submitForm = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateCategoryService(formData.value)
        ElMessage.success('修改成功')
      } else {
        await addCategoryService(formData.value)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadCategoryList()
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 状态切换
const handleStatusChange = async (row) => {
  loadingIds.value.add(row.id)
  try {
    await updateCategoryStatusService(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  } finally {
    loadingIds.value.delete(row.id)
  }
}

// 删除
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？删除后子分类也将被级联删除', '提示')
  loadingIds.value.add(id)
  try {
    await deleteCategoryService(id)
    ElMessage.success('删除成功')
    await loadCategoryList()
  } catch (e) {
    ElMessage.error('删除失败：' + (e.message || '请先删除子分类'))
  } finally {
    loadingIds.value.delete(id)
  }
}

onMounted(() => {
  loadCategoryList()
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
h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
.query-form {
  margin-bottom: 20px;
}
</style>