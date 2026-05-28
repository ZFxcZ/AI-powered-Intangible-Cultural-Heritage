<template>
  <div class="frontend-layout">
    <!-- 顶部导航栏 -->
    <Navbar />

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-top">
          <div class="footer-brand">
            <h2>{{ siteConfig.name }}</h2>
            <p>{{ siteConfig.description }}</p>
          </div>
          <div class="footer-links">
            <div class="footer-link-group">
              <h3>快速链接</h3>
              <ul>
                <li><a href="/home">首页</a></li>
                <li><a href="/heritage">非遗作品</a></li>
                <li><a href="/inheritor">传承人</a></li>
                <li><a href="/activity">活动中心</a></li>
              </ul>
            </div>
            <div class="footer-link-group">
              <h3>更多服务</h3>
              <ul>
                <li><a href="/course">在线课程</a></li>
                <li><a href="/shop">手办商城</a></li>
                <li><a href="/profile">个人中心</a></li>
                <li><a href="/ai-chat">AI 聊天</a></li>
              </ul>
            </div>
            <div class="footer-link-group">
              <h3>联系我们</h3>
              <ul>
                <li>{{ siteConfig.contact.email }}</li>
                <li>{{ siteConfig.contact.phone }}</li>
              </ul>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>{{ siteConfig.copyright.text }}</p>
        </div>
      </div>
    </footer>

    <!-- AI 聊天浮动按钮 -->
    <AiChatFloat />
  </div>
</template>

<script setup>
import Navbar from "@/components/frontend/Navbar.vue";
import siteConfig from "@/config/site";
import AiChatFloat from "@/components/frontend/AiChatFloat.vue";
</script>

<style scoped>
.frontend-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-background);
}

.main-content {
  flex: 1;
  margin-top: 72px;
  padding: var(--spacing-xl) 0;
}

.content-wrapper {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 var(--spacing-md);
}

.footer {
  background: white;
  color: var(--color-text-secondary);
  padding: var(--spacing-2xl) 0;
  margin-top: var(--spacing-2xl);
  border-top: var(--border-width) solid #E5E7EB;
}

.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 var(--spacing-md);
}

.footer-top {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-2xl);
  margin-bottom: var(--spacing-xl);
}

.footer-brand {
  flex: 1;
  min-width: 250px;
}

.footer-brand h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  font-family: var(--font-title);
}

.footer-brand p {
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--color-text-secondary);
  max-width: 400px;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xl);
  flex: 2;
  min-width: 300px;
}

.footer-link-group {
  min-width: 150px;
}

.footer-link-group h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  font-family: var(--font-title);
}

.footer-link-group ul {
  list-style: none;
}

.footer-link-group ul li {
  margin-bottom: var(--spacing-sm);
}

.footer-link-group ul li a {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.footer-link-group ul li a:hover {
  color: var(--color-primary);
}

.footer-bottom {
  border-top: var(--border-width) solid #E5E7EB;
  padding-top: var(--spacing-lg);
  text-align: center;
  font-size: 0.875rem;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--spacing-lg) 0;
  }

  .content-wrapper {
    padding: 0 var(--spacing-sm);
  }

  .footer {
    padding: var(--spacing-xl) 0;
    margin-top: var(--spacing-xl);
  }

  .footer-top {
    flex-direction: column;
    gap: var(--spacing-xl);
  }

  .footer-links {
    flex-direction: column;
    gap: var(--spacing-lg);
  }

  .footer-brand {
    min-width: 100%;
  }

  .footer-link-group {
    min-width: 100%;
  }
}
</style>
