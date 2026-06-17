<template>
  <el-container>
    <!--    <el-header class="header">-->
    <!--      <h2>公告列表</h2>-->
    <!--    </el-header>-->

    <el-main>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题关键词" clearable/>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
              v-if="currentUser.roleName === '管理员'"
              type="success"
              @click="handleAdd"
          >发布新公告
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
          :data="announcementList"
          style="width: 100%"
          stripe
          border
          v-loading="loading"
      >
        <el-table-column label="公告标题" prop="title" min-width="200"></el-table-column>
        <el-table-column label="发布人" prop="createUserName" width="120"></el-table-column>
        <el-table-column
            label="发布时间"
            prop="createTime"
            width="180"
            :formatter="(row) => formatDateTime(row.createTime)"
        ></el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewAnnouncement(row)">查看</el-button>
            <template v-if="currentUser.roleName === '管理员'">
              <el-button
                  size="small"
                  type="primary"
                  @click="handleEdit(row)"
              >编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(row)"
              >删除
              </el-button>
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
    </el-main>

    <!-- 查看/编辑对话框 -->
    <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="600px"
    >
      <el-divider></el-divider>
      <el-form
          v-if="isEdit"
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="100px"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title"/>
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
              type="textarea"
              v-model="form.content"
              :rows="6"
          />
        </el-form-item>
      </el-form>

      <div v-else class="view-content">
        <h3>{{ currentAnnouncement.title }}</h3>
        <div class="announcement-info">
          <span>发布人：{{ currentAnnouncement.createUserName }}</span>
          <span>发布时间：{{ formatDateTime(currentAnnouncement.createTime) }}</span>
        </div>
        <div class="announcement-content">{{ currentAnnouncement.content }}</div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
              v-if="isEdit"
              type="primary"
              @click="handleSubmit"
          >确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {request} from '../utils/request'

const currentUser = ref(JSON.parse(localStorage.getItem('currentUser') || '{}'))
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const announcementList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const currentAnnouncement = ref({})

const searchForm = reactive({
  title: ''
})

const form = reactive({
  id: null,
  title: '',
  content: ''
})

const rules = {
  title: [
    {required: true, message: '请输入公告标题', trigger: 'blur'},
    {min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur'}
  ],
  content: [
    {required: true, message: '请输入公告内容', trigger: 'blur'}
  ]
}

// 获取公告列表
const getList = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: currentPage.value,
      rows: pageSize.value,
      title: searchForm.title
    })

    const result = await request(`/api/announcement/list?${params.toString()}`)
    console.log('获取到的公告列表数据:', result)

    // 确保我们正确处理返回的数据
    if (result && result.data) {
      announcementList.value = result.data.records || []
      total.value = result.data.total || result.data.records.length || 0
      console.log('处理后的公告列表:', announcementList.value)
    } else {
      // 如果没有数据，设置为空数组
      announcementList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error(`获取公告列表失败: ${error.message}`)
    // 出错时也要清空数据
    announcementList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查看公告
const viewAnnouncement = (row) => {
  currentAnnouncement.value = row
  isEdit.value = false
  dialogTitle.value = '内容'
  dialogVisible.value = true
}

// 新增公告
const handleAdd = () => {
  form.id = null
  form.title = ''
  form.content = ''
  isEdit.value = true
  dialogTitle.value = '发布新公告'
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row) => {
  form.id = row.id
  form.title = row.title
  form.content = row.content
  isEdit.value = true
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

// 删除公告
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request(`/api/announcement/${row.id}`, {
        method: 'DELETE'
      })
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request('/api/announcement/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(form)
        })

        ElMessage.success('保存成功')
        dialogVisible.value = false
        getList()
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 重置
const handleReset = () => {
  searchForm.title = ''
  currentPage.value = 1
  getList()
}

// 页码变化
const handlePageChange = () => {
  getList()
}

// 格式化日期时间
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

onMounted(() => {
  getList()
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

.view-content {
  padding: 20px;
}

.announcement-info {
  color: #666;
  margin: 10px 0;
  font-size: 14px;
}

.announcement-info span {
  margin-right: 20px;
}

.announcement-content {
  margin-top: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.dialog-top {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  font-size: 16px;
  color: #333;
  line-height: 1.5;
}
</style>