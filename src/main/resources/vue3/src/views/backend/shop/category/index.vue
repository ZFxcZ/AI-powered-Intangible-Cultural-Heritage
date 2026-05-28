<template>
  <div class="shop-category-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品分类管理</h2>
      <a-button type="primary" @click="showCreateModal">
        <PlusOutlined />
        新增分类
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="分类名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入分类名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <SearchOutlined />
              查询
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined />
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 分类列表表格 -->
    <a-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 状态 -->
        <template #status="{ record }">
          <a-switch
            :checked="record.status === 1"
            @change="(checked) => handleStatusChange(record.id, checked ? 1 : 0)"
            :loading="record.statusLoading"
          />
          <span class="status-text">{{ record.statusDesc }}</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="showEditModal(record)"
            >
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个分类吗？"
              @confirm="handleDelete(record.id)"
              ok-text="确定"
              cancel-text="取消"
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
      </a-table>
    </a-card>

    <!-- 新增/编辑分类弹窗 -->
    <a-modal
      :title="modalTitle"
      :open="modalVisible"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      width="500px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="分类名称" name="name">
          <a-input
            v-model:value="formData.name"
            placeholder="请输入分类名称"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import {
  getCategoryPage,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus
} from '@/api/ShopCategoryApi';

// 响应式数据
const loading = ref(false);
const tableData = ref([]);
const modalVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref();
const isEdit = ref(false);
const currentEditId = ref(null);

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表单数据
const formData = reactive({
  name: '',
  status: 1
});

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称长度不能超过100个字符', trigger: 'blur' }
  ]
};

// 表格列定义
const columns = [
  {
    title: '分类ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '分类名称',
    dataIndex: 'name',
    key: 'name',
    width: 200
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 150,
    fixed: 'right'
  }
];

// 计算属性
const modalTitle = computed(() => {
  return isEdit.value ? '编辑分类' : '新增分类';
});

// 生命周期
onMounted(() => {
  fetchCategoryList();
});

// 方法
const fetchCategoryList = () => {
  loading.value = true;
  
  const params = {
    current: pagination.current,
    size: pagination.pageSize,
    name: searchForm.name || null,
    status: searchForm.status
  };
  
  getCategoryPage(params, {
    onSuccess: (res) => {
      tableData.value = res.records || [];
      pagination.total = res.total || 0;
      pagination.current = res.current || 1;
      loading.value = false;
    },
    onError: (error) => {
      message.error('获取分类列表失败：' + error.message);
      loading.value = false;
    }
  });
};

const handleSearch = () => {
  pagination.current = 1;
  fetchCategoryList();
};

const handleReset = () => {
  searchForm.name = '';
  searchForm.status = null;
  pagination.current = 1;
  fetchCategoryList();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchCategoryList();
};

const showCreateModal = () => {
  isEdit.value = false;
  currentEditId.value = null;
  modalVisible.value = true;
  // 使用 nextTick 确保 DOM 更新后再重置表单
  nextTick(() => {
    resetForm();
  });
};

const showEditModal = (record) => {
  isEdit.value = true;
  currentEditId.value = record.id;
  modalVisible.value = true;
  // 使用 nextTick 确保 DOM 更新后再设置表单数据
  nextTick(() => {
    Object.assign(formData, {
      name: record.name,
      status: record.status
    });
    // 清除表单验证状态
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  });
};

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    status: 1
  });
  // 清除表单验证状态
  if (formRef.value) {
    formRef.value.resetFields();
    formRef.value.clearValidate();
  }
};

const handleSubmit = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    
    const params = isEdit.value ? 
      { ...formData, id: currentEditId.value } :
      formData;
    
    const apiCall = isEdit.value ? updateCategory : createCategory;
    
    apiCall(params, {
      successMsg: isEdit.value ? '更新分类成功' : '创建分类成功',
      onSuccess: () => {
        submitLoading.value = false;
        modalVisible.value = false;
        fetchCategoryList();
        // 成功后重置表单
        nextTick(() => {
          resetForm();
        });
      },
      onError: (error) => {
        message.error((isEdit.value ? '更新' : '创建') + '分类失败：' + error.message);
        submitLoading.value = false;
      }
    });
  });
};

const handleCancel = () => {
  modalVisible.value = false;
  // 延迟重置表单，确保模态框完全关闭后再重置
  nextTick(() => {
    resetForm();
  });
};

const handleStatusChange = (id, status) => {
  const record = tableData.value.find(item => item.id === id);
  if (record) {
    record.statusLoading = true;
  }
  
  updateCategoryStatus(id, status, {
    successMsg: status === 1 ? '启用分类成功' : '禁用分类成功',
    onSuccess: () => {
      if (record) {
        record.status = status;
        record.statusDesc = status === 1 ? '启用' : '禁用';
        record.statusLoading = false;
      }
    },
    onError: (error) => {
      message.error('更新状态失败：' + error.message);
      if (record) {
        record.statusLoading = false;
      }
    }
  });
};

const handleDelete = (id) => {
  deleteCategory(id, {
    successMsg: '删除分类成功',
    onSuccess: () => {
      fetchCategoryList();
    },
    onError: (error) => {
      message.error('删除分类失败：' + error.message);
    }
  });
};
</script>

<style scoped>
.shop-category-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 24px;
}

.status-text {
  margin-left: 8px;
}
</style>