/**
 * 站点配置文件
 * 统一管理站点信息、描述、版权等
 */

export const siteConfig = {
  // 站点基本信息
  name: "非遗传承",
  shortName: "非遗",
  description: "传承千年文化，守护匠心技艺",
  slogan: "让非遗之美代代相传",

  // Logo配置
  logo: {
    icon: "/src/assets/logo.svg", // Logo图片路径
    text: "非遗传承",
  },

  // 后台管理系统配置
  admin: {
    name: "非遗传承管理系统",
    shortName: "管理后台",
    logo: {
      icon: "/src/assets/logo.svg", // Logo图片路径
      text: "非遗传承管理系统",
    },
  },

  // 版权信息
  copyright: {
    year: "2025",
    owner: "半夜撕代码",
    icp: "", // ICP备案号
    text: "",
  },

  // 联系方式
  contact: {
    email: "support@intangible-heritage.com",
    phone: "400-888-6688",
    address: "北京市东城区非遗文化街1号",
  },

  // 社交媒体
  social: {
    wechat: "",
    weibo: "",
    qq: "",
  },

  // 页脚链接
  footerLinks: [
    { text: "关于我们", url: "/about" },
    { text: "隐私政策", url: "/privacy" },
    { text: "用户协议", url: "/terms" },
    { text: "联系我们", url: "/contact" },
  ],

  // SEO配置
  seo: {
    keywords: "非遗,非物质文化遗产,传统文化,匠心传承,技艺保护",
    author: "非遗传承团队",
  },

  // UI主题配置（现代科技风格）
  theme: {
    colors: {
      primary: "#667eea", // 科技紫
      secondary: "#764ba2", // 梦幻紫
      accent: "#06B6D4", // 科技蓝
      background: "#F8FAFC", // 云雾白
      highlight: "#10B981", // 清新绿
      text: {
        primary: "#1E293B",
        secondary: "#64748B",
        light: "#94A3B8",
      },
    },
    fonts: {
      title: '"Inter", "Noto Sans SC", sans-serif',
      body: '"Noto Sans SC", "Inter", sans-serif',
    },
  },
};

/**
 * 获取完整的版权信息
 */
export function getCopyright() {
  return `© ${siteConfig.copyright.year} ${siteConfig.copyright.owner}. All rights reserved.`;
}

/**
 * 获取站点标题（用于页面title）
 */
export function getSiteTitle(pageTitle = "") {
  return pageTitle ? `${pageTitle} - ${siteConfig.name}` : siteConfig.name;
}

/**
 * 获取后台标题
 */
export function getAdminTitle(pageTitle = "") {
  return pageTitle
    ? `${pageTitle} - ${siteConfig.admin.name}`
    : siteConfig.admin.name;
}

export default siteConfig;
