<template>
  <div class="course-detail">
    <a-spin :spinning="loading">
      <div v-if="course" class="detail-container">
        <!-- 课程头部 -->
        <div class="course-header">
          <div class="header-content">
            <h1>{{ course.title }}</h1>
            <div class="course-tags">
              <a-tag :color="getCourseLevelColor(course.level)">
                {{ getCourseLevelName(course.level) }}
              </a-tag>
              <a-tag color="green">{{ course.statusName }}</a-tag>
            </div>
            <div class="course-stats">
              <span><i class="fas fa-book"></i> {{ chapters.length }} 章节</span>
            </div>
          </div>
        </div>

        <!-- 课程内容 -->
        <div class="course-body">
          <a-row :gutter="32">
            <a-col :span="16">
              <!-- 课程简介 -->
              <div class="info-section">
                <h2>
                  <i class="fas fa-info-circle"></i> 课程简介
                </h2>
                <div class="course-description">
                  {{ course.description || '暂无课程简介' }}
                </div>
              </div>

              <!-- 课程章节 -->
              <div class="info-section">
                <h2>
                  <i class="fas fa-list"></i> 课程目录
                </h2>
                <div class="chapter-list">
                  <div
                    v-for="(chapter, index) in chapters"
                    :key="chapter.id"
                    class="chapter-item"
                    @click="playChapter(chapter)"
                  >
                    <div class="chapter-number">{{ index + 1 }}</div>
                    <div class="chapter-info">
                      <h4>{{ chapter.title }}</h4>
                      <p v-if="chapter.content">
                        {{ chapter.content.substring(0, 60) }}...
                      </p>
                    </div>
                    <div class="chapter-icon">
                      <i v-if="chapter.videoFiles && chapter.videoFiles.length > 0" class="fas fa-play-circle"></i>
                      <i v-else class="fas fa-file-alt"></i>
                    </div>
                  </div>
                  <a-empty v-if="chapters.length === 0" description="暂无章节" />
                </div>
              </div>
            </a-col>

            <a-col :span="8">
              <!-- 学习卡片 -->
              <div class="learning-card">
                <div class="card-cover">
                  <img :src="getCourseCover()" :alt="course.title" />
                </div>
                <div class="card-content">
                  <a-button
                    v-if="course.status === 1"
                    type="primary"
                    size="large"
                    block
                    @click="startLearning"
                  >
                    开始学习
                  </a-button>
                  <a-button v-else size="large" block disabled>
                    课程暂未开放
                  </a-button>

                  <div class="learning-tips">
                    <h4>学习须知</h4>
                    <ul>
                      <li>课程章节按顺序学习效果更佳</li>
                      <li>建议准备好笔记本记录重点</li>
                      <li>遇到问题可在线咨询老师</li>
                      <li>完成所有章节可获得证书</li>
                    </ul>
                  </div>
                </div>
              </div>
            </a-col>
          </a-row>
        </div>
      </div>
    </a-spin>

    <!-- 章节详情弹窗 -->
    <a-modal
      v-model:open="isChapterModalVisible"
      :title="currentChapter?.title"
      width="800px"
      :footer="null"
    >
      <div class="chapter-detail">
        <!-- 视频播放区域 -->
        <div v-if="currentChapter?.videoFiles && currentChapter.videoFiles.length > 0" class="chapter-video">
          <video
            v-for="(video, index) in currentChapter.videoFiles"
            :key="video.id"
            :src="getVideoUrl(video.filePath)"
            controls
            preload="metadata"
            class="video-player"
            :class="{ 'mb-16': index < currentChapter.videoFiles.length - 1 }"
          >
            <p>您的浏览器不支持视频播放，请升级浏览器或使用其他浏览器。</p>
          </video>
        </div>
        
        <!-- 无视频时的提示 -->
        <div v-else class="no-video-tip">
          <i class="fas fa-file-text"></i>
          <p>本章节暂无视频内容</p>
        </div>
        
        <!-- 章节文字内容 -->
        <div class="chapter-content">
          <h3>章节内容</h3>
          <div class="content-text">
            {{ currentChapter?.content || '暂无文字内容' }}
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCourseDetail, getCourseChapters } from '@/api/CourseApi'
import { getCourseLevelName, getCourseLevelColor } from '@/config/courseLevel'

const route = useRoute()
const router = useRouter()

const course = ref(null)
const chapters = ref([])
const loading = ref(false)
const isChapterModalVisible = ref(false)
const currentChapter = ref(null)

// 加载课程详情
const loadDetail = () => {
  loading.value = true
  getCourseDetail(
    { courseId: route.params.id },
    {
      onSuccess: (data) => {
        course.value = data
        if (data.chapters && data.chapters.length > 0) {
          chapters.value = data.chapters
        } else {
          loadChapters()
        }
        loading.value = false
      },
      onError: () => {
        loading.value = false
        message.error('加载失败')
        router.back()
      }
    }
  )
}

// 加载章节列表
const loadChapters = () => {
  getCourseChapters(
    { courseId: route.params.id },
    {
      onSuccess: (data) => {
        chapters.value = data || []
      }
    }
  )
}

// 开始学习
const startLearning = () => {
  if (chapters.value.length > 0) {
    playChapter(chapters.value[0])
  } else {
    message.info('暂无章节内容')
  }
}

// 播放章节
const playChapter = (chapter) => {
  currentChapter.value = chapter
  isChapterModalVisible.value = true
}

// 工具函数
const getCourseCover = () => {
  if (course.value?.coverFilePath) {
    const path = course.value.coverFilePath
    if (path.startsWith('http')) return path
    // 静态资源直接访问，不需要API前缀
    return path.startsWith('/') ? path : '/' + path
  }
  return 'https://via.placeholder.com/400x250?text=' + encodeURIComponent(course.value?.title || '课程')
}

const getVideoUrl = (filePath) => {
  if (!filePath) return ''
  if (filePath.startsWith('http')) return filePath
  // 静态资源直接访问，不需要API前缀
  return filePath.startsWith('/') ? filePath : '/' + filePath
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="less">
.course-detail {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.course-header {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  padding: 60px 40px;
  color: #fff;

  .header-content {
    h1 {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 20px;
      color: #fff;
      font-family: 'SimSun', '宋体', serif;
      letter-spacing: 4px;
    }

    .course-tags {
      margin-bottom: 16px;

      :deep(.ant-tag) {
        font-size: 14px;
        padding: 6px 14px;
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
      }
    }

    .course-stats {
      font-size: 16px;
      opacity: 0.95;

      span {
        margin-right: 24px;

        i {
          margin-right: 8px;
          color: #c5a572;
        }
      }
    }
  }
}

.course-body {
  padding: 32px;

  .info-section {
    background: #fff;
    padding: 28px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
    border: 1px solid #e8e8e8;

    h2 {
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 24px;
      padding-bottom: 14px;
      border-bottom: 2px solid #e8e8e8;
      color: #2c2c2c;
      font-family: 'SimSun', '宋体', serif;
      letter-spacing: 2px;

      i {
        margin-right: 10px;
        color: #8b4513;
      }
    }

    .course-description {
      font-size: 16px;
      line-height: 2;
      color: #555;
      white-space: pre-wrap;
    }
  }
}

.chapter-list {
  .chapter-item {
    display: flex;
    align-items: center;
    padding: 18px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    margin-bottom: 14px;
    cursor: pointer;
    transition: all 0.3s;
    background: rgba(139, 69, 19, 0.02);

    &:hover {
      border-color: #c5a572;
      background: rgba(139, 69, 19, 0.06);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(139, 69, 19, 0.12);
    }

    .chapter-number {
      width: 44px;
      height: 44px;
      background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
      color: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      margin-right: 18px;
      font-size: 18px;
    }

    .chapter-info {
      flex: 1;

      h4 {
        font-size: 17px;
        font-weight: 600;
        margin-bottom: 6px;
        color: #2c2c2c;
        letter-spacing: 0.5px;
      }

      p {
        font-size: 14px;
        color: #666;
        margin: 0;
        line-height: 1.6;
      }
    }

    .chapter-icon {
      font-size: 26px;
      color: #8b4513;
    }
  }
}

.learning-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
  position: sticky;
  top: 24px;

  .card-cover {
    img {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }
  }

  .card-content {
    padding: 24px;

    :deep(.ant-btn-primary) {
      background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
      border: none;
      font-weight: 500;
      letter-spacing: 2px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
      }
    }

    .learning-tips {
      margin-top: 24px;
      padding-top: 20px;
      border-top: 2px solid #f0f0f0;

      h4 {
        font-size: 15px;
        font-weight: 600;
        margin-bottom: 14px;
        color: #2c2c2c;
        letter-spacing: 1px;
      }

      ul {
        padding-left: 20px;
        margin: 0;

        li {
          font-size: 14px;
          color: #666;
          line-height: 2.2;
        }
      }
    }
  }
}

.chapter-detail {
  .chapter-video {
    margin-bottom: 24px;
    
    .video-player {
      width: 100%;
      max-height: 400px;
      border-radius: 8px;
      background: #000;
      
      &.mb-16 {
        margin-bottom: 16px;
      }
    }
  }
  
  .no-video-tip {
    background: #f5f5f5;
    padding: 60px 20px;
    border-radius: 8px;
    text-align: center;
    margin-bottom: 24px;
    
    i {
      font-size: 48px;
      color: #ccc;
      margin-bottom: 16px;
      display: block;
    }
    
    p {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .chapter-content {
    h3 {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #f0f0f0;
    }

    .content-text {
      font-size: 15px;
      line-height: 2;
      color: #555;
      white-space: pre-wrap;
      padding: 20px;
      background: rgba(139, 69, 19, 0.02);
      border-radius: 6px;
      border: 1px solid #e8e8e8;
    }
  }
}
</style>

