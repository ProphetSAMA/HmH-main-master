<template>
  <el-container>
    <el-header class="header">
      <h2>发票管理</h2>
    </el-header>

    <el-main>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="发票号码">
          <el-input v-model="searchForm.invoiceNo" placeholder="请输入发票号码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未使用" :value="0" />
            <el-option label="已关联" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">上传发票</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="invoiceList"
        style="width: 100%"
        stripe
        border
        v-loading="loading"
      >
        <el-table-column label="发票号码" prop="invoiceNo" min-width="180"></el-table-column>
        <el-table-column label="发票代码" prop="invoiceCode" min-width="180"></el-table-column>
        <el-table-column label="金额" prop="amount" width="120">
          <template #default="{ row }">
            ¥ {{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="开票日期" prop="invoiceDate" width="120"></el-table-column>
        <el-table-column label="销售方" prop="seller" min-width="180"></el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">
              {{ row.status === 0 ? '未使用' : '已关联' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联报销单" prop="reimburseNo" min-width="150">
          <template #default="{ row }">
            <span v-if="row.reimburseNo">{{ row.reimburseNo }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewInvoice(row)">查看</el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleRelate(row)"
              :disabled="row.status === 1"
            >关联报销</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.status === 1"
            >删除</el-button>
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
    </el-main>

    <!-- 上传发票对话框 -->
    <el-dialog
      title="上传发票"
      v-model="uploadDialogVisible"
      width="600px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="OCR识别" name="ocr">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽发票图片到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持JPG/PNG格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
          <div class="mt-3 text-center">
            <el-button type="primary" @click="handleOcr">OCR识别</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="手动录入" name="manual">
          <el-form 
            :model="invoiceForm" 
            :rules="invoiceRules" 
            ref="invoiceFormRef" 
            label-width="100px"
          >
            <el-form-item label="发票号码" prop="invoiceNo">
              <el-input v-model="invoiceForm.invoiceNo" />
            </el-form-item>
            <el-form-item label="发票代码" prop="invoiceCode">
              <el-input v-model="invoiceForm.invoiceCode" />
            </el-form-item>
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="invoiceForm.amount" :precision="2" :step="0.01" :min="0" />
            </el-form-item>
            <el-form-item label="开票日期" prop="invoiceDate">
              <el-date-picker v-model="invoiceForm.invoiceDate" type="date" placeholder="选择日期" />
            </el-form-item>
            <el-form-item label="销售方" prop="seller">
              <el-input v-model="invoiceForm.seller" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button 
            v-if="activeTab === 'manual'"
            type="primary" 
            @click="handleSubmitInvoice"
          >保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看发票对话框 -->
    <el-dialog
      title="发票详情"
      v-model="viewDialogVisible"
      width="600px"
    >
      <div v-if="currentInvoice.imageUrl" class="invoice-image">
        <img :src="currentInvoice.imageUrl" alt="发票图片" />
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号码">{{ currentInvoice.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票代码">{{ currentInvoice.invoiceCode }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥ {{ currentInvoice.amount }}</el-descriptions-item>
        <el-descriptions-item label="开票日期">{{ currentInvoice.invoiceDate }}</el-descriptions-item>
        <el-descriptions-item label="销售方" :span="2">{{ currentInvoice.seller }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentInvoice.status === 0 ? 'success' : 'info'">
            {{ currentInvoice.status === 0 ? '未使用' : '已关联' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联报销单">
          {{ currentInvoice.reimburseNo || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 关联报销单对话框 -->
    <el-dialog
      title="关联报销单"
      v-model="relateDialogVisible"
      width="500px"
    >
      <el-form :model="relateForm" ref="relateFormRef" label-width="100px">
        <el-form-item label="报销单" prop="reimburseId">
          <el-select v-model="relateForm.reimburseId" placeholder="请选择报销单" filterable>
            <el-option 
              v-for="item in reimburseList" 
              :key="item.id" 
              :label="`${item.typeName} - ¥${item.money} - ${formatDate(item.createTime)}`" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="relateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitRelate">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { request } from '../utils/request'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const invoiceList = ref([])
const uploadDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const relateDialogVisible = ref(false)
const activeTab = ref('ocr')
const invoiceFormRef = ref()
const relateFormRef = ref()
const currentInvoice = ref({})
const reimburseList = ref([])
const token = localStorage.getItem('token')
const fileList = ref([])

const searchForm = reactive({
  invoiceNo: '',
  status: ''
})

const invoiceForm = reactive({
  invoiceNo: '',
  invoiceCode: '',
  amount: 0,
  invoiceDate: '',
  seller: ''
})

const relateForm = reactive({
  invoiceId: null,
  reimburseId: null
})

const invoiceRules = {
  invoiceNo: [
    { required: true, message: '请输入发票号码', trigger: 'blur' }
  ],
  invoiceCode: [
    { required: true, message: '请输入发票代码', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' }
  ],
  invoiceDate: [
    { required: true, message: '请选择开票日期', trigger: 'blur' }
  ],
  seller: [
    { required: true, message: '请输入销售方', trigger: 'blur' }
  ]
}

// 获取发票列表
const getInvoiceList = async () => {
  try {
    loading.value = true
    
    const response = await request(`/api/invoice/page?current=${currentPage.value}&size=${pageSize.value}&keyword=${searchForm.invoiceNo || ''}`, {
      method: 'GET'
    })
    
    console.log('发票列表响应:', response) // 添加调试日志
    
    if (response && response.data) {
      invoiceList.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      invoiceList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取发票列表失败:', error)
    ElMessage.error(`获取发票列表失败: ${error.message}`)
    invoiceList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取可关联的报销单列表
const getReimburseList = async () => {
  try {
    loading.value = true
    
    const response = await request('/api/reimburse/available', {
      method: 'GET'
    })
    
    console.log('可用报销单响应:', response) // 添加调试日志
    
    // 检查不同的响应格式
    if (response && response.data) {
      // 如果response.data是数组，直接使用
      if (Array.isArray(response.data)) {
        reimburseList.value = response.data
      } 
      // 如果response.data.data是数组，使用它
      else if (response.data.data && Array.isArray(response.data.data)) {
        reimburseList.value = response.data.data
      }
      // 其他情况，尝试转换为数组
      else {
        reimburseList.value = [response.data].filter(Boolean)
      }
      
      if (reimburseList.value.length === 0) {
        ElMessage.warning('没有可用的报销单，请先创建报销单')
      }
    } else {
      reimburseList.value = []
    }
  } catch (error) {
    console.error('获取报销单列表失败:', error)
    ElMessage.error(`获取报销单列表失败: ${error.message}`)
    reimburseList.value = []
  } finally {
    loading.value = false
  }
}

// 查看发票
const viewInvoice = (row) => {
  currentInvoice.value = row
  viewDialogVisible.value = true
}

// 新增发票
const handleAdd = () => {
  resetInvoiceForm()
  uploadDialogVisible.value = true
}

// 关联报销单
const handleRelate = (row) => {
  currentInvoice.value = row
  relateForm.invoiceId = row.id
  relateForm.reimburseId = null
  getReimburseList()
  relateDialogVisible.value = true
}

// 删除发票
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该发票吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request(`/api/invoice/${row.id}`, {
        method: 'DELETE'
      })
      ElMessage.success('删除成功')
      getInvoiceList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// OCR上传前检查
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 处理文件变化
const handleFileChange = (file) => {
  fileList.value = [file.raw]
}

// 处理OCR识别
const handleOcr = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择发票图片')
    return
  }
  
  try {
    loading.value = true
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', fileList.value[0])
    
    // 使用InvoiceController中的OCR端点
    const response = await request('/api/invoice/ocr', {
      method: 'POST',
      // 不要手动设置Content-Type，让浏览器自动设置为multipart/form-data
      body: formData
    })
    
    // 填充识别结果到表单
    if (response.data) {
      // 切换到手动录入选项卡
      activeTab.value = 'manual'
      
      const ocrData = response.data
      invoiceForm.invoiceNo = ocrData.invoiceNo || ''
      invoiceForm.invoiceCode = ocrData.invoiceCode || ''
      invoiceForm.amount = ocrData.amount || 0
      invoiceForm.invoiceDate = ocrData.invoiceDate || ''
      invoiceForm.seller = ocrData.seller || ''
      
      ElMessage.success('OCR识别成功，请检查并确认信息')
    }
  } catch (error) {
    console.error('OCR识别失败:', error)
    ElMessage.error(`OCR识别失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 提交手动录入的发票
const handleSubmitInvoice = async () => {
  if (!invoiceFormRef.value) return
  
  await invoiceFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        console.log('提交发票数据:', invoiceForm) // 添加调试日志
        
        const result = await request('/api/invoice/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(invoiceForm)
        })
        
        console.log('保存发票响应:', result) // 添加调试日志
        
        if (result.code === 200) {
          ElMessage.success('保存成功')
          uploadDialogVisible.value = false
          getInvoiceList()
        } else {
          ElMessage.error(result.message || '保存失败')
        }
      } catch (error) {
        console.error('保存发票失败:', error)
        ElMessage.error(`保存失败: ${error.message}`)
      } finally {
        loading.value = false
      }
    }
  })
}

// 提交关联报销单
const handleSubmitRelate = async () => {
  if (!relateForm.reimburseId) {
    ElMessage.warning('请选择报销单')
    return
  }
  
  try {
    // 使用InvoiceController中的关联端点
    await request('/api/invoice/relate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(relateForm)
    })
    
    ElMessage.success('关联成功')
    relateDialogVisible.value = false
    getInvoiceList()
  } catch (error) {
    ElMessage.error(`关联失败: ${error.message}`)
  }
}

// 重置发票表单
const resetInvoiceForm = () => {
  invoiceForm.invoiceNo = ''
  invoiceForm.invoiceCode = ''
  invoiceForm.amount = 0
  invoiceForm.invoiceDate = ''
  invoiceForm.seller = ''
  
  if (invoiceFormRef.value) {
    invoiceFormRef.value.resetFields()
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getInvoiceList()
}

// 重置
const handleReset = () => {
  searchForm.invoiceNo = ''
  searchForm.status = ''
  currentPage.value = 1
  getInvoiceList()
}

// 页码变化
const handlePageChange = () => {
  getInvoiceList()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  getInvoiceList()
})
</script>

<style scoped>
.header {
  background-color: #f1f1f1;
  padding: 10px;
  font-size: 20px;
  text-align: center;
}

.search-form {
  margin-bottom: 20px;
}

.el-table {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.invoice-image {
  text-align: center;
  margin-bottom: 20px;
}

.invoice-image img {
  max-width: 100%;
  max-height: 300px;
  border: 1px solid #eee;
}
</style> 