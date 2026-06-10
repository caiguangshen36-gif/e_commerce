<template>
  <div class="banner-box">
    <el-carousel
      height="420px"
      arrow="hover"
      indicator-position="none"
      :interval="4000"
      class="custom-carousel"
    >
      <el-carousel-item v-for="item in carouselList" :key="item.id">
        <div class="slide-wrapper">
          <img :src="item.pic" alt="轮播图" class="slide-img" />
          <div class="slide-overlay" />
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 自定义指示点 -->
    <div class="custom-dots">
      <span
        v-for="(item, index) in carouselList"
        :key="item.id"
        class="dot"
        :class="{ active: currentIndex === index }"
        @click="currentIndex = index"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { enableMarketingListService } from '@/api/user/marketing.js'

const carouselList = ref([])
const currentIndex = ref(0)

onMounted(() => {
  getEnableCarousel()
})

const getEnableCarousel = async () => {
  try {
    const res = await enableMarketingListService()
    console.log("获取启用的轮播列表：",res)
    carouselList.value = res.data
  } catch (err) {
    ElMessage.error('轮播图加载失败')
    console.error(err)
  }
}
</script>

<style scoped>

.banner-box {
  width: 100%;
  max-width: 100%;     
  margin: 0 0 32px;    
  border-radius: 12px;
}

.custom-carousel {
  border-radius: 8px;
  overflow: hidden;
}

.custom-carousel :deep(.el-carousel__arrow) {
  width: 36px;
  height: 50px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  transition: background 0.25s ease, transform 0.2s ease;
}

.custom-carousel :deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.45);
  transform: scale(1.08);
}

.custom-carousel :deep(.el-carousel__arrow--left) {
  left: 16px; 
}

.custom-carousel :deep(.el-carousel__arrow--right) {
  right: 16px;
}

.slide-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.slide-img {
  width: 100%;
  height: 420px;
  object-fit: cover;
  display: block;
  transition: transform 0.6s ease;
}

.slide-wrapper:hover .slide-img {
  transform: scale(1.02);
}

.slide-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.12), transparent);
  pointer-events: none;
}

.custom-dots {
  position: absolute;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 10;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  transition: width 0.3s ease, background 0.3s ease;
}

.dot.active {
  width: 18px;
  background: #ffffff;
}
</style>