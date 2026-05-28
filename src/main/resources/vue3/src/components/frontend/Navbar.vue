<template>
  <header class="frontend-navbar">
    <div class="navbar-container">
      <!-- Logo和站点名称 -->
      <div class="navbar-logo">
        <router-link to="/home">
          <img :src="siteConfig.logo.icon" alt="Logo" class="logo-icon" />
          <span class="logo-text">{{ siteConfig.logo.text }}</span>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <nav class="navbar-menu">
        <ul class="menu-list">
          <li class="menu-item" :class="{ active: selectedKeys.includes('home') }">
            <router-link to="/home" @click="setActiveKey('home')">
              <HomeOutlined />
              <span>首页</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: selectedKeys.includes('heritage') }">
            <router-link to="/heritage" @click="setActiveKey('heritage')">
              <i class="fas fa-landmark"></i>
              <span>非遗作品</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: selectedKeys.includes('inheritor') }">
            <router-link to="/inheritor" @click="setActiveKey('inheritor')">
              <i class="fas fa-user-tie"></i>
              <span>传承人</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: selectedKeys.includes('activity') }">
            <router-link to="/activity" @click="setActiveKey('activity')">
              <i class="fas fa-calendar-alt"></i>
              <span>活动中心</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: selectedKeys.includes('course') }">
            <router-link to="/course" @click="setActiveKey('course')">
              <i class="fas fa-graduation-cap"></i>
              <span>在线课程</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: selectedKeys.includes('shop') }">
            <router-link to="/shop" @click="setActiveKey('shop')">
              <i class="fas fa-shopping-bag"></i>
              <span>手办商城</span>
            </router-link>
          </li>
          <li v-if="isLoggedIn" class="menu-item" :class="{ active: selectedKeys.includes('profile') }">
            <router-link to="/profile" @click="setActiveKey('profile')">
              <UserOutlined />
              <span>个人中心</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- 右侧用户区域 -->
      <div class="navbar-user">
        <template v-if="isLoggedIn">
          <!-- 用户信息下拉菜单 -->
          <div class="user-dropdown">
            <button class="user-info" @click="toggleDropdown">
              <a-avatar :size="32" :src="userStore.avatar">
                {{ userStore.userInfo.username?.charAt(0) || "U" }}
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo.username }}</span>
              <DownOutlined :rotate="dropdownOpen ? 180 : 0" />
            </button>
            <div v-if="dropdownOpen" class="dropdown-menu">
              <a href="/profile" class="dropdown-item">
                <UserOutlined />
                <span>个人中心</span>
              </a>
              <a href="/orders" class="dropdown-item">
                <i class="fas fa-shopping-cart"></i>
                <span>我的订单</span>
              </a>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item" @click="handleLogout">
                <LogoutOutlined />
                <span>退出登录</span>
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <!-- 未登录状态 -->
          <div class="auth-buttons">
            <button
              class="btn btn-secondary"
              @click="router.push('/auth/login')"
            >
              登录
            </button>
            <button
              class="btn btn-primary"
              @click="router.push('/auth/register')"
            >
              注册
            </button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/store/user";
import { Modal } from "ant-design-vue";
import siteConfig from "@/config/site";
import {
  HomeOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined,
} from "@ant-design/icons-vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const selectedKeys = ref(["home"]);
const isLoggedIn = computed(() => userStore.isLoggedIn);
const dropdownOpen = ref(false);

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  const dropdown = document.querySelector('.user-dropdown');
  if (dropdown && !dropdown.contains(event.target)) {
    dropdownOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

watch(
  () => route.path,
  (newPath) => {
    if (newPath === "/home") {
      selectedKeys.value = ["home"];
    } else if (newPath.startsWith("/heritage")) {
      selectedKeys.value = ["heritage"];
    } else if (newPath.startsWith("/inheritor")) {
      selectedKeys.value = ["inheritor"];
    } else if (newPath.startsWith("/activity")) {
      selectedKeys.value = ["activity"];
    } else if (newPath.startsWith("/course")) {
      selectedKeys.value = ["course"];
    } else if (newPath.startsWith("/shop")) {
      selectedKeys.value = ["shop"];
    } else if (newPath.startsWith("/profile")) {
      selectedKeys.value = ["profile"];
    } else {
      selectedKeys.value = [];
    }
  },
  { immediate: true },
);

const setActiveKey = (key) => {
  selectedKeys.value = [key];
};

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value;
};

const handleLogout = () => {
  Modal.confirm({
    title: "确认退出",
    content: "确定要退出登录吗？",
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      await userStore.logout();
      router.push("/auth/login");
    },
  });
};
</script>

<style scoped>
.frontend-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  border-bottom: var(--border-width) solid #E5E7EB;
  box-shadow: var(--shadow-sm);
  padding: 0;
  height: 72px;
  line-height: 72px;
}

.navbar-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.navbar-logo a {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
  gap: var(--spacing-sm);
  transition: all var(--transition-fast);
}

.logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: var(--border-radius-sm);
}

.logo-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
  font-family: var(--font-title);
}

.navbar-menu {
  flex: 1;
  margin-left: 3rem;
  margin-right: var(--spacing-xl);
}

.menu-list {
  display: flex;
  align-items: center;
  gap: 2rem;
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  position: relative;
}

.menu-item a {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--color-text-secondary);
  font-weight: 500;
  font-size: 0.875rem;
  text-decoration: none;
  transition: all var(--transition-fast);
  border-radius: var(--border-radius-md);
}

.menu-item a:hover {
  color: var(--color-primary);
  background: rgba(22, 93, 255, 0.05);
}

.menu-item.active a {
  color: var(--color-primary);
  background: transparent;
  font-weight: 600;
  font-size: 0.9375rem;
}

.menu-item.active a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--gradient-primary);
  border-radius: 2px;
}

.navbar-user {
  flex-shrink: 0;
}

/* 用户下拉菜单 */
.user-dropdown {
  position: relative;
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-primary);
  background: transparent;
  border: none;
  font-family: var(--font-body);
}

.user-info:hover {
  background: rgba(22, 93, 255, 0.05);
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  font-size: 0.875rem;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: var(--spacing-xs);
  background: white;
  border: var(--border-width) solid #E5E7EB;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-md);
  padding: var(--spacing-sm);
  min-width: 180px;
  z-index: 1001;
  animation: fadeIn var(--transition-fast) ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  text-decoration: none;
  transition: all var(--transition-fast);
  border-radius: var(--border-radius-sm);
  background: transparent;
  border: none;
  width: 100%;
  text-align: center;
  justify-content: center;
  cursor: pointer;
  font-family: var(--font-body);
}

.dropdown-item:hover {
  color: var(--color-primary);
  background: rgba(22, 93, 255, 0.05);
}

.dropdown-divider {
  height: var(--border-width);
  background: #E5E7EB;
  margin: var(--spacing-sm) 0;
}

/* 登录注册按钮 */
.auth-buttons {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 var(--spacing-sm);
  }

  .navbar-menu {
    display: none;
  }

  .logo-text {
    font-size: 1rem;
  }

  .user-name {
    display: none;
  }

  .auth-buttons {
    gap: var(--spacing-xs);
  }

  .auth-buttons .btn {
    padding: 0.5rem 1rem;
    font-size: 0.75rem;
  }
}
</style>
