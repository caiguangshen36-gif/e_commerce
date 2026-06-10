<template>
  <!-- 最外层容器：左右分栏布局 -->
  <div class="login-container">
    <!-- 左侧登录表单区域 -->
    <div class="login-form-wrapper">
      <div class="login-form-card">
        <!-- 登录表单 -->
        <el-form 
          ref="loginFormRef" 
          size="large" 
          autocomplete="off" 
          :model="loginData" 
          :rules="rules"
          class="login-form"
        >
          <el-form-item>
            <h1 class="form-title">管理员登录</h1>
          </el-form-item>

          <el-form-item prop="username">
            <el-input 
              :prefix-icon="UserIcon" 
              placeholder="请输入管理员账号" 
              v-model="loginData.username"
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              name="password" 
              :prefix-icon="LockIcon" 
              type="password" 
              placeholder="请输入密码" 
              v-model="loginData.password"
              show-password
            ></el-input>
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input 
                :prefix-icon="CodeIcon" 
                placeholder="请输入验证码" 
                v-model="loginData.captchaCode"
              ></el-input>
              <!-- 验证码图片，点击刷新 -->
              <img 
                v-if="captchaImg" 
                :src="captchaImg" 
                class="captcha-img" 
                @click="refreshCaptcha" 
                title="点击刷新验证码"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button 
              class="submit-btn" 
              type="primary" 
              block 
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 右侧宣传区域 -->
    <div class="login-banner">
      <div class="banner-content">
        <h1 class="banner-title">电商管理系统</h1>
        <p class="banner-desc">后台管理员使用中心</p>
        <div class="banner-illustration"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Menu } from '@element-plus/icons-vue'
import { useTokenStore } from '@/stores/token.js'
import { getCaptchaService } from '@/api/capthca.js'
import {adminLoginService ,getAdminInfoService} from '@/api/admin/admin.js'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/user.js'
import { useMenuStore } from '@/stores/menu.js'

const userInfoStore = useUserInfoStore()
const tokenStore = useTokenStore()
const menuStore = useMenuStore()
const router = useRouter()

// 表单 
const loginFormRef = ref()

// 验证码相关
const captchaImg = ref('')
const captchaId = ref('')

// 登录表单数据
const loginData = ref({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: ''
})

// 图标
const UserIcon = User
const LockIcon = Lock
const CodeIcon = Menu

// 校验规则
const rules = reactive({
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度 5-20 位', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
})

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const result = await getCaptchaService()
    captchaId.value = result.data.captchaId
    captchaImg.value = result.data.captchaImg
    loginData.value.captchaId = captchaId.value
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

// 登录逻辑
const handleLogin = async () => {
  if (!loginFormRef.value) return

  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const res = await adminLoginService(loginData.value)
      ElMessage.success('登录成功')
      // 1. 保存 token
      tokenStore.setAdminToken(res.data)

      // 2. 保存用户信息
      userInfoStore.setInfo({
        username: res.data.username,
        userId: res.data.userId
      })

      //3. 保存菜单数据 
      menuStore.setMenus(res.data.menus)
      
      // 4. 跳转到后台首页
      router.push('/admin/home')
      
    } catch (err) {
      ElMessage.error(err.msg || '登录失败')
      refreshCaptcha()
    }
  })
}

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
/* 验证码样式 */
.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captcha-img {
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.submit-.btn {
  margin-top: 20px;

}

.login-container {
  display: flex;
  height: 100vh;
}
.login-form-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
}
.login-form-card {
  width: 360px;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.form-title {
  text-align: center;
  margin-bottom: 30px;
}
.submit-btn {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 360px;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}
.switch-link {
  text-align: center;
}
.login-banner {
  flex: 1;
  background-color: #409eff;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}
.banner-content {
  text-align: center;
}
.banner-title {
  font-size: 36px;
  margin-bottom: 20px;
}
</style>