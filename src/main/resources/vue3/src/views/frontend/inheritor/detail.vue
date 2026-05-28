<template>
  <div class="inheritor-detail-wrapper">
    <div class="inheritor-detail-page">
      <div v-if="loading" class="loading-container">
      <a-spin size="large" />
    </div>

    <div v-else-if="inheritor" class="detail-container">
      <!-- 基本信息区域 -->
      <div class="basic-info-section">
        <div class="avatar-column">
          <div class="avatar-wrapper">
            <a-avatar
              v-if="inheritor.avatarPath"
              :src="inheritor.avatarPath"
              :size="180"
            />
            <a-avatar v-else :size="180" class="default-avatar">
              {{ inheritor.name ? inheritor.name.charAt(0) : '?' }}
            </a-avatar>
          </div>
        </div>

        <div class="info-column">
          <h1 class="inheritor-name">{{ inheritor.name }}</h1>
          
          <div class="info-items-container">
            <div v-if="inheritor.title" class="info-item title-item">
              <i class="fas fa-award"></i>
              <span class="label">称号：</span>
              <span class="value">{{ inheritor.title }}</span>
            </div>

            <div v-if="inheritor.region" class="info-item">
              <i class="fas fa-map-marker-alt"></i>
              <span class="label">地区：</span>
              <span class="value">{{ inheritor.region }}</span>
            </div>

            <div v-if="inheritor.createTime" class="info-item">
              <i class="fas fa-calendar-alt"></i>
              <span class="label">入驻时间：</span>
              <span class="value">{{ formatDate(inheritor.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 简介区域 -->
      <div v-if="inheritor.bio" class="bio-section">
        <h2 class="section-title">传承人简介</h2>
        <div class="bio-content">
          {{ inheritor.bio }}
        </div>
      </div>

      <!-- 关联作品区域 -->
      <div v-if="inheritor.heritageItems && inheritor.heritageItems.length > 0" class="works-section">
        <h2 class="section-title">
          代表作品
          <span class="count">({{ inheritor.heritageItems.length }})</span>
        </h2>
        
        <div class="works-grid">
          <div
            v-for="item in inheritor.heritageItems"
            :key="item.id"
            class="work-card"
            @click="goToItemDetail(item.id)"
          >
            <div class="work-content">
              <h3 class="work-title">{{ item.title }}</h3>
              
              <div class="work-meta">
                <span class="work-category">
                  <i class="fas fa-tag"></i>
                  {{ item.category }}
                </span>
                <span v-if="item.region" class="work-region">
                  <i class="fas fa-map-marker-alt"></i>
                  {{ item.region }}
                </span>
              </div>

              <div v-if="item.summary" class="work-summary">
                {{ getShortSummary(item.summary) }}
              </div>

              <div class="work-status">
                <a-tag :color="getStatusColor(item.status)">
                  {{ item.statusName }}
                </a-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-works">
        <i class="fas fa-palette"></i>
        <p>暂无代表作品</p>
      </div>

      <!-- 返回按钮 -->
      <div class="action-section">
        <a-button size="large" @click="goBack">
          <i class="fas fa-arrow-left"></i>
          返回列表
        </a-button>
      </div>
    </div>

    <div v-else class="empty-inheritor">
      <i class="fas fa-user-slash"></i>
      <p>传承人信息不存在</p>
      <a-button type="primary" @click="goBack">返回列表</a-button>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getInheritorById } from '@/api/InheritorApi';

const router = useRouter();
const route = useRoute();

const inheritor = ref(null);
const loading = ref(false);

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// 获取简短摘要
const getShortSummary = (summary) => {
  if (!summary) return '';
  return summary.length > 100 ? summary.substring(0, 100) + '...' : summary;
};

// 获取状态颜色（使用新中式配色）
const getStatusColor = (status) => {
  const colorMap = {
    0: 'default',  // 草稿
    1: 'warning',  // 待审
    2: '',         // 已发布 - 使用默认样式
    3: 'error'     // 下架
  };
  return colorMap[status] || 'default';
};

// 跳转到作品详情
const goToItemDetail = (itemId) => {
  router.push(`/heritage/${itemId}`);
};

// 返回列表
const goBack = () => {
  router.push('/inheritor');
};

// 获取传承人详情
const fetchInheritorDetail = () => {
  const inheritorId = route.params.id;
  if (!inheritorId) {
    router.push('/frontend/inheritor');
    return;
  }

  loading.value = true;
  getInheritorById(inheritorId, {
    onSuccess: (res) => {
      inheritor.value = res;
      loading.value = false;
    },
    onError: () => {
      loading.value = false;
    }
  });
};

// 初始化
onMounted(() => {
  fetchInheritorDetail();
});
</script>

<style scoped>
/* ========== 页面整体 ========== */
.inheritor-detail-wrapper {
  min-height: 100vh;
  background: #faf8f3;
}

.inheritor-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px 40px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fff;
  border-radius: 8px;
}

.detail-container {

  border-radius: 8px;
  padding: 48px;

}

/* ========== 基本信息区域 ========== */
.basic-info-section {
  display: flex;
  gap: 48px;
  padding-bottom: 48px;
  border-bottom: 3px solid #f0f0f0;
  margin-bottom: 48px;
  position: relative;
}

.avatar-column {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
}

.avatar-wrapper :deep(.ant-avatar) {
  border: 5px solid #8b4513;
}

.default-avatar {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%) !important;
  font-size: 72px !important;
  font-weight: bold !important;
}

.info-column {
  flex: 1;
}

.inheritor-name {
  font-size: 48px;
  font-weight: 700;
  color: #2c2c2c;
  margin-bottom: 32px;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 4px;
}

.info-items-container {
  background: rgba(139, 69, 19, 0.03);
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 17px;
}

.info-item i {
  font-size: 20px;
  margin-right: 14px;
  color: #8b4513;
  width: 24px;
  text-align: center;
}

.info-item .label {
  color: #666;
  margin-right: 10px;
  font-weight: 500;
  letter-spacing: 1px;
}

.info-item .value {
  color: #2c2c2c;
  font-weight: 500;
}

.title-item {
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(139, 69, 19, 0.1);
}

.title-item i {
  color: #8b4513;
  font-size: 22px;
}

.title-item .value {
  color: #8b4513;
  font-weight: 600;
  font-size: 18px;
}

/* ========== 简介区域 ========== */
.bio-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e8e8e8;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 4px;
}

.section-title .count {
  font-size: 18px;
  color: #999;
  font-weight: 400;
  margin-left: 12px;
  letter-spacing: 2px;
}

.bio-content {
  font-size: 17px;
  line-height: 2.2;
  color: #555;
  white-space: pre-wrap;
  text-align: justify;
  padding: 24px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

/* ========== 作品区域 ========== */
.works-section {
  margin-bottom: 48px;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.work-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #e8e8e8;
  position: relative;
}

.work-card::before {
  content: '';
  position: absolute;
  top: 6px;
  left: 6px;
  right: 6px;
  bottom: 6px;
  border: 1px solid rgba(139, 69, 19, 0.08);
  border-radius: 6px;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s;
}

.work-card:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
}

.work-card:hover::before {
  opacity: 1;
}

.work-title {
  font-size: 19px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 14px;
  letter-spacing: 1px;
  font-family: 'SimSun', '宋体', serif;
}

.work-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 14px;
}

.work-category {
  color: #8b4513;
  background: rgba(139, 69, 19, 0.1);
  padding: 5px 12px;
  border-radius: 4px;
  font-weight: 500;
}

.work-region {
  color: #666;
  padding: 5px 12px;
  background: rgba(139, 69, 19, 0.03);
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.work-summary {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 14px;
  text-align: justify;
}

.work-status {
  margin-top: 14px;
}

.work-status :deep(.ant-tag) {
  border-radius: 4px;
  padding: 4px 12px;
  font-weight: 500;
}

/* ========== 操作区域 ========== */
.action-section {
  text-align: center;
  padding-top: 32px;
  border-top: 2px solid #f0f0f0;
  margin-top: 24px;
}

.action-section :deep(.ant-btn) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  color: #fff;
  font-size: 16px;
  padding: 12px 32px;
  height: auto;
  font-weight: 500;
  letter-spacing: 2px;
  transition: all 0.3s;
}

.action-section :deep(.ant-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
}

.action-section :deep(.ant-btn i) {
  margin-right: 8px;
}

/* ========== 空状态 ========== */
.empty-works {
  text-align: center;
  padding: 80px 20px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 2px dashed #e8e8e8;
  margin-bottom: 48px;
}

.empty-works i {
  font-size: 64px;
  color: #d4d4d4;
  margin-bottom: 20px;
  display: block;
}

.empty-works p {
  font-size: 18px;
  color: #999;
  letter-spacing: 2px;
  margin: 0;
}

.empty-inheritor {
  text-align: center;
  padding: 120px 20px;
  background: #fff;
  border-radius: 8px;
  border: 2px dashed #e8e8e8;
}

.empty-inheritor i {
  font-size: 80px;
  color: #d4d4d4;
  margin-bottom: 24px;
  display: block;
}

.empty-inheritor p {
  font-size: 20px;
  color: #666;
  letter-spacing: 2px;
  margin: 0 0 32px 0;
}

.empty-inheritor :deep(.ant-btn) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  color: #fff;
  font-size: 16px;
  padding: 12px 32px;
  height: auto;
  font-weight: 500;
  letter-spacing: 2px;
}

.empty-inheritor :deep(.ant-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .detail-container {
    padding: 28px 24px;
  }

  .basic-info-section {
    flex-direction: column;
    align-items: center;
    gap: 28px;
    text-align: center;
  }

  .inheritor-name {
    font-size: 36px;
  }

  .info-items-container {
    width: 100%;
  }

  .section-title {
    font-size: 24px;
  }

  .works-grid {
    grid-template-columns: 1fr;
  }

  .empty-works {
    padding: 60px 20px;
  }

  .empty-inheritor {
    padding: 80px 20px;
  }
}
</style>

