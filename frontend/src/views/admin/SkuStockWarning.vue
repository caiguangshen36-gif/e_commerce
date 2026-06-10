<template>
  <div class="stock-warning-list">
    <el-card header="库存预警商品">
      <!-- 表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="SKU ID" width="100" />
        
        <el-table-column prop="productId" label="商品ID" width="100" />

        <el-table-column prop="skuCode" label="SKU编码" width="180" />

        <el-table-column prop="pic" label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.pic"
              :src="row.pic"
              :preview-src-list="[row.pic]"
              :preview-teleported="true"
              style="width: 50px; height: 50px;"
              fit="cover"
            />
            <span v-else>无图片</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>

        <el-table-column prop="costPrice" label="成本价" width="100">
          <template #default="{ row }">
            ¥{{ row.costPrice }}
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.stock)" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="weight" label="重量(kg)" width="100" />

        <el-table-column prop="volume" label="体积(m³)" width="100" />

        <el-table-column label="规格属性" min-width="200">
          <template #default="{ row }">
            <div v-for="attr in row.skuAttrList" :key="attr.id" class="attr-item">
              <span class="attr-name">{{ attr.attrName }}:</span>
              <span class="attr-value">{{ attr.attrValue }}</span>
            </div>
            <span v-if="!row.skuAttrList || row.skuAttrList.length === 0">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" />

        <el-table-column prop="updateTime" label="更新时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getSkuStockWarningService } from '@/api/admin/productSku.js'; 

// 数据响应式变量
const tableData = ref([]);
const loading = ref(false);

// 根据库存数量返回标签类型（用于颜色区分）
const getStockTagType = (stock) => {
  if (stock <= 0) return 'danger'; // 无库存
  if (stock <= 5) return 'warning'; // 低库存
  return 'success'; // 正常库存（虽然这里是预警列表，但逻辑上可以展示）
};

// 获取数据的方法
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getSkuStockWarningService();
    // 假设 API 返回格式为 { code: 200, data: [...], message: "..." }
    if (res && Array.isArray(res.data)) {
      tableData.value = res.data;
    } else {
      ElMessage.error('API 数据格式异常');
      tableData.value = [];
    }
  } catch (error) {
    console.error('获取库存预警列表失败:', error);
    ElMessage.error('获取库存预警列表失败');
    tableData.value = [];
  } finally {
    loading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.stock-warning-list {
  padding: 20px;
}

.attr-item {
  margin-bottom: 2px;
  font-size: 12px;
}

.attr-name {
  color: #999;
  margin-right: 5px;
}

.attr-value {
  color: #333;
  font-weight: bold;
}
</style>