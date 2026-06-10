<template>
  <div class="address-page">
    <div class="container">
      <div class="address-card">
        <div class="header">
          <h3 class="card-title">我的收货地址</h3>
          <el-button type="primary" @click="openAddDialog">新增地址</el-button>
        </div>

        <!-- 地址列表（订单同款卡片布局） -->
        <div class="address-list">
          <div class="address-item" v-for="addr in addressList" :key="addr.id">
            <div class="radio-wrap">
              <el-radio
                v-model="defaultAddressId"
                :label="addr.id"
                @change="handleSetDefault(addr.id)"
              />
            </div>

            <div class="addr-info">
              <div class="info-row">
                <span class="receiver">{{ addr.receiver }}</span>
                <span class="phone">{{ addr.phone }}</span>
                <el-tag v-if="addr.isDefault" type="success" size="small">默认地址</el-tag>
              </div>
              <p class="addr-detail">
                {{ addr.province }} {{ addr.city }} {{ addr.area }} {{ addr.detail }}
              </p>
            </div>
            <div class="addr-actions">
              <el-button size="small" @click="openEditDialog(addr)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteAddress(addr.id)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="addressList.length === 0" class="empty-box">
          <el-empty description="暂无收货地址" />
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" title="地址管理" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地区">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            style="width: 100%"
            @change="handleRegionChange"
            placeholder="请选择省/市/区"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detail" type="textarea" placeholder="街道、门牌号等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getUserAddressService, 
  addUserAddressService,
  updateUserAddressService,
  deleteUserAddressService,
  setDefaultAddressService 
} from '@/api/user/address.js'
import chinaAreaData from 'china-area-data'

const addressList = ref([])
const dialogVisible = ref(false)

const defaultAddressId = ref(null)

function formatRegionData(rawData) {
  const provinces = rawData['86']
  if (!provinces) return []
  const result = []

  for (const provCode in provinces) {
    const provName = provinces[provCode]
    const provItem = { value: provName, label: provName, children: [] }

    const cities = rawData[provCode] || {}
    for (const cityCode in cities) {
      const cityName = cities[cityCode]
      const cityItem = { value: cityName, label: cityName, children: [] }

      const areas = rawData[cityCode] || {}
      for (const areaCode in areas) {
        const areaName = areas[areaCode]
        cityItem.children.push({ value: areaName, label: areaName })
      }
      provItem.children.push(cityItem)
    }
    result.push(provItem)
  }
  return result
}
const regionOptions = ref(formatRegionData(chinaAreaData))

// 地址表单数据
const addressForm = ref({
  id: null,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  area: '',
  detail: '',
  region: [],
  isDefault: false
})

// 省市区选择
const handleRegionChange = (val) => {
  if (!val || val.length < 3) return
  addressForm.value.province = val[0]
  addressForm.value.city = val[1]
  addressForm.value.area = val[2]
}

// 打开新增
const openAddDialog = () => {
  addressForm.value = {
    id: null, receiver: '', phone: '',
    province: '', city: '', area: '',
    detail: '', region: [], isDefault: false
  }
  dialogVisible.value = true
}

// 打开编辑
const openEditDialog = (addr) => {
  addressForm.value = { ...addr }
  addressForm.value.region = [addr.province, addr.city, addr.area]
  dialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressForm.value.receiver || !addressForm.value.phone || !addressForm.value.area) {
    ElMessage.warning('请完善必填信息')
    return
  }

  try {
    const submitData = {
      ...addressForm.value,
      isDefault: addressForm.value.isDefault ? 1 : 0
    }

    if (submitData.id) {
      await updateUserAddressService(submitData)
      ElMessage.success('修改成功')
    } else {
      await addUserAddressService(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadMyAddress() 
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 删除
const deleteAddress = async (id) => {
  try {
    await deleteUserAddressService(id)
    ElMessage.success('删除成功')
    loadMyAddress()
  } catch (err) {
    ElMessage.error('删除失败，请重试')
  }
}

// 加载当前用户地址
const loadMyAddress = async () => {
  const res = await getUserAddressService()
  addressList.value = res.data || []
  // 初始化时设置默认地址ID
  const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
  if (defaultAddr) {
    defaultAddressId.value = defaultAddr.id
  }
}

// 设置默认地址
const handleSetDefault = async (addressId) => {
  try {
    await setDefaultAddressService(addressId)
    ElMessage.success('默认地址设置成功')
    loadMyAddress()
  } catch (err) {
    ElMessage.error('设置默认地址失败')
    const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
    defaultAddressId.value = defaultAddr ? defaultAddr.id : null
  }
}

onMounted(() => {
  loadMyAddress()
})
</script>

<style scoped>
.address-page { 
  width: 100%;
}
.container { 
  width: 100%;
}
.address-card { 
  background: #fff; 
  padding: 20px; 
  border-radius: 8px; 
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 16px; 
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.address-list { 
  display: flex; 
  flex-direction: column; 
  gap: 16px; 
}
.address-item { 
  padding: 16px; 
  border: 1px solid #eee; 
  border-radius: 8px; 
  display: flex; 
  align-items: flex-start;
  gap: 12px;
  transition: all 0.2s;
}
.address-item:hover {
  border-color: #409eff;
}
.radio-wrap {
  padding-top: 2px;
}
.addr-info {
  flex: 1;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.receiver {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}
.phone {
  font-size: 14px;
  color: #666;
}
.addr-detail {
  font-size: 14px;
  color: #666;
  margin: 0;
}
.addr-actions {
  display: flex;
  gap: 8px;
}
.empty-box {
  padding: 60px 0;
  text-align: center;
}
</style>