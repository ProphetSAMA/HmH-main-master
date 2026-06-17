<template>
  <div class="rule-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报销规则设定</span>
          <el-button type="primary" @click="handleAddRule">新增规则</el-button>
        </div>
      </template>
      
      <!-- 规则列表 -->
      <el-table :data="ruleList" border style="width: 100%">
        <el-table-column prop="typeName" label="费用类型" width="120" />
        <el-table-column prop="maxAmount" label="最高限额" width="120">
          <template #default="{ row }">
            ¥{{ row.maxAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="warningAmount" label="预警金额" width="120">
          <template #default="{ row }">
            ¥{{ row.warningAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则说明" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEditRule(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="费用类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="选择类型">
            <el-option
              v-for="item in typeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="最高限额" prop="maxAmount">
          <el-input-number v-model="form.maxAmount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        
        <el-form-item label="预警金额" prop="warningAmount">
          <el-input-number v-model="form.warningAmount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        
        <el-form-item label="规则说明" prop="description">
          <el-input type="textarea" v-model="form.description" rows="3" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '../utils/request'

const ruleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const loading = ref(false)

// 类型选项
const typeOptions = ref([
  { id: 1, name: '车补' },
  { id: 2, name: '餐补' },
  { id: 3, name: '交通补' },
  { id: 4, name: '住宿' }
])

// 表单数据
const form = reactive({
  id: null,
  typeId: null,
  typeName: '',
  maxAmount: 0,
  warningAmount: 0,
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  typeId: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  maxAmount: [{ required: true, message: '请输入最高限额', trigger: 'blur' }],
  warningAmount: [{ required: true, message: '请输入预警金额', trigger: 'blur' }],
  description: [{ required: true, message: '请输入规则说明', trigger: 'blur' }]
}

// 获取规则列表
const getRuleList = async () => {
  try {
    loading.value = true
    const response = await request('/api/reimburse/rule/list', {
      method: 'GET'
    })
    
    console.log('规则列表响应:', response) // 添加调试日志
    
    // 处理不同的响应格式
    if (response && response.data) {
      // 如果response.data是数组，直接使用
      if (Array.isArray(response.data)) {
        ruleList.value = response.data
      } 
      // 如果response.data.data是数组，使用它
      else if (response.data.data && Array.isArray(response.data.data)) {
        ruleList.value = response.data.data
      }
      // 其他情况，尝试转换为数组
      else {
        ruleList.value = []
      }
    } else {
      ruleList.value = []
    }
  } catch (error) {
    console.error('获取规则列表失败:', error)
    ElMessage.error(`获取规则列表失败: ${error.message}`)
    ruleList.value = []
  } finally {
    loading.value = false
  }
}

// 新增规则
const handleAddRule = () => {
  dialogTitle.value = '新增报销规则'
  
  // 确保ruleList.value是数组
  const rules = Array.isArray(ruleList.value) ? ruleList.value : []
  
  // 检查已有规则中的类型
  const existingTypes = rules.map(rule => rule.typeId)
  
  // 过滤掉已有的类型选项
  const availableTypes = typeOptions.value.filter(type => !existingTypes.includes(type.id))
  
  if (availableTypes.length === 0) {
    ElMessage.warning('所有费用类型都已设置规则，请编辑现有规则')
    return
  }
  
  form.id = null
  form.typeId = availableTypes[0].id // 默认选择第一个可用类型
  form.typeName = availableTypes[0].name
  form.maxAmount = 0
  form.warningAmount = 0
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑规则
const handleEditRule = (row) => {
  dialogTitle.value = '编辑报销规则'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 切换规则状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const response = await request('/api/reimburse/rule/status', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: row.id,
        status: newStatus
      })
    })
    
    if (response && (response.code === 200 || response.success === true)) {
      ElMessage.success(`规则已${newStatus === 1 ? '启用' : '禁用'}`)
      getRuleList()
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error(`操作失败: ${error.message}`)
  }
}

// 删除规则
const handleDeleteRule = (row) => {
  ElMessageBox.confirm('确定要删除这条规则吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const response = await request(`/api/reimburse/rule/${row.id}`, {
        method: 'DELETE'
      })
      
      if (response && (response.code === 200 || response.success === true)) {
        ElMessage.success('删除成功')
        getRuleList()
      } else {
        ElMessage.error(response?.message || '删除失败')
      }
    } catch (error) {
      console.error('删除规则失败:', error)
      ElMessage.error(`删除失败: ${error.message}`)
    }
  })
}

// 提交规则
const handleSubmitRule = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 设置类型名称
        const selectedType = typeOptions.value.find(t => t.id === form.typeId)
        form.typeName = selectedType ? selectedType.name : ''
        
        const response = await request('/api/reimburse/rule/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(form)
        })
        
        if (response && (response.code === 200 || response.success === true)) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          getRuleList()
        } else {
          ElMessage.error(response?.message || '保存失败')
        }
      } catch (error) {
        console.error('保存规则失败:', error)
        ElMessage.error(`保存失败: ${error.message}`)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  getRuleList()
})
</script>

<style scoped>
.rule-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 