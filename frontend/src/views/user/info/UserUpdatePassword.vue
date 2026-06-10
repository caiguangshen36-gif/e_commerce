<template>
  <div class="page-container">
    <div class="page-header">
      <h2>修改密码</h2>
    </div>

    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      label-width="100px"
      style="max-width: 500px; margin-top: 20px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入当前密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="repPassword">
        <el-input
          v-model="form.repPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submit">确认修改</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUserPasswordService } from '@/api/user/user.js'

const formRef = ref(null)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  repPassword: ''
})

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 5, max: 20, message: '长度在 5 到 20 个字符', trigger: 'blur' }
  ],
  repPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const submit = async () => {
  try {
    await formRef.value.validate()
    await updateUserPasswordService({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      repPassword: form.repPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    reset()
  } catch (err) {
    ElMessage.error(err?.response?.data?.msg || '密码修改失败')
  }
}

const reset = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.repPassword = ''
  formRef.value?.clearValidate()
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