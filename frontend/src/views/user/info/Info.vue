<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-card">

        <!-- 头像区 -->
        <div class="avatar-section">
          <div class="avatar-wrap">
            <el-avatar :size="88" :src="userInfo.avatar" :key="userInfo.avatar" />
          </div>
          <div class="avatar-meta">
            <p class="avatar-name">{{ userInfo.username || '用户名' }}</p>
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              :http-request="uploadAvatar"
            >
              <span class="upload-link">更换头像</span>
            </el-upload>
          </div>
        </div>

        <div class="divider" />

        <!-- 表单 -->
        <el-form
          :model="userInfo"
          :rules="rules"
          ref="formRef"
          label-position="top"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled>
              <template #prefix>
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none" style="color:#bbb">
                  <circle cx="7.5" cy="5" r="3" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M1.5 13.5c0-3.314 2.686-5 6-5s6 1.686 6 5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userInfo.phone" placeholder="请输入手机号">
              <template #prefix>
                <svg width="15" height="15" viewBox="0 0 15 15" fill="none" style="color:#bbb">
                  <rect x="3.5" y="1.5" width="8" height="12" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
                  <circle cx="7.5" cy="11" r="0.75" fill="currentColor"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <!-- 余额 -->
          <el-form-item label="账户余额">
            <div class="balance-card">
              <div class="balance-left">
                <span class="balance-label">可用余额</span>
                <span class="balance-amount">¥{{ Number(userInfo.balance || 0).toFixed(2) }}</span>
              </div>
              <button class="recharge-btn" type="button" @click="openRechargeDialog">充值</button>
            </div>
          </el-form-item>

          <el-form-item>
            <button class="save-btn" type="button" @click="save">保存修改</button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="账户充值" width="380px" align-center>
      <div class="dialog-body">
        <p class="dialog-desc">请输入要充值的金额</p>
        <el-input
          v-model="rechargeForm.amount"
          type="number"
          placeholder="0.00"
          size="large"
        >
          <template #prefix><span style="color:#aaa;font-size:15px">¥</span></template>
        </el-input>
        <div class="quick-amounts">
          <span
            v-for="n in [50, 100, 200, 500]"
            :key="n"
            class="quick-tag"
            :class="{ active: rechargeForm.amount == n }"
            @click="rechargeForm.amount = n"
          >¥{{ n }}</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="rechargeDialogVisible = false">取消</button>
          <button class="btn-confirm" @click="handleRecharge">确认充值</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  updateUserInfoService,
  uploadAvatarService,
  getUserInfoService,
  updateUserBalanceService
} from '@/api/user/user.js'
import { useUserInfoStore } from '@/stores/user.js'

const userInfoStore = useUserInfoStore()
const formRef = ref(null)

const userInfo = ref({
  username: '',
  phone: '',
  avatar: '',
  balance: 0
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
  ]
}

const rechargeDialogVisible = ref(false)
const rechargeForm = ref({ amount: '' })

onMounted(async () => {
  try {
    const res = await getUserInfoService()
    console.log(res.data)
    userInfoStore.setInfo(res.data)
    syncUserInfo()
    console.log('获取用户信息成功：', res.data)
  } catch (err) {}
})

watch(
  () => userInfoStore.info,
  (newVal) => {
    if (newVal && !userInfo.value.avatar) {
      syncUserInfo()
    }
  },
  { deep: true }
)

function syncUserInfo() {
  if (userInfoStore.info) {
    userInfo.value = {
      ...userInfoStore.info,
      avatar: userInfo.value.avatar || userInfoStore.info.avatar
    }
  }
}

const save = async () => {
  await formRef.value.validate()
  await updateUserInfoService(userInfo.value)
  userInfoStore.setInfo({ ...userInfo.value })
  ElMessage.success('保存成功')
}

const uploadAvatar = async (params) => {
  const fd = new FormData()
  fd.append('file', params.file)
  try {
    const res = await uploadAvatarService(fd)
    console.log('上传接口返回：', res)
    userInfo.value.avatar = res.data
    ElMessage.success('头像上传成功！点击保存生效')
  } catch (err) {
    ElMessage.error('头像上传失败')
  }
}

const openRechargeDialog = () => {
  rechargeForm.value.amount = ''
  rechargeDialogVisible.value = true
}

const handleRecharge = async () => {
  const amount = parseFloat(rechargeForm.value.amount)

  if (!amount || amount <= 0) {
    return ElMessage.error('请输入正确的充值金额')
  }

  try {
    const currentBalance = parseFloat(userInfo.value.balance || 0)
    const newBalance = currentBalance + amount
    
    await updateUserBalanceService({balance: newBalance})
    const res = await getUserInfoService()
    userInfoStore.setInfo(res.data)
    syncUserInfo()

    rechargeDialogVisible.value = false
    ElMessage.success('充值成功')
  } catch (err) {
    console.error(err)
    ElMessage.error('充值失败')
  }
}
</script>

<style scoped>
.profile-page {
  width: 100%;
  
}

.container {
  /* width: 100%; */
  max-width: 520px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 卡片容器 */
.profile-card {
  max-width: 560px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 40px;
  border: 0.5px solid #ebebeb;
}

/* 头像区 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 28px;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar-wrap :deep(.el-avatar) {
  border: 2px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.avatar-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.01em;
}

.upload-link {
  font-size: 13px;
  color: #ab5026;
  cursor: pointer;
  transition: opacity 0.15s;
}

.upload-link:hover {
  opacity: 0.75;
}

/* 分割线 */
.divider {
  height: 0.5px;
  background: #f0f0f0;
  margin-bottom: 28px;
}

/* 表单 */
.profile-form {
  width: 100%;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #888;
  font-weight: 400;
  padding-bottom: 6px;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 0.5px #e0e0e0;
  transition: box-shadow 0.2s;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c0c0;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #ab5026 !important;
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #fafafa;
  box-shadow: 0 0 0 0.5px #ebebeb;
}

/* 余额卡片 */
.balance-card {
  width: 100%;
  background: #f7f7f5;
  border: 0.5px solid #e8e8e6;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.balance-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-label {
  font-size: 12px;
  color: #aaa;
}

.balance-amount {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}

.recharge-btn {
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 7px;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.recharge-btn:hover {
  background: #333;
}

/* 保存按钮 */
.save-btn {
  width: 100%;
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  letter-spacing: 0.02em;
  transition: background 0.15s;
  margin-top: 4px;
}

.save-btn:hover {
  background: #333;
}

/* 充值弹窗 */
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0 8px;
}

.dialog-desc {
  font-size: 13px;
  color: #888;
  margin: 0;
}

.quick-amounts {
  display: flex;
  gap: 8px;
}

.quick-tag {
  flex: 1;
  text-align: center;
  padding: 7px 0;
  border: 0.5px solid #e0e0e0;
  border-radius: 7px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.quick-tag:hover {
  border-color: #ab5026;
  color: #ab5026;
}

.quick-tag.active {
  background: #ab5026;
  border-color: #ab5026;
  color: #fff;
}

/* 弹窗底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  background: none;
  border: 0.5px solid #e0e0e0;
  border-radius: 7px;
  padding: 8px 18px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}

.btn-cancel:hover {
  border-color: #bbb;
  color: #333;
}

.btn-confirm {
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 7px;
  padding: 8px 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-confirm:hover {
  background: #333;
}
</style>

<!-- <style scoped>
/* ── 页面 ── */
.profile-page {
  min-height: calc(100vh - 70px);
  background: #f4f4f2;
  padding: 36px 0 60px;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.container {
  max-width: 520px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e4;
  padding: 36px 40px 32px;
}

/* ── 头像区 ── */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 28px;
}

.avatar-wrap :deep(.el-avatar) {
  border: 2px solid #f0f0ee;
  flex-shrink: 0;
}

.avatar-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-name {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.2px;
}

.upload-link {
  font-size: 13px;
  color: #086feb;
  cursor: pointer;
  transition: color 0.15s;
}

.upload-link:hover { color: #555; }

.divider {
  height: 1px;
  background: #f0f0ee;
  margin-bottom: 28px;
}

/* ── 表单 ── */
.profile-form {
  width: 100%;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #888;
  font-weight: 400;
  line-height: 1;
  margin-bottom: 8px !important;
  padding: 0 !important;
  height: auto !important;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #e0e0dc;
  box-shadow: none !important;
  padding: 0 12px;
  transition: border-color 0.15s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c0bc;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #1a1a1a !important;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #fafaf8;
  border-color: #ebebeb;
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: #aaa;
  cursor: not-allowed;
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #1a1a1a;
  height: 38px;
  line-height: 38px;
}

/* ── 余额卡片 ── */
.balance-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  background: #fafaf8;
  border: 1px solid #ebebeb;
  border-radius: 8px;
  padding: 14px 18px;
  box-sizing: border-box;
}

.balance-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-label {
  font-size: 12px;
  color: #aaa;
}

.balance-amount {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.recharge-btn {
  background: #1a1a1a;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 5px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
  flex-shrink: 0;
}

.recharge-btn:hover { opacity: 0.8; }

/* ── 保存按钮 ── */
.save-btn {
  width: 100%;
  background: #1a1a1a;
  color: #fff;
  border: none;
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
  letter-spacing: 0.3px;
}

.save-btn:hover { opacity: 0.82; }
.save-btn:active { opacity: 0.68; }

/* ── 充值弹窗 ── */
:deep(.el-dialog) {
  border-radius: 12px;
  padding: 0;
}

:deep(.el-dialog__header) {
  padding: 24px 28px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

:deep(.el-dialog__body) {
  padding: 16px 28px 8px;
}

:deep(.el-dialog__footer) {
  padding: 8px 28px 24px;
}


.dialog-desc {
  font-size: 13px;
  color: #aaa;
  margin: 0 0 12px;
}

:deep(.el-input--large .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-input--large .el-input__inner) {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.quick-amounts {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.quick-tag {
  flex: 1;
  text-align: center;
  padding: 7px 0;
  border-radius: 5px;
  font-size: 13px;
  color: #555;
  background: #f5f5f2;
  border: 1px solid #ebebeb;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.quick-tag:hover {
  border-color: #1a1a1a;
  color: #1a1a1a;
}

.quick-tag.active {
  background: #1a1a1a;
  color: #fff;
  border-color: #1a1a1a;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-cancel {
  background: #f5f5f2;
  color: #555;
  border: none;
  padding: 10px 22px;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-cancel:hover { background: #ebebeb; }

.btn-confirm {
  background: #1a1a1a;
  color: #fff;
  border: none;
  padding: 10px 22px;
  border-radius: 5px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
}

.btn-confirm:hover { opacity: 0.82; }
</style> -->