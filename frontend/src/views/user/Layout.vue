<template>
  <div class="layout-page">
    <el-header class="header">
      <el-container class="nav-box">
        <span class="logo-icon">🛍</span>
        <span class="logo-text">电商商城</span>

        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索商品"
            clearable
            @keyup.enter="handleSearch"
            @click="toSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/category">
            <el-icon><Grid /></el-icon>
            <template #title>商品分类</template>
          </el-menu-item>
          <el-menu-item index="/cart">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>购物车</template>
          </el-menu-item>
          <el-menu-item index="/userinfo">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          <el-menu-item index="/help">
            <el-icon><QuestionFilled /></el-icon>
            <template #title>帮助中心</template>
          </el-menu-item>
        </el-menu>

        <div class="user-info">
          <template v-if="isLogin">
            <el-dropdown trigger="click">
              <span class="user-name">
                <el-avatar :size="30" :src="userAvatar" />
                {{ username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="gotoNotice">消息中心</el-dropdown-item>
                  <el-dropdown-item @click="gotoCollect">我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="gotoFoot">我的浏览</el-dropdown-item>
                  <el-dropdown-item @click="gotoOrder">我的订单</el-dropdown-item>
                  <el-dropdown-item @click="gotoHelp">帮助中心</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" link @click="goLogin">登录 / 注册</el-button>
          </template>
        </div>
      </el-container>
    </el-header>

    <transition mode="out-in" name="page-fade">
      <el-main class="main-content" :key="$route.fullPath">
        <router-view />
      </el-main>
    </transition>

    <!-- 悬浮入口 -->
    <div class="chat-fab" @click="openAiChat">
      <div class="fab-inner">
        <el-icon :size="22"><ChatDotRound /></el-icon>
      </div>
      <span class="fab-badge">小购</span>
    </div>

    <!-- 助手抽屉 -->
    <el-drawer
      v-model="showAiChat"
      direction="rtl"
      size="400px"
      :with-header="false"
      :close-on-click-modal="true"
    >
      <div class="chat-panel">

        <!-- 顶栏 -->
        <div class="panel-header">
          <div class="header-left">
            <div class="bot-avatar">
              <el-icon :size="16"><ChatDotRound /></el-icon>
            </div>
            <div>
              <p class="bot-name">小购助手</p>
              <p class="bot-status">
                <span class="status-dot"></span>在线服务中
              </p>
            </div>
          </div>
          <button class="close-icon" @click="showAiChat = false" aria-label="关闭">
            <el-icon :size="16"><Close /></el-icon>
          </button>
        </div>

        <!-- 模式切换 -->
        <div class="mode-bar">
          <button
            :class="['mode-pill', mode === 'cs' ? 'active' : '']"
            @click="switchMode('cs')"
          >
            <el-icon><Service /></el-icon>
            客服咨询
          </button>
          <button
            :class="['mode-pill', mode === 'guide' ? 'active' : '']"
            @click="switchMode('guide')"
          >
            <el-icon><MagicStick /></el-icon>
            购物推荐
          </button>
        </div>

        <!-- 对话区 -->
        <div class="msg-feed" ref="messageListRef">

          <!-- 欢迎卡片 -->
          <div class="welcome-card">
            <p class="welcome-text">{{ welcomeMessage }}</p>
            <p class="welcome-sub">{{ modeHint }}</p>
          </div>

          <!-- 快捷问题（首次展示） -->
          <div class="quick-area" v-if="messages.length === 0 && !loading">
            <p class="quick-title">常见问题</p>
            <div class="quick-grid">
              <button
                v-for="(q, i) in quickQuestions"
                :key="i"
                class="quick-chip"
                @click="quickSend(q)"
              >
                {{ q }}
              </button>
            </div>
          </div>

          <!-- 对话记录 -->
          <template v-for="(msg, index) in messages" :key="index">
            <!-- 用户消息 -->
            <div class="row row-user">
              <el-avatar :size="28" :src="userAvatar" class="uavatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="bubble bubble-user">{{ msg.user }}</div>
            </div>

            <!-- 客服回复 -->
            <div class="row row-bot" v-if="msg.csReply !== undefined">
              <div class="bot-dot-avatar">
                <el-icon :size="14"><ChatDotRound /></el-icon>
              </div>
              <div class="bubble bubble-bot">{{ msg.csReply }}</div>
            </div>

            <!-- 导购回复 + 商品推荐 -->
            <div class="row row-bot" v-if="msg.guideReply !== undefined">
              <div class="bot-dot-avatar">
                <el-icon :size="14"><ChatDotRound /></el-icon>
              </div>
              <div class="bubble bubble-bot bubble-guide">
                <p class="guide-reply-text">{{ msg.guideReply }}</p>

                <!-- 商品推荐卡片 -->
                <div v-if="msg.products && msg.products.length > 0" class="product-list">
                  <p class="product-list-label">
                    <el-icon><Star /></el-icon>
                    为您挑选
                  </p>
                  <div
                    v-for="(product, pi) in msg.products"
                    :key="pi"
                    class="product-card"
                    @click="handleProductClick(product)"
                  >
                    <div class="product-img">
                      <el-image
                        :src="product.pic"
                        fit="cover"
                        :alt="product.productName"
                        lazy
                      >
                        <template #error>
                          <div class="img-placeholder"><el-icon><Picture /></el-icon></div>
                        </template>
                      </el-image>
                    </div>
                    <div class="product-detail">
                      <p class="product-name">{{ product.productName }}</p>
                      <div class="product-price-row">
                        <span class="price-now">¥{{ formatPrice(product.price) }}</span>
                        <span v-if="product.originalPrice" class="price-orig">¥{{ formatPrice(product.originalPrice) }}</span>
                      </div>
                      <p v-if="product.matchReason" class="product-reason">{{ product.matchReason }}</p>
                    </div>
                    <el-icon class="product-arrow"><ArrowRight /></el-icon>
                  </div>
                </div>

                <!-- 无商品提示 -->
                <div v-else-if="msg.products && msg.products.length === 0 && msg.guideReply" class="empty-result">
                  <el-icon><InfoFilled /></el-icon>
                  暂未找到相关商品，换个关键词试试吧
                </div>
              </div>
            </div>
          </template>

          <!-- 加载动画 -->
          <div class="row row-bot" v-if="loading">
            <div class="bot-dot-avatar">
              <el-icon :size="14"><ChatDotRound /></el-icon>
            </div>
            <div class="bubble bubble-bot typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-zone">
          <el-input
            v-model="question"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            :placeholder="inputPlaceholder"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <button
            class="send-btn"
            :class="{ active: question.trim() && !loading }"
            :disabled="!question.trim() || loading"
            @click="sendMessage"
          >
            <el-icon v-if="!loading" :size="18"><Promotion /></el-icon>
            <el-icon v-else :size="16" class="spin"><Loading /></el-icon>
          </button>
        </div>
        <p class="input-hint">Enter 发送 · Shift+Enter 换行</p>

      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  Search, House, Grid, ShoppingCart, User,
  QuestionFilled, ChatDotRound, Close,
  Service, MagicStick, Star, Picture,
  ArrowRight, InfoFilled, Promotion, Loading
} from '@element-plus/icons-vue'

import { useTokenStore } from '@/stores/token.js'
import { getUserInfoService } from '@/api/user/user.js'
import { aiCustomerService, aiShopGuide } from '@/api/user/ai'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()

// ---- 搜索 ----
const keyword = ref('')
const toSearch = () => router.push('/toSearch')
const handleSearch = () => router.push('/toSearch')

// ---- 导航 ----
const activeMenu = computed(() => route.path)
const handleMenuSelect = (index) => router.push(index)

// ---- 用户信息 ----
const isLogin = ref(false)
const username = ref('')
const userAvatar = ref('')

const getUserInfo = async () => {
  try {
    const res = await getUserInfoService()
    username.value = res.data.username
    userAvatar.value = res.data.avatar || ''
    isLogin.value = true
  } catch {
    isLogin.value = false
  }
}

const goLogin = () => router.push('/login')
const gotoCollect = () => router.push('/userinfo/collect')
const gotoFoot = () => router.push('/userinfo/foot')
const gotoOrder = () => router.push('/userinfo/order')
const gotoHelp = () => router.push('/help')
const gotoNotice = () => router.push('/userinfo/notice')

const handleLogout = () => {
  tokenStore.removeToken()
  ElMessage.success('已退出登录')
  isLogin.value = false
  username.value = ''
  router.push('/login')
}

// ---- 助手对话状态 ----
const showAiChat = ref(false)
const mode = ref('guide')
const question = ref('')
const messages = ref([])
const loading = ref(false)
const messageListRef = ref(null)

// ---- 模式配置 ----
const modeConfig = {
  cs: {
    welcome: '您好，有什么可以帮您？订单、物流、退换货、售后问题都可以问我。',
    hint: '订单查询 · 物流跟踪 · 退换货 · 售后服务',
    placeholder: '输入问题，例如：我的订单什么时候发货？',
    quickQuestions: ['如何申请退货？', '怎么查看订单状态？']
  },
  guide: {
    welcome: '告诉我您的需求或预算，帮您找到最合适的商品。',
    hint: '描述需求 · 智能匹配 · 个性推荐',
    placeholder: '例如：想买跑步鞋，预算 300 以内…',
    quickQuestions: ['热销手机推荐', '200 元以内的礼物', '夏季女装推荐', '学生党数码好物']
  }
}

const welcomeMessage = computed(() => modeConfig[mode.value].welcome)
const modeHint = computed(() => modeConfig[mode.value].hint)
const inputPlaceholder = computed(() => modeConfig[mode.value].placeholder)
const quickQuestions = computed(() => modeConfig[mode.value].quickQuestions)

const switchMode = (m) => {
  if (mode.value === m) return
  mode.value = m
  messages.value = []
}

const openAiChat = () => {
  showAiChat.value = true
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  const num = typeof price === 'string' ? parseFloat(price) : price
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

// ---- 发送消息 ----
const sendMessage = async () => {
  const msg = question.value.trim()
  if (!msg || loading.value) return

  question.value = ''
  loading.value = true

  const entry = { user: msg }
  messages.value.push(entry)
  scrollToBottom()

  try {
    if (mode.value === 'cs') {
      const res = await aiCustomerService({ message: msg })
      entry.csReply = res?.data || '抱歉，暂时无法回答，请联系人工客服。'
    } else {
      const userId = tokenStore.userId ? String(tokenStore.userId) : '0'
      const res = await aiShopGuide({ userId, message: msg })
      console.log('导购接口响应：', res)

      const data = res?.data || {}
      entry.guideReply = data.replyMessage || '抱歉，暂未找到合适的商品，请换个关键词试试。'
      entry.products = Array.isArray(data.products)
        ? data.products.map(p => ({ ...p, price: p.price, originalPrice: p.originalPrice, pic: p.pic || '' }))
        : []
    }
  } catch (err) {
    console.error('接口异常：', err)
    if (mode.value === 'cs') {
      entry.csReply = '客服服务暂时不可用，请稍后再试或拨打客服热线。'
    } else {
      entry.guideReply = '推荐服务暂时不可用，请稍后再试。'
      entry.products = []
    }
    ElMessage.error(mode.value === 'cs' ? '客服服务异常' : '推荐服务异常')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const quickSend = (q) => {
  question.value = q
  sendMessage()
}

const handleProductClick = (product) => {
  if (product.id) {
    router.push({ path: '/product/detail', query: { id: product.id } })
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  if (tokenStore.token) {
    getUserInfo()
  }
})
</script>

<style scoped>
/* ==================== 整体布局 ==================== */
.layout-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* ==================== 顶部导航 ==================== */
.header {
  background-color: #fff;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 99;
  height: 70px !important;
  line-height: 70px;
  padding: 0;
}

.nav-box {
  display: flex;
  align-items: center;
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  gap: 16px;
}

.logo-icon {
  font-size: 26px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 20px;
  font-weight: 800;
  color: #c0392b;
  letter-spacing: 1px;
  flex-shrink: 0;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 24px;
}

.nav-menu {
  flex: 0 0 auto;
  border-bottom: none;
  background: transparent;
}

.nav-menu :deep(.el-menu-item) {
  height: 70px;
  line-height: 70px;
  border-bottom: 2px solid transparent;
  font-size: 14px;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #c0392b;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #c0392b;
  border-bottom-color: #c0392b;
}

.user-info {
  margin-left: 16px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

/* ==================== 主内容 ==================== */
.main-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 40px;
  min-height: calc(100vh - 70px);
  box-sizing: border-box;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.1s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* ==================== 悬浮按钮 ==================== */
.chat-fab {
  position: fixed;
  right: 28px;
  bottom: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  z-index: 999;
  user-select: none;
}

.fab-inner {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #fff;
  border: 1.5px solid #e8eaf0;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.chat-fab:hover .fab-inner {
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.2), 0 2px 6px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.fab-badge {
  font-size: 11px;
  font-weight: 500;
  color: #888;
  background: #fff;
  border: 1px solid #eee;
  padding: 2px 8px;
  border-radius: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.07);
  white-space: nowrap;
  letter-spacing: 0.3px;
}

/* ==================== 抽屉面板 ==================== */
.chat-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f6f7f9;
  overflow: hidden;
}

/* ---- 顶栏 ---- */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: #fff;
  border-bottom: 1px solid #f0f1f3;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bot-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #eef5ff;
  border: 1.5px solid #d8eaff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  flex-shrink: 0;
}

.bot-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 2px;
  line-height: 1;
}

.bot-status {
  font-size: 11px;
  color: #8e9299;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
  flex-shrink: 0;
}

.close-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #b0b5bd;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  padding: 0;
}

.close-icon:hover {
  background: #f2f3f5;
  color: #555;
}

/* ---- 模式切换 ---- */
.mode-bar {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f1f3;
  flex-shrink: 0;
}

.mode-pill {
  flex: 1;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 12.5px;
  border-radius: 8px;
  border: 1px solid #e8eaed;
  background: #f8f9fa;
  color: #606266;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: inherit;
  letter-spacing: 0.2px;
}

.mode-pill:hover {
  border-color: #c5d8f8;
  color: #409eff;
  background: #f0f7ff;
}

.mode-pill.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
  font-weight: 500;
}

/* ---- 对话区 ---- */
.msg-feed {
  flex: 1;
  overflow-y: auto;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scroll-behavior: smooth;
}

.msg-feed::-webkit-scrollbar {
  width: 3px;
}

.msg-feed::-webkit-scrollbar-track {
  background: transparent;
}

.msg-feed::-webkit-scrollbar-thumb {
  background: #dde0e6;
  border-radius: 2px;
}

/* ---- 欢迎卡片 ---- */
.welcome-card {
  background: #fff;
  border: 1px solid #eef0f3;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 4px;
}

.welcome-text {
  font-size: 13.5px;
  color: #303133;
  margin: 0 0 6px;
  line-height: 1.65;
}

.welcome-sub {
  font-size: 11.5px;
  color: #b0b5c0;
  margin: 0;
  line-height: 1.4;
}

/* ---- 快捷问题 ---- */
.quick-area {
  margin-bottom: 4px;
}

.quick-title {
  font-size: 11px;
  color: #b5bac3;
  margin: 0 0 8px;
  letter-spacing: 0.3px;
}

.quick-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-chip {
  font-size: 12px;
  color: #555;
  background: #fff;
  border: 1px solid #e6e8ed;
  border-radius: 6px;
  padding: 5px 11px;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
  white-space: nowrap;
  line-height: 1.3;
}

.quick-chip:hover {
  background: #f0f7ff;
  border-color: #b8d5f9;
  color: #409eff;
}

/* ---- 消息行 ---- */
.row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  animation: msgIn 0.22s ease-out;
}

@keyframes msgIn {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

.row-user {
  flex-direction: row-reverse;
}

.uavatar {
  flex-shrink: 0;
  margin-bottom: 2px;
}

/* ---- 机器人小头像 ---- */
.bot-dot-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eef5ff;
  border: 1px solid #dce8fb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  flex-shrink: 0;
  margin-bottom: 2px;
}

/* ---- 气泡 ---- */
.bubble {
  max-width: 268px;
  padding: 10px 13px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.65;
  word-break: break-word;
}

.bubble-bot {
  background: #fff;
  color: #2c2f36;
  border-radius: 4px 14px 14px 14px;
  border: 1px solid #eef0f3;
}

.bubble-user {
  background: #409eff;
  color: #fff;
  border-radius: 14px 4px 14px 14px;
}

/* ---- 打字动画 ---- */
.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  min-width: 52px;
}

.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #c8ccd3;
  animation: blink 1.3s infinite both;
}

.dot:nth-child(2) { animation-delay: 0.18s; }
.dot:nth-child(3) { animation-delay: 0.36s; }

@keyframes blink {
  0%, 80%, 100% { transform: scale(0.65); opacity: 0.4; }
  40%            { transform: scale(1);    opacity: 1; }
}

/* ---- 导购气泡 ---- */
.bubble-guide {
  padding: 12px 13px;
  max-width: 296px;
}

.guide-reply-text {
  margin: 0 0 2px;
  font-size: 13px;
  line-height: 1.65;
  color: #2c2f36;
}

/* ---- 商品列表 ---- */
.product-list {
  margin-top: 10px;
  border-top: 1px solid #f0f1f4;
  padding-top: 10px;
}

.product-list-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  color: #999;
  margin: 0 0 8px;
}

.product-list-label .el-icon {
  color: #f0a030;
  font-size: 12px;
}

.product-card {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 10px;
  background: #f8f9fb;
  border: 1px solid #ebebee;
  border-radius: 10px;
  margin-bottom: 7px;
  cursor: pointer;
  transition: border-color 0.18s, background 0.18s;
  position: relative;
}

.product-card:last-child {
  margin-bottom: 0;
}

.product-card:hover {
  border-color: #c0d9f9;
  background: #f0f7ff;
}

.product-img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
}

.product-img :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 20px;
}

.product-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.product-name {
  font-size: 12.5px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.price-now {
  font-size: 13.5px;
  font-weight: 600;
  color: #e84040;
}

.price-orig {
  font-size: 11px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-reason {
  font-size: 11px;
  color: #aaa;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-arrow {
  color: #d0d3da;
  font-size: 13px;
  flex-shrink: 0;
  transition: color 0.18s;
}

.product-card:hover .product-arrow {
  color: #409eff;
}

/* ---- 空结果 ---- */
.empty-result {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #aaa;
  margin-top: 10px;
  padding: 8px 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.empty-result .el-icon {
  font-size: 13px;
  color: #ccc;
}

/* ---- 输入区 ---- */
.input-zone {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 14px 8px;
  background: #fff;
  border-top: 1px solid #f0f1f3;
  flex-shrink: 0;
}

.input-zone :deep(.el-textarea__inner) {
  border-radius: 10px;
  border-color: #e6e8ed;
  font-size: 13px;
  padding: 8px 12px;
  min-height: 38px !important;
  resize: none;
  line-height: 1.5;
  font-family: inherit;
  background: #f8f9fb;
  transition: border-color 0.18s, background 0.18s;
}

.input-zone :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  background: #fff;
}

.send-btn {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid #e6e8ed;
  background: #f2f3f5;
  color: #c0c4cc;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: not-allowed;
  flex-shrink: 0;
  transition: all 0.18s ease;
  padding: 0;
}

.send-btn.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
  cursor: pointer;
}

.send-btn.active:hover {
  background: #337ecc;
  border-color: #337ecc;
}

.send-btn.active:active {
  transform: scale(0.95);
}

.spin {
  animation: rotate 0.8s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.input-hint {
  font-size: 11px;
  color: #c8ccd3;
  text-align: center;
  padding: 0 14px 10px;
  background: #fff;
  flex-shrink: 0;
  margin: 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .nav-box {
    padding: 0 12px;
    gap: 8px;
  }

  .search-box {
    margin: 0 8px;
    max-width: 180px;
  }

  .main-content {
    padding: 16px 12px;
  }

  .chat-fab {
    right: 16px;
    bottom: 60px;
  }
}
</style>