<template>
  <div class="inheritor-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>传承人管理</h2>
      <a-button type="primary" @click="showCreateModal">
        <template #icon>
          <i class="fas fa-plus"></i>
        </template>
        新增传承人
      </a-button>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="姓名">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入姓名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        
        <a-form-item label="称号">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入称号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        
        <a-form-item label="地区">
          <a-input
            v-model:value="searchForm.region"
            placeholder="请输入地区"
            allow-clear
            style="width: 150px"
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
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :row-key="record => record.id"
      >
        <!-- 头像列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar
              v-if="record.avatarPath"
              :src="record.avatarPath"
              :size="50"
            />
            <a-avatar v-else :size="50">
              {{ record.name ? record.name.charAt(0) : '?' }}
            </a-avatar>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                查看
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该传承人吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :width="700"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="姓名" required>
          <a-input
            v-model:value="formData.name"
            placeholder="请输入姓名"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item label="称号">
          <a-input
            v-model:value="formData.title"
            placeholder="请输入称号，如：国家级代表性传承人"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item label="地区">
          <a-input
            v-model:value="formData.region"
            placeholder="请输入地区"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item label="简介">
          <a-textarea
            v-model:value="formData.bio"
            placeholder="请输入传承人简介"
            :rows="6"
            :maxlength="5000"
            show-count
          />
        </a-form-item>

        <a-form-item label="头像">
          <a-upload
            list-type="picture-card"
            :show-upload-list="false"
            :before-upload="beforeAvatarUpload"
            :custom-request="handleAvatarUpload"
          >
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="avatar"
              style="width: 100%; height: 100%; object-fit: cover;"
            />
            <div v-else>
              <i class="fas fa-plus" style="font-size: 24px; color: #999;"></i>
              <div style="margin-top: 8px">上传头像</div>
            </div>
          </a-upload>
          <div style="margin-top: 8px; color: #999; font-size: 12px;">
            支持jpg、png格式，大小不超过5MB
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情对话框 -->
    <a-modal
      v-model:open="detailVisible"
      title="传承人详情"
      :width="800"
      :footer="null"
    >
      <div v-if="currentRecord" class="inheritor-detail">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="头像" :span="2">
            <a-avatar
              v-if="currentRecord.avatarPath"
              :src="currentRecord.avatarPath"
              :size="80"
            />
            <a-avatar v-else :size="80">
              {{ currentRecord.name ? currentRecord.name.charAt(0) : '?' }}
            </a-avatar>
          </a-descriptions-item>
          <a-descriptions-item label="姓名">
            {{ currentRecord.name }}
          </a-descriptions-item>
          <a-descriptions-item label="称号">
            {{ currentRecord.title || '无' }}
          </a-descriptions-item>
          <a-descriptions-item label="地区">
            {{ currentRecord.region || '无' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDate(currentRecord.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="简介" :span="2">
            <div style="white-space: pre-wrap;">
              {{ currentRecord.bio || '暂无简介' }}
            </div>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 关联作品列表 -->
        <div v-if="currentRecord.heritageItems && currentRecord.heritageItems.length > 0" style="margin-top: 24px;">
          <h4>关联作品（{{ currentRecord.heritageItems.length }}）</h4>
          <a-list
            :data-source="currentRecord.heritageItems"
            :grid="{ gutter: 16, column: 2 }"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-card hoverable>
                  <div class="item-card">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-meta">
                      <span class="item-category">{{ item.category }}</span>
                      <span class="item-region">{{ item.region }}</span>
                    </div>
                  </div>
                </a-card>
              </a-list-item>
            </template>
          </a-list>
        </div>
        <a-empty v-else description="暂无关联作品" style="margin-top: 24px;" />

        <!-- 关联作品管理按钮 -->
        <div style="margin-top: 24px; text-align: right;">
          <a-button type="primary" @click="showItemRelationModal">
            <i class="fas fa-link"></i>
            管理关联作品
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 作品关联管理对话框 -->
    <a-modal
      v-model:open="itemRelationVisible"
      title="管理关联作品"
      :width="800"
      @ok="handleItemRelationOk"
      @cancel="handleItemRelationCancel"
    >
      <a-tabs v-model:activeKey="relationTab">
        <!-- 添加关联 -->
        <a-tab-pane key="add" tab="添加关联">
          <a-form layout="vertical">
            <a-form-item label="选择作品">
              <a-select
                v-model:value="selectedItemId"
                placeholder="请选择要关联的作品"
                show-search
                :filter-option="filterItemOption"
                style="width: 100%"
              >
                <a-select-option
                  v-for="item in availableItems"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.title }} - {{ item.category }} ({{ item.region }})
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-button type="primary" @click="handleAddRelation" :disabled="!selectedItemId">
              <i class="fas fa-plus"></i>
              添加关联
            </a-button>
          </a-form>
        </a-tab-pane>

        <!-- 已关联作品 -->
        <a-tab-pane key="list" tab="已关联作品">
          <a-list
            v-if="currentRecord && currentRecord.heritageItems && currentRecord.heritageItems.length > 0"
            :data-source="currentRecord.heritageItems"
            item-layout="horizontal"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="`类别：${item.category} | 地区：${item.region}`"
                />
                <template #actions>
                  <a-popconfirm
                    title="确定要移除此关联吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleRemoveRelation(item.id)"
                  >
                    <a-button type="link" danger size="small">
                      <i class="fas fa-unlink"></i>
                      移除
                    </a-button>
                  </a-popconfirm>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="暂无关联作品" />
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  getInheritorPage,
  createInheritor,
  updateInheritor,
  deleteInheritor,
  getInheritorById,
  addItemRelation,
  removeItemRelation
} from '@/api/InheritorApi';
import { getHeritageItemPage } from '@/api/HeritageApi';
import { uploadBusinessFile } from '@/api/FileApi';
import { generateBusinessUUID, BUSINESS_TYPES } from '@/utils/uuidUtils';

// 表格列定义
const columns = [
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    align: 'center'
  },
  {
    title: '姓名',
    dataIndex: 'name',
    key: 'name',
    width: 120
  },
  {
    title: '称号',
    dataIndex: 'title',
    key: 'title',
    width: 180
  },
  {
    title: '地区',
    dataIndex: 'region',
    key: 'region',
    width: 120
  },
  {
    title: '简介',
    dataIndex: 'bio',
    key: 'bio',
    ellipsis: true,
    customRender: ({ text }) => text ? (text.length > 50 ? text.substring(0, 50) + '...' : text) : '暂无'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    customRender: ({ text }) => formatDate(text)
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
];

// 搜索表单
const searchForm = reactive({
  name: '',
  title: '',
  region: ''
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: total => `共 ${total} 条记录`
});

// 对话框状态
const modalVisible = ref(false);
const detailVisible = ref(false);
const isEdit = ref(false);
const currentRecord = ref(null);

// 作品关联对话框状态
const itemRelationVisible = ref(false);
const relationTab = ref('add');
const selectedItemId = ref(null);
const availableItems = ref([]);

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  title: '',
  region: '',
  bio: '',
  avatarFileId: null
});

// 头像URL
const avatarUrl = ref('');

// 对话框标题
const modalTitle = computed(() => {
  return isEdit.value ? '编辑传承人' : '新增传承人';
});

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取传承人列表
const fetchInheritors = () => {
  loading.value = true;
  const params = {
    current: pagination.current,
    size: pagination.pageSize,
    ...searchForm
  };

  getInheritorPage(params, {
    onSuccess: (res) => {
      tableData.value = res.records || [];
      pagination.total = res.total || 0;
      loading.value = false;
    },
    onError: () => {
      loading.value = false;
    }
  });
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchInheritors();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    title: '',
    region: ''
  });
  pagination.current = 1;
  fetchInheritors();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchInheritors();
};

// 显示新增对话框
const showCreateModal = () => {
  isEdit.value = false;
  resetForm();
  modalVisible.value = true;
};

// 查看详情
const handleView = (record) => {
  getInheritorById(record.id, {
    onSuccess: (res) => {
      currentRecord.value = res;
      detailVisible.value = true;
    }
  });
};

// 编辑
const handleEdit = (record) => {
  isEdit.value = true;
  currentRecord.value = record;
  Object.assign(formData, {
    id: record.id,
    name: record.name,
    title: record.title,
    region: record.region,
    bio: record.bio,
    avatarFileId: record.avatarFileId
  });
  avatarUrl.value = record.avatarPath || '';
  modalVisible.value = true;
};

// 删除
const handleDelete = (record) => {
  deleteInheritor(record.id, {
    successMsg: '删除成功',
    onSuccess: () => {
      fetchInheritors();
    }
  });
};

// 对话框确定
const handleModalOk = () => {
  if (!formData.name || !formData.name.trim()) {
    message.warning('请输入姓名');
    return;
  }

  if (isEdit.value) {
    // 更新
    updateInheritor(formData.id, formData, {
      successMsg: '更新成功',
      onSuccess: () => {
        modalVisible.value = false;
        fetchInheritors();
      }
    });
  } else {
    // 新增 - 生成UUID
    formData.id = generateBusinessUUID(BUSINESS_TYPES.INHERITOR);
    createInheritor(formData, {
      successMsg: '创建成功',
      onSuccess: () => {
        modalVisible.value = false;
        fetchInheritors();
      }
    });
  }
};

// 对话框取消
const handleModalCancel = () => {
  modalVisible.value = false;
  resetForm();
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: '',
    name: '',
    title: '',
    region: '',
    bio: '',
    avatarFileId: null
  });
  avatarUrl.value = '';
};

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件!');
    return false;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB!');
    return false;
  }
  return true;
};

// 头像上传
const handleAvatarUpload = ({ file }) => {
  // 判断是新增还是编辑
  if (isEdit.value) {
    // 编辑模式：使用策略C - 直接业务绑定上传
    const businessInfo = {
      businessType: BUSINESS_TYPES.INHERITOR,
      businessId: formData.id,  // 使用已存在的业务ID
      businessField: 'avatar'
    };

    uploadBusinessFile(file, businessInfo, true,{
      onSuccess: (res) => {
        if (res && res.id) {
          formData.avatarFileId = res.id;
          avatarUrl.value = res.filePath;
          message.success('头像上传成功');
        }
      },
      onError: () => {
        message.error('头像上传失败');
      }
    });
  } else {
    // 新增模式：使用策略B - 前端UUID预生成上传
    // 确保有UUID
    if (!formData.id) {
      formData.id = generateBusinessUUID(BUSINESS_TYPES.INHERITOR);
    }

    const businessInfo = {
      businessType: BUSINESS_TYPES.INHERITOR,
      businessId: formData.id,  // 使用前端生成的UUID
      businessField: 'avatar'
    };

    uploadBusinessFile(file, businessInfo,true, {
      onSuccess: (res) => {
        if (res && res.id) {
          formData.avatarFileId = res.id;
          avatarUrl.value = res.filePath;
          message.success('头像上传成功');
        }
      },
      onError: () => {
        message.error('头像上传失败');
      }
    });
  }
};

// 显示作品关联管理对话框
const showItemRelationModal = () => {
  itemRelationVisible.value = true;
  relationTab.value = 'add';
  selectedItemId.value = null;
  fetchAvailableItems();
};

// 获取可用的作品列表
const fetchAvailableItems = () => {
  getHeritageItemPage({
    current: 1,
    size: 100,
    status: 2  // 只显示已发布的作品
  }, {
    onSuccess: (res) => {
      availableItems.value = res.records || [];
    }
  });
};

// 作品选择过滤
const filterItemOption = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

// 添加作品关联
const handleAddRelation = () => {
  if (!selectedItemId.value) {
    message.warning('请选择作品');
    return;
  }

  addItemRelation(currentRecord.value.id, selectedItemId.value, {
    successMsg: '关联添加成功',
    onSuccess: () => {
      selectedItemId.value = null;
      // 刷新当前传承人详情
      refreshCurrentRecord();
    }
  });
};

// 移除作品关联
const handleRemoveRelation = (itemId) => {
  removeItemRelation(currentRecord.value.id, itemId, {
    successMsg: '关联移除成功',
    onSuccess: () => {
      // 刷新当前传承人详情
      refreshCurrentRecord();
    }
  });
};

// 刷新当前记录
const refreshCurrentRecord = () => {
  if (currentRecord.value && currentRecord.value.id) {
    getInheritorById(currentRecord.value.id, {
      onSuccess: (res) => {
        currentRecord.value = res;
      }
    });
  }
};

// 关联对话框确定
const handleItemRelationOk = () => {
  itemRelationVisible.value = false;
};

// 关联对话框取消
const handleItemRelationCancel = () => {
  itemRelationVisible.value = false;
  selectedItemId.value = null;
};

// 初始化
onMounted(() => {
  fetchInheritors();
});
</script>

<style scoped>
.inheritor-management {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.search-section {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.table-section {
  margin-top: 16px;
}

.inheritor-detail {
  padding: 16px 0;
}

.item-card {
  padding: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #262626;
}

.item-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #8c8c8c;
}

.item-category {
  padding: 2px 8px;
  background: #e6f7ff;
  border-radius: 2px;
  color: #1890ff;
}

.item-region {
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 2px;
}
</style>

