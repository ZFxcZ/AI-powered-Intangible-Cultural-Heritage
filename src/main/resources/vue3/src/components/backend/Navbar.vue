<template>
  <div class="navbar">
    <div class="left-menu">
      <div class="hamburger" @click="toggleSidebar">
        <MenuUnfoldOutlined v-if="appStore.sidebarCollapsed" />
        <MenuFoldOutlined v-else />
      </div>
      <a-breadcrumb separator="/">
        <a-breadcrumb-item>
          <router-link to="/dashboard">首页</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item v-if="route.meta.title">{{
          route.meta.title
        }}</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <div class="right-menu">
      <div class="right-menu-item" @click="toggleFullScreen">
        <FullscreenExitOutlined v-if="isFullscreen" />
        <FullscreenOutlined v-else />
      </div>

      <a-dropdown trigger="click">
        <div class="avatar-wrapper">
          <a-avatar :size="32" :src="avatarUrl">
            {{
              userInfo?.name?.charAt(0)?.toUpperCase() ||
              userInfo?.username?.charAt(0)?.toUpperCase() ||
              "U"
            }}
          </a-avatar>
          <span class="user-name">{{
            userInfo?.name || userInfo?.username || "用户"
          }}</span>
          <DownOutlined class="dropdown-icon" />
        </div>
        <template #overlay>
          <a-menu>
            <a-menu-item key="logout" @click="handleLogout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/store/user";
import { useAppStore } from "@/store/app";
import { Modal } from "ant-design-vue";
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  DownOutlined,
  LogoutOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
} from "@ant-design/icons-vue";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const appStore = useAppStore();

const userInfo = computed(() => userStore.userInfo);
const isFullscreen = ref(false);

const toggleSidebar = () => {
  appStore.toggleSidebar();
};
const avatarUrl = computed(() => {
  return userInfo.value?.avatar;
});

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
    isFullscreen.value = true;
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
      isFullscreen.value = false;
    }
  }
};

const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement;
};

document.addEventListener("fullscreenchange", fullscreenChangeHandler);

onUnmounted(() => {
  document.removeEventListener("fullscreenchange", fullscreenChangeHandler);
});

const handleLogout = () => {
  Modal.confirm({
    title: "提示",
    content: "确定要退出登录吗?",
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      await userStore.logout();
      router.push("/login");
    },
  });
};
</script>

<style lang="scss" scoped>
.navbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  z-index: 10;

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      color: #475569;
      height: 32px;
      width: 32px;
      transition: all 0.2s ease;

      &:hover {
        background: #f1f5f9;
        color: #3b82f6;
      }
    }

    :deep(.ant-breadcrumb) {
      a {
        color: #64748b;
        transition: color 0.2s ease;

        &:hover {
          color: #3b82f6;
        }
      }

      .ant-breadcrumb-separator {
        color: #cbd5e1;
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 8px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #64748b;
      border-radius: 4px;
      transition: all 0.2s ease;
      height: 32px;
      width: 32px;

      &:hover {
        background: #f1f5f9;
        color: #3b82f6;
      }
    }

    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 12px;
      height: 40px;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.2s ease;

      &:hover {
        background: #f1f5f9;
      }

      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #0f172a;
        font-weight: 500;
      }

      .dropdown-icon {
        color: #94a3b8;
        font-size: 12px;
      }
    }
  }
}
</style>
