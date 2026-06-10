<template>
  <div class="page-container">
    <div class="page-header">
      <h2>修改密码</h2>
    </div>

    <el-form
      :model="form"
      label-width="100px"
      style="max-width: 500px; margin-top: 20px;"
    >
      <el-form-item label="原密码" required>
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入当前密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" required>
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" required>
        <el-input
          v-model="form.repPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitUpdate">确认修改</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePasswordService } from '@/api/admin/admin.js'

const form = ref({
  oldPassword: '',
  newPassword: '',
  repPassword: ''
})

// 提交修改
const submitUpdate = async () => {
  const { oldPassword, newPassword, repPassword } = form.value

  if (!oldPassword || !newPassword || !repPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (newPassword !== repPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  try {
    await updatePasswordService(oldPassword, newPassword, repPassword)
    ElMessage.success('密码修改成功，请重新登录')
    reset()
  } catch (err) {
    ElMessage.error(err.msg || '密码修改失败')
  }
}

// 重置
const reset = () => {
  form.value = {
    oldPassword: '',
    newPassword: '',
    repPassword: ''
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
  margin-bottom: 10px;
}
h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
</style>