<template>
  <div class="order-confirm-wrapper">
    <div class="order-confirm-page">
      <div class="page-header">
        <h1>确认订单</h1>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
      <a-skeleton :loading="true" active :paragraph="{ rows: 8 }" />
    </div>

    <!-- 订单内容 -->
    <div v-else class="order-content">
      <!-- 收货地址 -->
      <a-card title="收货地址" class="section-card">
        <template #extra>
          <a-button type="link" @click="showAddressModal = true">
            <i class="fas fa-plus"></i>
            新增地址
          </a-button>
        </template>

        <div v-if="addressList.length === 0" class="empty-state">
          <i class="fas fa-map-marker-alt"></i>
          <p>暂无收货地址，请先添加地址</p>
          <a-button type="primary" @click="showAddressModal = true">
            添加收货地址
          </a-button>
        </div>

        <a-radio-group v-else v-model:value="selectedAddressId" class="address-list">
          <div
            v-for="addr in addressList"
            :key="addr.id"
            class="address-item"
            :class="{ 'selected': selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <a-radio :value="addr.id">
              <div class="address-info">
                <div class="address-header">
                  <span class="receiver-name">{{ addr.receiver }}</span>
                  <span class="receiver-phone">{{ addr.phone }}</span>
                  <a-tag v-if="addr.isDefault" color="blue">默认地址</a-tag>
                </div>
                <div class="address-detail">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
                </div>
              </div>
            </a-radio>
          </div>
        </a-radio-group>
      </a-card>

      <!-- 商品信息 -->
      <a-card title="商品信息" class="section-card">
        <div v-if="product" class="product-info">
          <div class="product-image">
            <img v-if="product.coverFilePath" :src="product.coverFilePath" :alt="product.title" />
            <div v-else class="no-image">
              <i class="fas fa-image"></i>
            </div>
          </div>
          <div class="product-details">
            <h3>{{ product.title }}</h3>
            <p class="product-category">{{ product.categoryName }}</p>
            <div class="product-price-row">
              <span class="price-label">单价：</span>
              <span class="price-value">¥{{ product.price }}</span>
            </div>
            <div class="product-quantity-row">
              <span class="quantity-label">数量：</span>
              <span class="quantity-value">{{ orderQuantity }}</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 订单备注 -->
      <a-card title="订单备注" class="section-card">
        <a-textarea
          v-model:value="orderRemark"
          placeholder="选填，请输入订单备注信息"
          :rows="3"
          :maxlength="200"
          show-count
        />
      </a-card>

      <!-- 订单金额 -->
      <a-card class="section-card amount-card">
        <div class="amount-row">
          <span class="amount-label">商品金额：</span>
          <span class="amount-value">¥{{ totalAmount }}</span>
        </div>
        <div class="amount-row total-row">
          <span class="amount-label">应付总额：</span>
          <span class="amount-value total-amount">¥{{ totalAmount }}</span>
        </div>
      </a-card>

      <!-- 提交订单 -->
      <div class="submit-section">
        <a-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!selectedAddressId"
          @click="handleSubmitOrder"
        >
          提交订单
        </a-button>
      </div>
    </div>

    <!-- 新增地址弹窗 -->
    <a-modal
      v-model:open="showAddressModal"
      title="新增收货地址"
      :width="600"
      @ok="handleAddAddress"
      @cancel="resetAddressForm"
    >
      <a-form
        :model="addressForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item label="收货人" required>
          <a-input v-model:value="addressForm.receiver" placeholder="请输入收货人姓名" />
        </a-form-item>
        <a-form-item label="联系电话" required>
          <a-input v-model:value="addressForm.phone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="省份" required>
          <a-input v-model:value="addressForm.province" placeholder="请输入省份" />
        </a-form-item>
        <a-form-item label="城市" required>
          <a-input v-model:value="addressForm.city" placeholder="请输入城市" />
        </a-form-item>
        <a-form-item label="区/县" required>
          <a-input v-model:value="addressForm.district" placeholder="请输入区/县" />
        </a-form-item>
        <a-form-item label="详细地址" required>
          <a-textarea
            v-model:value="addressForm.detail"
            placeholder="请输入详细地址"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="设为默认">
          <a-switch v-model:checked="addressForm.isDefault" />
        </a-form-item>
      </a-form>
    </a-modal>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getProductById } from '@/api/ShopProductApi'
import { getUserAddressList, createAddress } from '@/api/AddressApi'
import { createOrder } from '@/api/OrderApi'

// ========== 路由 ==========
const route = useRoute()
const router = useRouter()

// ========== 响应式数据 ==========
const loading = ref(true)
const submitting = ref(false)
const product = ref(null)
const orderQuantity = ref(1)
const addressList = ref([])
const selectedAddressId = ref(null)
const orderRemark = ref('')
const showAddressModal = ref(false)

// 新增地址表单
const addressForm = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

// ========== 计算属性 ==========
const totalAmount = computed(() => {
  if (!product.value) return '0.00'
  return (product.value.price * orderQuantity.value).toFixed(2)
})

// ========== 生命周期 ==========
onMounted(() => {
  const productId = route.query.productId
  const quantity = route.query.quantity

  if (!productId || !quantity) {
    message.error('订单信息不完整')
    router.push('/shop')
    return
  }

  orderQuantity.value = parseInt(quantity)
  fetchProductDetail(productId)
  fetchAddressList()
})

// ========== 方法 ==========
const fetchProductDetail = (productId) => {
  getProductById(productId, {
    onSuccess: (res) => {
      product.value = res
      loading.value = false
    },
    onError: (error) => {
      message.error('获取商品信息失败：' + error.message)
      loading.value = false
    }
  })
}

const fetchAddressList = () => {
  getUserAddressList({
    onSuccess: (res) => {
      addressList.value = res || []
      // 自动选择默认地址
      const defaultAddr = addressList.value.find(addr => addr.isDefault)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (addressList.value.length > 0) {
        selectedAddressId.value = addressList.value[0].id
      }
    },
    onError: (error) => {
      message.error('获取地址列表失败：' + error.message)
    }
  })
}

const handleAddAddress = () => {
  // 验证表单
  if (!addressForm.value.receiver || !addressForm.value.phone ||
      !addressForm.value.province || !addressForm.value.city ||
      !addressForm.value.district || !addressForm.value.detail) {
    message.warning('请填写完整的地址信息')
    return
  }

  createAddress(addressForm.value, {
    successMsg: '地址添加成功',
    onSuccess: (res) => {
      showAddressModal.value = false
      resetAddressForm()
      fetchAddressList()
    },
    onError: (error) => {
      message.error('添加地址失败：' + error.message)
    }
  })
}

const resetAddressForm = () => {
  addressForm.value = {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  }
}

const handleSubmitOrder = () => {
  if (!selectedAddressId.value) {
    message.warning('请选择收货地址')
    return
  }

  submitting.value = true

  const orderData = {
    productId: product.value.id,
    quantity: orderQuantity.value,
    addressId: selectedAddressId.value,
    remark: orderRemark.value || null
  }

  createOrder(orderData, {
    successMsg: '订单提交成功',
    onSuccess: (res) => {
      submitting.value = false
      // 跳转到订单详情页
      router.push(`/orders/${res.id}`)
    },
    onError: (error) => {
      message.error('订单提交失败：' + error.message)
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.order-confirm-wrapper {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.order-confirm-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #2c2c2c;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 3px;
}

.loading-container {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;

  :deep(.ant-card-head) {
    border-bottom: 2px solid #e8e8e8;
  }

  :deep(.ant-card-head-title) {
    font-size: 20px;
    font-weight: 600;
    color: #2c2c2c;
    letter-spacing: 1px;
  }

  :deep(.ant-btn-link) {
    color: #8b4513;

    &:hover {
      color: #a0522d;
    }

    i {
      margin-right: 6px;
    }
  }
}

/* 收货地址 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
}

.empty-state i {
  font-size: 56px;
  margin-bottom: 20px;
  color: #c5a572;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 15px;
}

.empty-state :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.address-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.address-item {
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #c5a572;
  background: rgba(139, 69, 19, 0.02);
}

.address-item.selected {
  border-color: #8b4513;
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.04) 0%, rgba(197, 165, 114, 0.06) 100%);
}

.address-info {
  width: 100%;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 10px;
}

.receiver-name {
  font-size: 17px;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 0.5px;
}

.receiver-phone {
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

.address-detail {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
}

/* 商品信息 */
.product-info {
  display: flex;
  gap: 24px;
}

.product-image {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  background: #faf8f3;
  flex-shrink: 0;
  border: 1px solid #e8e8e8;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.no-image i {
  font-size: 48px;
}

.product-details {
  flex: 1;
}

.product-details h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}

.product-category {
  font-size: 14px;
  color: #999;
  margin-bottom: 16px;
}

.product-price-row,
.product-quantity-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.price-label,
.quantity-label {
  font-size: 15px;
  color: #666;
  margin-right: 10px;
  font-weight: 600;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #8b4513;
}

.quantity-value {
  font-size: 17px;
  color: #2c2c2c;
  font-weight: 600;
}

/* 订单金额 */
.amount-card {
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.04) 0%, rgba(197, 165, 114, 0.06) 100%);
  border: 2px solid #e8e8e8;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  font-size: 16px;
}

.amount-row.total-row {
  border-top: 2px solid #e8e8e8;
  padding-top: 18px;
  margin-top: 10px;
}

.amount-label {
  color: #666;
  font-weight: 600;
}

.amount-value {
  font-weight: 700;
  color: #2c2c2c;
}

.total-amount {
  font-size: 28px;
  color: #8b4513;
}

/* 提交订单 */
.submit-section {
  display: flex;
  justify-content: flex-end;
  padding: 24px 0;
}

.submit-section :deep(.ant-btn-primary) {
  min-width: 220px;
  height: 52px;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .order-confirm-page {
    padding: 16px;
  }

  .product-info {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 200px;
  }
}
</style>

