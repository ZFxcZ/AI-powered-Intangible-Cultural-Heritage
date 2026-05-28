<template>
  <div class="heritage-list-page">
    <!-- 页面标题和搜索 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">
          非遗作品
        </h1>
        <p class="scroll-header-subtitle">
          探索中华非物质文化遗产的魅力
        </p>
        <div class="scroll-header-seal">
          非遗
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索作品名称、类别、地区..."
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
          v-for="category in ['全部', ...HERITAGE_CATEGORIES]"
          :key="category"
          :type="selectedCategory === category ? 'primary' : 'default'"
          @click="handleCategoryFilter(category)"
        >
          {{ category }}
        </a-button>
      </div>

      <div class="filter-controls">
        <a-select
          v-model:value="selectedRegion"
          placeholder="选择地区"
          allow-clear
          style="width: 120px"
          @change="handleFilter"
        >
          <a-select-option value="">
            全部地区
          </a-select-option>
          <a-select-option
            v-for="region in regionList"
            :key="region"
            :value="region"
          >
            {{ region }}
          </a-select-option>
        </a-select>

        <a-select
          v-model:value="sortBy"
          style="width: 120px"
          @change="handleFilter"
        >
          <a-select-option value="publish_time">
            最新发布
          </a-select-option>
          <a-select-option value="create_time">
            最新创建
          </a-select-option>
          <a-select-option value="title">
            标题排序
          </a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 作品网格 -->
    <div class="heritage-grid">
      <div
        v-if="loading"
        class="loading-container"
      >
        <a-skeleton
          v-for="n in 12"
          :key="n"
          active
          :loading="true"
        >
          <template #default>
            <div class="skeleton-card">
              <a-skeleton-image style="width: 100%; height: 200px" />
              <div style="padding: 16px">
                <a-skeleton :paragraph="{ rows: 2 }" />
              </div>
            </div>
          </template>
        </a-skeleton>
      </div>

      <div
        v-else-if="heritageList.length > 0"
        class="grid-container"
      >
        <HeritageItemCard
          v-for="item in heritageList"
          :key="item.id"
          :item="item"
          @click="handleItemClick"
        />
      </div>

      <div
        v-else
        class="empty-state"
      >
        <i class="fas fa-search" />
        <p>暂无相关作品</p>
        <a-button @click="handleReset">
          重置筛选
        </a-button>
      </div>
    </div>

    <!-- 分页 -->
    <div
      v-if="total > 0"
      class="pagination-section"
    >
      <a-pagination
        v-model:current="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-size-options="['12', '24', '48']"
        show-size-changer
        show-total
        @change="handleCurrentChange"
        @show-size-change="handleSizeChange"
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
import {
  getHeritageItemPage,
  searchHeritageItems,
  HERITAGE_CATEGORIES,
} from "@/api/HeritageApi";
import HeritageItemCard from "@/components/common/HeritageItemCard.vue";
import "@/styles/scroll-header.css";

// ========== 路由 ==========
const router = useRouter();

// ========== 响应式数据 ==========
const loading = ref(false);
const searchKeyword = ref("");
const selectedCategory = ref("全部");
const selectedRegion = ref("");
const sortBy = ref("publish_time");
const currentPage = ref(1);
const pageSize = ref(12);

const heritageList = ref([]);
const total = ref(0);

// 地区列表（可以从API获取或配置）
const regionList = ref([
  "北京",
  "上海",
  "广东",
  "江苏",
  "浙江",
  "山东",
  "河南",
  "湖北",
  "湖南",
  "四川",
  "陕西",
  "山西",
  "河北",
  "辽宁",
  "吉林",
  "黑龙江",
  "安徽",
  "江西",
  "福建",
  "广西",
  "海南",
  "重庆",
  "贵州",
  "云南",
  "西藏",
  "甘肃",
  "青海",
  "宁夏",
  "新疆",
  "内蒙古",
]);

// ========== 计算属性 ==========
const searchParams = computed(() => {
  return {
    currentPage: currentPage.value,
    size: pageSize.value,
    title: searchKeyword.value,
    category: selectedCategory.value === "全部" ? "" : selectedCategory.value,
    region: selectedRegion.value,
    status: 2, // 只显示已发布的作品
    orderBy: sortBy.value,
    orderDirection: "desc",
  };
});

// ========== 方法 ==========

/**
 * 获取作品列表
 */
function fetchHeritageList() {
  loading.value = true;

  getHeritageItemPage(searchParams.value, {
    onSuccess: (res) => {
      heritageList.value = res.records || [];
      total.value = res.total || 0;
      loading.value = false;
    },
    onError: (error) => {
      message.error("获取作品列表失败", error);
      loading.value = false;
    },
  });
}

/**
 * 搜索处理
 */
function handleSearch() {
  if (searchKeyword.value.trim()) {
    // 使用搜索API
    loading.value = true;
    searchHeritageItems(
      {
        keyword: searchKeyword.value.trim(),
        limit: pageSize.value * currentPage.value,
      },
      {
        onSuccess: (res) => {
          heritageList.value = res || [];
          total.value = res.length || 0;
          loading.value = false;
        },
        onError: (error) => {
          message.error("搜索失败", error);
          loading.value = false;
        },
      },
    );
  } else {
    currentPage.value = 1;
    fetchHeritageList();
  }
}

/**
 * 类别筛选
 */
function handleCategoryFilter(category) {
  selectedCategory.value = category;
  currentPage.value = 1;
  fetchHeritageList();
}

/**
 * 筛选处理
 */
function handleFilter() {
  currentPage.value = 1;
  fetchHeritageList();
}

/**
 * 重置筛选
 */
function handleReset() {
  searchKeyword.value = "";
  selectedCategory.value = "全部";
  selectedRegion.value = "";
  sortBy.value = "publish_time";
  currentPage.value = 1;
  fetchHeritageList();
}

/**
 * 页面大小改变
 */
function handleSizeChange() {
  currentPage.value = 1;
  fetchHeritageList();
}

/**
 * 当前页改变
 */
function handleCurrentChange() {
  fetchHeritageList();
}

/**
 * 作品点击处理
 */
function handleItemClick(item) {
  router.push(`/heritage/${item.id}`);
}

// ========== 生命周期 ==========
onMounted(() => {
  fetchHeritageList();
});
</script>

<style scoped>
/* 新中式配色 */
.heritage-list-page {
  min-height: 100vh;
  background-image: url("/src/assets/1690268977680789.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}

.heritage-list-page::before {
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

.heritage-list-page > * {
  position: relative;
  z-index: 1;
}

/* 搜索栏样式 */
.search-bar {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

/* 筛选区域 - 简洁设计 */
.filter-section {
  background: #fff;
  padding: 24px 40px;
  margin: 0 auto;
  max-width: 1200px;
  border: 1px solid #e8e8e8;
  border-left: none;
  border-right: none;
  transform: translateY(-20px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
  gap: 12px;
  align-items: center;
}

/* 作品展示区 - 创新画轴式布局 */
.heritage-grid {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

/* 画轴装饰 */
.heritage-grid::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    #8b4513 5%,
    #8b4513 95%,
    transparent 100%
  );
}

.heritage-grid::after {
  content: "";
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    #8b4513 5%,
    #8b4513 95%,
    transparent 100%
  );
}

.loading-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px 24px;
  padding: 0 40px;
}

.skeleton-card {
  background: white;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

/* 固定布局：每行4个卡片 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px 24px;
  padding: 0 40px;
}

/* 卡片基础样式 */
.grid-container :deep(.heritage-item-card) {
  transition: all 0.3s ease;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100px 40px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #d4d4d4;
}

.empty-state p {
  font-size: 18px;
  margin-bottom: 20px;
  color: #666;
}

/* 分页区域 - 画轴收尾 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 60px 20px 40px;
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
}

/* 画轴底部装饰 */
.pagination-section::before {
  content: "";
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 4px;
  background: #8b4513;
  border-radius: 2px;
}

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

/* 响应式设计 - 固定列数 */
@media (max-width: 1400px) {
  .grid-container {
    grid-template-columns: repeat(4, 1fr);
    gap: 28px 20px;
  }
}

@media (max-width: 1200px) {
  .grid-container {
    grid-template-columns: repeat(3, 1fr);
    gap: 28px 20px;
  }

  .loading-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px 16px;
  }

  .loading-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    padding: 16px 20px;
  }

  .filter-tabs {
    justify-content: center;
  }

  .filter-controls {
    justify-content: center;
  }

  .heritage-grid {
    padding: 20px;
  }

  /* 移除画轴装饰 */
  .heritage-grid::before,
  .heritage-grid::after {
    display: none;
  }

  .grid-container {
    padding: 0 20px;
    gap: 20px 12px;
  }

  .loading-container {
    padding: 0 20px;
  }

  .empty-state {
    padding: 100px 20px;
  }

  .pagination-section::before {
    width: 80px;
    height: 3px;
  }
}

@media (max-width: 600px) {
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px 12px;
  }

  .loading-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px 12px;
  }
}

@media (max-width: 480px) {
  .grid-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .loading-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .filter-tabs :deep(.ant-btn) {
    font-size: 12px;
    padding: 4px 12px;
    height: auto;
  }
}
</style>
