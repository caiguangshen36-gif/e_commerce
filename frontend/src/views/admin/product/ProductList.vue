<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商品列表</h2>
      <el-button type="primary" @click="openAddDialog">新增商品</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-form inline class="search-form" style="margin-bottom: 20px;" :model="query">
      <el-form-item label="商品名称">
        <el-input v-model="query.keyword" placeholder="请输入商品名称" style="width: 200px;" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="query.categoryId" placeholder="请选择分类" style="width: 150px;" clearable>
          <el-option
            v-for="cat in filteredCategoryList"
            :key="cat.id"
            :label="cat.categoryName"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="请选择状态" style="width: 120px;" clearable>
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="productList" border stripe v-loading="tableLoading">
      <el-table-column label="ID" prop="id" width="80" align="center" />
      <el-table-column label="商品图片" align="center" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.pic"
            style="width: 50px; height: 50px; border-radius: 4px;"
            fit="cover"
            preview-teleported
            :preview-src-list="[row.pic]"
          />
        </template>
      </el-table-column>
      <el-table-column label="商品名称" prop="productName" min-width="180" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" align="center" width="120" />

      <!-- 是否热门（开关） -->
      <el-table-column label="是否热门" align="center" width="110">
        <template #default="{ row }">
          <el-switch
            v-model="row.isHot"
            :active-value="1"
            :inactive-value="0"
            :loading="actionLoading[row.id + '_hot']"
            @change="handleToggleHot(row)"
          />
        </template>
      </el-table-column>

      <!-- 热门排序 -->
      <el-table-column label="热门排序" align="center" width="130">
        <template #default="{ row }">
          <el-input-number
            v-model="row.hotSort"
            :min="0"
            :max="999"
            size="small"
            :disabled="!row.isHot"
            @change="handleUpdateHotSort(row)"
            placeholder="排序值"
          />
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" width="180" />
      <el-table-column label="操作" align="center" width="300" fixed="right">
        <template #default="{ row }">
          <el-button type="info" link @click="openViewDetailDialog(row)">查看详情</el-button>
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button
            type="success"
            link
            :loading="actionLoading[row.id + '_status']"
            @click="handleUpdateStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button type="danger" link :loading="actionLoading[row.id + '_delete']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="getProductList"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="所属分类" required>
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品主图" required>
          <el-upload
            :http-request="customUpload"
            list-type="picture"
            :limit="1"
            :on-exceed="() => ElMessage.warning('只能上传一张主图')"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="form.pic" style="margin-top: 8px;">
            <el-image :src="form.pic" style="width: 100px; height: 100px;" fit="cover" />
          </div>
        </el-form-item>

        <el-form-item label="是否热门">
          <el-radio-group v-model="form.isHot">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="热门排序">
          <el-input-number v-model="form.hotSort" :min="0" :max="999" :disabled="!form.isHot" placeholder="数字越小越靠前" />
        </el-form-item>

        <el-form-item label="商品详情">
          <div style="display: flex; flex-direction: column; gap: 8px; width: 100%;">
            <el-input
              v-model="form.detailHtml"
              type="textarea"
              :rows="6"
              placeholder="请输入商品详情，支持HTML格式"
            />
            <el-button
              type="primary"
              link
              style="align-self: flex-end;"
              @click="openAiDetailDialog"
            >
              <el-icon><Document /></el-icon> AI生成详情
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI生成详情弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI生成商品详情" width="600px" destroy-on-close>
      <el-form :model="aiForm" label-width="120px">
        <el-form-item label="商品名称" required>
          <el-input v-model="aiForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" required>
          <el-select v-model="aiForm.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品售价" required>
          <el-input-number
            v-model="aiForm.price"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 100%;"
            placeholder="请输入商品售价"
          />
        </el-form-item>
        <el-form-item label="商品原价">
          <el-input-number
            v-model="aiForm.originalPrice"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="商品卖点">
          <el-input v-model="aiForm.sellingPoints" type="textarea" :rows="2" placeholder="多个用逗号分隔，例如：高性能,AI功能,长续航" />
        </el-form-item>
        <el-form-item label="商品规格">
          <el-input v-model="aiForm.spec" type="textarea" :rows="2" placeholder="例如：屏幕:6.7英寸,存储:256G,颜色:黑色" />
        </el-form-item>
        <el-form-item label="适用人群/场景">
          <el-input v-model="aiForm.targetUser" placeholder="例如：商务人士,摄影爱好者" />
        </el-form-item>
        <el-form-item label="商品品牌">
          <el-input v-model="aiForm.brand" placeholder="例如：Apple" />
        </el-form-item>
        <el-form-item label="商品标签">
          <el-input v-model="aiForm.tags" placeholder="多个用逗号分隔，例如：新品,爆款,旗舰机" />
        </el-form-item>
        <el-form-item label="商品库存">
          <el-input-number v-model="aiForm.stock" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="文案类型">
          <el-select v-model="aiForm.descType" style="width: 100%;">
            <el-option label="标题(TITLE)" value="TITLE" />
            <el-option label="详情(DETAIL)" value="DETAIL" />
            <el-option label="营销文案(MARKETING)" value="MARKETING" />
            <el-option label="SEO文案(SEO)" value="SEO" />
            <el-option label="综合(ALL)" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="文案风格">
          <el-select v-model="aiForm.style" style="width: 100%;">
            <el-option label="专业" value="专业" />
            <el-option label="活泼" value="活泼" />
            <el-option label="简洁" value="简洁" />
            <el-option label="高端" value="高端" />
            <el-option label="接地气" value="接地气" />
            <el-option label="种草" value="种草" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标平台">
          <el-select v-model="aiForm.platform" style="width: 100%;">
            <el-option label="淘宝" value="淘宝" />
            <el-option label="京东" value="京东" />
            <el-option label="抖音" value="抖音" />
            <el-option label="小红书" value="小红书" />
            <el-option label="微信朋友圈" value="朋友圈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="额外需求">
          <el-input v-model="aiForm.extraRequirement" type="textarea" :rows="3" placeholder="例如：突出性价比,适合学生党,强调拍照功能" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiGenerating" @click="generateAiDetail">生成并填充</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="viewDetailVisible" title="商品详情" width="700px">
      <div class="detail-header">
        <el-image :src="viewDetailData.pic" style="width: 100px; height: 100px; border-radius: 8px;" fit="cover" />
        <div class="detail-info">
          <h3>{{ viewDetailData.productName }}</h3>
          <p><strong>分类：</strong>{{ viewDetailData.categoryName }}</p>
          <p><strong>状态：</strong>
            <el-tag :type="viewDetailData.status === 1 ? 'success' : 'danger'">
              {{ viewDetailData.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </p>
          <p><strong>创建时间：</strong>{{ viewDetailData.createTime }}</p>
          <p><strong>是否热门：</strong> {{ viewDetailData.isHot === 1 ? '是' : '否' }}</p>
          <p v-if="viewDetailData.isHot"><strong>热门排序：</strong> {{ viewDetailData.hotSort }}</p>
        </div>
      </div>
      <el-divider />
      <div class="detail-content">
        <h4>商品详情：</h4>
        <div class="html-content" v-html="viewDetailData.detailHtml || '<span style=&quot;color:#999&quot;>暂无商品详情</span>'"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import {
  getProductListService,
  addProductService,
  updateProductService,
  deleteProductService,
  updateProductStatusService,
  setHotProductService
} from '@/api/admin/product.js'
import { uploadImageService } from '@/api/admin/upload.js'
import { getAllCategoryListService } from '@/api/admin/category.js'
import { aiGenerateGoodsDescService } from '@/api/admin/ai.js'

// 查询与列表 
const tableLoading = ref(false)
const saveLoading = ref(false)
const actionLoading = reactive({})

const query = reactive({
  keyword: '',      
  categoryId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const productList = ref([])
const total = ref(0)

const getProductList = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || null,
      status: query.status ?? null,
      categoryId: query.categoryId ?? null
    }

    const res = await getProductListService(params)

    if (res?.code === 200) {
      const pageData = res.data || {}
      productList.value = pageData.records || pageData.list || pageData.rows || []
      total.value = pageData.total || 0
    } else {
      productList.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取商品列表失败', err)
    ElMessage.error('获取商品列表失败')
    productList.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  getProductList()
}

const handleSizeChange = () => {
  query.pageNum = 1
  getProductList()
}

// 重置查询
const resetQuery = () => {
  Object.assign(query, {
    keyword: '',
    categoryId: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10
  })
  getProductList()
}

// 分类列表 
const categoryList = ref([])

const getCategoryList = async () => {
  try {
    const res = await getAllCategoryListService()
    categoryList.value = res.data || []
  } catch (err) {
    console.error('获取分类列表失败', err)
  }
}

// 过滤掉禁用的分类（仅用于搜索栏）
const filteredCategoryList = computed(() => {
  return categoryList.value.filter(cat => cat.status !== 0)
})

//新增/编辑弹窗 
const dialogVisible = ref(false)
const form = ref({})

const getDefaultForm = () => ({
  id: null,
  productName: '',
  categoryId: null,
  pic: '',
  detailHtml: '',
  status: 1,
  isHot: 0,
  hotSort: 0
})

const openAddDialog = () => {
  form.value = getDefaultForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

// 自定义上传
const customUpload = async (params) => {
  try {
    const formData = new FormData()
    formData.append('file', params.file)
    const res = await uploadImageService(formData)
    form.value.pic = res.data
    ElMessage.success('上传成功')
  } catch (err) {
    ElMessage.error('上传失败')
  }
}

const saveProduct = async () => {
  if (!form.value.productName) return ElMessage.warning('请输入商品名称')
  if (!form.value.categoryId) return ElMessage.warning('请选择所属分类')
  if (!form.value.pic) return ElMessage.warning('请上传商品主图')

  saveLoading.value = true
  try {
    if (form.value.id) {
      await updateProductService(form.value)
      ElMessage.success('编辑成功')
    } else {
      await addProductService(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getProductList()
  } catch (err) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const handleUpdateStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const oldStatus = row.status
  const loadingKey = row.id + '_status'

  actionLoading[loadingKey] = true
  row.status = newStatus

  try {
    await updateProductStatusService(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
  } catch (err) {
    row.status = oldStatus
    ElMessage.error('状态更新失败')
  } finally {
    actionLoading[loadingKey] = false
  }
}

const handleToggleHot = async (row) => {
  const oldIsHot = row.isHot === 1 ? 0 : 1
  const loadingKey = row.id + '_hot'

  actionLoading[loadingKey] = true
  try {
    await setHotProductService({
      id: row.id,
      isHot: row.isHot,
      hotSort: row.hotSort || 0
    })
    ElMessage.success(row.isHot === 1 ? '已设为热门' : '已取消热门')
  } catch (err) {
    row.isHot = oldIsHot
    ElMessage.error('操作失败')
  } finally {
    actionLoading[loadingKey] = false
  }
}

const handleUpdateHotSort = async (row) => {
  const loadingKey = row.id + '_hot'
  actionLoading[loadingKey] = true
  try {
    await setHotProductService({
      id: row.id,
      isHot: row.isHot || 0,
      hotSort: row.hotSort
    })
    ElMessage.success('排序已保存')
  } catch (err) {
    ElMessage.error('排序保存失败')
  } finally {
    actionLoading[loadingKey] = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.productName}」？`, '提示', { type: 'warning' })

    const loadingKey = row.id + '_delete'
    actionLoading[loadingKey] = true

    try {
      await deleteProductService(row.id)
      ElMessage.success('删除成功')
      if (productList.value.length === 1 && query.pageNum > 1) {
        query.pageNum--
      }
      getProductList()
    } finally {
      actionLoading[loadingKey] = false
    }
  } catch (err) {
    if (err !== 'cancel') console.error('删除失败', err)
  }
}

//  详情弹窗 
const viewDetailVisible = ref(false)
const viewDetailData = ref({})

const openViewDetailDialog = (row) => {
  viewDetailData.value = { ...row }
  viewDetailVisible.value = true
}

// AI生成详情
const aiDialogVisible = ref(false)
const aiGenerating = ref(false)
const aiForm = ref({})

const openAiDetailDialog = () => {
  aiForm.value = {
    productName: form.value.productName || '',
    categoryId: form.value.categoryId || null,
    price: form.value.price || 0,
    originalPrice: form.value.originalPrice || null,
    sellingPoints: '',
    spec: '',
    targetUser: '',
    brand: '',
    tags: '',
    stock: form.value.stock || null,
    pic: form.value.pic || '',
    descType: 'DETAIL',
    style: '',
    platform: '',
    extraRequirement: ''
  }
  aiDialogVisible.value = true
}

const generateAiDetail = async () => {
  if (!aiForm.value.productName) return ElMessage.warning('请输入商品名称')
  if (!aiForm.value.categoryId) return ElMessage.warning('请选择商品分类')
  if (!aiForm.value.price) return ElMessage.warning('请输入商品售价')

  aiGenerating.value = true
  try {
    const res = await aiGenerateGoodsDescService(aiForm.value)
    const generatedDetail = res.data?.detailHtml || res.data

    if (generatedDetail) {
      form.value.detailHtml = generatedDetail
      ElMessage.success('AI生成成功')
      aiDialogVisible.value = false
    } else {
      ElMessage.warning('AI未生成有效内容')
    }
  } catch (err) {
    console.error('AI生成失败：', err)
    ElMessage.error('生成失败：' + (err.message || '请检查输入参数'))
  } finally {
    aiGenerating.value = false
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  Promise.all([getCategoryList(), getProductList()])
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

.detail-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.detail-info {
  flex: 1;
}

.html-content {
  line-height: 1.6;
  color: #333;
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.search-form {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.search-form .el-form-item:last-of-type {
  margin-left: auto;
}

/* AI生成详情弹窗样式优化 */
.el-dialog .el-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

/* 美化滚动条 */
.el-dialog .el-form::-webkit-scrollbar {
  width: 6px;
}

.el-dialog .el-form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.el-dialog .el-form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.el-dialog .el-form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>