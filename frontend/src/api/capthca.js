//导入request.js请求工具
import request from '@/utils/request.js'

//提供调用获取验证码接口的函数
export const getCaptchaService = () => {
  return request.get('/captcha/get');
}