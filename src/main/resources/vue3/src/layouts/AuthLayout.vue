<template>
  <div class="auth-layout">
    <!-- 背景遮罩 -->
    <div class="auth-overlay"></div>

    <div class="auth-container">
      <div class="auth-header">
        <div class="logo-wrapper">
          <h1 class="site-title">{{ siteName }}</h1>
        </div>
        <p class="site-description">{{ siteDescription }}</p>
      </div>

      <div class="auth-content">
        <router-view />
      </div>

      <div class="auth-footer">
        <p class="copyright">{{ siteConfig.copyright.text }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import siteConfig from "@/config/site";

const siteName = siteConfig.name;
const siteDescription = siteConfig.description;
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url("@/assets/1120.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  font-family: "Inter", "Noto Sans SC", sans-serif;
  position: relative;
  overflow: hidden;
}

/* 背景遮罩 - 浅色 */
.auth-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.4);
  z-index: 0;
}

/* 表单容器 */
.auth-container {
  width: 480px;
  max-width: 90%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-header {
  margin-bottom: 2rem;
  text-align: center;
}

.logo-wrapper {
  margin-bottom: 0.75rem;
}

.site-title {
  margin: 0;
  font-family: "Inter", "Noto Sans SC", sans-serif;
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.site-description {
  margin: 0;
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.5;
}

.auth-content {
  flex: 1;
}

.auth-footer {
  padding-top: 1.5rem;
  margin-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.copyright {
  margin: 0;
  color: #94a3b8;
  font-size: 0.875rem;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .auth-container {
    padding: 32px 24px;
  }

  .site-title {
    font-size: 1.75rem;
  }
}
</style>
