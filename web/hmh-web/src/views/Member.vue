<template>
    <div class="member-container">
      <el-card>
        <!-- 搜索和操作栏 -->
        <div class="toolbar">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.userName" placeholder="用户名" clearable />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="searchForm.roleName" placeholder="选择角色" clearable>
                <el-option label="员工" value="员工" />
                <el-option label="经理" value="经理" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">添加成员</el-button>
            </el-form-item>
          </el-form>
        </div>
  
        <!-- 成员列表 -->
        <el-table :data="tableData" border style="width: 100%" v-if="tableData.length">
          <el-table-column prop="userName" label="账号">
            <template #default="{ row }">
              {{ row.userName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="trueName" label="姓名">
            <template #default="{ row }">
              {{ row.trueName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="roleName" label="角色">
            <template #default="{ row }">
              <el-tag :type="row.roleName === '经理' ? 'success' : ''">
                {{ row.roleName || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机">
            <template #default="{ row }">
              {{ row.phone || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱">
            <template #default="{ row }">
              {{ row.email || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary" 
                @click="handleEdit(row)"
                v-if="row.roleName !== '管理员'"
              >编辑</el-button>
              <el-button 
                size="small" 
                type="warning" 
                @click="handleChangeRole(row)"
                v-if="row.roleName !== '管理员'"
              >修改角色</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDelete(row)"
                v-if="row.roleName !== '管理员'"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-text">暂无数据</div>
  
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
  
      <!-- 添加/编辑对话框 -->
      <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="500px"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="账号" prop="userName" v-if="!form.id">
            <el-input v-model="form.userName" />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!form.id">
            <el-input v-model="form.password" type="password" />
          </el-form-item>
          <el-form-item label="姓名" prop="trueName">
            <el-input v-model="form.trueName" />
          </el-form-item>
          <el-form-item label="角色" prop="roleName">
            <el-select v-model="form.roleName">
              <el-option label="员工" value="员工" />
              <el-option label="经理" value="经理" />
            </el-select>
          </el-form-item>
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
  
      <!-- 修改角色对话框 -->
      <el-dialog
        title="修改角色"
        v-model="roleDialogVisible"
        width="400px"
      >
        <el-form
          ref="roleFormRef"
          :model="roleForm"
          label-width="80px"
        >
          <el-form-item label="当前角色">
            <el-tag :type="roleForm.oldRole === '经理' ? 'success' : ''">
              {{ roleForm.oldRole }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新角色">
            <el-select v-model="roleForm.newRole">
              <el-option label="员工" value="员工" />
              <el-option label="经理" value="经理" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="roleDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { request } from '../utils/request'
  
  // 搜索表单
  const searchForm = reactive({
    userName: '',
    roleName: ''
  })
  
  // 表格数据
  const tableData = ref([])
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  // 对话框控制
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formRef = ref()
  const roleDialogVisible = ref(false)
  const roleFormRef = ref()
  
  // 表单数据
  const form = ref({
    id: null,
    userName: '',
    password: '',
    trueName: '',
    roleName: '',
    phone: '',
    email: ''
  })
  
  // 角色修改表单
  const roleForm = ref({
    id: null,
    oldRole: '',
    newRole: ''
  })
  
  // 表单验证规则
  const rules = {
    userName: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    trueName: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ],
    roleName: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ],
    phone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }
  
  // 获取列表数据
  const getList = async () => {
    try {
      const params = new URLSearchParams({
        page: currentPage.value,
        rows: pageSize.value,
        userName: searchForm.userName,
        roleName: searchForm.roleName
      })
  
      const data = await request(`/api/user/list?${params.toString()}`)
      if (!data) return
      tableData.value = Array.isArray(data.data) ? data.data : []
      total.value = data.total || 0
    } catch (error) {
      ElMessage.error(error.message)
      tableData.value = []
      total.value = 0
    }
  }
  
  // 搜索
  const handleSearch = () => {
    currentPage.value = 1
    getList()
  }
  
  // 重置
  const handleReset = () => {
    searchForm.userName = ''
    searchForm.roleName = ''
    handleSearch()
  }
  
  // 添加
  const handleAdd = () => {
    dialogTitle.value = '添加成员'
    form.value = {
      id: null,
      userName: '',
      password: '',
      trueName: '',
      roleName: '员工',
      phone: '',
      email: ''
    }
    dialogVisible.value = true
  }
  
  // 编辑
  const handleEdit = (row) => {
    dialogTitle.value = '编辑成员'
    form.value = { ...row }
    dialogVisible.value = true
  }
  
  // 修改角色
  const handleChangeRole = (row) => {
    roleForm.value = {
      id: row.id,
      oldRole: row.roleName,
      newRole: row.roleName === '员工' ? '经理' : '员工'
    }
    roleDialogVisible.value = true
  }
  
  // 删除
  const handleDelete = (row) => {
    ElMessageBox.confirm(
      '确定要删除该成员吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        const data = await request(`/api/user/delete/${row.id}`, {
          method: 'DELETE'
        })
        if (!data) return
        ElMessage.success('删除成功')
        getList()
      } catch (error) {
        ElMessage.error(error.message)
      }
    })
  }
  
  // 提交表单
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const data = await request('/api/user/save', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(form.value)
          })
          if (!data) return
          ElMessage.success(form.value.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          getList()
        } catch (error) {
          ElMessage.error(error.message)
        }
      }
    })
  }
  
  // 提交角色修改
  const handleRoleSubmit = async () => {
    try {
      const data = await request('/api/user/updateRole', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          id: roleForm.value.id,
          roleName: roleForm.value.newRole
        })
      })
      if (!data) return
      ElMessage.success('角色修改成功')
      roleDialogVisible.value = false
      getList()
    } catch (error) {
      ElMessage.error(error.message)
    }
  }
  
  // 分页
  const handlePageChange = () => {
    getList()
  }
  
  // 初始化
  onMounted(() => {
    getList()
  })
  </script>
  
  <style scoped>
  .member-container {
    padding: 20px;
  }
  
  .toolbar {
    margin-bottom: 20px;
  }
  
  .mt-4 {
    margin-top: 16px;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
  
  .empty-text {
    text-align: center;
    color: #909399;
    padding: 30px 0;
  }
  </style>