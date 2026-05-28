import { defineStore } from 'pinia'

// export const useAppStore = defineStore('app', {
//   state: () => ({
//     sidebarCollapsed: false
//   }),
  
//   actions: {
//     toggleSidebar() {
//       this.sidebarCollapsed = !this.sidebarCollapsed
//     }
//   }
// }) 

// 追加修改AI对话
// 安全的 localStorage 操作工具
const storage = {
  get(key) {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : null
    } catch (error) {
      console.warn(`Failed to parse localStorage item: ${key}`, error)
      localStorage.removeItem(key)
      return null
    }
  },

  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`Failed to set localStorage item: ${key}`, error)
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error(`Failed to remove localStorage item: ${key}`, error)
    }
  }
}

export const useAppStore = defineStore('app', {
  state: () => ({
    // AI 聊天会话 ID，持久化保存
    aiChatSessionId: storage.get('aiChatSessionId') || null
  }),
  
  getters: {
    // 是否有活跃的 AI 聊天会话
    hasActiveChatSession: (state) => !!state.aiChatSessionId
  },

  actions: {
    // 设置 AI 聊天会话 ID
    setAiChatSessionId(sessionId) {
      if (!sessionId) {
        console.warn('setAiChatSessionId: sessionId 不能为空')
        return
      }
      this.aiChatSessionId = sessionId
      storage.set('aiChatSessionId', sessionId)
      console.log('AI 会话 ID 已保存:', sessionId)
    },

    // 清除 AI 聊天会话 ID
    clearAiChatSessionId() {
      this.aiChatSessionId = null
      storage.remove('aiChatSessionId')
      console.log('AI 会话 ID 已清除')
    }
  }
})
