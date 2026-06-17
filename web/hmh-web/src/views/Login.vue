<template>
    <div class="login-container">
      <div class="bg-mask"></div>
      <el-card class="login-card">
        <h2>系统登录</h2>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
          <el-form-item prop="userName">
            <el-input v-model="loginForm.userName" placeholder="用户名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="roleName">
            <el-select v-model="loginForm.roleName" placeholder="请选择角色" style="width: 100%">
              <el-option label="管理员" value="管理员" />
              <el-option label="经理" value="经理" />
              <el-option label="员工" value="员工" />
            </el-select>
          </el-form-item>
          
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { User, Lock } from '@element-plus/icons-vue'
  import { request } from '../utils/request'
  
  const router = useRouter()
  
  const loginForm = reactive({
    userName: '',
    password: '',
    roleName: '员工' // 默认角色
  })
  
  const loading = ref(false)
  const loginFormRef = ref(null)
  
  const rules = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ]
  }
  
  const handleLogin = async () => {
    if (!loginFormRef.value) return
    
    await loginFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        
        try {
          const response = await request('/api/user/login', {
            method: 'POST',
            body: JSON.stringify(loginForm)
          })
          
          // 登录成功，保存token和用户信息
          localStorage.setItem('token', response.token)
          
          // 构建并保存用户信息
          const currentUser = {
            id: response.userId,
            userName: response.userName,
            trueName: response.trueName,
            roleName: response.roleName
          }
          localStorage.setItem('currentUser', JSON.stringify(currentUser))
          
          ElMessage.success('登录成功')
          
          // 跳转到首页
          router.push('/')
        } catch (error) {
          ElMessage.error(error.message || '登录失败，请检查用户名和密码')
        } finally {
          loading.value = false
        }
      }
    })
  }
  
  onMounted(() => {
    const container = document.querySelector('.login-container')
    const moveBg = (e) => {
      // 获取鼠标在窗口中的百分比
      const x = e.clientX / window.innerWidth
      const y = e.clientY / window.innerHeight
      // 计算偏移量（-20% ~ +20%）
      const offsetX = (x - 0.5) * 15
      const offsetY = (y - 0.5) * 25
      container.style.backgroundPosition = `calc(50% + ${offsetX}%) calc(50% + ${offsetY}%)`
    }
    window.addEventListener('mousemove', moveBg)
    // 离开页面时移除监听
    onBeforeUnmount(() => {
      window.removeEventListener('mousemove', moveBg)
    })
  })
  </script>
  
  <style scoped>
  .login-container {
    position: relative;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: url('/bg.png') no-repeat center center;
    background-size: 80vw auto;
    overflow: hidden;
    transition: background-position 0.2s;
  }
  
  .bg-mask {
    position: absolute;
    left: 0; top: 0; right: 0; bottom: 0;
    background: rgba(255,255,255,0.3);
    backdrop-filter: blur(8px);
    z-index: 1;
  }
  
  .login-card {
    width: 480px;
    position: relative;
    z-index: 2;
    box-shadow: 0 4px 32px rgba(0,0,0,0.18);
    background: rgba(255,255,255,0.95);
    border-radius: 18px;
    overflow: hidden;
  }
  
  h2 {
    text-align: center;
    margin-bottom: 30px;
  }
  </style>