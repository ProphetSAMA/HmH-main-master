<template>
  <div class="reimburse-container">
    <div style="margin-bottom: 10px;">
      <p>当前用户角色: {{ currentUser?.roleName || '未知' }}</p>
    </div>
    <el-card>
      <!-- 分类标签 -->
      <div v-if="currentUser.roleName === '经理'" class="filter-tabs">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="1,2">已处理</el-radio-button>
          <el-radio-button label="3">待处理</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="报销类型">
          <el-select 
            v-model="searchForm.type" 
            placeholder="选择类型" 
            clearable
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="审批人" v-if="currentUser.roleName !== '经理'">
          <el-input v-model="searchForm.spName" placeholder="审批人姓名" clearable />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button v-if="showAddButton" type="success" @click="handleAdd">新增报销</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%">
        <template v-for="col in columns" :key="col.prop">
          <el-table-column v-bind="col">
            <template #default="{ row }" v-if="col.prop === 'status'">
              <el-tooltip
                :content="getTooltipContent(row)"
                placement="top"
                :disabled="!shouldShowTooltip(row)"
              >
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </el-tooltip>
            </template>
            <template #default="{ row }" v-else-if="col.prop === 'money'">
              ¥{{ row.money }}
            </template>
          </el-table-column>
        </template>

        <el-table-column label="操作" width="200" v-if="currentUser.roleName !== '管理员'">
          <template #default="{ row }">
            <!-- 经理的操作按钮 -->
            <template v-if="currentUser.roleName === '经理' && row.status === 3">
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            </template>
            
            <!-- 员工的操作按钮 -->
            <template v-if="currentUser.roleName === '员工' && row.status === 3 && row.sqUserId === currentUser.id">
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
        layout="total, prev, pager, next"
        class="mt-4"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="报销类型" prop="type">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option
              v-for="item in typeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="金额" prop="money">
          <el-input-number v-model="form.money" :min="0" />
        </el-form-item>
        
        <el-form-item label="说明" prop="reason">
          <el-input type="textarea" v-model="form.reason" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加拒绝理由对话框 -->
    <el-dialog
      title="拒绝原因"
      v-model="rejectDialogVisible"
      width="500px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectRules"
        label-width="80px"
      >
        <el-form-item label="拒绝理由" prop="denyReason">
          <el-input
            type="textarea"
            v-model="rejectForm.denyReason"
            :rows="3"
            placeholder="请输入拒绝理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReject">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { useRouter } from 'vue-router'
import { request } from '../utils/request'
import { wsClient } from '../utils/websocket'

const router = useRouter()
const currentUser = ref(JSON.parse(localStorage.getItem('currentUser') || '{}'))

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

// 添加过滤状态
const filterStatus = ref('')

// 修改搜索表单
const searchForm = reactive({
  type: null,
  spName: '',
  status: ''
})

const form = reactive({
  id: null,
  type: null,
  money: 0,
  reason: ''
})

const rules = {
  type: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  money: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

// 新增报销按钮的显示控制
const showAddButton = computed(() => currentUser.value.roleName === '员工')

// 添加类型映射
const typeMap = {
  1: '车补',
  2: '餐补',
  3: '交通补',
  4: '住宿'
}

// 修改为静态类型选项
const typeOptions = ref([
  { id: 1, name: '车补' },
  { id: 2, name: '餐补' },
  { id: 3, name: '交通补' },
  { id: 4, name: '住宿' }
])

// 获取列表数据
const getList = async () => {
  try {
    // 构建基本参数
    const params = new URLSearchParams()
    params.append('page', currentPage.value)
    params.append('rows', pageSize.value)

    // 添加搜索条件
    if (searchForm.type) {
      params.append('type', searchForm.type)
    }
    if (searchForm.spName) {
      params.append('spName', searchForm.spName)
    }

    // 根据角色和筛选条件添加参数
    const userRole = currentUser.value?.roleName
    const userId = currentUser.value?.id

    if (userRole === '经理') {
      // 经理角色：根据状态筛选
      if (filterStatus.value === '1,2') {
        params.append('status', '1,2') // 已处理（已通过或已拒绝）
      } else if (filterStatus.value === '3') {
        params.append('status', '3') // 待处理
      }
      // 全部状态时不添加 status 参数
      
      // 添加经理ID作为查询条件
      params.append('managerId', userId)
    } else if (userRole === '员工') {
      // 员工角色：只查看自己的申请
      params.append('sqUserId', userId)
    }

    console.log('=== Request Info ===')
    console.log('Current user:', currentUser.value)
    console.log('Filter status:', filterStatus.value)
    console.log('Search form:', searchForm)
    console.log('Request params:', params.toString())

    const url = `/api/reimburse/list?${params.toString()}`
    console.log('Request URL:', url)
    
    const result = await request(url)
    console.log('API Response:', result)

    if (!result?.data) {
      console.warn('No data in response:', result)
      tableData.value = []
      total.value = 0
      return
    }

    // 更新表格数据
    if (Array.isArray(result.data)) {
      tableData.value = result.data
      total.value = result.total || result.data.length
    } else if (result.data && Array.isArray(result.data.data)) {
      tableData.value = result.data.data
      total.value = result.data.total || result.data.data.length
    } else {
      console.warn('Unexpected data format:', result)
      tableData.value = []
      total.value = 0
    }

    console.log('=== Updated State ===')
    console.log('Table data:', tableData.value)
    console.log('Total:', total.value)
  } catch (error) {
    console.error('Error in getList:', error)
    ElMessage.error('获取列表失败')
    tableData.value = []
    total.value = 0
  }
}

// 修改过滤切换处理函数
const handleFilterChange = (value) => {
  filterStatus.value = value
  currentPage.value = 1 // 切换筛选时重置页码
  getList()
}

// 修改重置函数
const handleReset = () => {
  searchForm.type = null
  searchForm.spName = ''
  filterStatus.value = ''
  currentPage.value = 1
  getList()
}

// 操作按钮的显示控制
const showOperations = computed(() => {
  return (row) => {
    const { roleName } = currentUser.value
    const { status, sqUserId, spUserId } = row

    switch (roleName) {
      case '员工':
        // 员工只能操作自己的待审核记录
        return status === 3 && sqUserId === currentUser.value.id
      case '经理':
        // 经理只能操作待审核的记录
        return status === 3
      case '管理员':
        // 管理员可以查看所有记录，但不能操作
        return false
      default:
        return false
    }
  }
})

// 表格列的显示控制
const columns = computed(() => {
  const { roleName } = currentUser.value
  const baseColumns = [
    { prop: 'id', label: 'ID', width: '80' },
    { 
      prop: 'type', 
      label: '报销类型',
      formatter: (row) => typeMap[row.type] || '未知'
    },
    { prop: 'money', label: '金额' },
    { prop: 'reason', label: '说明' },
    { prop: 'sqName', label: '申请人' },
    { 
      prop: 'createTime', 
      label: '申请时间',
      formatter: (row) => formatDateTime(row.createTime)
    },
    { 
      prop: 'status', 
      label: '状态',
      formatter: (row) => getStatusText(row.status)
    }
  ]

  // 根据角色决定是否显示审批人列
  if (roleName !== '员工') {
    baseColumns.push({ prop: 'spName', label: '审批人' })
    baseColumns.push({ 
      prop: 'endTime', 
      label: '审批时间',
      formatter: (row) => formatDateTime(row.endTime)
    })
  }

  return baseColumns
})

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

// 各种操作处理函数
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 审核通过
const handleApprove = async (row) => {
  try {
    const result = await request('/api/reimburse/save', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: row.id,
        status: 1,
        spUserId: currentUser.value.id,
        endTime: new Date().toISOString()
      })
    })
    
    if (!result) return
    
    ElMessage.success('审核通过')
    // 发送 WebSocket 通知
    if (wsClient.ws && wsClient.ws.readyState === WebSocket.OPEN) {
      wsClient.ws.send(JSON.stringify({
        type: 'notification',
        title: '审批通知',
        message: `${currentUser.value.trueName || '经理'} 已通过 ${row.sqName} 的报销申请，金额：¥${row.money}`,
        notificationType: 'success'
      }))
    }
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 添加拒绝相关的响应式变量
const rejectDialogVisible = ref(false)
const rejectFormRef = ref()
const rejectForm = reactive({
  id: null,
  denyReason: ''
})

// 添加拒绝表单验证规则
const rejectRules = {
  denyReason: [
    { required: true, message: '请输入拒绝理由', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ]
}

// 修改处理拒绝的函数
const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.denyReason = ''
  rejectDialogVisible.value = true
}

// 添加提交拒绝的函数
const submitReject = async () => {
  if (!rejectFormRef.value) return

  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const result = await request('/api/reimburse/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            id: rejectForm.id,
            status: 2,
            spUserId: currentUser.value.id,
            denyReason: rejectForm.denyReason,
            endTime: new Date().toISOString()
          })
        })
        
        if (!result) return
        
        ElMessage.success('已拒绝')
        // 发送 WebSocket 通知
        if (wsClient.ws && wsClient.ws.readyState === WebSocket.OPEN) {
          wsClient.ws.send(JSON.stringify({
            type: 'notification',
            title: '审批通知',
            message: `${currentUser.value.trueName || '经理'} 已拒绝 ${tableData.value.find(item => item.id === rejectForm.id)?.sqName || '用户'} 的报销申请，原因：${rejectForm.denyReason}`,
            notificationType: 'warning'
          }))
        }
        rejectDialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 添加工具提示内容获取函数
const getTooltipContent = (row) => {
  if (row.status === 1) {
    return row.endTime ? '审批时间：' + formatDateTime(row.endTime) : ''
  } else if (row.status === 2) {
    return row.denyReason ? '拒绝理由：' + row.denyReason : ''
  }
  return ''
}

// 添加工具提示显示控制函数
const shouldShowTooltip = (row) => {
  return (row.status === 1 && row.endTime) || (row.status === 2 && row.denyReason)
}

const handleAdd = () => {
  dialogTitle.value = '新增报销'
  form.id = null
  form.type = null
  form.money = 0
  form.reason = ''
  form.createTime = new Date().toISOString()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const result = await request(`/api/reimburse/${row.id}`)
    if (!result) return
    
    Object.assign(form, result.data)
    dialogTitle.value = '编辑报销'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条报销记录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const result = await request(`/api/reimburse/${row.id}`, {
        method: 'DELETE'
      })
      
      if (!result) return
      
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const result = await request('/api/reimburse/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(form)
        })
        
        if (!result) return
        
        ElMessage.success('保存成功')
        dialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const handlePageChange = () => {
  getList()
}

// 修改日期格式化函数
const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 修改 onMounted
onMounted(async () => {
  console.log('=== Component Mounted ===')
  
  // 获取并验证用户信息
  const userInfo = localStorage.getItem('currentUser')
  console.log('User info from localStorage:', userInfo)
  
  if (!userInfo) {
    console.error('No user info in localStorage')
    ElMessage.error('未找到用户信息')
    return
  }

  try {
    currentUser.value = JSON.parse(userInfo)
    console.log('Parsed user info:', currentUser.value)
    
    if (!currentUser.value?.id) {
      console.error('Invalid user info:', currentUser.value)
      ElMessage.error('用户信息不完整')
      return
    }

    // 连接 WebSocket
    wsClient.connect()

    await getList()
  } catch (error) {
    console.error('Error in onMounted:', error)
    ElMessage.error('初始化失败')
  }
})

// 添加 onUnmounted 钩子
onUnmounted(() => {
  // 断开 WebSocket 连接
  wsClient.disconnect()
})
</script>

<style scoped>
.reimburse-container {
  padding: 20px;
}

.filter-tabs {
  margin-bottom: 20px;
}

.search-form {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 16px;
}
</style> 
