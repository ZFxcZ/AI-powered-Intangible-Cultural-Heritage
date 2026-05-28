<template>
  <div class="heritage-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>非遗作品管理</h2>
      <a-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <i class="fas fa-plus"></i>
        </template>
        新增作品
      </a-button>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入作品标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        
        <a-form-item label="类别">
          <a-select
            v-model:value="searchForm.category"
            placeholder="请选择类别"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="category in HERITAGE_CATEGORIES"
              :key="category"
              :value="category"
            >
              {{ category }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="地区">
          <a-input
            v-model:value="searchForm.region"
            placeholder="请输入地区"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option
              v-for="(status, key) in HERITAGE_ITEM_STATUS"
              :key="key"
              :value="status.code"
            >
              {{ status.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="创建时间">
          <a-range-picker
            v-model:value="dateRange"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
        :data-source="heritageStore.heritageList"
        :loading="heritageStore.loading"
        :pagination="pagination"
        :row-key="record => record.id"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 标题 -->
          <template v-if="column.key === 'title'">
            <div class="title-cell">
              <span class="title-text" @click="handleView(record)">{{ record.title }}</span>
            </div>
          </template>

          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <HeritageStatusTag :status="record.status" :status-name="record.statusName" show-icon />
          </template>

          <!-- 创建时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <!-- 发布时间 -->
          <template v-else-if="column.key === 'publishTime'">
            {{ record.publishTime ? formatDate(record.publishTime) : '-' }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button 
                type="default" 
                size="small" 
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-dropdown>
                <a-button size="small">
                  更多
                  <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu @click="({ key }) => handleAction(key, record)">
                    <a-menu-item 
                      v-if="canPublish(record)" 
                      key="publish"
                    >
                      <i class="fas fa-upload"></i>
                      发布
                    </a-menu-item>
                    <a-menu-item 
                      v-if="canOffline(record)" 
                      key="offline"
                    >
                      <i class="fas fa-download"></i>
                      下架
                    </a-menu-item>
                    <a-menu-divider v-if="canDelete(record)" />
                    <a-menu-item 
                      v-if="canDelete(record)" 
                      key="delete"
                      danger
                    >
                      <i class="fas fa-trash"></i>
                      删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 对话框组件 -->
    <HeritageItemCreate
      v-model:open="showCreateModal"
      mode="admin"
      @success="handleModalSuccess"
    />
    
    <HeritageDetailModal
      v-model:open="showDetailModal"
      :item-id="currentItemId"
      @edit="handleModalEdit"
      @success="handleModalSuccess"
    />
    
    <HeritageItemEdit
      v-model:open="showEditModal"
      :item-id="currentItemId"
      mode="admin"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'
import { useHeritageStore } from '@/store/heritage'
import { 
  getHeritageItemPage, 
  publishHeritageItem, 
  offlineHeritageItem, 
  deleteHeritageItem,
  HERITAGE_ITEM_STATUS,
  HERITAGE_CATEGORIES
} from '@/api/HeritageApi'
import HeritageStatusTag from '@/components/common/HeritageStatusTag.vue'
import HeritageItemCreate from '@/components/common/HeritageItemCreate.vue'
import HeritageDetailModal from './detail.vue'
import HeritageItemEdit from '@/components/common/HeritageItemEdit.vue'
import { formatLocalDate } from '@/utils/dateUtils'

// ========== 路由和状态 ==========
const router = useRouter()
const heritageStore = useHeritageStore()

// ========== 响应式数据 ==========
const searchForm = reactive({
  title: '',
  category: '',
  region: '',
  status: null
})

const dateRange = ref([])

// 对话框状态
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const showEditModal = ref(false)
const currentItemId = ref(null)

// ========== 表格配置 ==========
const columns = [
  {
    title: '标题',
    key: 'title',
    dataIndex: 'title',
    minWidth: 200,
    ellipsis: true
  },
  {
    title: '类别',
    key: 'category',
    dataIndex: 'category',
    width: 100
  },
  {
    title: '地区',
    key: 'region',
    dataIndex: 'region',
    width: 120
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '创建人',
    key: 'creatorName',
    dataIndex: 'creatorName',
    width: 120
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '发布时间',
    key: 'publishTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
    align: 'center'
  }
]

// 分页配置
const pagination = computed(() => ({
  current: heritageStore.searchParams.currentPage,
  pageSize: heritageStore.searchParams.size,
  total: heritageStore.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100']
}))

// ========== 方法 ==========

/**
 * 格式化日期
 */
function formatDate(dateStr) {
  if (!dateStr) return ''
  try {
    return formatLocalDate(new Date(dateStr))
  } catch (error) {
    return ''
  }
}

/**
 * 判断是否可以发布
 */
function canPublish(item) {
  return item.status === 0 || item.status === 1 // 草稿或待审
}

/**
 * 判断是否可以下架
 */
function canOffline(item) {
  return item.status === 2 // 已发布
}

/**
 * 判断是否可以删除
 */
function canDelete(item) {
  return item.status === 0 || item.status === 3 // 草稿或下架
}

/**
 * 获取作品列表
 */
function fetchHeritageList() {
  heritageStore.setLoading(true)
  
  const params = {
    ...heritageStore.searchParams,
    ...searchForm
  }
  
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  
  getHeritageItemPage(params, {
    onSuccess: (res) => {
      heritageStore.setHeritageList(res.records || [], res.total || 0)
      heritageStore.setLoading(false)
    },
    onError: (error) => {
      message.error('获取作品列表失败')
      heritageStore.setLoading(false)
    }
  })
}

/**
 * 搜索
 */
function handleSearch() {
  heritageStore.searchParams.currentPage = 1
  fetchHeritageList()
}

/**
 * 重置搜索
 */
function handleReset() {
  Object.assign(searchForm, {
    title: '',
    category: '',
    region: '',
    status: null
  })
  dateRange.value = []
  heritageStore.resetSearchParams()
  fetchHeritageList()
}

/**
 * 表格变化处理
 */
function handleTableChange(pag, filters, sorter) {
  heritageStore.searchParams.currentPage = pag.current
  heritageStore.searchParams.size = pag.pageSize
  fetchHeritageList()
}

/**
 * 页面大小改变
 */
function handleSizeChange() {
  fetchHeritageList()
}

/**
 * 当前页改变
 */
function handleCurrentChange() {
  fetchHeritageList()
}

/**
 * 新增作品
 */
function handleCreate() {
  showCreateModal.value = true
}

/**
 * 查看作品
 */
function handleView(item) {
  currentItemId.value = item.id
  showDetailModal.value = true
}

/**
 * 编辑作品
 */
function handleEdit(item) {
  currentItemId.value = item.id
  showEditModal.value = true
}

/**
 * 对话框成功回调
 */
function handleModalSuccess() {
  fetchHeritageList()
}

/**
 * 详情对话框编辑回调
 */
function handleModalEdit(item) {
  showDetailModal.value = false
  currentItemId.value = item.id
  showEditModal.value = true
}

/**
 * 处理操作
 */
function handleAction(command, item) {
  switch (command) {
    case 'publish':
      handlePublish(item)
      break
    case 'offline':
      handleOffline(item)
      break
    case 'delete':
      handleDelete(item)
      break
  }
}

/**
 * 发布作品
 */
function handlePublish(item) {
  Modal.confirm({
    title: '提示',
    content: `确定要发布作品"${item.title}"吗？`,
    onOk() {
      publishHeritageItem({ itemId: item.id }, {
        successMsg: '发布成功',
        onSuccess: () => {
          heritageStore.updateItemStatus(item.id, 2)
        }
      })
    }
  })
}

/**
 * 下架作品
 */
function handleOffline(item) {
  Modal.confirm({
    title: '提示',
    content: `确定要下架作品"${item.title}"吗？`,
    onOk() {
      offlineHeritageItem({ itemId: item.id }, {
        successMsg: '下架成功',
        onSuccess: () => {
          heritageStore.updateItemStatus(item.id, 3)
        }
      })
    }
  })
}

/**
 * 删除作品
 */
function handleDelete(item) {
  Modal.confirm({
    title: '危险操作',
    content: `确定要删除作品"${item.title}"吗？此操作不可恢复！`,
    okText: '确定删除',
    cancelText: '取消',
    okType: 'danger',
    onOk() {
      deleteHeritageItem({ itemId: item.id }, {
        successMsg: '删除成功',
        onSuccess: () => {
          heritageStore.removeItemFromList(item.id)
        }
      })
    }
  })
}

// ========== 生命周期 ==========
onMounted(() => {
  fetchHeritageList()
})

// ========== 监听器 ==========
watch(() => heritageStore.searchParams.currentPage, () => {
  fetchHeritageList()
})
</script>

<style scoped>
.heritage-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-cell {
  display: flex;
  align-items: center;
}

.title-text {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}

.title-text:hover {
  text-decoration: underline;
}


.text-danger {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .heritage-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-section :deep(.ant-form) {
    display: flex;
    flex-direction: column;
  }
  
  .search-section :deep(.ant-form-item) {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>

