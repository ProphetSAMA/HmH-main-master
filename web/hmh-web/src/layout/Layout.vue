<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h2>报销管理系统</h2>
      </div>
      <el-menu
          :default-openeds="['1','2']"
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-menu-item index="/news">
          <el-icon><Notification /></el-icon>
          <template #title>公告</template>
        </el-menu-item>

        <el-sub-menu index="1">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>报销管理</span>
          </template>
            <el-menu-item index="edit">新增报销</el-menu-item>
            <el-menu-item index="reimburse">报销记录</el-menu-item>
            <el-menu-item index="invoice">发票管理</el-menu-item>
            <el-menu-item index="reimburse-rule">报销规则</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="profile">个人信息</el-menu-item>
          <el-menu-item @click="handleCommand('logout')">退出登录</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/stats">
          <el-icon><PieChart /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>

        <!-- 管理员菜单 -->
        <el-menu-item 
          index="/member" 
          v-if="currentUser.roleName === '管理员'"
        >
          <el-icon><UserFilled /></el-icon>
          <template #title>成员管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="toggle-icon" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-icon><Avatar /></el-icon>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ currentUser?.trueName || '未登录' }}
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 添加全局面包屑导航 -->
      <el-header style="height: auto; padding: 0;">
        <div class="breadcrumb-container">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" 
                               :key="index">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  HomeFilled, 
  Money,
  User,
  UserFilled,
  CaretBottom,
  Expand, 
  Fold ,
  PieChart,
  Notification,
  Avatar
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

// 当前用户信息
const currentUser = ref(JSON.parse(localStorage.getItem('currentUser') || '{}'))

// 监听 localStorage 中的用户信息变化
const updateUserInfo = () => {
  const userInfo = localStorage.getItem('currentUser')
  if (userInfo) {
    currentUser.value = JSON.parse(userInfo)
  } else {
    currentUser.value = {}
  }
}

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 清除token并重定向到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
      router.push('/login')
    })
  }
}

// 组件挂载时更新用户信息
onMounted(() => {
  updateUserInfo()
  
  // 添加 storage 事件监听
  window.addEventListener('storage', (e) => {
    if (e.key === 'currentUser') {
      updateUserInfo()
    }
  })
})

const breadcrumbItems = ref([])

// 监听路由变化更新面包屑
watch(() => route.meta.breadcrumb, (newVal) => {
  breadcrumbItems.value = newVal || []
}, { immediate: true })
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  border-bottom: 1px solid #1f2d3d;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.el-menu-vertical {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-info .el-icon {
  margin-left: 4px;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}

.breadcrumb-container {
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.el-breadcrumb {
  font-size: 14px;
}
</style>