<template>
  <div class="course-list">
    <!-- 页面标题 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">在线课程</h1>
        <p class="scroll-header-subtitle">学习非遗文化，传承匠心技艺</p>
        <div class="scroll-header-seal">课程</div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-input
            v-model:value="searchForm.title"
            placeholder="搜索课程标题"
            allow-clear
            @press-enter="handleSearch"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </a-input>
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="searchForm.level"
            placeholder="难度等级"
            allow-clear
            style="width: 100%"
          >
            <a-select-option
              v-for="option in COURSE_LEVEL_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-button type="primary" block @click="handleSearch">
            <i class="fas fa-search"></i> 搜索
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 课程列表 -->
    <div class="course-grid">
      <a-spin :spinning="loading">
        <a-row :gutter="[24, 24]">
          <a-col
            v-for="course in courseList"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <div class="course-card" @click="goToDetail(course.id)">
              <div class="course-content">
                <div class="course-header-row">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <a-tag :color="getCourseLevelColor(course.level)">
                    {{ getCourseLevelName(course.level) }}
                  </a-tag>
                </div>
                <div class="course-meta">
                  <div class="meta-item">
                    <i class="fas fa-book"></i>
                    <span>{{ course.chapterCount || 0 }} 章节</span>
                  </div>
                  <div class="meta-item">
                    <i class="fas fa-signal"></i>
                    <span>{{ getCourseLevelName(course.level) }}</span>
                  </div>
                </div>
                <div class="course-action">
                  <a-button type="primary" size="small"> 开始学习 </a-button>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>

        <!-- 空状态 -->
        <a-empty
          v-if="!loading && courseList.length === 0"
          description="暂无课程"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getCoursePage } from "@/api/CourseApi";
import {
  COURSE_LEVEL_OPTIONS,
  getCourseLevelName,
  getCourseLevelColor,
} from "@/config/courseLevel";
import "@/styles/scroll-header.css";

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  title: "",
  level: undefined,
  status: 1, // 只显示已发布的课程
});

// 课程列表
const courseList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
});

// 加载数据
const loadData = () => {
  loading.value = true;
  getCoursePage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    },
    {
      onSuccess: (data) => {
        courseList.value = data.records || [];
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
  router.push(`/course/${id}`);
};

// 页面加载
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
/* 新中式配色 */
.course-list {
  min-height: 100vh;
  background-image: url("/src/assets/1690268977680789.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
}

.course-list::before {
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

.course-list > * {
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

.course-grid {
  min-height: 400px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
}

.course-card {
  background: #fff;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    border-color: #8b4513;
    box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  }

  .course-content {
    padding: 24px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .course-header-row {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 2px solid #f0f0f0;
    }

    .course-title {
      font-size: 18px;
      font-weight: 600;
      color: #2c2c2c;
      flex: 1;
      line-height: 1.5;
      font-family: "SimSun", "宋体", serif;
      letter-spacing: 1px;
      margin: 0;
    }

    .course-meta {
      margin-bottom: 20px;
      flex: 1;

      .meta-item {
        display: inline-flex;
        align-items: center;
        font-size: 14px;
        color: #666;
        margin-right: 20px;
        margin-bottom: 8px;

        i {
          margin-right: 6px;
          color: #8b4513;
          font-size: 16px;
        }
      }
    }

    .course-action {
      text-align: center;
      margin-top: auto;

      :deep(.ant-btn) {
        width: 100%;
        height: 40px;
        font-size: 15px;
        font-weight: 500;
        letter-spacing: 2px;
        background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
        border: none;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
        }
      }
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

/* 响应式设计 */
@media (max-width: 768px) {
  .search-section {
    padding: 16px 20px;
  }

  .course-grid {
    padding: 0 20px;
  }

  .pagination-section {
    padding: 24px 20px;
  }
}
</style>
