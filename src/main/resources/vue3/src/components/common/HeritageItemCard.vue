<template>
  <div class="heritage-item-card" @click="handleCardClick">
    <!-- 封面图片 -->
    <div class="card-cover">
      <img
        v-if="item.coverImage"
        :src="item.coverImage"
        :alt="item.title"
        class="cover-image"
      />
      <div v-else class="no-image">
        <i class="fas fa-image"></i>
        <span>暂无封面</span>
      </div>

      <!-- 状态标签 -->
      <div class="status-tag" :class="statusClass">
        {{ item.statusName || getStatusName(item.status) }}
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="card-content">
      <!-- 标题 -->
      <h3 class="item-title" :title="item.title">{{ item.title }}</h3>

      <!-- 类别和地区 -->
      <div class="item-meta">
        <span v-if="item.category" class="meta-tag category">
          <i class="fas fa-tag"></i>
          {{ item.category }}
        </span>
        <span v-if="item.region" class="meta-tag region">
          <i class="fas fa-map-marker-alt"></i>
          {{ item.region }}
        </span>
      </div>

      <!-- 摘要 -->
      <p v-if="item.summary" class="item-summary">{{ item.summary }}</p>

      <!-- 底部信息 -->
      <div class="card-footer">
        <div class="time-info">
          <i class="fas fa-clock"></i>
          <span>{{ formatDate(item.createTime) }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showActions" class="card-actions">
        <a-space>
          <a-button
            v-if="canEdit"
            type="primary"
            size="small"
            @click.stop="handleEdit"
          >
            编辑
          </a-button>
          <a-button
            v-if="canPublish"
            type="primary"
            size="small"
            @click.stop="handlePublish"
          >
            发布
          </a-button>
          <a-button
            v-if="canOffline"
            type="default"
            size="small"
            @click.stop="handleOffline"
          >
            下架
          </a-button>
          <a-button
            v-if="canDelete"
            danger
            size="small"
            @click.stop="handleDelete"
          >
            删除
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import DateUtils from "@/utils/dateUtils";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  showActions: {
    type: Boolean,
    default: false,
  },
  clickable: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["click", "edit", "publish", "offline", "delete"]);

const statusClass = computed(() => {
  const statusMap = {
    0: "draft",
    1: "pending",
    2: "published",
    3: "offline",
  };
  return statusMap[props.item.status] || "draft";
});

const canEdit = computed(() => {
  return props.item.status === 0 || props.item.status === 1;
});

const canPublish = computed(() => {
  return props.item.status === 0 || props.item.status === 1;
});

const canOffline = computed(() => {
  return props.item.status === 2;
});

const canDelete = computed(() => {
  return props.item.status === 0 || props.item.status === 3;
});

function getStatusName(status) {
  const statusMap = {
    0: "草稿",
    1: "待审",
    2: "已发布",
    3: "下架",
  };
  return statusMap[status] || "未知";
}

function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    return DateUtils.formatDate(dateStr);
  } catch (error) {
    return "";
  }
}

function handleCardClick() {
  if (props.clickable) {
    emit("click", props.item);
  }
}

function handleEdit() {
  emit("edit", props.item);
}

function handlePublish() {
  emit("publish", props.item);
}

function handleOffline() {
  emit("offline", props.item);
}

function handleDelete() {
  emit("delete", props.item);
}
</script>

<style scoped>
.heritage-item-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s ease;
  background: white;
  cursor: pointer;
}

.heritage-item-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
  border-color: #cbd5e1;
}

.card-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #f1f5f9;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.heritage-item-card:hover .cover-image {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  color: #94a3b8;
}

.no-image i {
  font-size: 48px;
  margin-bottom: 8px;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.status-tag.draft {
  background: #64748b;
}

.status-tag.pending {
  background: #f59e0b;
}

.status-tag.published {
  background: #10b981;
}

.status-tag.offline {
  background: #ef4444;
}

.card-content {
  padding: 20px;
}

.item-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 12px;
  color: #475569;
  font-weight: 500;
}

.meta-tag.category {
  background: #eff6ff;
  color: #3b82f6;
}

.meta-tag.region {
  background: #f0fdf4;
  color: #10b981;
}

.item-summary {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.creator-info,
.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .card-content {
    padding: 16px;
  }

  .item-title {
    font-size: 15px;
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
