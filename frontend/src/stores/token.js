import { defineStore } from "pinia";
import { ref } from 'vue';

export const useTokenStore = defineStore('token', () => {
    // 前台用户 token
    const token = ref('')
    // 后台管理员 token
    const adminToken = ref('')

    // --- 前台用户方法 ---
    const setToken = (newToken) => {
        token.value = newToken
    }
    const removeToken = () => {
        token.value = ''
    }

    // --- 后台管理员方法 ---
    const setAdminToken = (data) => {
        adminToken.value = data.token || data
    }
    const removeAdminToken = () => {
        adminToken.value = ''
    }

    // 提供给 axios 拦截器调用的方法
    const getAdminToken = () => adminToken.value

    return {
        token, setToken, removeToken,
        adminToken, setAdminToken, removeAdminToken, getAdminToken
    }
}, {
    persist: {
        paths: ['token', 'adminToken']
    }
})