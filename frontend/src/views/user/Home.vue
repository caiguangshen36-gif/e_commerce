<template>
   <div class ="home-page">
    <Banner />
    <HotProduct />
    <Recommend />
   </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Banner from './home/Carousel.vue'
import HotProduct from './home/HotProduct.vue'
import Recommend from './home/Recommend.vue'
</script>

<style scoped>
  .home-page {
    background-color: #f5f7fa;
    min-height: calc(100vh - 70px);
  }
</style>