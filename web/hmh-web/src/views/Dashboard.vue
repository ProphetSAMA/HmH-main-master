<template>
    <div class="dashboard-container">
      <!-- 统计卡片区域 -->
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in statCards" :key="index">
          <el-card class="stat-card" :body-style="{ padding: '20px' }">
            <div class="stat-icon">
              <el-icon :size="40" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 图表区域 -->
      <el-row :gutter="20" class="mt-20">
        <el-col :span="24">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span>报销统计</span>
                <el-radio-group v-model="chartType" size="small" @change="updateChart">
                  <el-radio-button label="month">月度</el-radio-button>
                  <el-radio-button label="category">类别</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="chartRef" style="width: 100%; height: 400px;"></div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 快捷操作和公告区域 -->
      <el-row :gutter="20" class="mt-20">
        <!-- 快捷操作 -->
        <el-col :span="12">
          <el-card class="box-card">
            <template #header>
              <div class="card-header quick-action-header">
                <span>快捷操作</span>
              </div>
            </template>
            <div class="quick-actions">
              <el-button 
                v-for="(action, index) in quickActions" 
                :key="index"
                :type="action.type"
                :icon="action.icon"
                @click="handleQuickAction(action)"
                class="action-button"
              >
                {{ action.name }}
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <!-- 最新公告 -->
        <el-col :span="12">
          <el-card class="box-card">
            <template #header>
              <div class="card-header announcement-header">
                <span>最新公告</span>
                <el-button text @click="viewAllAnnouncements">查看全部</el-button>
              </div>
            </template>
            <div class="news-list">
              <div v-if="latestAnnouncements.length === 0" class="empty-news">
                暂无公告
              </div>
              <div v-else class="single-news">
                <div class="news-title">
                  {{ latestAnnouncements[0].title }}
                  <span class="news-date">{{ formatDate(latestAnnouncements[0].createTime) }}</span>
                </div>
                <!-- <div class="news-summary">{{ truncateContent(latestAnnouncements[0].content, 100) }}</div> -->
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import * as echarts from 'echarts'
  import { request } from '../utils/request'
  import { ElMessage } from 'element-plus'
  import { 
    Document, 
    Money, 
    User, 
    Bell,
    Plus,
    Search,
    Setting
  } from '@element-plus/icons-vue'
  
  const router = useRouter()
  const chartType = ref('month')
  const chartInstance = ref(null)
  const chartRef = ref(null)
  const latestAnnouncements = ref([])
  
  // 统计卡片数据
  const statCards = ref([
    { title: '待处理报销', value: 0, icon: 'Document', color: '#409EFF' },
    { title: '本月报销总额', value: '¥0', icon: 'Money', color: '#67C23A' },
    { title: '系统用户数', value: 0, icon: 'User', color: '#E6A23C' },
    { title: '未读消息', value: 0, icon: 'Bell', color: '#F56C6C' }
  ])
  
  // 快捷操作
  const quickActions = [
    { name: '申请报销', icon: 'Plus', type: 'primary', route: '/edit' },
    { name: '我的信息', icon: 'Document', type: 'success', route: '/profile' },
    { name: '查询报销', icon: 'Search', type: 'warning', route: '/reimburse' },
    { name: '报销规则', icon: 'Setting', type: 'info', route: '/reimburse/rule' }
  ]
  
  // 处理快捷操作点击
  const handleQuickAction = (action) => {
    router.push(action.route)
  }
  
  // 查看所有公告
  const viewAllAnnouncements = () => {
    router.push('/announcement')
  }
  
  // 格式化日期
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  
  // 截断内容
  const truncateContent = (content, maxLength) => {
    if (!content) return ''
    return content.length > maxLength ? content.substring(0, maxLength) + '...' : content
  }
  
  // 获取最新公告
  const getLatestAnnouncements = async () => {
    try {
      const response = await request('/api/announcement/latest', {
        method: 'GET'
      })
      
      console.log('公告响应:', response) // 添加调试日志
      
      // 处理不同的响应格式
      if (response && response.data) {
        // 如果response.data是数组，直接使用
        if (Array.isArray(response.data)) {
          latestAnnouncements.value = response.data
        } 
        // 如果response.data.data是数组，使用它
        else if (response.data.data && Array.isArray(response.data.data)) {
          latestAnnouncements.value = response.data.data
        }
        // 其他情况，尝试转换为数组
        else {
          latestAnnouncements.value = []
        }
      } else {
        latestAnnouncements.value = []
      }
    } catch (error) {
      console.error('获取最新公告失败:', error)
      latestAnnouncements.value = []
    }
  }
  
  // 初始化图表
  const initChart = async () => {
    // 等待DOM更新完成
    await nextTick()
    
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }
    
    if (chartRef.value) {
      chartInstance.value = echarts.init(chartRef.value)
      updateChart()
    }
  }
  
  // 更新图表
  const updateChart = async () => {
    if (!chartInstance.value) {
      await initChart()
      return
    }
    
    try {
      let response
      let option
      
      if (chartType.value === 'month') {
        response = await request('/api/stats/monthly', {
          method: 'GET'
        })
        
        const data = Array.isArray(response.data) ? response.data : 
                    (response.data && Array.isArray(response.data.data) ? response.data.data : [])
        
        console.log('月度数据:', data) // 添加调试日志
        
        option = {
          title: {
            text: '月度报销统计'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          xAxis: {
            type: 'category',
            data: data.map(item => item.month)
          },
          yAxis: {
            type: 'value',
            name: '金额 (元)'
          },
          series: [
            {
              name: '报销金额',
              type: 'bar',
              data: data.map(item => item.amount),
              itemStyle: {
                color: '#409EFF'
              }
            }
          ]
        }
      } else {
        response = await request('/api/stats/category', {
          method: 'GET'
        })
        
        // 确保data是数组
        const data = Array.isArray(response.data) ? response.data : 
                    (response.data && Array.isArray(response.data.data) ? response.data.data : [])
        
        console.log('类别数据:', data) // 添加调试日志
        
        option = {
          title: {
            text: '报销类别统计'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            right: 10,
            top: 'center',
            data: data.map(item => item.name)
          },
          series: [
            {
              name: '报销金额',
              type: 'pie',
              radius: ['50%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 16,
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: data.map(item => ({
                name: item.name,
                value: item.value
              }))
            }
          ]
        }
      }
      
      chartInstance.value.setOption(option)
    } catch (error) {
      console.error('更新图表失败:', error)
    }
  }
  
  // 获取统计数据
  const getStats = async () => {
    try {
      const response = await request('/api/stats/dashboard', {
        method: 'GET'
      })
      
      if (response && response.data) {
        const stats = response.data
        
        // 更新统计卡片数据
        statCards.value[0].value = stats.pendingCount || 0
        statCards.value[1].value = `¥${stats.monthlyAmount || 0}`
        statCards.value[2].value = stats.userCount || 0
        statCards.value[3].value = stats.unreadCount || 0
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }
  
  // 窗口大小变化时重新调整图表大小
  const handleResize = () => {
    if (chartInstance.value) {
      chartInstance.value.resize()
    }
  }
  
  onMounted(async () => {
    getStats()
    getLatestAnnouncements()
    await initChart()
    window.addEventListener('resize', handleResize)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }
  })
  </script>
  
  <style scoped>
  .dashboard-container {
    padding: 20px;
  }
  
  .stat-card {
    display: flex;
    align-items: center;
    height: 100px;
  }
  
  .stat-icon {
    margin-right: 15px;
  }
  
  .stat-content {
    flex: 1;
  }
  
  .stat-title {
    font-size: 14px;
    color: #909399;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    margin-top: 5px;
  }
  
  .mt-20 {
    margin-top: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 20px;
    line-height: 40px;
    padding: 0 15px;
  }
  
  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-content: center;
  }
  
  .action-button {
    flex: 1;
    min-width: 120px;
  }
  
  .news-list {
    padding: 0px 0;
  }
  
  .single-news {
    padding: 0px 0;
  }
  
  .news-title {
    font-weight: bold;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    font-size: 16px;
  }
  
  .news-date {
    font-weight: normal;
    font-size: 12px;
    color: #909399;
  }
  
  .news-summary {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
  }
  
  .empty-news {
    text-align: center;
    color: #909399;
    padding: 20px 0;
  }
  
  .mr-5 {
    margin-right: 5px;
  }


  </style>