<template>
  <div class="inheritor-list-page">
    <!-- 页面标题 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">非遗传承人</h1>
        <p class="scroll-header-subtitle">传承匠心，守护文化根脉</p>
        <div class="scroll-header-seal">传承</div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <a-space size="large">
        <a-input
          v-model:value="filters.name"
          placeholder="搜索传承人姓名"
          allow-clear
          style="width: 250px"
          @pressEnter="handleSearch"
        >
          <template #prefix>
            <i class="fas fa-search"></i>
          </template>
        </a-input>

        <a-input
          v-model:value="filters.region"
          placeholder="地区"
          allow-clear
          style="width: 180px"
          @pressEnter="handleSearch"
        />

        <a-button type="primary" @click="handleSearch"> 搜索 </a-button>

        <a-button @click="handleReset"> 重置 </a-button>
      </a-space>
    </div>

    <!-- 传承人列表 -->
    <div class="inheritor-grid">
      <div
        v-for="inheritor in inheritorList"
        :key="inheritor.id"
        class="inheritor-card"
        @click="goToDetail(inheritor.id)"
      >
        <div class="card-avatar">
          <a-avatar
            v-if="inheritor.avatarPath"
            :src="inheritor.avatarPath"
            :size="120"
          />
          <a-avatar v-else :size="120" class="default-avatar">
            {{ inheritor.name ? inheritor.name.charAt(0) : "?" }}
          </a-avatar>
        </div>

        <div class="card-content">
          <h3 class="inheritor-name">{{ inheritor.name }}</h3>

          <div v-if="inheritor.title" class="inheritor-title">
            <i class="fas fa-award"></i>
            {{ inheritor.title }}
          </div>

          <div v-if="inheritor.region" class="inheritor-region">
            <i class="fas fa-map-marker-alt"></i>
            {{ inheritor.region }}
          </div>

          <div v-if="inheritor.bio" class="inheritor-bio">
            {{ getShortBio(inheritor.bio) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <a-empty
      v-if="!loading && inheritorList.length === 0"
      description="暂无传承人信息"
      style="margin-top: 60px"
    />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <a-pagination
        v-model:current="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :show-size-changer="false"
        :show-total="(total) => `共 ${total} 位传承人`"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getInheritorPage } from "@/api/InheritorApi";
import "@/styles/scroll-header.css";

const router = useRouter();

// 筛选条件
const filters = reactive({
  name: "",
  region: "",
});

// 传承人列表
const inheritorList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

// 获取传承人列表
const fetchInheritors = () => {
  loading.value = true;
  const params = {
    current: currentPage.value,
    size: pageSize.value,
    name: filters.name,
    region: filters.region,
  };

  getInheritorPage(params, {
    onSuccess: (res) => {
      inheritorList.value = res.records || [];
      total.value = res.total || 0;
      loading.value = false;
    },
    onError: () => {
      loading.value = false;
    },
  });
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchInheritors();
};

// 重置
const handleReset = () => {
  Object.assign(filters, {
    name: "",
    region: "",
  });
  currentPage.value = 1;
  fetchInheritors();
};

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchInheritors();
  window.scrollTo({ top: 0, behavior: "smooth" });
};

// 获取简短简介
const getShortBio = (bio) => {
  if (!bio) return "";
  return bio.length > 80 ? bio.substring(0, 80) + "..." : bio;
};

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/inheritor/${id}`);
};

// 初始化
onMounted(() => {
  fetchInheritors();
});
</script>

<style scoped>
/* 新中式配色 */
.inheritor-list-page {
  min-height: 100vh;
  background-image: url("/src/assets/1690268977680789.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}

.inheritor-list-page::before {
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

.inheritor-list-page > * {
  position: relative;
  z-index: 1;
}

/* 筛选区域 - 简洁设计 */
.filter-section {
  background: #fff;
  padding: 24px 40px;
  margin: 0 auto 40px;
  max-width: 1200px;
  border: 1px solid #e8e8e8;
  border-left: none;
  border-right: none;
  transform: translateY(-20px);
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-section :deep(.ant-btn-primary) {
  background: #8b4513; /* 使用传统棕色 */
  border-color: #8b4513;
}

.filter-section :deep(.ant-btn-primary:hover) {
  background: #a0522d;
  border-color: #a0522d;
}

.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin: 0 auto 32px;
  max-width: 1200px;
  padding: 0 40px;
}

.inheritor-card {
  background: #fff;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8; /* 移除圆角和阴影，使用边框 */
}

.inheritor-card:hover {
  border-color: #8b4513; /* 悬停时边框变色 */
  transform: translateY(-2px); /* 减小悬停移动距离 */
}

.card-avatar {
  margin-bottom: 16px;
}

.default-avatar {
  background: #8b4513; /* 使用传统棕色 */
  font-size: 48px;
  font-weight: bold;
}

.card-content {
  text-align: center;
}

.inheritor-name {
  font-size: 20px;
  font-weight: 600;
  color: #2c2c2c; /* 墨黑色 */
  margin-bottom: 12px;
  font-family: "SimSun", "宋体", serif;
  letter-spacing: 2px;
}

.inheritor-title {
  font-size: 14px;
  color: #8b4513; /* 金箔黄棕色 */
  margin-bottom: 8px;
  padding: 4px 12px;
  background: transparent;
  border: 1px solid #8b4513;
  display: inline-block;
}

.inheritor-title i {
  margin-right: 6px;
}

.inheritor-region {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.inheritor-region i {
  margin-right: 6px;
  color: #8b4513; /* 统一颜色 */
}

.inheritor-bio {
  font-size: 13px;
  color: #8c8c8c;
  line-height: 1.6;
  text-align: left;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 48px;
  max-width: 1200px;
  margin: 48px auto 0;
  padding: 0 40px;
}

/* 分页样式统一 */
.pagination-container :deep(.ant-pagination-item-active) {
  border-color: #8b4513;
  background: #8b4513;
}

.pagination-container :deep(.ant-pagination-item-active a) {
  color: #fff;
}

.pagination-container :deep(.ant-pagination-item:hover) {
  border-color: #8b4513;
}

.pagination-container :deep(.ant-pagination-item:hover a) {
  color: #8b4513;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section {
    padding: 16px 20px;
  }

  .inheritor-grid {
    padding: 0 20px;
  }

  .pagination-container {
    padding: 0 20px;
  }
}
</style>
