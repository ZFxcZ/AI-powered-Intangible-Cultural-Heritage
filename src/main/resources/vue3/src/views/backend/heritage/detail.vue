<template>
  <a-modal
    v-model:open="visible"
    title="作品详情"
    width="1000px"
    @cancel="handleCancel"
  >
    <template #title>
      <div class="modal-header">
        <span>作品详情</span>
        <a-space v-if="currentItem">
          <a-button
            v-if="canEdit"
            type="primary"
            size="small"
            @click="handleEdit"
          >
            <i class="fas fa-edit" />
            编辑
          </a-button>
          <a-button
            v-if="canPublish"
            type="primary"
            size="small"
            @click="handlePublish"
          >
            <i class="fas fa-upload" />
            发布
          </a-button>
          <a-button
            v-if="canOffline"
            type="default"
            size="small"
            @click="handleOffline"
          >
            <i class="fas fa-download" />
            下架
          </a-button>
          <a-button
            v-if="canDelete"
            danger
            size="small"
            @click="handleDelete"
          >
            <i class="fas fa-trash" />
            删除
          </a-button>
        </a-space>
      </div>
    </template> 

    <template #footer>
      <a-button @click="handleCancel">
        关闭
      </a-button>
    </template>

    <!-- 加载状态 -->
    <div
      v-if="itemLoading"
      class="loading-container"
    >
      <a-skeleton
        :rows="8"
        active
      />
    </div>

    <!-- 作品内容 -->
    <div
      v-else-if="currentItem"
      class="content-conta
    iner"
    >
      <!-- 基本信息卡片
        -->
       
     
      <a-card
        class="info-card"
        title="基本信息"
      >
        <template #extra>
          <HeritageStatusTag
        
            :status="currentItem
              . status"
    
            :status-name="currentItem.statusName"
            s
            h
            ow-icon
       
      
            size="large"
          />
        </template>

        <div class="info-content">
          <a-row :gutter="24">
            <a-col :span="12">
              <div class="info-item">
                <label>作品标题：</label>
                <span>{{ currentItem.title }}</span>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="info-item">
                <label>类别：</label>
                <a-tag color="blue">
                  {{ currentItem.category }}
                </a-tag>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="info-item">
                <label>地区：</label>
                  
                
                <span>{{ currentItem.region || "-" }}</span>
              </div>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="8">
              <div class="info-item">
                <label>创建人：</label>
                <span>{{ currentItem.creatorName || "-" }}</span>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ formatDate(currentItem.createTime) }}</span>
              </div>
            </a-col>
            <a-col :span="8">
              <div class="info-item">
                <label>发布时间：</label>
                <span>{{
                  currentItem.publishTime
                    ? formatDate(currentItem.publishTime)
                    : "-"
                }}</span>
              </div>
            </a-col>
          </a-row>

          <div class="info-item full-width">
            <label>摘要：</label>
            <div class="summary-content">
              {{ currentItem.summary || "暂无摘要" }}
            </div>
          </div>

          <div class="info-item full-width">
            <label>详细描述：</label>
            <div class="description-content">
              <div
                v-if="currentItem.description"
                v-html="formatDescription(currentItem.description)"
              />
              <div
                v-else
                class="no-content"
              >
                暂无详细描述
              </div>
            </div>
          </div>
        </div>
      </a-card>

       
       
      
      <!-- 媒体文件卡片 -->
      <a-card
        class="media-card"
        :title="`媒体文件 (${mediaList.length})`"
      >
        <template #extra>
          <a-button
            v-if="canEdit"
            type="primary"
            size="small"
            @click="showMediaManager(true)">
            <i class="fas fa-plus" />
            管理媒体
          </a-button>
          </template>
      </a-card>
         
        

          <div
            v-if="mediaList.length > 0"
            class="media-grid"
          >
            <div
              v-for="media in mediaList"
              :key="media.id"
              class="media-item"
              @click="handlePreviewMedia(media)"
            >
              <!-- 图
片               预览 -->
               
             
              <div
                v-if="media.type === 'IMG'"
                class="media-preview"
              >
                <img
                  :src="media.filePath"
                  :alt="media.originalName"
                >
              </div>

             
             
            
              <!-- 视频预览 --> 
              <div
                v-else-if="media.type === 'VIDEO'"
                class="media-preview video"
              >
                <i class="fas fa-play-circle" />
                <span>{{ media.originalName }}</span>
              </div>

             
             
            
              <!-- 音频预览 --> 
              <div
                v-else-if="media.type === 'AUDIO'"
                class="media-preview audio"
              >
                <i class="fas fa-music" />
                <span>{{ media.originalName }}</span>
              </div>

             
             
            
              <!-- PDF预览 --> 
              <div
                v-else-if="media.type === 'PDF'"
                class="media-preview pdf"
              >
                <i class="fas fa-file-pdf" />
                <span>{{ media.originalName }}</span>
              </div>

             
             
            
              <!-- 其他文件 --> 
              <div
                v-else
                class="media-preview file"
              >
                <i class="fas fa-file" />
                <span>{{ media.originalName }}</span>
              </div>

               
               
              
              <div class="media-info">
                <div
                  class="media-name"
                  :title="media.originalName"
                >
                  {{ media.originalName }}
                </div>
                <div class="media-meta">
                  <span class="media-type">{{
                    getMediaTypeName(media.type)
                  }}</span>
                  <span class="media-size">{{
                    formatFileSize(media.fileSize)
            
                  }}
                  </span>
                </div> 
              </div>
            </div>
          </div>

          <div
            v-else
            class="no-media"
          >
            <i 
              class="fas fa-image"
            />
       
       
            <p>暂无媒体文件</p>
          </div>
<!--        </template>-->
<!--      </a-card>-->

      <!-- 传承人信息卡片 -->
      <a-card
        v-if="currentItem.inheritorList && currentItem.inheritorList.length > 0"
        class="inheritor-card"
        :title="`关联传承人 (${currentItem.inheritorList.length})`"
      >
        <div class="inheritor-list">
          <div
            v-for="inheritor in currentItem.inheritorList"
            :key="inheritor.id"
                
              
            class="inheritor-item"
          >
            <div class="inheritor-avatar">
              <img
                v-if="inheritor.avatarPath"
                :src="inheritor.avatarPath"
                :alt="inheritor.name"
              >
        
              <i v-else class="fa
              s fa-user"></i>
            </div>
            <div class="inheritor-info">
              <div class="inheritor-name">
                {{ inheritor.name }}
              </div>
              <div class="inheritor-title">
                {{ inheritor.title || "-" }}
              </div>
              <div class="inheritor-region">
                {{ inheritor.region || "-" }}
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 未找到内容 -->
    <div
      v-else
      class="not-found"
    >
      <a-empty description="未找到该作品信息" />
    </div>

    <!-- 媒体管理对话框 -->
    <a-modal
      v-model:open="showMediaManager"
      title="媒体文件管理"
      width="80%"
            
          
      :mask-closabl
      e="false"
    >
      <HeritageMediaUpload
        v-model="mediaList"
        media-type="ALL"
        :readonly="!canEdit"
        @upload-success="handleMediaUploadSuccess"
        @remove="handleMediaRemove"
      />

      <template #footer>
        <a-space>
          <a-button @click="showMediaManager = false">
            关闭
          </a-button>
          <a-button
            v-if="canEdit"
            type="primary"
            @click="handleSaveMedia"
          >
            保存更改
          </a-button>
        </a-space>
      </template>
    </a-modal>

    <!-- 媒体预览对话框 -->
    <a-mdal
      v-model:open="previewVisible"
      :title="previewMedia?.originalName"
      width="80%"
      centered
      :footer="null"
    >
      <div class="preview-content">
        <!-- 图片预览 -->
        <img
          v-if="previewMedia?.type === 'IMG'"
          :src="previewMedia.filePath"
          class="preview-image"
        >

        <!-- 视频预览 -->
        <video
          v-else-if="previewMedia?.type === 'VIDEO'"
          :src="previewMedia.filePath"
          controls
          class="preview-video"
        >
          您的浏览器不支持视频播放
        </video>

        <!-- 音频预览 -->
        <audio
          v-if="previewMedia?.type === 'AUDIO'"
          :src="previewMedia.filePath"
          controls
          class="preview-audio"
        >
          您的浏览器不支持音频播放
        </audio>

        <!-- PDF预览 -->
        <iframe
          v-else-if="previewMedia?.type === 'PDF'"
          :src="previewMedia.filePath"
          class="preview-pdf"
        />
      </div>
    </a-mdal>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { message, Modal } from "ant-design-vue";
import {
  getHeritageItemDetail,
  publishHeritageItem,
  offlineHeritageItem,
  deleteHeritageItem,
} from "@/api/HeritageApi";
import HeritageStatusTag from "@/components/common/HeritageStatusTag.vue";
import HeritageMediaUpload from "@/components/common/HeritageMediaUpload.vue";
import { formatLocalDate } from "@/utils/dateUtils";
import { useUserStore } from "@/store/user";

const userStore = useUserStore();

// ========== 组件属性 ==========
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  itemId: {
    type: [String, Number],
    default: null,
  },
});

const emit = defineEmits(["update:open", "edit", "success"]);

// ========== 响应式数据 ==========
const visible = ref(false);
const itemLoading = ref(false);
const currentItem = ref(null);
const showMediaManager = ref(false);
const previewVisible = ref(false);
const previewMedia = ref(null);
const mediaList = ref([]);

// ========== 监听器 ==========
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal;
    if (newVal && props.itemId) {
      fetchItemDetail();
    }
  },
);

watch(visible, (newVal) => {
  emit("update:open", newVal);
});

// ========== 计算属性 ==========
// 判断当前用户是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.userType === "ADMIN";
});

// 管理员可以编辑任何状态的作品，普通用户只能编辑草稿和待审
const canEdit = computed(() => {
  if (!currentItem.value) return false;
  if (isAdmin.value) return true;
  return currentItem.value.status === 0 || currentItem.value.status === 1;
});

// 管理员可以发布任何非发布状态的作品，普通用户只能发布草稿和待审
const canPublish = computed(() => {
  if (!currentItem.value) return false;
  if (isAdmin.value) {
    return currentItem.value.status !== 2; // 管理员可以发布除了已发布外的任何状态
  }
  return currentItem.value.status === 0 || currentItem.value.status === 1;
});

// 管理员可以下架已发布的作品
const canOffline = computed(() => {
  if (!currentItem.value) return false;
  if (isAdmin.value) {
    return currentItem.value.status === 2;
  }
  return false; // 普通用户不能下架
});

// 管理员可以删除草稿和下架状态的作品
const canDelete = computed(() => {
  if (!currentItem.value) return false;
  if (isAdmin.value) {
    return currentItem.value.status === 0 || currentItem.value.status === 3;
  }
  return currentItem.value.status === 0; // 普通用户只能删除自己的草稿
});

// ========== 方法 ==========

/**
 * 格式化日期
 */
function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    return formatLocalDate(new Date(dateStr));
  } catch (error) {
    return "";
  }
}

/**
 * 格式化描述内容
 */
function formatDescription(description) {
  if (!description) return "";
  // 简单的换行处理
  return description.replace(/\n/g, "<br>");
}

/**
 * 获取媒体类型名称
 */
function getMediaTypeName(type) {
  const typeMap = {
    IMG: "图片",
    VIDEO: "视频",
    AUDIO: "音频",
    PDF: "PDF",
    FILE: "文件",
  };
  return typeMap[type] || "文件";
}

/**
 * 格式化文件大小
 */
function formatFileSize(bytes) {
  if (!bytes) return "0 B";

  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

/**
 * 获取作品详情
 */
function fetchItemDetail() {
  if (!props.itemId) {
    message.error("作品ID不能为空");
    handleCancel();
    return;
  }

  itemLoading.value = true;

  getHeritageItemDetail(
    { itemId: props.itemId },
    {
      onSuccess: (res) => {
        currentItem.value = res;
        itemLoading.value = false;

        // 直接从作品详情中获取媒体文件列表
        if (res.mediaList && res.mediaList.length > 0) {
          mediaList.value = res.mediaList;
          console.log(`成功加载 ${res.mediaList.length} 个媒体文件`);
        } else {
          mediaList.value = [];
        }
      },
      onError: (error) => {
        message.error("获取作品详情失败");
        itemLoading.value = false;
      },
    },
  );
}

// fetchMediaList 函数已移除，直接从作品详情中获取媒体文件

/**
 * 取消操作
 */
function handleCancel() {
  visible.value = false;
  currentItem.value = null;
  mediaList.value = [];
}

/**
 * 编辑
 */
function handleEdit() {
  emit("edit", currentItem.value);
}

/**
 * 发布
 */
function handlePublish() {
  Modal.confirm({
    title: "提示",
    content: `确定要发布作品"${currentItem.value.title}"吗？`,
    onOk() {
      publishHeritageItem(
        { itemId: currentItem.value.id },
        {
          successMsg: "发布成功",
          onSuccess: () => {
            // 更新当前项状态
            currentItem.value.status = 2;
            currentItem.value.statusName = "已发布";
            emit("success");
          },
        },
      );
    },
  });
}

/**
 * 下架
 */
function handleOffline() {
  Modal.confirm({
    title: "提示",
    content: `确定要下架作品"${currentItem.value.title}"吗？`,
    onOk() {
      offlineHeritageItem(
        { itemId: currentItem.value.id },
        {
          successMsg: "下架成功",
          onSuccess: () => {
            // 更新当前项状态
            currentItem.value.status = 3;
            currentItem.value.statusName = "下架";
            emit("success");
          },
        },
      );
    },
  });
}

/**
 * 删除
 */
function handleDelete() {
  Modal.confirm({
    title: "危险操作",
    content: `确定要删除作品"${currentItem.value.title}"吗？此操作不可恢复！`,
    okText: "确定删除",
    cancelText: "取消",
    okType: "danger",
    onOk() {
      deleteHeritageItem(
        { itemId: currentItem.value.id },
        {
          successMsg: "删除成功",
          onSuccess: () => {
            visible.value = false;
            emit("success");
          },
        },
      );
    },
  });
}

/**
 * 预览媒体
 */
function handlePreviewMedia(media) {
  previewMedia.value = media;
  previewVisible.value = true;
}

/**
 * 媒体上传成功
 */
function handleMediaUploadSuccess(media) {
  message.success("媒体上传成功");
}

/**
 * 媒体移除
 */
function handleMediaRemove(media) {
  message.success("媒体移除成功");
}

/**
 * 保存媒体更改
 */
function handleSaveMedia() {
  // 这里可以调用API保存媒体关联关系
  showMediaManager.value = false;
  message.success("媒体更改已保存");
}
</script>

<style scoped>
/* 对话框头部样式 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 对话框内容样式 */
:deep(.ant-modal-body) {
  max-height: 70vh;
  overflow-y: auto;
  padding: 16px;
}

.loading-container {
  padding: 20px;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
.media-card,
.inheritor-card {
  margin-bottom: 16px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.info-item.full-width {
  flex-direction: column;
  align-items: stretch;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  flex-shrink: 0;
}

.summary-content,
.description-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
  margin-top: 8px;
}

.no-content {
  color: #c0c4cc;
  font-style: italic;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.media-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.media-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.media-preview {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  flex-direction: column;
  gap: 8px;
}

.media-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-preview.video,
.media-preview.audio,
.media-preview.pdf,
.media-preview.file {
  color: #606266;
}

.media-preview i {
  font-size: 32px;
}

.media-info {
  padding: 12px;
}

.media-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.media-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.no-media {
  text-align: center;
  padding: 40px;
  color: #c0c4cc;
}

.no-media i {
  font-size: 48px;
  margin-bottom: 16px;
}

.inheritor-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.inheritor-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.inheritor-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.inheritor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.inheritor-avatar i {
  font-size: 24px;
  color: #c0c4cc;
}

.inheritor-info {
  flex: 1;
}

.inheritor-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.inheritor-title,
.inheritor-region {
  font-size: 14px;
  color: #606266;
  margin-bottom: 2px;
}

.not-found {
  text-align: center;
  padding: 40px 20px;
}

/* 预览样式 */
.preview-content {
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

.preview-video {
  width: 100%;
  max-height: 60vh;
}

.preview-audio {
  width: 100%;
}

.preview-pdf {
  width: 100%;
  height: 60vh;
  border: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.ant-modal) {
    margin: 0;
    max-width: 100vw;
    top: 0;
  }

  :deep(.ant-modal-content) {
    border-radius: 0;
  }

  .modal-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .media-grid,
  .inheritor-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-item label {
    min-width: auto;
  }
}
</style>
