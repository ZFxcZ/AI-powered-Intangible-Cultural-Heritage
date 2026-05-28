<template>
  <div class="product-detail-wrapper">
    <div class="product-detail-page">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
      <a-skeleton :loading="true" active :paragraph="{ rows: 10 }" />
    </div>

    <!-- 商品详情 -->
    <div v-else-if="product" class="detail-container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <a-breadcrumb>
          <a-breadcrumb-item>
            <router-link to="/">首页</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>
            <router-link to="/shop">商城</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>{{ product.title }}</a-breadcrumb-item>
        </a-breadcrumb>
      </div>

      <!-- 主体内容 -->
      <div class="detail-body">
        <!-- 左侧：商品图片 -->
        <div class="image-section">
          <!-- 主图展示 -->
          <div class="main-image">
            <img
              v-if="mainImage"
              :src="mainImage.filePath"
              :alt="mainImage.originalName || product.title"
            />
            <div v-else class="no-image">
              <i class="fas fa-image"></i>
              <p>暂无图片</p>
            </div>
          </div>

          <!-- 缩略图列表 -->
          <div v-if="imageList.length > 1" class="image-thumbnails">
            <div
              v-for="(image, index) in imageList"
              :key="image.id"
              class="thumbnail-item"
              :class="{ active: currentImageIndex === index }"
              @click="handleImageChange(index)"
            >
              <img
                :src="image.filePath"
                :alt="image.originalName"
              />
            </div>
          </div>
        </div>

        <!-- 右侧：商品信息 -->
        <div class="info-section">
          <div class="product-header">
            <h1 class="product-title">{{ product.title }}</h1>
            <p v-if="product.subtitle" class="product-subtitle">
              {{ product.subtitle }}
            </p>
            <div class="product-category">
              <a-tag color="blue">{{ product.categoryName }}</a-tag>
            </div>
          </div>

          <div class="product-price-box">
            <div class="price-row">
              <span class="price-label">价格</span>
              <div class="price-value">
                <span class="price-symbol">¥</span>
                <span class="price-number">{{ product.price }}</span>
              </div>
            </div>
            <div class="stock-row">
              <span class="stock-label">库存</span>
              <span class="stock-value" :class="{ 'out-of-stock': product.stock <= 0 }">
                {{ product.stock > 0 ? `${product.stock} 件` : '已售罄' }}
              </span>
            </div>
          </div>

          <div class="purchase-section">
            <div class="quantity-selector">
              <span class="quantity-label">数量</span>
              <a-input-number
                v-model:value="quantity"
                :min="1"
                :max="product.stock"
                :disabled="product.stock <= 0"
              />
            </div>

            <div class="action-buttons">
              <a-button
                type="primary"
                size="large"
                block
                :disabled="product.stock <= 0"
                @click="handleBuyNow"
              >
                <i class="fas fa-bolt"></i>
                立即购买
              </a-button>
            </div>
          </div>

          <div class="product-tips">
            <div class="tip-item">
              <i class="fas fa-shield-alt"></i>
              <span>正品保证</span>
            </div>
            <div class="tip-item">
              <i class="fas fa-truck"></i>
              <span>全国包邮</span>
            </div>
            <div class="tip-item">
              <i class="fas fa-undo"></i>
              <span>7天无理由退换</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品详情描述 -->
      <div class="detail-description">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="detail" tab="商品详情">
            <div class="description-content">
              <div v-if="product.detail" class="detail-html" v-html="product.detail"></div>
              <div v-else class="no-detail">
                <p>暂无详细描述</p>
              </div>
            </div>
          </a-tab-pane>
          
          <a-tab-pane key="info" tab="商品信息">
            <div class="info-content">
              <a-descriptions bordered :column="2">
                <a-descriptions-item label="商品ID">
                  {{ product.id }}
                </a-descriptions-item>
                <a-descriptions-item label="商品分类">
                  {{ product.categoryName }}
                </a-descriptions-item>
                <a-descriptions-item label="商品价格">
                  ¥{{ product.price }}
                </a-descriptions-item>
                <a-descriptions-item label="库存数量">
                  {{ product.stock }} 件
                </a-descriptions-item>
                <a-descriptions-item label="上架时间" :span="2">
                  {{ product.createTime }}
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 推荐商品 -->
      <div v-if="recommendProducts.length > 0" class="recommend-section">
        <h2>相关推荐</h2>
        <div class="recommend-grid">
          <div
            v-for="item in recommendProducts"
            :key="item.id"
            class="recommend-card"
            @click="handleProductClick(item.id)"
          >
            <div class="recommend-image">
              <img
                v-if="item.coverFilePath"
                :src="item.coverFilePath"
                :alt="item.title"
              />
              <div v-else class="no-image">
                <i class="fas fa-image"></i>
              </div>
            </div>
            <div class="recommend-info">
              <h3>{{ item.title }}</h3>
              <div class="recommend-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <i class="fas fa-exclamation-circle"></i>
      <p>商品不存在或已下架</p>
      <a-button type="primary" @click="$router.push('/shop')">
        返回商城
      </a-button>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getProductById, getProductRecommendations } from '@/api/ShopProductApi'
import { useUserStore } from '@/store/user'

// ========== 路由 ==========
const route = useRoute()
const router = useRouter()

// ========== 响应式数据 ==========
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const activeTab = ref('detail')
const recommendProducts = ref([])
const imageList = ref([])
const currentImageIndex = ref(0)

// ========== 计算属性 ==========
const mainImage = computed(() => {
  if (imageList.value.length > 0) {
    return imageList.value[currentImageIndex.value]
  }
  // 如果没有图片列表，使用封面
  if (product.value && product.value.coverFilePath) {
    return {
      id: product.value.coverFileId,
      filePath: product.value.coverFilePath,
      originalName: '封面图片',
      type: 'IMG'
    }
  }
  return null
})

// ========== 生命周期 ==========
onMounted(() => {
  fetchProductDetail()
  fetchRecommendProducts()
})

// ========== 方法 ==========
const fetchProductDetail = () => {
  loading.value = true
  const productId = route.params.id
  
  getProductById(productId, {
    onSuccess: (res) => {
      product.value = res
      loading.value = false
      
      // 加载图片列表
      if (res.imageList && res.imageList.length > 0) {
        imageList.value = res.imageList
        currentImageIndex.value = 0
        console.log(`成功加载 ${res.imageList.length} 张商品图片`)
      } else {
        imageList.value = []
        currentImageIndex.value = 0
      }
    },
    onError: (error) => {
      message.error('获取商品详情失败：' + error.message)
      loading.value = false
    }
  })
}

const fetchRecommendProducts = () => {
  const productId = route.params.id
  
  // 使用基于协同过滤的推荐算法获取相关推荐商品
  getProductRecommendations(productId, 4, {
    onSuccess: (res) => {
      recommendProducts.value = res || []
      console.log('推荐商品加载成功，数量:', recommendProducts.value.length)
    },
    onError: (error) => {
      console.warn('推荐商品加载失败:', error)
      // 静默失败，不显示错误提示
      recommendProducts.value = []
    }
  })
}

const handleBuyNow = () => {
  // 检查登录状态
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/auth/login')
    return
  }
  
  // 跳转到订单确认页面，携带商品信息
  router.push({
    name: 'OrderConfirm',
    query: {
      productId: product.value.id,
      quantity: quantity.value
    }
  })
}

const handleProductClick = (id) => {
  router.push(`/shop/${id}`)
  // 重新加载商品详情
  fetchProductDetail()
  fetchRecommendProducts()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleImageChange = (index) => {
  currentImageIndex.value = index
}
</script>

<style scoped>
/* 最外层容器 */
.product-detail-wrapper {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.product-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 面包屑 */
.breadcrumb {
  margin-bottom: 28px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;

  :deep(.ant-breadcrumb-link) {
    color: #666;
    transition: color 0.3s;

    &:hover {
      color: #8b4513;
    }
  }

  :deep(.ant-breadcrumb-separator) {
    color: #999;
  }
}

/* 主体内容 */
.detail-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

/* 图片区域 */
.image-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #faf8f3;
  border: 1px solid #e8e8e8;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.main-image:hover img {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.no-image i {
  font-size: 80px;
  margin-bottom: 16px;
}

/* 缩略图列表 */
.image-thumbnails {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 4px;
  margin-top: 16px;
}

/* 自定义滚动条 */
.image-thumbnails::-webkit-scrollbar {
  height: 6px;
}

.image-thumbnails::-webkit-scrollbar-track {
  background: rgba(139, 69, 19, 0.05);
  border-radius: 3px;
}

.image-thumbnails::-webkit-scrollbar-thumb {
  background: rgba(139, 69, 19, 0.3);
  border-radius: 3px;
}

.thumbnail-item {
  width: 88px;
  height: 88px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid #e8e8e8;
  transition: all 0.3s ease;
  flex-shrink: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  position: relative;
}

.thumbnail-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid transparent;
  transition: border-color 0.3s;
  pointer-events: none;
}

.thumbnail-item:hover {
  border-color: #c5a572;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(139, 69, 19, 0.15);
}

.thumbnail-item.active {
  border-color: #8b4513;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.25);
}

.thumbnail-item.active::after {
  border-color: rgba(139, 69, 19, 0.2);
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 信息区域 */
.info-section {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.product-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #e8e8e8;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c2c2c;
  margin-bottom: 16px;
  line-height: 1.4;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 2px;
}

.product-subtitle {
  font-size: 17px;
  color: #666;
  margin-bottom: 16px;
  line-height: 1.8;
}

.product-category {
  margin-top: 14px;

  :deep(.ant-tag) {
    padding: 6px 16px;
    font-size: 14px;
    border-radius: 4px;
  }
}

.product-price-box {
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.04) 0%, rgba(197, 165, 114, 0.06) 100%);
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 32px;
}

.price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.price-label {
  font-size: 17px;
  color: #666;
  font-weight: 600;
  letter-spacing: 1px;
}

.price-value {
  display: flex;
  align-items: baseline;
  color: #8b4513;
  font-weight: 700;
}

.price-symbol {
  font-size: 22px;
  margin-right: 4px;
}

.price-number {
  font-size: 40px;
}

.stock-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stock-label {
  font-size: 17px;
  color: #666;
  font-weight: 600;
  letter-spacing: 1px;
}

.stock-value {
  font-size: 17px;
  font-weight: 600;
  color: #52c41a;
}

.stock-value.out-of-stock {
  color: #c5322d;
}

.purchase-section {
  margin-bottom: 32px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.quantity-label {
  font-size: 17px;
  color: #666;
  font-weight: 600;
  letter-spacing: 1px;
}

.action-buttons {
  width: 100%;

  :deep(.ant-btn-primary) {
    height: 50px;
    font-size: 17px;
    font-weight: 600;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
    border: none;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
    }

    i {
      margin-right: 8px;
    }
  }
}

.product-tips {
  display: flex;
  gap: 28px;
  padding-top: 24px;
  border-top: 2px solid #e8e8e8;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 15px;
  font-weight: 500;
}

.tip-item i {
  color: #8b4513;
  font-size: 18px;
}

/* 详情描述 */
.detail-description {
 
  border-radius: 8px;
  padding: 32px;

  margin-bottom: 40px;

  :deep(.ant-tabs-nav) {
    margin-bottom: 24px;
  }

  :deep(.ant-tabs-tab) {
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    color: #666;

    &:hover {
      color: #8b4513;
    }
  }

  :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: #8b4513;
  }

  :deep(.ant-tabs-ink-bar) {
    background: #8b4513;
  }
}

.description-content,
.info-content {
  padding: 20px 0;

  :deep(.ant-descriptions-item-label) {
    font-weight: 600;
    color: #666;
    letter-spacing: 0.5px;
    background: rgba(139, 69, 19, 0.03);
  }

  :deep(.ant-descriptions-item-content) {
    color: #2c2c2c;
    font-weight: 500;
  }
}

/* HTML内容渲染样式 */
.detail-html {
  font-size: 16px;
  line-height: 2;
  color: #555;
}

/* 为HTML内容添加样式 */
.detail-html :deep(h1),
.detail-html :deep(h2),
.detail-html :deep(h3),
.detail-html :deep(h4),
.detail-html :deep(h5),
.detail-html :deep(h6) {
  margin-top: 28px;
  margin-bottom: 18px;
  font-weight: 600;
  color: #2c2c2c;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 1px;
}

.detail-html :deep(h1) {
  font-size: 30px;
}

.detail-html :deep(h2) {
  font-size: 26px;
}

.detail-html :deep(h3) {
  font-size: 22px;
  color: #8b4513;
  border-bottom: 2px solid #c5a572;
  padding-bottom: 10px;
}

.detail-html :deep(h4) {
  font-size: 19px;
}

.detail-html :deep(p) {
  margin-bottom: 14px;
  text-align: justify;
}

.detail-html :deep(ul),
.detail-html :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.detail-html :deep(li) {
  margin-bottom: 8px;
  line-height: 1.8;
}

.detail-html :deep(ul li) {
  list-style-type: disc;
}

.detail-html :deep(ol li) {
  list-style-type: decimal;
}

.detail-html :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
  display: block;
}

.detail-html :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.detail-html :deep(table th),
.detail-html :deep(table td) {
  border: 1px solid #e8e8e8;
  padding: 12px;
  text-align: left;
}

.detail-html :deep(table th) {
  background-color: #fafafa;
  font-weight: 600;
}

.detail-html :deep(blockquote) {
  border-left: 4px solid #8b4513;
  padding-left: 20px;
  margin: 20px 0;
  color: #666;
  font-style: italic;
  background: rgba(139, 69, 19, 0.03);
  padding: 16px 20px;
  border-radius: 4px;
}

.detail-html :deep(code) {
  background-color: rgba(139, 69, 19, 0.05);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #8b4513;
}

.detail-html :deep(pre) {
  background-color: rgba(139, 69, 19, 0.03);
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
  border: 1px solid #e8e8e8;
}

.detail-html :deep(a) {
  color: #8b4513;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s;
}

.detail-html :deep(a:hover) {
  border-bottom-color: #8b4513;
}

.detail-html :deep(strong),
.detail-html :deep(b) {
  font-weight: 700;
  color: #2c2c2c;
}

.detail-html :deep(em),
.detail-html :deep(i) {
  font-style: italic;
}

.no-detail {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 16px;
}

/* 推荐商品 */
.recommend-section {

  border-radius: 8px;
  padding: 32px;

}

.recommend-section h2 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 28px;
  color: #2c2c2c;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 2px;
  padding-bottom: 14px;
  border-bottom: 2px solid #e8e8e8;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.recommend-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  border-color: #c5a572;
}

.recommend-image {
  width: 100%;
  aspect-ratio: 1;
  background: #faf8f3;
}

.recommend-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommend-card:hover .recommend-image img {
  transform: scale(1.08);
}

.recommend-info {
  padding: 14px;
  background: rgba(139, 69, 19, 0.02);
}

.recommend-info h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #2c2c2c;
}

.recommend-price {
  display: flex;
  align-items: baseline;
  color: #8b4513;
  font-weight: 700;
}

.recommend-price .price-symbol {
  font-size: 13px;
  margin-right: 2px;
}

.recommend-price .price-value {
  font-size: 20px;
}

/* 错误状态 */
.error-state {
  text-align: center;
  padding: 120px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.error-state i {
  font-size: 80px;
  color: #c5322d;
  margin-bottom: 24px;
}

.error-state p {
  font-size: 18px;
  color: #666;
  margin-bottom: 28px;
  font-weight: 500;
}

.error-state :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
}

/* 加载状态 */
.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

/* 响应式 */
@media (max-width: 1024px) {
  .detail-body {
    grid-template-columns: 1fr;
  }
  
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .product-tips {
    flex-direction: column;
    gap: 12px;
  }
  
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>

