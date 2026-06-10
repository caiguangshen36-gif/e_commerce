<template>
  <div class="page-container">
    <div class="page-header">
      <h2>首页轮播图管理</h2>
      <el-button type="primary" @click="openAddDialog">新增轮播图</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" border stripe>
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="轮播图片" width="160" align="center">
        <template #default="{ row }">
          <el-image
            :src="row.pic"
            style="width: 120px; height: 60px; object-fit: cover; border-radius: 4px"
            preview-teleported
          />
        </template>
      </el-table-column>
      <el-table-column label="跳转地址" prop="url" min-width="200" />
      <el-table-column label="排序" prop="sort" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="轮播图信息" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="轮播图片" required>
          <el-upload
            :http-request="customUpload"
            list-type="picture"
            limit="1"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="form.pic" style="margin-top: 10px">
            <el-image :src="form.pic" style="width: 160px; height: 80px" fit="cover" />
          </div>
        </el-form-item>

        <el-form-item label="跳转地址">
          <el-input v-model="form.url" placeholder="可填商品/活动链接" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input v-model="form.sort" placeholder="数字越小越靠前" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCarouselListService,
  addCarouselService,
  updateCarouselService,
  deleteCarouselService,
  updateCarouselStatusService
} from '@/api/admin/carousel.js'
import { uploadImageService } from '@/api/admin/upload.js'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = ref({
  id: '',
  pic: '',
  url: '',
  sort: 0,
  status: 1
})

// 获取列表
const getList = async () => {
  const res = await getCarouselListService()
  console.log('轮播图列表数据：', res.data)
  list.value = res.data
}

// 上传图片
const customUpload = async (params) => {
  const fd = new FormData()
  fd.append('file', params.file)
  const res = await uploadImageService(fd)
  form.value.pic = res.data
  ElMessage.success('上传成功')
}

// 新增
const openAddDialog = () => {
  isEdit.value = false
  form.value = { id: '', pic: '', url: '', sort: 0, status: 1 }
  dialogVisible.value = true
}

// 编辑
const openEditDialog = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存
const save = async () => {
  if (isEdit.value) {
    await updateCarouselService(form.value)
    ElMessage.success('修改成功')
  } else {
    await addCarouselService(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

// 状态切换
const handleStatusChange = async (row) => {
  await updateCarouselStatusService(row.id)
  ElMessage.success('状态已更新')
}

// 删除
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？')
  await deleteCarouselService(id)
  ElMessage.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
</style>