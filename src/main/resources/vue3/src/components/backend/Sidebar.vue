<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <img :src="siteConfig.admin.logo.icon" alt="Logo" class="logo-icon" />
      <span class="logo-text" v-show="!isCollapsed">{{
        siteConfig.admin.logo.text
      }}</span>
    </div>
    <div class="menu-wrapper">
      <a-menu
        v-model:selectedKeys="selectedKeys"
        :inline-collapsed="isCollapsed"
        mode="inline"
        class="sidebar-menu"
      >
        <a-menu-item
          key="/back/dashboard"
          @click="handleMenuClick('/back/dashboard')"
        >
          <template #icon>
            <i class="fas fa-house"></i>
          </template>
          <span>首页</span>
        </a-menu-item>

        <a-menu-item key="/back/user" @click="handleMenuClick('/back/user')">
          <template #icon>
            <i class="fas fa-user"></i>
          </template>
          <span>用户管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/heritage"
          @click="handleMenuClick('/back/heritage')"
        >
          <template #icon>
            <i class="fas fa-landmark"></i>
          </template>
          <span>非遗作品管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/inheritor"
          @click="handleMenuClick('/back/inheritor')"
        >
          <template #icon>
            <i class="fas fa-user-tie"></i>
          </template>
          <span>传承人管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/activity"
          @click="handleMenuClick('/back/activity')"
        >
          <template #icon>
            <i class="fas fa-calendar-alt"></i>
          </template>
          <span>活动管理</span>
        </a-menu-item>

        <a-menu-item
          key="/back/course"
          @click="handleMenuClick('/back/course')"
        >
          <template #icon>
            <i class="fas fa-graduation-cap"></i>
          </template>
          <span>课程管理</span>
        </a-menu-item>

        <a-sub-menu key="shop">
          <template #icon>
            <i class="fas fa-store"></i>
          </template>
          <template #title>商城管理</template>

          <a-menu-item
            key="/back/shop/category"
            @click="handleMenuClick('/back/shop/category')"
          >
            <template #icon>
              <i class="fas fa-tags"></i>
            </template>
            <span>商品分类</span>
          </a-menu-item>

          <a-menu-item
            key="/back/shop/product"
            @click="handleMenuClick('/back/shop/product')"
          >
            <template #icon>
              <i class="fas fa-box"></i>
            </template>
            <span>商品管理</span>
          </a-menu-item>

          <a-menu-item
            key="/back/shop/orders"
            @click="handleMenuClick('/back/shop/orders')"
          >
            <template #icon>
              <i class="fas fa-shopping-cart"></i>
            </template>
            <span>订单管理</span>
          </a-menu-item>
        </a-sub-menu>

        <a-menu-item
          key="/back/profile"
          @click="handleMenuClick('/back/profile')"
        >
          <template #icon>
            <i class="fas fa-user"></i>
          </template>
          <span>个人信息</span>
        </a-menu-item>
      </a-menu>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAppStore } from "@/store/app";
import siteConfig from "@/config/site";

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();

const isCollapsed = computed(() => appStore.sidebarCollapsed);
const selectedKeys = ref([route.path]);

watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath];
  },
);

const handleMenuClick = (path) => {
  router.push(path);
};
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  min-height: 100vh;
  background: white;
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.2s ease;
  border-right: 1px solid #e2e8f0;

  &.is-collapsed {
    width: 64px;

    .logo {
      padding: 0;
      justify-content: center;

      .logo-icon {
        margin: 0;
      }
    }

    :deep(.ant-menu) {
      .ant-menu-submenu-title > span:not(.anticon),
      .ant-menu-item > span:not(.anticon) {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }

  .logo {
    height: 64px;
    flex-shrink: 0;
    border-bottom: 1px solid #e2e8f0;
    display: flex;
    align-items: center;
    padding: 0 20px;
    transition: all 0.2s ease;

    .logo-icon {
      width: 28px;
      height: 28px;
      margin-right: 12px;
      object-fit: contain;
      transition: margin 0.2s ease;
    }

    .logo-text {
      color: #0f172a;
      font-size: 16px;
      font-weight: 700;
      white-space: nowrap;
      letter-spacing: -0.02em;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 12px 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #cbd5e1;
      border-radius: 2px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    width: 100% !important;
    background: transparent;

    .ant-menu-item {
      margin: 2px 8px;
      border-radius: 6px;
      height: 40px;
      line-height: 40px;

      &:hover {
        background: #f1f5f9;
      }

      &.ant-menu-item-selected {
        background: #eff6ff;
        color: #3b82f6;

        &::after {
          display: none;
        }

        a {
          color: #3b82f6;
        }

        i {
          color: #3b82f6;
        }
      }
    }

    i {
      color: #64748b;
      transition: color 0.2s ease;
    }

    span {
      color: #0f172a;
      font-weight: 500;
    }
  }
}
</style>
