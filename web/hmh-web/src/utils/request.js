import { ElMessage } from 'element-plus'
import router from '../router'

// 创建请求工具函数
export const request = async (url, options = {}) => {
  try {
    // 获取 token
    const token = localStorage.getItem('token')
    console.log('Token from localStorage:', token)

    // 设置默认 headers
    const headers = {
      ...options.headers
    }

    // 如果有 token，添加到 headers
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    // 如果不是FormData，设置Content-Type
    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = headers['Content-Type'] || 'application/json'
    }

    // 构建完整的请求配置
    const config = {
      ...options,
      headers
    }

    console.log('Request URL:', url)
    console.log('Request config:', config)

    const response = await fetch(url, config)
    console.log('Response status:', response.status)
    console.log('Response headers:', Object.fromEntries(response.headers.entries()))

    return await responseInterceptor(response)
  } catch (error) {
    console.error('Request error:', error)
    // 抛出错误，让调用方处理具体的错误信息
    throw error
  }
}

// 响应拦截器
async function responseInterceptor(response) {
  // 检查 HTTP 状态码
  if (!response.ok) {
    // 处理 401 错误
    if (response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
      router.push('/login')
      throw new Error('未授权，请重新登录')
    }
    throw new Error(`HTTP 错误: ${response.status}`)
  }
  
  // 解析 JSON 响应
  const data = await response.json()
  
  // 添加调试信息
  console.log('响应数据详情:', JSON.stringify(data, null, 2))
  
  // 处理登录响应 - 直接返回登录数据，不做额外处理
  if (data.token) {
    return data
  }
  
  // 兼容多种不同的 API 响应格式
  if (data.code === 200) {
    // 第一种格式: { code: 200, message: "操作成功", data: {...} }
    return data
  } else if (data.success === true) {
    // 第二种格式: { success: true, message: "保存成功" }
    return {
      code: 200,
      message: data.message || "操作成功",
      data: data.data || {}
    }
  } else {
    // 错误情况
    console.error('业务错误:', data)
    const errorMessage = data.message || (data.success === false ? "操作失败" : "请求失败")
    throw new Error(errorMessage)
  }
} 