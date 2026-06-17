<template>
  <div class="edit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ dialogTitle }}</h2>
        </div>
      </template>

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

        <el-form-item>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>


<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '../utils/request'

// 将 dialogVisible 初始值设为 true，使弹出框自动显示
const dialogVisible = ref(false) // 自动显示
const dialogTitle = ref('新增报销')

const form = reactive({
  type: null,
  money: 0,
  reason: ''
})

const rules = {
  type: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  money: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const typeOptions = [
  { id: 1, name: '车补' },
  { id: 2, name: '餐补' },
  { id: 3, name: '交通补' },
  { id: 4, name: '住宿' }
]

const formRef = ref()

// 提交表单
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

        if (result?.data) {
          ElMessage.success('新增报销成功')
          dialogVisible.value = false
        } else {
          ElMessage.error('新增报销失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}
</script>

<style scoped>
.edit-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
