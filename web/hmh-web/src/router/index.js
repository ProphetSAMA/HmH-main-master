import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      // 首页
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      // 报销管理
      {
        path: 'reimburse',
        name: 'Reimburse',
        component: () => import('../views/Reimburse.vue'),
        meta: { requiresAuth: true }
      },
      // 个人信息
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      // 成员管理
      {
        path: 'member',
        name: 'Member',
        component: () => import('../views/Member.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 检查token是否有效（简单的本地验证）
 * @param token JWT token
 * @returns boolean
 */
function isTokenValid(token) {
  if (!token) return false

  try {
    // 解析token payload
    const payload = JSON.parse(atob(token.split('.')[1]))

    // 检查是否过期
    const currentTime = Math.floor(Date.now() / 1000)
    if (payload.exp && payload.exp < currentTime) {
      return false
    }

    return true
  } catch (error) {
    return false
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}')

  // 处理登录页面
  if (to.path === '/login') {
    if (token && currentUser.id && isTokenValid(token)) {
      next('/')
      return
    }
    // 清除无效的token
    if (token && !isTokenValid(token)) {
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
    }
    next()
    return
  }

  // 处理需要认证的页面
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || !currentUser.id) {
      next('/login')
      return
    }

    // 验证token有效性
    if (!isTokenValid(token)) {
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
      ElMessage.error('登录已过期，请重新登录')
      next('/login')
      return
    }

    // 检查管理员权限
    if (to.matched.some(record => record.meta.requiresAdmin) && currentUser.roleName !== '管理员') {
      ElMessage.error('权限不足，无法访问此页面')
      next('/dashboard')
      return
    }
  }

  next()
})

export default router