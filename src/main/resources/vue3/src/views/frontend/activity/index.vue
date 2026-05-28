<template>
  <div class="activity-list">
    <!-- 页面标题 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">活动中心</h1>
        <p class="scroll-header-subtitle">探索丰富多彩的非遗文化活动</p>
        <div class="scroll-header-seal">活动</div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model:value="searchForm.title"
            placeholder="搜索活动标题"
            allow-clear
            @press-enter="handleSearch"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </a-input>
        </a-col>
        <a-col :span="5">
          <a-select
            v-model:value="searchForm.type"
            placeholder="活动类型"
            allow-clear
            style="width: 100%"
          >
            <a-select-option value="展演">展演</a-select-option>
            <a-select-option value="展览">展览</a-select-option>
            <a-select-option value="培训">培训</a-select-option>
            <a-select-option value="比赛">比赛</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="5">
          <a-select
            v-model:value="searchForm.status"
            placeholder="活动状态"
            allow-clear
            style="width: 100%"
          >
            <a-select-option :value="1">报名中</a-select-option>
            <a-select-option :value="2">进行中</a-select-option>
            <a-select-option :value="3">已结束</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-button type="primary" block @click="handleSearch">
            <i class="fas fa-search"></i> 搜索
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 活动列表 -->
    <div class="activity-grid">
      <a-spin :spinning="loading">
        <a-row :gutter="[24, 24]">
          <a-col
            v-for="activity in activityList"
            :key="activity.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <div class="activity-card" @click="goToDetail(activity.id)">
              <div class="activity-cover">
                <img :src="getActivityCover(activity)" :alt="activity.title" />
                <div class="activity-status">
                  <a-tag :color="getStatusColor(activity.status)">
                    {{ activity.statusName }}
                  </a-tag>
                </div>
              </div>
              <div class="activity-content">
                <h3 class="activity-title">{{ activity.title }}</h3>
                <div class="activity-meta">
                  <div class="meta-item">
                    <i class="fas fa-tag"></i>
                    <span>{{ activity.type }}</span>
                  </div>
                  <div class="meta-item">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>{{ activity.location || "待定" }}</span>
                  </div>
                  <div class="meta-item">
                    <i class="fas fa-calendar"></i>
                    <span>{{ formatDate(activity.startTime) }}</span>
                  </div>
                </div>
                <div class="activity-action">
                  <a-button
                    v-if="activity.status === 1"
                    type="primary"
                    size="small"
                    @click.stop="handleSignup(activity)"
                  >
                    立即报名
                  </a-button>
                  <a-button v-else size="small"> 查看详情 </a-button>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>

        <!-- 空状态 -->
        <a-empty
          v-if="!loading && activityList.length === 0"
          description="暂无活动"
        />
      </a-spin>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="pagination.total > 0">
      <a-pagination
        v-model:current="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="true"
        :show-total="(total) => `共 ${total} 条`"
        @change="handlePageChange"
      />
    </div>

    <!-- 报名弹窗 -->
    <a-modal
      v-model:open="isSignupModalVisible"
      title="活动报名"
      @ok="handleSignupOk"
      @cancel="isSignupModalVisible = false"
    >
      <div class="signup-info">
        <h3>{{ selectedActivity?.title }}</h3>
        <p>
          <strong>活动时间：</strong
          >{{ formatDateTime(selectedActivity?.startTime) }} 至
          {{ formatDateTime(selectedActivity?.endTime) }}
        </p>
        <p><strong>活动地点：</strong>{{ selectedActivity?.location }}</p>
        <p><strong>活动类型：</strong>{{ selectedActivity?.type }}</p>
        <a-alert
          message="温馨提示"
          description="提交报名后，我们会尽快审核您的申请，请保持手机畅通。"
          type="info"
          show-icon
          style="margin-top: 16px"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { getActivityPage, signupActivity } from "@/api/ActivityApi";
import { useUserStore } from "@/store/user";
import "@/styles/scroll-header.css";

const router = useRouter();
const userStore = useUserStore();

// 搜索表单
const searchForm = reactive({
  title: "",
  type: undefined,
  status: 2, // 默认显示进行中的活动
});

// 活动列表
const activityList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
});

// 报名相关
const isSignupModalVisible = ref(false);
const selectedActivity = ref(null);

// 加载数据
const loadData = () => {
  loading.value = true;
  getActivityPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    },
    {
      onSuccess: (data) => {
        activityList.value = data.records || [];
        pagination.total = data.total || 0;
        loading.value = false;
      },
      onError: () => {
        loading.value = false;
      },
    },
  );
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

// 分页变化
const handlePageChange = () => {
  loadData();
  window.scrollTo({ top: 0, behavior: "smooth" });
};

// 跳转详情
const goToDetail = (id) => {
  router.push(`/activity/${id}`);
};

// 报名
const handleSignup = (activity) => {
  if (!userStore.isLoggedIn) {
    message.warning("请先登录");
    router.push("/login");
    return;
  }

  selectedActivity.value = activity;
  isSignupModalVisible.value = true;
};

// 确认报名
const handleSignupOk = () => {
  if (!userStore.userId) {
    message.error("用户信息异常");
    return;
  }

  // 活动ID是字符串格式（UUID），直接使用
  const activityId = selectedActivity.value.id;
  if (!activityId) {
    message.error("活动ID不能为空");
    return;
  }

  signupActivity(
    {
      activityId: activityId, // 直接使用字符串ID
      userId: userStore.userId,
    },
    {
      onSuccess: () => {
        message.success("报名成功，请等待审核");
        isSignupModalVisible.value = false;
        selectedActivity.value = null;
      },
      successMsg: false,
    },
  );
};

// 工具函数
const getStatusColor = (status) => {
  const colors = {
    1: "blue",
    2: "green",
    3: "gray",
  };
  return colors[status] || "default";
};

const getActivityCover = (activity) => {
  if (activity.coverFilePath) {
    const path = activity.coverFilePath;
    if (path.startsWith("http")) return path;
    // 静态资源直接访问，不需要API前缀
    return path.startsWith("/") ? path : "/" + path;
  }
  return (
    "https://via.placeholder.com/400x300?text=" +
    encodeURIComponent(activity.title)
  );
};

const formatDate = (dateStr) => {
  return dateStr ? dayjs(dateStr).format("YYYY-MM-DD") : "-";
};

const formatDateTime = (dateStr) => {
  return dateStr ? dayjs(dateStr).format("YYYY-MM-DD HH:mm") : "-";
};

// 页面加载
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
/* 新中式配色 */
.activity-list {
  min-height: 100vh;
  background-image: url("/src/assets/1690268977680789.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}

.activity-list::before {
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

.activity-list > * {
  position: relative;
  z-index: 1;
}

/* 搜索筛选区域 - 简洁设计 */
.search-section {
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

.search-section :deep(.ant-btn-primary) {
  background: #8b4513; /* 使用传统棕色 */
  border-color: #8b4513;
}

.search-section :deep(.ant-btn-primary:hover) {
  background: #a0522d;
  border-color: #a0522d;
}

.activity-grid {
  min-height: 400px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
}

.activity-card {
  background: #fff;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e8e8e8; /* 移除圆角和阴影，使用边框 */

  &:hover {
    transform: translateY(-2px); /* 减小悬停移动距离 */
    border-color: #8b4513; /* 悬停时边框变色 */
  }

  .activity-cover {
    position: relative;
    height: 200px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    &:hover img {
      transform: scale(1.05);
    }

    .activity-status {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }

  .activity-content {
    padding: 16px;

    .activity-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 12px;
      color: #2c2c2c; /* 墨黑色 */
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-family: "SimSun", "宋体", serif;
      letter-spacing: 1px;
    }

    .activity-meta {
      margin-bottom: 16px;

      .meta-item {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;

        i {
          margin-right: 8px;
          color: #999;
          width: 16px;
        }

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .activity-action {
      text-align: center;
    }
  }
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin: 32px auto 0;
  padding: 24px 40px;
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

.signup-info {
  h3 {
    font-size: 18px;
    font-weight: 500;
    margin-bottom: 16px;
    color: #2c2c2c;
  }

  p {
    margin-bottom: 8px;
    line-height: 1.8;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-section {
    padding: 16px 20px;
  }

  .activity-grid {
    padding: 0 20px;
  }

  .pagination-section {
    padding: 24px 20px;
  }
}
</style>
