<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="handleAddAdmin">新增管理员</el-button>
    </div>

    <!-- 管理员列表 -->
    <el-table
      :data="adminList"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" />
      
      <!-- 用户角色列 -->
      <el-table-column prop="roleName" label="当前角色" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.roleName" type="success" effect="plain">{{ row.roleName }}</el-tag>
          <span v-else style="color: #999;">未分配</span>
        </template>
      </el-table-column>

      <!-- 状态开关 -->
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />

      <!-- 操作 -->
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEditAdmin(row)">编辑</el-button>
          <el-button type="success" link @click="openAssignRole(row)">分配角色</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 / 编辑 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form
        :model="formData"
        label-width="100px"
        :rules="formRules"
        ref="formRef"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>

        <!-- 新增时才显示密码 -->
        <el-form-item
          label="密码"
          prop="password"
          v-if="isAdd"
        >
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="450px">
      <el-form label-width="100px">
        <el-form-item label="选择角色">
          <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width:100%">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssignRole">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAdminListService,
  addAdminService,
  updateAdminService,
  updateAdminStatusService
} from '@/api/admin/admin.js'

// 角色相关接口
import {
  getRoleListService,
  getRolesByUserIdService,
  assignRoleService
} from '@/api/admin/role.js'

const loading = ref(false)
const adminList = ref([])
const roleList = ref([]) 

// 新增/编辑
const dialogVisible = ref(false)
const dialogTitle = ref('新增管理员')
const formRef = ref(null)
const isAdd = ref(true)
const formData = ref({
  id: null,
  username: '',
  phone: '',
  password: '',
  status: 1
})

// 分配角色
const roleDialogVisible = ref(false)
const currentUserId = ref(null)
const selectedRoleId = ref(null)

// 校验规则
const formRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

// 初始化加载
onMounted(async () => {
  await getAdminList()
  await getAllRoles()
})

// 获取管理员列表
const getAdminList = async () => {
  loading.value = true
  try {
    const res = await getAdminListService()
    let list = res.data || []
    
    if (list.length > 0) {
      const rolePromises = list.map(async (admin) => {
        try {
          const roleRes = await getRolesByUserIdService({ userId: admin.id })
          if (roleRes.data && roleRes.data.length > 0) {
            admin.roleName = roleRes.data[0].roleName
          } else {
            admin.roleName = ''
          }
        } catch (e) {
          console.warn(`获取管理员 ${admin.id} 角色失败`, e)
          admin.roleName = ''
        }
        return admin
      })
      list = await Promise.allSettled(rolePromises).then(results => 
        results.map(r => r.status === 'fulfilled' ? r.value : r.reason)
      )
    }
    
    adminList.value = list
  } catch (err) {
    ElMessage.error('获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

// 获取所有角色（分配角色用）
const getAllRoles = async () => {
  try {
    const res = await getRoleListService()
    roleList.value = res.data || []
  } catch (e) {
    ElMessage.error('获取角色失败')
  }
}

// 新增
const handleAddAdmin = () => {
  isAdd.value = true
  dialogTitle.value = '新增管理员'
  formData.value = {
    id: null,
    username: '',
    phone: '',
    password: '',
    status: 1
  }
  dialogVisible.value = true
}

// 编辑
const handleEditAdmin = (row) => {
  isAdd.value = false
  dialogTitle.value = '编辑管理员'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 提交保存
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isAdd.value) {
        await addAdminService(formData.value)
        ElMessage.success('新增成功')
      } else {
        await updateAdminService(formData.value)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      getAdminList()
    } catch (err) {
      ElMessage.error('操作失败')
    }
  })
}

// 状态切换
const handleStatusChange = async (row) => {
  // 记录切换前的状态，以便失败时回滚
  const previousStatus = row.status === 1 ? 0 : 1
  
  try {
    await updateAdminStatusService(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (err) {
    // 失败时回滚状态
    row.status = previousStatus
    ElMessage.error('状态更新失败')
  }
}

// 分配角色 
const openAssignRole = async (row) => {
  try {
    currentUserId.value = row.id
    selectedRoleId.value = null

    const res = await getRolesByUserIdService({ userId: row.id })

    if (res.data && res.data.length > 0) {
      selectedRoleId.value = res.data[0].id
    }

    roleDialogVisible.value = true
  } catch (err) {
    console.error(err)
    ElMessage.error('加载角色信息失败')
    roleDialogVisible.value = true
  }
}

// 保存分配角色
const saveAssignRole = async () => {
  if (!selectedRoleId.value) {
    return ElMessage.warning('请选择角色')
  }
  try {
    await assignRoleService(currentUserId.value, selectedRoleId.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    
    getAdminList()
  } catch (err) {
    ElMessage.error('分配失败')
  }
}
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