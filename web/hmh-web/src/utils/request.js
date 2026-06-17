import { ElMessage } from 'element-plus'
import router from '../router'

// 创建请求工具函数
export const request = async (url, options = {}) => {
  try {
    // 获取 token
    const token = localStorage.getItem('token')

    // 设置默认 headers
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    }

    // 如果有 token，添加到 headers
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    // 构建完整的请求配置
    const config = {
      ...options,
      headers
    }

    // 仅在开发环境输出日志
    if (import.meta.env.DEV) {
      console.log('Request URL:', url)
    }

    const response = await fetch(url, config)

    // 仅在开发环境输出日志
    if (import.meta.env.DEV) {
      console.log('Response status:', response.status)
    }

    // 尝试解析响应
    const data = await response.json()

    // 处理 401 错误
    if (response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
      return null
    }

    // 处理其他错误
    if (!response.ok) {
      ElMessage.error(data.message || '请求失败')
      return null
    }

    return data
  } catch (error) {
    // 仅在开发环境输出详细错误
    if (import.meta.env.DEV) {
      console.error('Request error:', error)
    }
    ElMessage.error('网络请求失败')
    return null
  }
} 