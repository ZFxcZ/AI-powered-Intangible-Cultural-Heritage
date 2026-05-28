<template>
  <div class="heritage-home">
    <!-- 侧边栏导航 -->
    <aside
      class="sidebar-nav"
      :class="{ active: showSidebar, collapsed: sidebarCollapsed }"
    >
      <div class="sidebar-inner">
        <a
          v-for="item in navItems"
          :key="item.key"
          :class="['nav-item', { active: activeNav === item.key }]"
          @click="scrollToSection(item.key)"
        >
          <component
            :is="iconComponents[item.icon]"
            class="nav-icon"
          />
          <span class="nav-text">{{ item.label }}</span>
        </a>
      </div>
      <button
        class="sidebar-toggle"
        @click.stop="sidebarCollapsed = !sidebarCollapsed"
      >
        <LeftOutlined v-if="!sidebarCollapsed" />
        <RightOutlined v-else />
      </button>
    </aside>

    <!-- 英雄横幅区 -->
    <section class="hero-banner">
      <div class="hero-container">
        <div class="hero-content">
          <h1 class="main-title">
            非遗传承 文化瑰宝
          </h1>
          <p class="subtitle">
            探索中华五千年文化遗产 · 传承匠心技艺
          </p>
          <div class="hero-actions">
            <a-button
              type="primary"
              size="large"
              class="btn-explore"
              @click="exploreHeritage"
            >
              <template #icon>
                <CompassOutlined />
              </template>
              探索非遗
            </a-button>
            <a-button
              size="large"
              class="btn-learn"
              @click="learnCourses"
            >
              <template #icon>
                <BookOutlined />
              </template>
              学习课程
            </a-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 精选作品 -->
    <section
      id="section-featured"
      class="featured-items"
    >
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            精选作品
          </h2>
          <p class="section-subtitle">
            Discover Heritage Masterpieces
          </p>
        </div>
        <a-button
          type="link"
          class="btn-view-more"
          @click="viewAllItems"
        >
          查看更多 <ArrowRightOutlined />
        </a-button>
      </div>

      <div class="items-grid">
        <div
          v-for="item in featuredItems"
          :key="item.id"
          class="item-card"
          @click="viewItemDetail(item.id)"
        >
          <div class="item-image">
            <img
              :src="getItemCoverUrl(item)"
              :alt="item.title"
            >
            <div class="item-category-tag">
              {{ item.category }}
            </div>
          </div>
          <div class="item-content">
            <h4 class="item-title">
              {{ item.title }}
            </h4>
            <p class="item-region">
              <EnvironmentOutlined />
              {{ item.region }}
            </p>
            <p class="item-summary">
              {{ item.summary }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 传承人风采 -->
    <section
      id="section-inheritor"
      class="inheritor-showcase"
    >
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            传承人风采
          </h2>
          <p class="section-subtitle">
            Heritage Inheritors
          </p>
        </div>
        <a-button
          type="link"
          class="btn-view-more"
          @click="viewAllInheritors"
        >
          查看更多 <ArrowRightOutlined />
        </a-button>
      </div>

      <div class="inheritor-grid">
        <div
          v-for="inheritor in inheritors"
          :key="inheritor.id"
          class="inheritor-card"
          @click="viewInheritorDetail(inheritor.id)"
        >
          <div class="inheritor-avatar">
            <img
              :src="getInheritorAvatarUrl(inheritor)"
              :alt="inheritor.name"
            >
          </div>
          <div class="inheritor-info">
            <h4 class="inheritor-name">
              {{ inheritor.name }}
            </h4>
            <p class="inheritor-title">
              {{ inheritor.title }}
            </p>
            <p class="inheritor-region">
              {{ inheritor.region }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 近期活动 -->
    <section
      id="section-activity"
      class="upcoming-activities"
    >
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            近期活动
          </h2>
          <p class="section-subtitle">
            Upcoming Events
          </p>
        </div>
        <a-button
          type="link"
          class="btn-view-more"
          @click="viewAllActivities"
        >
          查看更多 <ArrowRightOutlined />
        </a-button>
      </div>

      <div class="activity-list">
        <div
          v-for="activity in activities"
          :key="activity.id"
          class="activity-card"
          @click="viewActivityDetail(activity.id)"
        >
          <div class="activity-date">
            <div class="date-day">
              {{ formatDateDay(activity.startTime) }}
            </div>
            <div class="date-month">
              {{ formatDateMonth(activity.startTime) }}
            </div>
          </div>
          <div class="activity-content">
            <div class="activity-cover">
              <img
                :src="getActivityCoverUrl(activity)"
                :alt="activity.title"
              >
            </div>
            <div class="activity-info">
              <h4 class="activity-title">
                {{ activity.title }}
              </h4>
              <div class="activity-meta">
                <a-tag color="blue">
                  {{ activity.type }}
                </a-tag>
                <span class="activity-location">
                  <EnvironmentOutlined />
                  {{ activity.location }}
                </span>
              </div>
              <p class="activity-desc">
                {{ activity.description?.substring(0, 100) }}...
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 在线课程 -->
    <section
      id="section-course"
      class="online-courses"
    >
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            在线课程
          </h2>
          <p class="section-subtitle">
            Online Courses
          </p>
        </div>
        <a-button
          type="link"
          class="btn-view-more"
          @click="viewAllCourses"
        >
          查看更多 <ArrowRightOutlined />
        </a-button>
      </div>

      <div class="courses-grid">
        <div
          v-for="course in courses"
          :key="course.id"
          class="course-card"
          @click="viewCourseDetail(course.id)"
        >
          <div class="course-icon">
            <BookOutlined />
          </div>
          <div class="course-content">
            <h4 class="course-title">
              {{ course.title }}
            </h4>
            <div class="course-meta">
              <span class="course-level">{{ getLevelText(course.level) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import {
  CompassOutlined,
  BookOutlined,
  ArrowRightOutlined,
  EnvironmentOutlined,
  StarOutlined,
  UserOutlined,
  ScheduleOutlined,
  LeftOutlined,
  RightOutlined,
} from "@ant-design/icons-vue";
import { getHeritageItemPage } from "@/api/HeritageApi";
import { getInheritorPage } from "@/api/InheritorApi";
import { getActivityPage } from "@/api/ActivityApi";
import { getCoursePage } from "@/api/CourseApi";
import { API_BASE_URL } from "@/config/api";

const router = useRouter();

const featuredItems = ref([]);
const inheritors = ref([]);
const activities = ref([]);
const courses = ref([]);

// 侧边栏导航
const showSidebar = ref(false);
const activeNav = ref("");
const sidebarCollapsed = ref(false);

const navItems = [
  { key: "featured", label: "精品作品", icon: "StarOutlined" },
  { key: "inheritor", label: "传承人风采", icon: "UserOutlined" },
  { key: "activity", label: "近期活动", icon: "ScheduleOutlined" },
  { key: "course", label: "在线课程", icon: "BookOutlined" },
];

// 图标组件映射
const iconComponents = {
  StarOutlined,
  UserOutlined,
  ScheduleOutlined,
  BookOutlined,
};

// 滚动到对应区域
const scrollToSection = (key) => {
  const el = document.getElementById(`section-${key}`);
  if (el) {
    const offsetTop = el.getBoundingClientRect().top + window.scrollY - 80;
    window.scrollTo({ top: offsetTop, behavior: "smooth" });
  }
};

// 监听滚动
const handleScroll = () => {
  showSidebar.value = window.scrollY > 300;

  const sections = ["featured", "inheritor", "activity", "course"];
  for (let i = sections.length - 1; i >= 0; i--) {
    const el = document.getElementById(`section-${sections[i]}`);
    if (el) {
      const rect = el.getBoundingClientRect();
      if (rect.top <= 150) {
        activeNav.value = sections[i];
        break;
      }
    }
  }
};

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
  handleScroll();
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

onMounted(async () => {
  try {
    const itemsRes = await getHeritageItemPage(
      { current: 1, size: 4, status: 2 },
      { showDefaultMsg: false },
    );
    featuredItems.value = itemsRes?.records || [];

    const inheritorsRes = await getInheritorPage(
      { current: 1, size: 4 },
      { showDefaultMsg: false },
    );
    inheritors.value = inheritorsRes?.records || [];

    const activitiesRes = await getActivityPage(
      { current: 1, size: 4, status: 1 },
      { showDefaultMsg: false },
    );
    activities.value = activitiesRes?.records || [];

    const coursesRes = await getCoursePage(
      { current: 1, size: 4, status: 1 },
      { showDefaultMsg: false },
    );
    courses.value = coursesRes?.records || [];
  } catch (error) {
    console.error("加载首页数据失败:", error);
  }
});

const getItemCoverUrl = (item) => {
  if (item.coverImage) return item.coverImage;
  if (item.coverFileId)
    return `${API_BASE_URL}/api/file/preview/${item.coverFileId}`;
  return "/api/placeholder/400/400";
};

const getInheritorAvatarUrl = (inheritor) => {
  if (inheritor.avatarPath) return inheritor.avatarPath;
  if (inheritor.avatarFileId)
    return `${API_BASE_URL}/api/file/preview/${inheritor.avatarFileId}`;
  return "/api/placeholder/160/160";
};

const getActivityCoverUrl = (activity) => {
  if (activity.coverFilePath) return activity.coverFilePath;
  if (activity.coverFileId)
    return `${API_BASE_URL}/api/file/preview/${activity.coverFileId}`;
  return "/api/placeholder/400/300";
};

const formatDateDay = (dateStr) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.getDate();
};

const formatDateMonth = (dateStr) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, "0")}`;
};

const getLevelText = (level) => {
  const map = {
    beginner: "入门",
    elementary: "初级",
    intermediate: "中级",
    advanced: "高级",
  };
  return map[level] || "入门";
};

const exploreHeritage = () => router.push("/heritage");
const learnCourses = () => router.push("/course");
const viewAllItems = () => router.push("/heritage");
const viewItemDetail = (id) => router.push(`/heritage/${id}`);
const viewAllInheritors = () => router.push("/inheritor");
const viewInheritorDetail = (id) => router.push(`/inheritor/${id}`);
const viewAllActivities = () => router.push("/activity");
const viewActivityDetail = (id) => router.push(`/activity/${id}`);
const viewAllCourses = () => router.push("/course");
const viewCourseDetail = (id) => router.push(`/course/${id}`);
</script>

<style scoped>
.heritage-home {
  width: 100%;
  background: #f8fafc;
}

/* 侧边栏导航 */
.sidebar-nav {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.sidebar-nav.active {
  opacity: 1;
  visibility: visible;
}

.sidebar-nav.collapsed .sidebar-inner {
  opacity: 0;
  visibility: hidden;
  transform: translateX(-20px);
  pointer-events: none;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: white;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 14px;
  white-space: nowrap;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.nav-item.active {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  color: white;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-text {
  font-weight: 500;
}

.sidebar-toggle {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 12px;
}

.sidebar-toggle:hover {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  color: white;
  border-color: #8b4513;
}

@media (max-width: 1400px) {
  .sidebar-nav {
    left: 10px;
  }

  .nav-text {
    display: none;
  }

  .nav-item {
    padding: 10px;
    justify-content: center;
  }
}

@media (max-width: 1200px) {
  .sidebar-nav {
    display: none;
  }
}

/* 英雄横幅 */
.hero-banner {
  background-image: url("/src/assets/Design006_n2NkQWib3A.jpg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 120px 24px 80px;
  position: relative;
  overflow: hidden;
}

.hero-banner::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 0;
}

.hero-container {
  max-width: 1280px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-content {
  max-width: 700px;
}

.main-title {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 16px;
  color: white;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.btn-explore {
  background: white !important;
  color: #3b82f6 !important;
  border: none !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  height: 48px !important;
  padding: 0 32px !important;
}

.btn-explore:hover {
  background: #f8fafc !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

.btn-learn {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  color: white !important;
  border-radius: 6px !important;
  font-weight: 600 !important;
  height: 48px !important;
  padding: 0 32px !important;
}

.btn-learn:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: white !important;
}

/* 通用 Section */
.featured-items,
.inheritor-showcase,
.upcoming-activities,
.online-courses {
  max-width: 1280px;
  margin: 0 auto;
  padding: 80px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 48px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.header-left {
  flex: 1;
}

.section-title {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.section-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.btn-view-more {
  color: #3b82f6 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
}

.btn-view-more:hover {
  color: #2563eb !important;
}

/* 作品网格 */
.items-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.item-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.item-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.item-image {
  height: 200px;
  position: relative;
  overflow: hidden;
  background: #f1f5f9;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.item-card:hover .item-image img {
  transform: scale(1.05);
}

.item-category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #3b82f6;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.item-content {
  padding: 20px;
}

.item-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-region {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-summary {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 传承人网格 */
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.inheritor-card {
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.inheritor-card:hover {
  transform: translateY(-4px);
}

.inheritor-card:hover .inheritor-avatar {
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.25);
}

.inheritor-avatar {
  width: 140px;
  height: 140px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
}

.inheritor-avatar::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6) border-box;
  -webkit-mask:
    linear-gradient(#fff 0 0) padding-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

.inheritor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.inheritor-info {
  padding: 0;
  width: 100%;
}

.inheritor-name {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.inheritor-title {
  font-size: 13px;
  color: #3b82f6;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.inheritor-region {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  display: flex;
  gap: 24px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.activity-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.activity-date {
  flex-shrink: 0;
  width: 80px;
  text-align: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.date-day {
  font-size: 32px;
  font-weight: 800;
  color: #3b82f6;
  line-height: 1;
  margin-bottom: 4px;
}

.date-month {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.activity-content {
  flex: 1;
  display: flex;
  gap: 20px;
}

.activity-cover {
  width: 160px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #f1f5f9;
}

.activity-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info {
  flex: 1;
}

.activity-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 12px;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.activity-location {
  font-size: 14px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.course-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 16px;
}

.course-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.course-icon {
  width: 48px;
  height: 48px;
  background: #eff6ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  font-size: 20px;
  flex-shrink: 0;
}

.course-content {
  flex: 1;
}

.course-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.course-level {
  font-size: 13px;
  color: #3b82f6;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1024px) {
  .items-grid,
  .inheritor-grid,
  .courses-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .main-title {
    font-size: 36px;
  }

  .items-grid,
  .inheritor-grid,
  .courses-grid {
    grid-template-columns: 1fr;
  }

  .activity-card {
    flex-direction: column;
  }

  .activity-content {
    flex-direction: column;
  }

  .activity-cover {
    width: 100%;
    height: 160px;
  }
}
</style>
