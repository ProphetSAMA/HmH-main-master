<template>
    <div class="profile-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账号">{{ userInfo.userName }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ userInfo.roleName }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userInfo.trueName }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ userInfo.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ userInfo.email || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
  
      <!-- 编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="编辑个人信息"
        width="500px"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="手机" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <el-upload
        class="avatar-uploader"
        action="/api/user/avatar"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :before-upload="beforeAvatarUpload"
        :data="{ userId: currentUser.id }"
      >
        <img v-if="currentUser.avatar" :src="currentUser.avatar" class="avatar" />
        <img v-else src="/default-avatar.jpg" class="avatar" />
      </el-upload>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, reactive } from 'vue'
  import { ElMessage } from 'element-plus'
  import { request } from '../utils/request'
  
  const userInfo = ref({})
  const dialogVisible = ref(false)
  const formRef = ref()
  
  const form = ref({
    phone: '',
    email: ''
  })
  
  const rules = {
    phone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }

  const currentUser = reactive({
    id: 1, // 实际应从登录信息获取
    avatar: '' // 登录后赋值
  })

  // 获取用户信息
  const getUserInfo = async () => {
    if (!currentUser.id) {
      console.error('No user ID found in localStorage')
      ElMessage.error('未找到用户信息')
      return
    }

    try {
      console.log('Fetching user info for ID:', currentUser.id)
      const result = await request(`/api/user/${currentUser.id}`)
      console.log('User info result:', result)
      
      if (!result) {
        console.error('No result from request')
        return
      }
      
      if (result.code !== 200) {
        console.error('Request not successful:', result)
        ElMessage.error(result.message || '获取用户信息失败')
        return
      }
      
      userInfo.value = result.data || {}
      console.log('Updated userInfo:', userInfo.value)
    } catch (error) {
      console.error('Error fetching user info:', error)
      ElMessage.error('获取用户信息失败')
      userInfo.value = {}
    }
  }
  
  // 打开编辑对话框
  const handleEdit = () => {
    console.log('Opening edit dialog with data:', userInfo.value)
    form.value = {
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || ''
    }
    dialogVisible.value = true
  }
  
  // 提交编辑
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          console.log('Submitting form with data:', {
            ...form.value,
            id: currentUser.id
          })
          
          const result = await request('/api/user/update', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              ...form.value,
              id: currentUser.id
            })
          })
          
          console.log('Update result:', result)
          
          if (!result) {
            console.error('No result from update request')
            return
          }
          
          if (result.code !== 200) {
            console.error('Update not successful:', result)
            ElMessage.error(result.message || '保存失败')
            return
          }
          
          ElMessage.success('保存成功')
          dialogVisible.value = false
          getUserInfo() // 重新获取用户信息
        } catch (error) {
          console.error('Error updating user info:', error)
          ElMessage.error('保存失败')
        }
      }
    })
  }
  
  const handleAvatarSuccess = (response) => {
    if (response.success) {
      currentUser.avatar = response.avatar
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  }

  const beforeAvatarUpload = (file) => {
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
    const isLt2M = file.size / 1024 / 1024 < 2
    if (!isJPG) {
      ElMessage.error('只能上传 JPG/PNG 格式图片!')
      return false
    }
    if (!isLt2M) {
      ElMessage.error('图片大小不能超过 2MB!')
      return false
    }
    return true
  }
  
  onMounted(() => {
    getUserInfo()
  })
  </script>
  
  <style scoped>
  .profile-container {
    padding: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  </style>