<template>
  <div class="backend-layout">
    <!-- 侧边栏 -->
    <Sidebar />

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <Navbar @logout="handleLogout" />

      <!-- 页面内容 -->
      <div class="content-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useUserStore } from "@/store/user";
import { useRouter } from "vue-router";
import Sidebar from "@/components/backend/Sidebar.vue";
import Navbar from "@/components/backend/Navbar.vue";
const userStore = useUserStore();
const router = useRouter();

const isAdmin = computed(() => userStore.role === "admin");

const handleLogout = () => {
  userStore.clearUserInfo();
  router.push("/login");
};
</script>

<style lang="scss" scoped>
.backend-layout {
  display: flex;
  height: 100vh;
  min-height: 100vh;
  background: #f1f5f9;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.content-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  position: relative;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 3px;

    &:hover {
      background: #94a3b8;
    }
  }

  &::-webkit-scrollbar-track {
    background: #f1f5f9;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(8px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}
</style>
