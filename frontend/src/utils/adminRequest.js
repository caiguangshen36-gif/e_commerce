import axios from 'axios'
import { useTokenStore } from '@/stores/token.js'
import JSONbig from 'json-bigint'

const adminRequest = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器：带上管理员 Token
adminRequest.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    const adminToken = tokenStore.adminToken
    if (adminToken) {
      config.headers.Authorization = `Bearer ${adminToken}`
    }
    return config
  }
)
// 关键：自定义响应转换器，处理大整数
adminRequest.defaults.transformResponse = [
  function (data) {
    // 当响应数据存在时，用 JSONbig 解析
    if (data) {
      return JSONbig.parse(data)
    }
    return data
  }
]

// 响应拦截
adminRequest.interceptors.response.use(
  (res) => res.data,
  (err) => Promise.reject(err)
)

export default adminRequest