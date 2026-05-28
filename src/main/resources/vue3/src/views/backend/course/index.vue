<template>
  <div class="course-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>课程管理</h2>
      <a-button type="primary" @click="showCreateModal">
        <template #icon>
          <i class="fas fa-plus"></i>
        </template>
        新增课程
      </a-button>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入课程标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        
        <a-form-item label="难度">
          <a-select
            v-model:value="searchForm.level"
            placeholder="请选择难度"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="option in COURSE_LEVEL_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="2">下架</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <i class="fas fa-search"></i>
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <i class="fas fa-redo"></i>
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'coverFileId'">
            <img
              v-if="record.coverFilePath"
              :src="getFullImagePath(record.coverFilePath)"
              alt="封面"
              style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;"
            />
            <span v-else>暂无封面</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewChapters(record)">
                章节管理
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除此课程吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 创建/编辑课程弹窗 -->
    <a-modal
      v-model:open="isModalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="课程标题" required>
          <a-input v-model:value="formData.title" placeholder="请输入课程标题" />
        </a-form-item>

        <a-form-item label="难度等级">
          <a-select v-model:value="formData.level" placeholder="请选择难度等级">
            <a-select-option
              v-for="option in COURSE_LEVEL_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="课程封面">
          <a-upload
            v-model:file-list="coverFileList"
            :before-upload="handleCoverBeforeUpload"
            :remove="handleRemoveCover"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="coverFileList.length < 1">
              <i class="fas fa-upload"></i>
              <div style="margin-top: 8px">上传封面</div>
            </div>
          </a-upload>
          <div style="color: #999; font-size: 12px; margin-top: 8px">
            支持JPG、PNG格式，建议尺寸16:9，文件大小不超过5MB
          </div>
        </a-form-item>

        <a-form-item label="课程描述">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入课程描述"
            :rows="4"
          />
        </a-form-item>

        <a-form-item label="课程状态">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="2">下架</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 章节管理弹窗 -->
    <a-modal
      v-model:open="isChapterModalVisible"
      title="章节管理"
      width="1000px"
      :footer="null"
    >
      <div class="chapter-header">
        <h3>课程: {{ currentCourse?.title }}</h3>
        <a-button type="primary" size="small" @click="showAddChapterModal">
          <template #icon>
            <i class="fas fa-plus"></i>
          </template>
          添加章节
        </a-button>
      </div>

      <a-table
        :columns="chapterColumns"
        :data-source="chapterData"
        :loading="chapterLoading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'videoFileId'">
            <a-tag v-if="record.videoFiles && record.videoFiles.length > 0" color="green">
              <i class="fas fa-video"></i> {{ record.videoFiles.length }}个视频
            </a-tag>
            <a-tag v-else color="default">
              <i class="fas fa-file-alt"></i> 无视频
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEditChapter(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除此章节吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDeleteChapter(record.id)"
              >
                <a-button type="link" danger size="small">
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 添加/编辑章节弹窗 -->
    <a-modal
      v-model:open="isChapterFormVisible"
      :title="chapterFormTitle"
      width="700px"
      @ok="handleChapterFormOk"
      @cancel="handleChapterFormCancel"
    >
      <a-form
        :model="chapterFormData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="章节标题" required>
          <a-input v-model:value="chapterFormData.title" placeholder="请输入章节标题" />
        </a-form-item>

        <a-form-item label="排序" required>
          <a-input-number
            v-model:value="chapterFormData.sort"
            :min="1"
            placeholder="请输入排序号"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="章节内容">
          <a-textarea
            v-model:value="chapterFormData.content"
            placeholder="请输入章节内容"
            :rows="6"
          />
        </a-form-item>

        <a-form-item label="章节视频">
          <a-upload
            :file-list="videoFileList"
            :before-upload="handleVideoBeforeUpload"
            @remove="handleRemoveVideo"
            accept="video/*"
            :max-count="5"
            list-type="picture-card"
            :show-upload-list="{ showPreviewIcon: false }"
          >
            <div v-if="videoFileList.length < 5">
              <i class="fas fa-upload"></i>
              <div style="margin-top: 8px">上传视频</div>
            </div>
          </a-upload>
          <div style="color: #999; font-size: 12px; margin-top: 8px">
            支持上传MP4、AVI、MOV等格式，单个文件不超过500MB
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { generateUUID } from '@/composables/useBusinessUUID'
import {
  getCoursePage,
  createCourse,
  updateCourse,
  deleteCourse,
  getCourseChapters,
  createChapter,
  updateChapter,
  deleteChapter
} from '@/api/CourseApi'
import { uploadBusinessFile, deleteFile } from '@/api/FileApi'
import { COURSE_LEVEL_OPTIONS, getCourseLevelName } from '@/config/courseLevel'

// 搜索表单
const searchForm = reactive({
  title: '',
  level: undefined,
  status: undefined
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: total => `共 ${total} 条数据`
})

// 表格列定义
const columns = [
  { title: '课程ID', dataIndex: 'id', key: 'id', width: 180 },
  { title: '标题', dataIndex: 'title', key: 'title' },
  { 
    title: '难度等级', 
    key: 'level', 
    width: 100,
    customRender: ({ record }) => getCourseLevelName(record.level)
  },
  { title: '封面', key: 'coverFileId', width: 100 },
  { title: '章节数', dataIndex: 'chapterCount', key: 'chapterCount', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 220 }
]

// 课程弹窗相关
const isModalVisible = ref(false)
const modalTitle = ref('新增课程')
const isEdit = ref(false)
const formData = reactive({
  id: '',
  title: '',
  level: '',
  description: '',
  status: 0,
  coverFileId: null
})

// 封面上传相关
const coverFileList = ref([])
const uploadingCover = ref(false)

// 章节管理相关
const isChapterModalVisible = ref(false)
const currentCourse = ref(null)
const chapterData = ref([])
const chapterLoading = ref(false)

const chapterColumns = [
  { title: '章节ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '视频', key: 'videoFileId', width: 100 },
  { title: '操作', key: 'action', width: 150 }
]

// 章节表单
const isChapterFormVisible = ref(false)
const chapterFormTitle = ref('添加章节')
const isChapterEdit = ref(false)
const chapterFormData = reactive({
  id: null,
  courseId: '',
  title: '',
  content: '',
  sort: 1
})

// 视频文件列表
const videoFileList = ref([])
const uploadingVideo = ref(false)

// 加载课程数据
const loadData = () => {
  loading.value = true
  getCoursePage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    },
    {
      onSuccess: (data) => {
        tableData.value = data.records || []
        pagination.total = data.total || 0
        loading.value = false
      },
      onError: () => {
        loading.value = false
      }
    }
  )
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.title = ''
  searchForm.level = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false
  modalTitle.value = '新增课程'
  resetForm()
  isModalVisible.value = true
}

// 编辑
const handleEdit = (record) => {
  isEdit.value = true
  modalTitle.value = '编辑课程'
  Object.assign(formData, record)
  // 加载课程封面
  loadCourseCover(record)
  isModalVisible.value = true
}

// 加载课程封面
const loadCourseCover = (course) => {
  coverFileList.value = []
  if (course.coverFileId) {
    // 如果有封面文件ID，构造文件信息用于显示
    coverFileList.value = [{
      uid: course.coverFileId,
      name: '课程封面',
      status: 'done',
      url: course.coverFilePath || '',
      fileId: course.coverFileId
    }]
  }
}

// 封面上传前处理
const handleCoverBeforeUpload = async (file) => {
  // 文件大小验证（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('封面文件大小不能超过 5MB!')
    return false
  }

  // 文件类型验证
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }

  try {
    uploadingCover.value = true

    // 生成临时文件ID用于显示上传状态
    const tempFileId = Date.now()

    // 添加上传中的文件到列表
    coverFileList.value = [{
      uid: tempFileId,
      name: file.name,
      status: 'uploading',
      percent: 0
    }]

    // 如果是新建课程，需要先生成UUID
    if (!formData.id) {
      formData.id = generateUUID()
    }

    // 上传封面文件
    await uploadBusinessFile(
      file,
      {
        businessType: 'COURSE',
        businessId: formData.id,
        businessField: 'cover'
      },
      false,
      {
        onSuccess: (data) => {
          // 移除上传中的临时文件
          coverFileList.value = []

          // 添加上传成功的文件
          coverFileList.value = [{
            uid: data.id,
            name: data.originalName || file.name,
            status: 'done',
            url: data.filePath,
            fileId: data.id
          }]

          // 更新表单数据
          formData.coverFileId = data.id
          message.success('封面上传成功')
          uploadingCover.value = false
        },
        onError: () => {
          // 移除上传失败的文件
          coverFileList.value = []
          message.error('封面上传失败')
          uploadingCover.value = false
        },
        successMsg: false
      }
    )

  } catch (error) {
    console.error('上传失败:', error)
    coverFileList.value = []
    uploadingCover.value = false
  }

  return false // 阻止默认上传行为
}

// 删除封面
const handleRemoveCover = (file) => {
  return new Promise((resolve, reject) => {
    try {
      if (file.fileId) {
        // 有fileId，需要调用后端删除API
        deleteFile(
          { fileId: file.fileId },
          {
            onSuccess: () => {
              // 清空文件列表和表单数据
              coverFileList.value = []
              formData.coverFileId = null
              message.success('封面删除成功')
              resolve(true) // 允许Ant Design移除
            },
            onError: () => {
              message.error('删除失败')
              reject(false) // 阻止Ant Design移除
            },
            successMsg: false
          }
        )
      } else {
        // 没有fileId（可能是上传中或上传失败的文件），直接移除
        coverFileList.value = []
        formData.coverFileId = null
        message.success('文件移除成功')
        resolve(true) // 允许Ant Design移除
      }
    } catch (error) {
      console.error('删除封面异常:', error)
      message.error('删除失败')
      reject(false) // 阻止Ant Design移除
    }
  })
}

// 弹窗确认
const handleModalOk = () => {
  if (!formData.title) {
    message.error('请填写课程标题')
    return
  }

  // 构建提交数据
  const submitData = {
    ...formData
  }

  if (isEdit.value) {
    updateCourse(
      { courseId: formData.id, ...submitData },
      {
        onSuccess: () => {
          message.success('更新成功')
          isModalVisible.value = false
          loadData()
        },
        successMsg: false
      }
    )
  } else {
    createCourse(
      submitData,
      {
        onSuccess: () => {
          message.success('创建成功')
          isModalVisible.value = false
          loadData()
        },
        successMsg: false
      }
    )
  }
}

// 弹窗取消
const handleModalCancel = () => {
  isModalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.title = ''
  formData.level = ''
  formData.description = ''
  formData.status = 0
  formData.coverFileId = null
  coverFileList.value = []
}

// 删除
const handleDelete = (id) => {
  deleteCourse(
    { courseId: id },
    {
      onSuccess: () => {
        message.success('删除成功')
        loadData()
      },
      successMsg: false
    }
  )
}

// 查看章节
const handleViewChapters = (record) => {
  currentCourse.value = record
  isChapterModalVisible.value = true
  loadChapters()
}

// 加载章节数据
const loadChapters = () => {
  chapterLoading.value = true
  getCourseChapters(
    { courseId: currentCourse.value.id },
    {
      onSuccess: (data) => {
        chapterData.value = data || []
        chapterLoading.value = false
      },
      onError: () => {
        chapterLoading.value = false
      }
    }
  )
}

// 显示添加章节弹窗
const showAddChapterModal = () => {
  isChapterEdit.value = false
  chapterFormTitle.value = '添加章节'
  resetChapterForm()
  chapterFormData.courseId = currentCourse.value.id
  isChapterFormVisible.value = true
}

// 编辑章节
const handleEditChapter = (record) => {
  isChapterEdit.value = true
  chapterFormTitle.value = '编辑章节'
  Object.assign(chapterFormData, record)
  
  // 加载视频文件列表
  loadChapterVideos(record)
  
  isChapterFormVisible.value = true
}

// 章节表单确认
const handleChapterFormOk = () => {
  if (!chapterFormData.title || !chapterFormData.sort) {
    message.error('请填写必填项')
    return
  }

  // 如果已经有章节ID（已创建），只需更新章节信息
  if (chapterFormData.id) {
    if (isChapterEdit.value) {
      // 编辑模式：更新章节信息
      updateChapter(
        { chapterId: chapterFormData.id, ...chapterFormData },
        {
          onSuccess: () => {
            message.success('更新成功')
            isChapterFormVisible.value = false
            loadChapters()
          },
          successMsg: false
        }
      )
    } else {
      // 新增模式但已创建章节（因为上传了视频）
      message.success('章节创建成功')
      isChapterFormVisible.value = false
      loadChapters()
    }
  } else {
    // 没有章节ID，说明没有上传视频，直接创建章节
    createChapter(
      chapterFormData,
      {
        onSuccess: () => {
          message.success('创建成功')
          isChapterFormVisible.value = false
          loadChapters()
        },
        successMsg: false
      }
    )
  }
}

// 章节表单取消
const handleChapterFormCancel = () => {
  isChapterFormVisible.value = false
  resetChapterForm()
}

// 重置章节表单
const resetChapterForm = () => {
  chapterFormData.id = null
  chapterFormData.courseId = ''
  chapterFormData.title = ''
  chapterFormData.content = ''
  chapterFormData.sort = 1
  videoFileList.value = [] // 清空视频列表
}

// 删除章节
const handleDeleteChapter = (id) => {
  deleteChapter(
    { chapterId: id },
    {
      onSuccess: () => {
        message.success('删除成功')
        loadChapters()
      },
      successMsg: false
    }
  )
}

// 加载章节视频文件
const loadChapterVideos = (chapter) => {
  videoFileList.value = []
  
  // 如果章节有视频文件列表
  if (chapter.videoFiles && Array.isArray(chapter.videoFiles)) {
    videoFileList.value = chapter.videoFiles.map(video => ({
      uid: video.id,
      name: video.originalName || '视频文件',
      status: 'done',
      url: video.filePath,
      fileId: video.id
    }))
  }
}

// 视频上传前处理
const handleVideoBeforeUpload = async (file) => {
  // 文件大小验证（500MB）
  const isLt500M = file.size / 1024 / 1024 < 500
  if (!isLt500M) {
    message.error('视频文件大小不能超过 500MB!')
    return false
  }

  // 文件类型验证
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    message.error('只能上传视频文件!')
    return false
  }

  try {
    uploadingVideo.value = true

    // 生成临时文件ID用于显示上传中状态
    const tempFileId = Date.now()
    
    // 添加上传中的文件到列表
    videoFileList.value.push({
      uid: tempFileId,
      name: file.name,
      status: 'uploading',
      percent: 0
    })

    // 如果是新增章节，需要先创建章节获取ID
    if (!chapterFormData.id) {
      // 验证必填项
      if (!chapterFormData.title || !chapterFormData.sort) {
        message.error('请先填写章节标题和排序号')
        // 移除上传中的文件
        const index = videoFileList.value.findIndex(item => item.uid === tempFileId)
        if (index > -1) {
          videoFileList.value.splice(index, 1)
        }
        uploadingVideo.value = false
        return false
      }

      // 先创建章节
      await new Promise((resolve, reject) => {
        createChapter(
          {
            courseId: chapterFormData.courseId,
            title: chapterFormData.title,
            content: chapterFormData.content,
            sort: chapterFormData.sort
          },
          {
            onSuccess: (data) => {
              chapterFormData.id = data.id
              message.success('章节创建成功，正在上传视频...')
              resolve()
            },
            onError: (error) => {
              message.error('章节创建失败')
              // 移除上传中的文件
              const index = videoFileList.value.findIndex(item => item.uid === tempFileId)
              if (index > -1) {
                videoFileList.value.splice(index, 1)
              }
              reject(error)
            },
            successMsg: false
          }
        )
      })
    }

    // 上传视频文件
    await uploadBusinessFile(
      file,
      {
        businessType: 'COURSE_CHAPTER',
        businessId: String(chapterFormData.id),
        businessField: 'video'
      },
      false,
      {
        onSuccess: (data) => {
          // 移除上传中的临时文件
          const index = videoFileList.value.findIndex(item => item.uid === tempFileId)
          if (index > -1) {
            videoFileList.value.splice(index, 1)
          }
          
          // 添加上传成功的文件
          videoFileList.value.push({
            uid: data.id,
            name: data.originalName || file.name,
            status: 'done',
            url: data.filePath,
            fileId: data.id
          })
          message.success('视频上传成功')
          uploadingVideo.value = false
        },
        onError: () => {
          // 移除上传中的文件，标记为失败
          const index = videoFileList.value.findIndex(item => item.uid === tempFileId)
          if (index > -1) {
            videoFileList.value[index].status = 'error'
            // 2秒后移除失败的文件
            setTimeout(() => {
              const idx = videoFileList.value.findIndex(item => item.uid === tempFileId)
              if (idx > -1) {
                videoFileList.value.splice(idx, 1)
              }
            }, 2000)
          }
          message.error('视频上传失败')
          uploadingVideo.value = false
        },
        successMsg: false
      }
    )

  } catch (error) {
    console.error('上传失败:', error)
    uploadingVideo.value = false
  }

  return false // 阻止默认上传行为
}

// 删除视频
const handleRemoveVideo = async (file) => {
  return new Promise((resolve, reject) => {
    try {
      if (file.fileId) {
        // 有fileId，需要调用后端删除API
        deleteFile(
          { fileId: file.fileId },
          {
            onSuccess: () => {
              // 从列表中移除文件
              const index = videoFileList.value.findIndex(item => item.uid === file.uid)
              if (index > -1) {
                videoFileList.value.splice(index, 1)
              }
              message.success('视频删除成功')
              resolve(true)
            },
            onError: () => {
              message.error('删除失败')
              reject(false)
            },
            successMsg: false
          }
        )
      } else {
        // 没有fileId，直接移除
        const index = videoFileList.value.findIndex(item => item.uid === file.uid)
        if (index > -1) {
          videoFileList.value.splice(index, 1)
        }
        message.success('文件移除成功')
        resolve(true)
      }
    } catch (error) {
      console.error('删除视频异常:', error)
      message.error('删除失败')
      reject(false)
    }
  })
}

// 工具函数
const getStatusColor = (status) => {
  const colors = {
    0: 'default',
    1: 'green',
    2: 'gray'
  }
  return colors[status] || 'default'
}

const getFullImagePath = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 静态资源直接访问，不需要API前缀
  return path.startsWith('/') ? path : '/' + path
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="less">
.course-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 4px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.search-section {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.table-section {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
}
</style>