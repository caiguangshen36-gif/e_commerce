<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAddRole">新增角色</el-button>
    </div>

    <!-- 角色列表 -->
    <el-table
      :data="roleList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="roleName" label="角色名称" align="center" />
      <el-table-column prop="description" label="角色描述" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      
      <el-table-column label="操作" width="300" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEditRole(row)">编辑</el-button>
          <el-button type="success" link @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button type="danger" link @click="handleDeleteRole(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 / 编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form
        :model="formData"
        label-width="100px"
        :rules="formRules"
        ref="formRef"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗（树形菜单） -->
    <el-dialog v-model="permissionDialogVisible" title="分配菜单权限" width="500px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :props="{ children: 'children', label: 'menuName' }"
        show-checkbox
        node-key="id"
        :default-expand-all="true"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermission">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRoleListService,
  addRoleService,
  updateRoleService,
  deleteRoleService
} from '@/api/admin/role.js'
import {
  getMenuListService,
  getRoleMenuIdsService,
  assignMenusToRoleService
} from '@/api/admin/menu.js'

const loading = ref(false)
const roleList = ref([])

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isAddMode = ref(true)
const formRef = ref(null)
const formData = ref({
  id: null,
  roleName: '',
  description: ''
})
const formRules = ref({
  roleName: [
    { required: true, message: '角色名称不能为空', trigger: 'blur' }
  ]
})

// 分配权限弹窗
const permissionDialogVisible = ref(false)
const currentRoleId = ref(null)
const menuTree = ref([])
const treeRef = ref(null)

// 加载角色列表
const getRoleList = async () => {
  loading.value = true
  try {
    const res = await getRoleListService()
    roleList.value = res.data || []
  } catch (err) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 新增角色
const handleAddRole = () => {
  isAddMode.value = true
  dialogTitle.value = '新增角色'
  formData.value = { id: null, roleName: '', description: '' }
  dialogVisible.value = true
}

// 编辑角色
const handleEditRole = (row) => {
  isAddMode.value = false
  dialogTitle.value = '编辑角色'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 提交角色
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isAddMode.value) {
        await addRoleService(formData.value)
        ElMessage.success('新增角色成功')
      } else {
        await updateRoleService(formData.value)
        ElMessage.success('修改角色成功')
      }
      dialogVisible.value = false
      getRoleList()
    } catch (err) {
      ElMessage.error('操作失败')
    }
  })
}

// 删除角色 
const handleDeleteRole = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该角色？', '提示')
    await deleteRoleService(id)
    ElMessage.success('删除成功')
    getRoleList()
  } catch (err) {
    ElMessage.info('已取消删除')
  }
}

// 打开分配权限弹窗 
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  try {
    // 获取所有菜单树
    const menuRes = await getMenuListService()
    menuTree.value = menuRes.data || []
    
    // 获取当前角色已有的菜单ID 
    const idsRes = await getRoleMenuIdsService({ roleId: row.id })
    const checkedIds = idsRes.data || []
    
    // 回显勾选
    permissionDialogVisible.value = true
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(checkedIds)
    }, 100)
  } catch (e) {
    ElMessage.error('加载权限失败')
  }
}

// 保存权限分配 
const savePermission = async () => {
  try {
    const checkedKeys = treeRef.value.getCheckedKeys()
    await assignMenusToRoleService(currentRoleId.value, checkedKeys)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  getRoleList()
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
  color: #1f2337;
}
</style>