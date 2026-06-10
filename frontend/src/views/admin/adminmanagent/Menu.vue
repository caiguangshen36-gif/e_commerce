<template>
  <div class="page-container">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="openAdd">新增菜单</el-button>
    </div>

    <!-- 树形菜单表格 -->
    <el-table
      :data="menuList"
      border
      stripe
      row-key="id"
      :tree-props="{ children: 'children', label: 'menuName' }"
      v-loading="loading"
    >
      <el-table-column label="菜单名称" prop="menuName" align="left" />
      <el-table-column label="菜单类型" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'primary' : row.type === 2 ? 'success' : 'danger'">
            {{ row.type === 1 ? '目录' : row.type === 2 ? '菜单' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="路由地址" prop="path" align="center" />
      <el-table-column label="图标" prop="icon" align="center" />
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form
        :model="form"
        label-width="100px"
        ref="formRef"
        :rules="rules"
      >
        <el-form-item label="父级菜单" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择父级菜单" style="width:100%">
            <el-option label="顶级目录" :value="0" />
            <el-option
              v-for="item in menuList"
              :key="item.id"
              :label="item.menuName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="例如：/admin/menu" />
        </el-form-item>

        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="form.icon" placeholder="例如：Menu" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input v-model.number="form.sort" placeholder="数字越小越靠前" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMenuListService,
  addMenuService,
  updateMenuService,
  deleteMenuService
} from '@/api/admin/menu.js'

const loading = ref(false)
const menuList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  parentId: 0,
  menuName: '',
  type: 2,
  path: '',
  icon: '',
  sort: 0
})

const rules = ref({
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '菜单类型不能为空', trigger: 'change' }],
  path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }]
})

// 获取菜单树形列表
const getMenuList = async () => {
  loading.value = true
  try {
    const res = await getMenuListService()
    menuList.value = res.data
  } catch (err) {
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 新增
const openAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  form.value = {
    id: null,
    parentId: 0,
    menuName: '',
    type: 2,
    path: '',
    icon: '',
    sort: 0
  }
  dialogVisible.value = true
}

// 编辑
const openEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  form.value = { ...row }
  dialogVisible.value = true
}

// 提交保存
const submit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await updateMenuService(form.value)
        ElMessage.success('修改成功')
      } else {
        await addMenuService(form.value)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getMenuList()
    } catch (err) {
      ElMessage.error('操作失败')
    }
  })
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该菜单？')
    await deleteMenuService(id)
    ElMessage.success('删除成功')
    getMenuList()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}


onMounted(() => {
  getMenuList()
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
</style>