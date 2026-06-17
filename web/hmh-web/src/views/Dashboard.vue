<template>
    <div class="dashboard-container">
      <!-- 统计卡片区域 -->
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <template #header>
              <div class="card-header">
                <span>待审核报销</span>
                <el-tag type="warning" effect="plain">待处理</el-tag>
              </div>
            </template>
            <div class="card-body">
              <div class="stat-number">{{ stats.pending || 0 }}</div>
              <div class="stat-text">待审核申请</div>
            </div>
          </el-card>
        </el-col>
  
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <template #header>
              <div class="card-header">
                <span>本月已通过</span>
                <el-tag type="success" effect="plain">已通过</el-tag>
              </div>
            </template>
            <div class="card-body">
              <div class="stat-number">{{ stats.approved || 0 }}</div>
              <div class="stat-text">已通过申请</div>
            </div>
          </el-card>
        </el-col>
  
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <template #header>
              <div class="card-header">
                <span>本月报销总额</span>
                <el-tag type="info" effect="plain">金额</el-tag>
              </div>
            </template>
            <div class="card-body">
              <div class="stat-number">¥ {{ stats.totalAmount || 0 }}</div>
              <div class="stat-text">报销金额</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
  
      <!-- 最近报销记录 -->
      <el-card class="recent-records" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>最近报销记录</span>
            <el-button type="primary" @click="$router.push('/reimburse')">查看更多</el-button>
          </div>
        </template>
  
        <el-table :data="recentRecords" style="width: 100%" v-if="recentRecords.length">
          <el-table-column prop="typeName" label="报销类型">
            <template #default="{ row }">
              {{ row.typeName || getTypeName(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="money" label="金额">
            <template #default="{ row }">
              ¥ {{ row.money || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="sqName" label="申请人">
            <template #default="{ row }">
              {{ row.sqName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-text">暂无数据</div>
      </el-card>
  
      <!-- 欢迎信息 -->
      <el-card class="welcome-card" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>快捷操作</span>
          </div>
        </template>
        <div class="quick-actions">
          <el-button type="primary" @click="$router.push('/reimburse')">
            <el-icon><Plus /></el-icon>新增报销
          </el-button>
          <el-button type="success" @click="$router.push('/reimburse')">
            <el-icon><Document /></el-icon>查看报销
          </el-button>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { Plus, Document } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  import { request } from '../utils/request'
  
  // 统计数据
  const stats = ref({
    pending: 0,
    approved: 0,
    totalAmount: 0
  })
  
  // 最近记录
  const recentRecords = ref([])
  
  // 获取统计数据
  const getStats = async () => {
    try {
      const response = await request('/api/reimburse/stats');
      if (response && response.success) {
        // 更新 stats 对象以匹配后端返回的数据结构
        stats.value.pending = response.data.pendingCount; // 更新待审核数量
        stats.value.approved = response.data.approvedCount; // 更新已通过数量
        stats.value.totalAmount = response.data.totalAmount; // 更新总报销金额
      } else {
        ElMessage.error('获取统计数据失败');
      }
    } catch (error) {
      ElMessage.error('获取统计数据失败');
    }
  }
  
  // 获取最近记录
  const getRecentRecords = async () => {
    try {
      const data = await request('/api/reimburse/list?page=1&rows=5')
      if (!data) return
      recentRecords.value = Array.isArray(data.data) ? data.data : []
    } catch (error) {
      ElMessage.error('获取最近记录失败')
      recentRecords.value = []
    }
  }
  
  // 状态显示
  const getStatusText = (status) => {
    const statusMap = {
      1: '已通过',
      2: '已拒绝',
      3: '待审核'
    }
    return statusMap[status] || '未知'
  }
  
  const getStatusType = (status) => {
    const typeMap = {
      1: 'success',
      2: 'danger',
      3: 'warning'
    }
    return typeMap[status] || 'info'
  }
  
  // 类型名称映射
  const typeMap = {
    1: '车补',
    2: '餐补',
    3: '交通补',
    4: '住宿'
  }
  
  const getTypeName = (type) => {
    return typeMap[type] || '未知'
  }
  
  // 格式化日期
  const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  onMounted(() => {
    getStats()
    getRecentRecords()
  })
  </script>
  
  <style scoped>
  .dashboard-container {
    padding: 20px;
  }
  
  .stat-card {
    height: 180px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-body {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100px;
  }
  
  .stat-number {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .stat-text {
    font-size: 14px;
    color: #909399;
  }
  
  .quick-actions {
    display: flex;
    gap: 16px;
  }
  
  .quick-actions .el-button {
    width: 120px;
  }
  
  .el-icon {
    margin-right: 4px;
  }
  
  .recent-records {
    margin-bottom: 20px;
  }
  
  .welcome-card {
    background-color: #f8f9fa;
  }
  
  .empty-text {
    text-align: center;
    color: #909399;
    padding: 30px 0;
  }
  </style>