<template>
  <div class="activity-detail">
    <a-spin :spinning="loading">
      <div v-if="activity" class="detail-container">
        <!-- 活动信息 -->
        <div class="activity-info">
          <a-row :gutter="32">
            <a-col :span="12">
              <!-- 封面图 -->
              <div class="activity-cover">
                <img :src="getActivityCover()" :alt="activity.title" />
              </div>
            </a-col>

            <a-col :span="12">
              <!-- 活动标题和基本信息 -->
              <div class="activity-header">
                <h1>{{ activity.title }}</h1>
                <div class="activity-tags">
                  <a-tag>{{ activity.type }}</a-tag>
                  <a-tag :color="getStatusColor(activity.status)">
                    {{ activity.statusName }}
                  </a-tag>
                </div>

                <!-- 快速信息 -->
                <div class="quick-info">
                  <div class="info-row">
                    <i class="fas fa-calendar-alt" />
                    <div class="info-content">
                      <span class="label">活动时间</span>
                      <span class="value">{{
                        formatDateTime(activity.startTime)
                      }}</span>
                      <span class="value"
                        >至 {{ formatDateTime(activity.endTime) }}</span
                      >
                    </div>
                  </div>
                  <div class="info-row">
                    <i class="fas fa-map-marker-alt" />
                    <div class="info-content">
                      <span class="label">活动地点</span>
                      <span class="value"
                        >{ activity.location || "待定" }}</span
                      >
                    </div>
                  </div>
                  <div class="info-row">
                    <i class="fas fa-users" />
                    <div class="info-content">
                      <span class="label">报名人数</span>
                      <span class="value"
                        >{{ activity.signupCount || 0 }} 人</span
                      >
                    </div>
                  </div>
                </div>

                <!-- 报名按钮 -->
                <div class="action-buttons">
                  <template v-if="activity.status === 1">
                    <a-button
                      type="primary"
                      size="large"
                      block
                      @click="handleSignup"
                    >
                      <i class="fas fa-edit" />
                      立即报名
                    </a-button>
                  </template>

                  <template v-else-if="activity.stat == 2">
                    <a-button size="large" block disabled>
                      <i class="fas fa-clock" />
                      活动进行中
                    </a-button>
                  </template>

                  <template v-else-if="activity.status = 3">
                    <a-button size="large" block disabled>
                      <i class="fas fa-times-circle" />
                      活动已结束
                    </a-button>
                  </template>
                  <template v-else>
                    <a-button size="large" block disabled>
                      <i class="fas fa-hourglass-half" />
                      筹备中
                    </a-button>
                  </template>
                </div>
              </div>
            </a-col>
          </a-row>

          <!-- 活动详情区域 -->
          <a-row :gutter="32" style="margin-top: 32px">
            <a-col :span="16">
              <!-- 活动描述 -->
              <div class="info-section">
                <h2><i class="fas fa-file-alt" /> 活动详情</h2>
                <div class="activity-description">
                  {{ activity.description || "暂无详细描述" }}
                </div>
              </div>
            </a-col>

            <a-col :span="8">
              <!-- 报名须知 -->
              <div class="signup-tips-card">
                <h3>
                  <i class="fas fa-info-circle" />
                  报名须知
                </h3>
                <ul>
                  <li>请确保个人信息准确无误</li>
                  <li>报名后请等待审核通知</li>
                  <li>活动当天请准时签到</li>
                  <li>如有疑问请联系工作人员</li>
                </ul>
              </div>
            </a-col>
          </a-row>
        </div>
      </div>
    </a-spin>

    <!-- 报名弹窗 -->
    <a-modal
      v-model:open="isSignupModalVisible"
      title="活动报名"
      @ok="handleSignupOk"
      @cancel="isSignupModalVisible = false"
    >
      <div class="signup-form">
        <h3>{{ activity?.title }}</h3>
        <p>
          <strong>活动时间：</strong
          >{{ formatDateTime(activity?.startTime) }} 至
          {{ formatDateTime(activity?.endTime) }}
        </p>
        <p><strong>活动地点：</strong>{{ activity?.location }}</p>
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
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { getActivityDetail, signupActivity } from "@/api/ActivityApi";
import { useUserStore } from "@/store/user";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activity = ref(null);
const loading = ref(false);
const isSignupModalVisible = ref(false);

// 加载活动详情
const loadDetail = () => {
  loading.value = true;
  getActivityDetail(
    { activityId: route.params.id },
    {
      onSuccess: (data) => {
        activity.value = data;
        loading.value = false;
      },
      onError: () => {
        loading.value = false;
        message.error("加载失败");
        router.back();
      },
    },
  );
};

// 报名
const handleSignup = () => {
  if (!userStore.isLoggedIn) {
    message.warning("请先登录");
    router.push("/login");
    return;
  }
  isSignupModalVisible.value = true;
};

// 确认报名
const handleSignupOk = () => {
  if (!userStore.userId) {
    message.error("用户信息异常");
    return;
  }

  // 活动ID是字符串格式（UUID），直接使用
  const activityId = activity.value.id;
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
        loadDetail(); // 刷新数据
      },
      successMsg: false,
    },
  );
};

// 工具函数
const getStatusColor = (status) => {
  const colors = {
    0: "default",
    1: "blue",
    2: "green",
    3: "gray",
  };
  return colors[status] || "default";
};

const getActivityCover = () => {
  if (activity.value?.coverFilePath) {
    const path = activity.value.coverFilePath;
    if (path.startsWith("http")) return path;
    // 静态资源直接访问，不需要API前缀
    return path.startsWith("/") ? path : "/" + path;
  }
  return (
    "https://via.placeholder.com/1200x400?text=" +
    encodeURIComponent(activity.value?.title || "活动")
  );
};

const formatDateTime = (dateStr) => {
  return dateStr ? dayjs(dateStr).format("YYYY-MM-DD HH:mm") : "-";
};

onMounted(() => {
  loadDetail();
});
</script>

<style scoped lang="less">
.activity-detail {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.activity-cover {
  width: 100%;
  aspect-ratio: 1 / 1; /* 方形 */
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.12);
  border: 1px solid #e8e8e8;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.activity-header {
  padding: 0;

  h1 {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 20px;
    color: #2c2c2c;
    font-family: "SimSun", "宋体", serif;
    letter-spacing: 3px;
    line-height: 1.4;
  }

  .activity-tags {
    margin-bottom: 32px;

    :deep(.ant-tag) {
      font-size: 14px;
      padding: 6px 14px;
      border-radius: 4px;
      margin-right: 10px;
    }
  }

  .quick-info {
    background: rgba(139, 69, 19, 0.03);
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 24px;

    .info-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      i {
        font-size: 20px;
        color: #8b4513;
        width: 28px;
        margin-right: 16px;
        margin-top: 2px;
      }

      .info-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .label {
          font-size: 14px;
          color: #999;
          font-weight: 500;
          letter-spacing: 0.5px;
        }

        .value {
          font-size: 16px;
          color: #2c2c2c;
          font-weight: 500;
          line-height: 1.6;
        }
      }
    }
  }

  .action-buttons {
    :deep(.ant-btn) {
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 2px;

      i {
        margin-right: 8px;
      }
    }

    :deep(.ant-btn-primary) {
      background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
      border: none;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
      }
    }
  }
}

.activity-info {
  padding: 32px;

  .info-section {
    background: #fff;
    padding: 28px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
    border: 1px solid #e8e8e8;

    h2 {
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 24px;
      padding-bottom: 14px;
      border-bottom: 2px solid #e8e8e8;
      color: #2c2c2c;
      font-family: "SimSun", "宋体", serif;
      letter-spacing: 2px;

      i {
        margin-right: 10px;
        color: #8b4513;
      }
    }

    :deep(.ant-descriptions-item-label) {
      font-weight: 600;
      color: #666;
      letter-spacing: 0.5px;
    }

    :deep(.ant-descriptions-item-content) {
      color: #2c2c2c;
    }

    .activity-description {
      font-size: 16px;
      line-height: 2;
      color: #555;
      white-space: pre-wrap;
    }
  }
}

.signup-tips-card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
  position: sticky;
  top: 120px;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #2c2c2c;
    letter-spacing: 1px;
    display: flex;
    align-items: center;

    i {
      margin-right: 8px;
      color: #8b4513;
    }
  }

  ul {
    padding-left: 20px;
    margin: 0;

    li {
      font-size: 14px;
      color: #666;
      line-height: 2.2;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.signup-form {
  h3 {
    font-size: 18px;
    font-weight: 500;
    margin-bottom: 16px;
  }

  p {
    margin-bottom: 8px;
    line-height: 1.8;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-info {
    padding: 20px;

    :deep(.ant-row) {
      flex-direction: column;
    }

    :deep(.ant-col) {
      max-width: 100% !important;
      flex: 0 0 100% !important;
      margin-bottom: 24px;
    }
  }

  .activity-header {
    h1 {
      font-size: 28px;
      letter-spacing: 2px;
    }

    .quick-info {
      padding: 20px;

      .info-row {
        margin-bottom: 16px;

        i {
          font-size: 18px;
        }

        .info-content {
          .value {
            font-size: 15px;
          }
        }
      }
    }

    .action-buttons {
      :deep(.ant-btn) {
        height: 44px;
        font-size: 15px;
      }
    }
  }

  .signup-tips-card {
    position: static;
    margin-bottom: 24px;
  }

  .info-section {
    padding: 20px;

    h2 {
      font-size: 20px;
    }
  }
}
</style>
