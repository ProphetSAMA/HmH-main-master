import { createRouter, createWebHistory } from 'vue-router'

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
        meta: { 
          requiresAuth: true,
          breadcrumb: ['仪表盘']
        }
      },
      // 公告
      {
        path: 'news',
        name: 'News',
        component: () => import('../views/News.vue'),
        meta: {
          requiresAuth: true,
          breadcrumb: ['公告管理', '公告列表']
        }
      },
      // 报销管理
      {
        path: 'reimburse',
        name: 'Reimburse',
        component: () => import('../views/Reimburse.vue'),
        meta: { 
          requiresAuth: true,
          breadcrumb: ['报销管理', '报销记录']
        }
      },
      // 个人信息
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: {
          requiresAuth: true,
          breadcrumb: ['个人中心', '个人信息']
        }
      },
      // 数据统计
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue'),
        meta: { 
          requiresAuth: true,
          breadcrumb: ['数据统计']
        }
      },
      {
        path: 'edit',
        name: 'Edit',
        component: () => import('../views/Edit.vue'),
        meta: { 
          requiresAuth: true,
          breadcrumb: ['报销管理', '新增报销']
        }
      },
      // 成员管理
      {
        path: 'member',
        name: 'Member',
        component: () => import('../views/Member.vue'),
        meta: { 
          requiresAuth: true,
          requiresAdmin: true,
          breadcrumb: ['系统管理', '成员管理']
        }
      },
      // 发票管理
      {
        path: 'invoice',
        name: 'Invoice',
        component: () => import('../views/Invoice.vue'),
        meta: { 
          requiresAuth: true,
          breadcrumb: ['财务管理', '发票管理']
        }
      },
      {
        path: 'reimburse-rule',
        name: 'ReimburseRule',
        component: () => import('../views/ReimburseRule.vue'),
        meta: {
          title: '报销规则设定',
          requiresAuth: true,
          roles: ['管理员', '经理'] // 只允许管理员和经理访问
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}')

  // 处理登录页面
  if (to.path === '/login') {
    if (token && currentUser.id) {
      next('/')
      return
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

    // 检查管理员权限
    if (to.matched.some(record => record.meta.requiresAdmin) && currentUser.roleName !== '管理员') {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router