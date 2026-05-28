<template>
  <div class="shop-page">
    <!-- 页面标题和搜索 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">非遗手办商城</h1>
        <p class="scroll-header-subtitle">精选非遗文化手办，传承中华文化</p>
        <div class="scroll-header-seal">商城</div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索商品名称..."
          size="large"
          allow-clear
          @search="handleSearch"
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <a-button
          :type="selectedCategoryId === null ? 'primary' : 'default'"
          @click="handleCategoryFilter(null)"
        >
          全部商品
        </a-button>
        <a-button
          v-for="category in categoryList"
          :key="category.id"
          :type="selectedCategoryId === category.id ? 'primary' : 'default'"
          @click="handleCategoryFilter(category.id)"
        >
          {{ category.name }}
        </a-button>
      </div>

      <div class="filter-controls">
        <a-select
          v-model:value="priceRange"
          placeholder="价格范围"
          allow-clear
          style="width: 150px"
          @change="handleFilter"
        >
          <a-select-option value="">全部价格</a-select-option>
          <a-select-option value="0-50">¥0 - ¥50</a-select-option>
          <a-select-option value="50-100">¥50 - ¥100</a-select-option>
          <a-select-option value="100-200">¥100 - ¥200</a-select-option>
          <a-select-option value="200-500">¥200 - ¥500</a-select-option>
          <a-select-option value="500+">¥500+</a-select-option>
        </a-select>

        <a-select
          v-model:value="sortBy"
          style="width: 120px"
          @change="handleFilter"
        >
          <a-select-option value="create_time">最新上架</a-select-option>
          <a-select-option value="price_asc">价格从低到高</a-select-option>
          <a-select-option value="price_desc">价格从高到低</a-select-option>
        </a-select>

        <a-checkbox v-model:checked="onlyInStock" @change="handleFilter">
          仅显示有货
        </a-checkbox>
      </div>
    </div>

    <!-- 商品网格 -->
    <div class="product-grid">
      <div v-if="loading" class="loading-container">
        <a-skeleton v-for="n in 12" :key="n" active :loading="true">
          <template #default>
            <div class="skeleton-card">
              <a-skeleton-image style="width: 100%; height: 240px" />
              <div style="padding: 16px">
                <a-skeleton :paragraph="{ rows: 3 }" />
              </div>
            </div>
          </template>
        </a-skeleton>
      </div>

      <div v-else-if="productList.length > 0" class="grid-container">
        <div
          v-for="product in productList"
          :key="product.id"
          class="product-card"
          @click="handleProductClick(product.id)"
        >
          <div class="product-image">
            <img
              v-if="product.coverFilePath"
              :src="product.coverFilePath"
              :alt="product.title"
            />
            <div v-else class="no-image">
              <i class="fas fa-image"></i>
              <span>暂无图片</span>
            </div>

            <!-- 库存标签 -->
            <div v-if="product.stock <= 0" class="stock-badge out-of-stock">
              已售罄
            </div>
            <div v-else-if="product.stock < 10" class="stock-badge low-stock">
              仅剩{{ product.stock }}件
            </div>
          </div>

          <div class="product-info">
            <h3 class="product-title">{{ product.title }}</h3>
            <p v-if="product.subtitle" class="product-subtitle">
              {{ product.subtitle }}
            </p>
            <div class="product-category">
              <a-tag color="blue">{{ product.categoryName }}</a-tag>
            </div>
            <div class="product-footer">
              <div class="product-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ product.price }}</span>
              </div>
              <a-button
                type="primary"
                :disabled="product.stock <= 0"
                @click.stop="handleBuyNow(product)"
              >
                <i class="fas fa-bolt"></i>
                {{ product.stock > 0 ? "立即购买" : "已售罄" }}
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <i class="fas fa-shopping-bag"></i>
        <p>暂无相关商品</p>
        <a-button @click="handleReset">重置筛选</a-button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-section">
      <a-pagination
        v-model:current="currentPage"
        v-model:pageSize="pageSize"
        :total="total"
        :page-size-options="['12', '24', '48']"
        show-size-changer
        show-total
        @change="handlePageChange"
        @showSizeChange="handleSizeChange"
      >
        <template #buildOptionText="props">
          <span>{{ props.value }} 条/页</span>
        </template>
      </a-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getProductPage } from "@/api/ShopProductApi";
import { getEnabledCategories } from "@/api/ShopCategoryApi";
import { useUserStore } from "@/store/user";
import "@/styles/scroll-header.css";

// ========== 路由 ==========
const router = useRouter();

// ========== 响应式数据 ==========
const loading = ref(false);
const productList = ref([]);
const categoryList = ref([]);
const searchKeyword = ref("");
const selectedCategoryId = ref(null);
const priceRange = ref("");
const sortBy = ref("create_time");
const onlyInStock = ref(false);
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

// ========== 生命周期 ==========
onMounted(() => {
  fetchCategories();
  fetchProducts();
});

// ========== 方法 ==========
const fetchCategories = () => {
  getEnabledCategories({
    onSuccess: (res) => {
      categoryList.value = res || [];
    },
    onError: (error) => {
      message.error("获取分类列表失败：" + error.message);
    },
  });
};

const fetchProducts = () => {
  loading.value = true;

  const params = {
    page: currentPage.value,
    pageSize: pageSize.value,
    title: searchKeyword.value || null,
    categoryId: selectedCategoryId.value,
    status: 1, // 只显示上架商品
    hasStock: onlyInStock.value ? true : null,
  };

  // 处理价格范围
  if (priceRange.value) {
    if (priceRange.value === "500+") {
      params.minPrice = 500;
    } else {
      const [min, max] = priceRange.value.split("-");
      params.minPrice = parseFloat(min);
      params.maxPrice = parseFloat(max);
    }
  }

  // 处理排序
  if (sortBy.value === "price_asc") {
    params.sortField = "price";
    params.sortOrder = "asc";
  } else if (sortBy.value === "price_desc") {
    params.sortField = "price";
    params.sortOrder = "desc";
  } else {
    params.sortField = "create_time";
    params.sortOrder = "desc";
  }

  getProductPage(params, {
    onSuccess: (res) => {
      productList.value = res.records || [];
      total.value = res.total || 0;
      loading.value = false;
    },
    onError: (error) => {
      message.error("获取商品列表失败：" + error.message);
      loading.value = false;
    },
  });
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

const handleCategoryFilter = (categoryId) => {
  selectedCategoryId.value = categoryId;
  currentPage.value = 1;
  fetchProducts();
};

const handleFilter = () => {
  currentPage.value = 1;
  fetchProducts();
};

const handleReset = () => {
  searchKeyword.value = "";
  selectedCategoryId.value = null;
  priceRange.value = "";
  sortBy.value = "create_time";
  onlyInStock.value = false;
  currentPage.value = 1;
  fetchProducts();
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchProducts();
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: "smooth" });
};

const handleSizeChange = (current, size) => {
  currentPage.value = 1;
  pageSize.value = size;
  fetchProducts();
};

const handleProductClick = (id) => {
  router.push(`/shop/${id}`);
};

const handleBuyNow = (product) => {
  // 检查登录状态
  const userStore = useUserStore();
  if (!userStore.isLoggedIn) {
    message.warning("请先登录");
    router.push("/auth/login");
    return;
  }

  // 检查库存
  if (product.stock <= 0) {
    message.warning("该商品已售罄");
    return;
  }

  // 跳转到订单确认页面，携带商品信息
  router.push({
    name: "OrderConfirm",
    query: {
      productId: product.id,
      quantity: 1,
    },
  });
};
</script>

<style scoped>
/* 新中式配色 */
.shop-page {
  min-height: 100vh;
  background-image: url("/src/assets/1690268977680789.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}

.shop-page::before {
  content: "";
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(250, 248, 243, 0.85);
  z-index: 0;
  pointer-events: none;
}

.shop-page > * {
  position: relative;
  z-index: 1;
}

/* 搜索栏样式 */
.search-bar {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

/* 筛选器 - 简洁设计 */
.filter-section {
  background: #fff;
  padding: 24px 40px;
  margin: 0 auto 40px;
  max-width: 1200px;
  border: 1px solid #e8e8e8;
  border-left: none;
  border-right: none;
  transform: translateY(-20px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-tabs :deep(.ant-btn-primary) {
  background: #8b4513; /* 使用传统棕色 */
  border-color: #8b4513;
}

.filter-tabs :deep(.ant-btn-primary:hover) {
  background: #a0522d;
  border-color: #a0522d;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 商品网格 */
.product-grid {
  min-height: 500px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.product-card {
  background: #fff;
  overflow: hidden;
  border: 1px solid #e8e8e8; /* 移除圆角和阴影，使用边框 */
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-2px); /* 减小悬停移动距离 */
  border-color: #8b4513; /* 悬停时边框变色 */
}

.product-image {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 14px;
}

.no-image i {
  font-size: 48px;
  margin-bottom: 8px;
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
}

.out-of-stock {
  background: rgba(0, 0, 0, 0.6);
}

.low-stock {
  background: rgba(255, 77, 79, 0.9);
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2c2c; /* 墨黑色 */
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: "SimSun", "宋体", serif;
  letter-spacing: 1px;
}

.product-subtitle {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  margin-bottom: 12px;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.product-price {
  display: flex;
  align-items: baseline;
  color: #c8332b; /* 朱砂红 */
  font-weight: 600;
}

.price-symbol {
  font-size: 14px;
  margin-right: 2px;
}

.price-value {
  font-size: 24px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state i {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin: 40px auto 0;
  padding: 20px 40px;
  max-width: 1200px;
}

/* 分页样式统一 */
.pagination-section :deep(.ant-pagination-item-active) {
  border-color: #8b4513;
  background: #8b4513;
}

.pagination-section :deep(.ant-pagination-item-active a) {
  color: #fff;
}

.pagination-section :deep(.ant-pagination-item:hover) {
  border-color: #8b4513;
}

.pagination-section :deep(.ant-pagination-item:hover a) {
  color: #8b4513;
}

/* 加载状态 */
.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.skeleton-card {
  background: #fff;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

/* 响应式 */
@media (max-width: 768px) {
  .filter-section {
    padding: 16px 20px;
  }

  .product-grid {
    padding: 0 20px;
  }

  .pagination-section {
    padding: 20px 20px;
  }

  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .product-image {
    height: 180px;
  }

  .filter-tabs {
    gap: 8px;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls > * {
    width: 100% !important;
  }
}
</style>
