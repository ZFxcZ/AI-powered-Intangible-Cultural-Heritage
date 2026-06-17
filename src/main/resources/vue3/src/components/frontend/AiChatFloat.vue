<template>
  <div class="ai-chat-float" :style="{ right: btnRight + 'px', bottom: btnBottom + 'px' }">
    <a-button
      v-if="!visible"
      type="primary"
      class="chat-trigger-btn"
      @click="openChat"
      @mousedown="handleMouseDown"
    >
      <template #icon>
        <i class="fas fa-robot" />
      </template>
    </a-button>

    <a-modal
      v-model:open="visible"
      :width="1000"
      :footer="null"
      :closable="true"
      :destroy-on-close="false"
      class="ai-chat-modal"
      @cancel="closeChat"
    >
      <template #title>
        <div class="modal-title">
          <div class="title-icon">
            <i class="fas fa-comments" />
          </div>
          <div class="title-text">
            <h3>非遗智能助手</h3>
            <p>探索非遗知识 · 传承文化瑰宝</p>
          </div>
        </div>
      </template>

      <div class="chat-container">
        <div class="sidebar">
          <div class="sidebar-header">
            <a-button
              type="primary"
              block
              @click="handleNewSession"
            >
              <i class="fas fa-plus" />
              新建会话
            </a-button>
          </div>
          <div class="session-list">
            <div
              v-for="session in sessionList"
              :key="session.sessionId"
              class="session-item"
              :class="{ active: session.sessionId === currentSessionId }"
              @click="switchSession(session)"
            >
              <div class="session-info">
                <i class="fas fa-comment-dots session-icon" />
                <span class="session-title">{{ session.title }}</span>
              </div>
              <div class="session-actions">
                <a-popover
                  placement="right"
                  trigger="click"
                >
                  <template #content>
                    <div class="edit-popover">
                      <a-input
                        v-model:value="editingTitle"
                        placeholder="输入新标题"
                        size="small"
                        @press-enter="confirmEditTitle(session.sessionId)"
                      />
                      <div class="popover-buttons">
                        <a-button
                          size="small"
                          @click="editingSessionId = null"
                        >
                          取消
                        </a-button>
                        <a-button
                          size="small"
                          type="primary"
                          @click="confirmEditTitle(session.sessionId)"
                        >
                          确定
                        </a-button>
                      </div>
                    </div>
                  </template>
                  <a-button
                    type="text"
                    size="small"
                    @click.stop="startEditTitle(session)"
                  >
                    <i class="fas fa-edit" />
                  </a-button>
                </a-popover>
                <a-popconfirm
                  title="确定要删除这个会话吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteSession(session.sessionId)"
                >
                  <a-button
                    type="text"
                    size="small"
                    danger
                  >
                    <i class="fas fa-trash" />
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-area">
          <div
            ref="messagesContainer"
            class="messages-container"
              >
            <div class="message ai-message welcome-message">
              <div class="message-avatar">
                <i class="fas fa-robot" />
              </div>
              <div class="message-content">
                <div class="message-text">
                  <p class="welcome-title">
                    您好，我是非遗智能助手
                  </p>
                  <p>我可以帮您了解：</p>
                  <ul class="welcome-list">
                    <li>• 非遗作品知识（苏绣、蜀绣、皮影戏、风筝、瓷器等）</li>
                    <li>• 传承人信息和代表作品</li>
                    <li>• 平台活动和课程推荐</li>
                    <li>• 非遗手办商城咨询</li>
                  </ul>
                  <p class="welcome-footer">
                    请问有什么可以帮助您的吗？
                  </p>
                </div>
              </div>
            </div>

            <div
              v-for="(message, index) in messages"
              :key="index"
              class="message"
              :class="message.role === 'user' ? 'user-message' : 'ai-message'"
            >
              <div
                v-if="message.role === 'assistant'"
                class="message-avatar"
              >
                <i class="fas fa-robot" />
              </div>
              <div class="message-content">
                <div
                  class="message-text"
                  v-html="formatMessage(message.content)"
                />
                <div class="message-time">
                  {{ formatTime(message.createTime) }}
                </div>
              </div>
              <div
                v-if="message.role === 'user'"
                class="message-avatar user-avatar"
              >
                <i class="fas fa-user" />
              </div>
            </div>

            <div
              v-if="loading"
              class="message ai-message"
            >
              <div class="message-avatar">
                <i class="fas fa-robot" />
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span />
                  <span />
                  <span />
                </div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <a-textarea
              v-model:value="inputMessage"
              placeholder="请输入您的问题..."
              :auto-size="{ minRows: 2, maxRows: 4 }"
              :maxlength="500"
              @press-enter="handleSend"
            />
            <a-button
              type="primary"
              :loading="loading"
              :disabled="!inputMessage.trim()"
              @click="handleSend"
            >
              <i class="fas fa-paper-plane" />
              发送
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import { message } from "ant-design-vue";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useUserStore } from "@/store/user";
import { useAppStore } from "@/store/app";
import { marked } from "marked";
import {
  createSession as createSessionApi,
  getSessionMessages,
  getChatStreamUrl,
  getSessionList as getSessionListApi,
  updateSessionTitle as updateSessionTitleApi,
  deleteSession as deleteSessionApi,
} from "@/api/AiChatApi";
import { useRouter } from "vue-router";

marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false,
  pedantic: false,
});

const userStore = useUserStore();
const appStore = useAppStore();
const router = useRouter();
const visible = ref(false);
const inputMessage = ref("");
const messages = ref([]);
const loading = ref(false);
const messagesContainer = ref(null);
const currentSessionId = ref(null);
const sessionList = ref([]);
const editingSessionId = ref(null);
const editingTitle = ref("");

// 拖动相关变量
const isDragging = ref(false);
const startX = ref(0);
const startY = ref(0);
const btnRight = ref(24); // 初始右侧距离
const btnBottom = ref(24); // 初始底部距离

const loadSessionList = async () => {
  try {
    console.log("开始加载会话列表...");
    const res = await getSessionListApi();
    console.log(
      "会话列表响应:",
      res,
      "类型:",
      Array.isArray(res) ? "Array" : typeof res,
    );

    // request.js 拦截器已经返回了 data.data，所以 res 直接就是会话数组
    if (res && Array.isArray(res)) {
      sessionList.value = res;
      console.log("会话列表加载成功，数量:", res.length);
    } else if (res && res.code === 200 && Array.isArray(res.data)) {
      // 兼容旧格式
      sessionList.value = res.data;
      console.log("会话列表加载成功 (旧格式)，数量:", res.data.length);
    } else {
      console.warn("会话列表响应数据异常:", res);
      sessionList.value = [];
    }
  } catch (error) {
    console.error("加载会话列表失败:", error);
    message.error("加载会话列表失败");
    sessionList.value = [];
  }
};

const createSession = async (title = "新对话") => {
  try {
    console.log("开始创建会话，title:", title);
    const res = await createSessionApi(title);
    console.log("创建会话响应:", res, "类型:", typeof res);

    // request.js 拦截器已经返回了 data.data，所以 res 直接就是 sessionId 字符串
    if (res && typeof res === "string") {
      currentSessionId.value = res;
      appStore.setAiChatSessionId(res);
      messages.value = [];
      await loadSessionList();
      console.log("会话创建成功:", res);
      return true;
    } else if (res && res.code === 200 && res.data) {
      // 兼容旧格式
      currentSessionId.value = res.data;
      appStore.setAiChatSessionId(res.data);
      messages.value = [];
      await loadSessionList();
      console.log("会话创建成功 (旧格式):", res.data);
      return true;
    } else {
      console.error("创建会话失败，响应数据:", res);
      message.error("创建会话失败：响应数据格式异常");
    }
    return false;
  } catch (error) {
    console.error("创建会话异常:", error);
    message.error("创建会话异常：" + (error.message || "未知错误"));
    return false;
  }
};

const loadMessages = async (sessionId) => {
  if (!sessionId) return;

  try {
    console.log("加载会话消息:", sessionId);
    const res = await getSessionMessages(sessionId);
    console.log(
      "加载消息响应:",
      res,
      "类型:",
      Array.isArray(res) ? "Array" : typeof res,
    );

    // request.js 拦截器已经返回了 data.data，所以 res 直接就是消息数组
    if (res && Array.isArray(res)) {
      messages.value = res;
      console.log("加载消息成功，数量:", res.length);
    } else if (res && res.code === 200 && Array.isArray(res.data)) {
      // 兼容旧格式
      messages.value = res.data || [];
      console.log("加载消息成功 (旧格式)，数量:", res.data.length);
    } else {
      console.warn("未加载到消息数据:", res);
      messages.value = [];
    }
    scrollToBottom();
  } catch (error) {
    console.error("加载消息失败:", error);
    message.error("加载历史消息失败");
    messages.value = [];
  }
};

const handleNewSession = async () => {
  const success = await createSession("新对话");
  if (success) {
    message.success("已创建新会话");
  }
};

const switchSession = async (session) => {
  if (session.sessionId === currentSessionId.value) return;

  console.log("切换会话:", {
    from: currentSessionId.value,
    to: session.sessionId,
    sessionTitle: session.title,
  });

  currentSessionId.value = session.sessionId;
  appStore.setAiChatSessionId(session.sessionId);
  await loadMessages(session.sessionId);
};

const startEditTitle = (session) => {
  editingSessionId.value = session.sessionId;
  editingTitle.value = session.title;
};

const confirmEditTitle = async (sessionId) => {
  if (!editingTitle.value.trim()) {
    message.error("标题不能为空");
    return;
  }

  try {
    await updateSessionTitleApi(sessionId, editingTitle.value);
    message.success("标题已更新");
    editingSessionId.value = null;
    editingTitle.value = "";
    await loadSessionList();
  } catch (error) {
    message.error("更新标题失败");
  }
};

// 拖动相关函数
const handleMouseDown = (e) => {
  isDragging.value = true;
  startX.value = e.clientX;
  startY.value = e.clientY;
  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
};

const handleMouseMove = (e) => {
  if (!isDragging.value) return;
  
  const deltaX = e.clientX - startX.value;
  const deltaY = e.clientY - startY.value;
  
  // 计算新的位置（从右侧和底部计算）
  const newRight = btnRight.value - deltaX;
  const newBottom = btnBottom.value - deltaY;
  
  // 限制在窗口内
  const windowWidth = window.innerWidth;
  const windowHeight = window.innerHeight;
  const btnWidth = 56; // 按钮宽度
  const btnHeight = 56; // 按钮高度
  
  // 确保按钮可以移动到屏幕的任何位置，包括边缘
  if (newRight >= 0 && newRight <= windowWidth - btnWidth) {
    btnRight.value = newRight;
  }
  
  if (newBottom >= 0 && newBottom <= windowHeight - btnHeight) {
    btnBottom.value = newBottom;
  }
  
  startX.value = e.clientX;
  startY.value = e.clientY;
};

const handleMouseUp = () => {
  isDragging.value = false;
  document.removeEventListener('mousemove', handleMouseMove);
  document.removeEventListener('mouseup', handleMouseUp);
};

const handleDeleteSession = async (sessionId) => {
  try {
    console.log("删除会话:", sessionId);
    await deleteSessionApi(sessionId);
    message.success("会话已删除");

    await loadSessionList();
    console.log("当前会话列表:", sessionList.value);

    if (sessionId === currentSessionId.value) {
      if (sessionList.value.length > 0) {
        console.log("切换到第一个会话:", sessionList.value[0].sessionId);
        await switchSession(sessionList.value[0]);
      } else {
        console.log("会话列表为空，创建新会话");
        await createSession("新对话");
      }
    }
  } catch (error) {
    console.error("删除会话失败:", error);
    message.error("删除会话失败：" + (error.message || "未知错误"));
  }
};

const handleSend = async () => {
  if (!inputMessage.value.trim() || loading.value) return;

  const userMessage = inputMessage.value.trim();
  inputMessage.value = "";

  console.log("发送消息:", {
    sessionId: currentSessionId.value,
    message: userMessage,
    hasToken: !!userStore.token,
  });

  messages.value.push({
    role: "user",
    content: userMessage,
    createTime: new Date().toISOString(),
  });

  scrollToBottom();
  loading.value = true;

  try {
    let aiResponse = "";

    const token = userStore.token;
    if (!token) {
      message.error("请先登录");
      loading.value = false;
      return;
    }

    if (!currentSessionId.value) {
      console.error("sessionId 为空，尝试创建新会话");
      const success = await createSession("新对话");
      if (!success || !currentSessionId.value) {
        message.error("会话初始化失败，请刷新页面");
        loading.value = false;
        return;
      }
    }

    const apiUrl = getChatStreamUrl();
    const abortController = new AbortController();

    console.log("开始 SSE 请求:", {
      url: apiUrl,
      sessionId: currentSessionId.value,
    });

    await fetchEventSource(apiUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        sessionId: currentSessionId.value,
        userMessage: userMessage,
      }),
      signal: abortController.signal,
      openWhenHidden: true,
      onmessage(event) {
        if (event.data === "[DONE]") {
          loading.value = false;
          return;
        }
        if (event.data.startsWith("[ERROR]")) {
          message.error("AI 响应失败：" + event.data.substring(7));
          loading.value = false;
          return;
        }

        aiResponse += event.data;

        const lastMessage = messages.value[messages.value.length - 1];
        if (lastMessage && lastMessage.role === "assistant") {
          lastMessage.content = aiResponse;
        } else {
          messages.value.push({
            role: "assistant",
            content: aiResponse,
            createTime: new Date().toISOString(),
          });
        }

        scrollToBottom();
      },
      onerror(error) {
        console.error("SSE 错误:", error);
        message.error("连接 AI 服务失败");
        loading.value = false;
        return 999999999;
      },
      onclose() {
        loading.value = false;
      },
    });
  } catch (error) {
    console.error("发送消息失败:", error);
    message.error("发送消息失败：" + (error.message || "未知错误"));
    loading.value = false;
  }
};

const formatMessage = (content) => {
  if (!content) return "";
  try {
    const normalizedContent = content.endsWith("\n") ? content : content + "\n";
    const html = marked.parse(normalizedContent);
    return html.trim();
  } catch (error) {
    return content
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\n/g, "<br/>");
  }
};
const routeLinkHandler = async (event) => {
  const link = event.target.closest('a');
  if (!link) return;
  const href = link.getAttribute('href') || '';
  if (href.startsWith("action://")) {
    event.preventDefault();
    event.stopPropagation();

    const actionUrl = href.replace('action://', '');
    const [action, queryString] = actionUrl.split('?');
    const params = queryString ? Object.fromEntries(new URLSearchParams(queryString)) : {};

    if (action === 'signup' && params.id) {
      event.preventDefault();
      event.stopPropagation();

      const msgIdx = messages.value.length;
      messages.value.push({
        role: "assistant",
        content: "⏳ **正在提交报名，请稍候...**",
        createTime: new Date().toISOString()
      });

      try {
        const token = userStore.token;
        if (!token) {
          messages.value[msgIdx].content = "⚠️ **请先登录**\n\n报名活动需要先登录账号。";
          return;
        }

        const response = await fetch('/api/activity/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
          },
          body: JSON.stringify({
            activityId: params.id,
            userId: userStore.userId
          })
        });

        const result = await response.json();
        if (result.code === "200") {
          const activityTitle = result.data?.activityTitle || "该活动";
          messages.value[msgIdx].content = "✅ **报名成功！**\n\n您已成功报名 **" + activityTitle + "**。\n\n🔗 [查看活动详情](route://activity?id=" + params.id + ")\n💡 请留意活动通知，按时参加活动哦～";
        } else {
          messages.value[msgIdx].content = "❌ **" + (result.message || "报名失败，请稍后重试。") + "**";
        }
      } catch (error) {
        messages.value[msgIdx].content = "❌ **报名失败**\n\n" + (error.message || "网络错误，请稍后重试。");
      }

      return;
    }
    return;
  }
  if (!href.startsWith("route://")) return;

  event.preventDefault();
  event.stopPropagation();

  const url = href.replace('route://', '');
  const [path, queryString] = url.split('?');
  const params = queryString ? Object.fromEntries(new URLSearchParams(queryString)) : {};

  const routeMap = {
    heritage:  (id) => id ? '/heritage/' + id : '/heritage',
    inheritor: (id) => id ? '/inheritor/' + id : '/inheritor',
    activity:  (id) => id ? '/activity/' + id : '/activity',
    course:    (id) => id ? '/course/' + id : '/course',
    shop:      (id) => id ? '/shop/' + id : '/shop',
    orders:    ()  => '/orders',
  };

  const navigate = routeMap[path];
  if (!navigate) return;

  visible.value = false;
  const target = params.id ? navigate(params.id) : navigate();
  window.location.href = target;
};

onMounted(() => {
  document.addEventListener("click", routeLinkHandler);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", routeLinkHandler);
});
const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${hours}:${minutes}`;
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const openChat = () => {
  visible.value = true;
};

const closeChat = () => {
  visible.value = false;
};

onMounted(async () => {
  try {
    await loadSessionList();

    console.log("会话列表加载完成:", sessionList.value);
    console.log("保存的 SessionID:", appStore.aiChatSessionId);

    const savedSessionId = appStore.aiChatSessionId;

    if (savedSessionId && sessionList.value.length > 0) {
      const isValidSession = sessionList.value.find(
        (s) => s.sessionId === savedSessionId,
      );
      if (isValidSession) {
        currentSessionId.value = savedSessionId;
        console.log("使用保存的会话:", savedSessionId);
        await loadMessages(savedSessionId);
      } else {
        console.log("保存的会话无效，创建新会话");
        const success = await createSession("新对话");
        if (!success) {
          message.error("创建会话失败");
        }
      }
    } else {
      console.log("没有保存的会话或会话列表为空，创建新会话");
      const success = await createSession("新对话");
      if (!success) {
        message.error("创建会话失败");
      }
    }

    console.log("最终 SessionID:", currentSessionId.value);
    console.log("当前消息列表:", messages.value);
  } catch (error) {
    console.error("onMounted 初始化失败:", error);
    message.error("初始化失败，请刷新页面重试");
    // 即使出错也尝试创建一个会话
    await createSession("新对话");
  }
});
</script>

<style scoped>
.ai-chat-float {
  position: fixed;
  z-index: 999;
}

.chat-trigger-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  transition: all 0.2s ease;
  color: white;
}

.chat-trigger-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
  background: linear-gradient(135deg, #a0522d 0%, #8b4513 100%);
}

.ai-chat-modal :deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.ai-chat-modal :deep(.ant-modal-body) {
  padding: 0;
  flex: 1;
  overflow: hidden;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: #f8fafc;
  color: #1e293b;
}

.title-icon {
  width: 48px;
  height: 48px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  color: #475569;
}

.title-text h3 {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 700;
  font-family: "SimSun", "宋体", serif;
  letter-spacing: 2px;
}

.title-text p {
  margin: 0;
  opacity: 0.95;
  font-size: 13px;
  letter-spacing: 1px;
}

.chat-container {
  display: flex;
  height: calc(80vh - 100px);
}

.sidebar {
  width: 280px;
  background: #f5f5f5;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.sidebar-header :deep(.ant-btn-primary) {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.session-item:hover {
  background: #f1f5f9;
  border-color: rgba(148, 163, 184, 0.2);
}

.session-item.active {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border-color: #8b4513;
  color: white;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  overflow: hidden;
}

.session-icon {
  color: #94a3b8;
  font-size: 16px;
}

.session-item.active .session-icon {
  color: white;
}

.session-title {
  flex: 1;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item.active .session-title {
  color: white;
}

.session-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.session-item.active .session-actions {
  opacity: 1;
}

.session-item.active .session-actions a {
  color: white;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.edit-popover {
  width: 200px;
}

.popover-buttons {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f8fafc;
}

.message {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
}

.ai-message {
  justify-content: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(148, 163, 184, 0.3);
}

.user-avatar {
  background: #10b981;
}

.message-content {
  max-width: 70%;
  margin: 0 14px;
}

.user-message .message-content {
  margin: 0 14px 0 0;
}

.message-text {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.12);
  line-height: 1.8;
  word-wrap: break-word;
  white-space: normal;
  overflow-wrap: break-word;
  color: #1e293b;
}

.user-message .message-text {
  background: #3b82f6;
  color: white;
  border: none;
}

.welcome-message .message-text {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.welcome-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
}

.welcome-list li {
  padding: 10px 0;
  color: #555;
  font-size: 15px;
  line-height: 1.8;
}

.welcome-footer {
  margin-top: 16px;
  color: #666;
  font-size: 15px;
  font-weight: 500;
}

.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.4;
  font-family: "SimSun", "宋体", serif;
  letter-spacing: 1px;
  color: #2c2c2c;
}

.message-text :deep(h1) {
  font-size: 1.5em;
}
.message-text :deep(h2) {
  font-size: 1.3em;
}
.message-text :deep(h3) {
  font-size: 1.1em;
  color: #667eea;
}

.message-text :deep(p) {
  margin: 8px 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

.message-text :deep(p:first-child) {
  margin-top: 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.message-text :deep(li) {
  margin: 4px 0;
  line-height: 1.8;
  white-space: normal;
}

.message-text :deep(code) {
  background: rgba(102, 126, 234, 0.08);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: "Courier New", monospace;
  font-size: 0.9em;
  color: #667eea;
}

.message-text :deep(pre) {
  background: #1e293b;
  color: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.message-text :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.message-text :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 16px;
  margin: 16px 0;
  color: #666;
  font-style: italic;
  background: rgba(139, 69, 19, 0.03);
  padding: 12px 16px;
  border-radius: 4px;
}

.message-text :deep(a) {
  color: #667eea;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s;
}

.message-text :deep(a:hover) {
  border-bottom-color: #667eea;
}

.message-text :deep(strong) {
  font-weight: 700;
  color: #2c2c2c;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(hr) {
  border: none;
  border-top: 2px solid #e8e8e8;
  margin: 20px 0;
}

.message-text :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
}

.message-text :deep(th),
.message-text :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 10px;
  text-align: left;
}

.message-text :deep(th) {
  background: rgba(102, 126, 234, 0.06);
  font-weight: 600;
  color: #1e293b;
}

.user-message .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.user-message .message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.2);
}

.user-message .message-text :deep(a) {
  color: #fff;
  text-decoration: underline;
}

.user-message .message-text :deep(strong) {
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  padding: 0 4px;
}

.typing-indicator {
  display: flex;
  gap: 8px;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-12px);
    opacity: 1;
  }
}

.input-area {
  padding: 24px;
  background: #fff;
  border-top: 2px solid #e8e8e8;
  display: flex;
  gap: 16px;
  align-items: flex-end;
}

.input-area :deep(.ant-input) {
  flex: 1;
  border-radius: 8px;
  font-size: 15px;
  border: 1px solid #e8e8e8;
}

.input-area :deep(.ant-input):focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.input-area :deep(.ant-btn-primary) {
  border-radius: 6px;
  height: auto;
  padding: 10px 28px;
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  color: white;
}

.input-area :deep(.ant-btn-primary):hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
  background: linear-gradient(135deg, #a0522d 0%, #8b4513 100%);
}

.input-area :deep(.ant-btn-primary) i {
  margin-right: 6px;
}

.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #764ba2 0%, #667eea 100%);
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(139, 69, 19, 0.3);
  border-radius: 4px;
}

@media (max-width: 768px) {
  .ai-chat-float {
    right: 16px;
    bottom: 16px;
  }

  .chat-trigger-btn {
    width: 52px;
    height: 52px;
    font-size: 20px;
  }

  .chat-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }

  .chat-area {
    height: auto;
    flex: 1;
  }

  .messages-container {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .input-area {
    padding: 16px;
    gap: 12px;
  }

  .input-area :deep(.ant-btn-primary) {
    padding: 8px 20px;
  }
}
</style>