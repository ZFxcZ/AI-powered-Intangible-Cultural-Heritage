<template>
  <div class="shop-product-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品管理</h2>
      <a-button type="primary" @click="showCreateModal">
        <PlusOutlined />
        新增商品
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <a-card class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="商品标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入商品标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="商品分类">
          <a-select
            v-model:value="searchForm.categoryId"
            placeholder="请选择分类"
            allow-clear
            style="width: 150px"
          >
            <a-select-option
              v-for="category in categoryList"
              :key="category.id"
              :value="category.id"
            >
              {{ category.name }}
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
            <a-select-option :value="1">上架</a-select-option>
            <a-select-option :value="0">下架</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="库存">
          <a-select
            v-model:value="searchForm.hasStock"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="true">有库存</a-select-option>
            <a-select-option :value="false">无库存</a-select-option>
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

    <!-- 商品列表表格 -->
    <a-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 封面图片 -->
        <template #coverImage="{ record }">
          <a-image
            v-if="record.coverFilePath"
            :src="record.coverFilePath"
            :width="60"
            :height="60"
            :preview="true"
            style="object-fit: cover; border-radius: 4px;"
          />
          <span v-else style="color: #999;">暂无图片</span>
        </template>

        <!-- 价格 -->
        <template #price="{ record }">
          <span style="color: #ff4d4f; font-weight: 500;">
            ¥{{ record.price }}
          </span>
        </template>

        <!-- 库存 -->
        <template #stock="{ record }">
          <a-tag :color="record.stock > 0 ? 'success' : 'error'">
            {{ record.stock }}
          </a-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ record }">
          <a-switch
            :checked="record.status === 1"
            @change="(checked) => handleStatusChange(record.id, checked ? 1 : 0)"
            :loading="record.statusLoading"
          />
          <span class="status-text">{{ record.statusName }}</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="showDetailModal(record)"
            >
              详情
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="showEditModal(record)"
            >
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="showStockModal(record)"
            >
              库存
            </a-button>
            <a-popconfirm
              title="确定要删除这个商品吗？"
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

    <!-- 新增/编辑商品弹窗 -->
    <a-modal
      :title="modalTitle"
      :open="modalVisible"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      width="800px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="商品标题" name="title">
          <a-input
            v-model:value="formData.title"
            placeholder="请输入商品标题"
            :maxlength="200"
          />
        </a-form-item>

        <a-form-item label="副标题" name="subtitle">
          <a-input
            v-model:value="formData.subtitle"
            placeholder="请输入副标题"
            :maxlength="255"
          />
        </a-form-item>

        <a-form-item label="商品分类" name="categoryId">
          <a-select
            v-model:value="formData.categoryId"
            placeholder="请选择商品分类"
          >
            <a-select-option
              v-for="category in categoryList"
              :key="category.id"
              :value="category.id"
            >
              {{ category.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="商品价格" name="price">
          <a-input-number
            v-model:value="formData.price"
            placeholder="请输入商品价格"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          >
            <template #addonBefore>¥</template>
          </a-input-number>
        </a-form-item>

        <a-form-item label="库存数量" name="stock">
          <a-input-number
            v-model:value="formData.stock"
            placeholder="请输入库存数量"
            :min="0"
            :precision="0"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="封面图片" name="coverFileId">
          <a-upload
            v-model:file-list="coverFileList"
            list-type="picture-card"
            :before-upload="beforeImageUpload"
            :custom-request="handleCoverUpload"
            :max-count="1"
            @remove="handleCoverRemove"
          >
            <div v-if="coverFileList.length < 1">
              <PlusOutlined />
              <div style="margin-top: 8px">上传封面</div>
            </div>
          </a-upload>
          <div class="upload-tip">建议尺寸：800x800px，支持jpg、png格式，大小不超过5MB</div>
        </a-form-item>

        <a-form-item label="商品图片" name="imageFiles">
          <a-upload
            v-model:file-list="imageFileList"
            list-type="picture-card"
            :before-upload="beforeImageUpload"
            :custom-request="handleImageUpload"
            :max-count="10"
            @remove="handleImageRemove"
            multiple
          >
            <div v-if="imageFileList.length < 10">
              <PlusOutlined />
              <div style="margin-top: 8px">上传图片</div>
            </div>
          </a-upload>
          <div class="upload-tip">最多上传10张，建议尺寸：800x800px，支持jpg、png格式，大小不超过5MB</div>
        </a-form-item>

        <a-form-item label="商品详情" name="detail">
          <RichTextEditor
            v-model="formData.detail"
            placeholder="请输入商品详情，支持富文本格式"
            height="400px"
          />
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">上架</a-radio>
            <a-radio :value="0">下架</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 商品详情弹窗 -->
    <a-modal
      title="商品详情"
      :open="detailVisible"
      @cancel="detailVisible = false"
      :footer="null"
      width="800px"
    >
      <a-descriptions bordered :column="2" v-if="currentProduct">
        <a-descriptions-item label="商品ID" :span="2">
          {{ currentProduct.id }}
        </a-descriptions-item>
        <a-descriptions-item label="商品标题" :span="2">
          {{ currentProduct.title }}
        </a-descriptions-item>
        <a-descriptions-item label="副标题" :span="2">
          {{ currentProduct.subtitle || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="商品分类">
          {{ currentProduct.categoryName }}
        </a-descriptions-item>
        <a-descriptions-item label="商品价格">
          <span style="color: #ff4d4f; font-weight: 500;">
            ¥{{ currentProduct.price }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="库存数量">
          <a-tag :color="currentProduct.stock > 0 ? 'success' : 'error'">
            {{ currentProduct.stock }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentProduct.status === 1 ? 'success' : 'default'">
            {{ currentProduct.statusName }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="封面图片" :span="2">
          <a-image
            v-if="currentProduct.coverFilePath"
            :src="currentProduct.coverFilePath"
            :width="200"
            :preview="true"
          />
          <span v-else>暂无图片</span>
        </a-descriptions-item>
        <a-descriptions-item label="商品详情" :span="2">
          <div 
            v-if="currentProduct.detail" 
            class="product-detail-content"
            v-html="currentProduct.detail"
          ></div>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentProduct.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ currentProduct.updateTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 库存管理弹窗 -->
    <a-modal
      title="库存管理"
      :open="stockVisible"
      @ok="handleStockSubmit"
      @cancel="stockVisible = false"
      :confirm-loading="stockLoading"
      width="400px"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="商品标题">
          <span>{{ currentProduct?.title }}</span>
        </a-form-item>
        <a-form-item label="当前库存">
          <a-tag :color="currentProduct?.stock > 0 ? 'success' : 'error'">
            {{ currentProduct?.stock }}
          </a-tag>
        </a-form-item>
        <a-form-item label="新库存数量">
          <a-input-number
            v-model:value="newStock"
            placeholder="请输入新库存数量"
            :min="0"
            :precision="0"
            style="width: 100%"
          />
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
  getProductPage,
  getProductById,
  createProduct,
  updateProduct,
  deleteProduct,
  onShelfProduct,
  offShelfProduct,
  updateProductStock
} from '@/api/ShopProductApi';
import { getEnabledCategories } from '@/api/ShopCategoryApi';
import { uploadBusinessFile, getFilesByBusinessField } from '@/api/FileApi';
import { useBusinessUUID } from '@/composables/useBusinessUUID';
import RichTextEditor from '@/components/common/RichTextEditor.vue';

// 响应式数据
const loading = ref(false);
const tableData = ref([]);
const modalVisible = ref(false);
const detailVisible = ref(false);
const stockVisible = ref(false);
const submitLoading = ref(false);
const stockLoading = ref(false);
const formRef = ref();
const isEdit = ref(false);
const currentEditId = ref(null);
const currentProduct = ref(null);
const categoryList = ref([]);
const coverFileList = ref([]);
const imageFileList = ref([]); // 商品图片列表
const newStock = ref(0);

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: null,
  status: null,
  hasStock: null
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
  title: '',
  subtitle: '',
  categoryId: null,
  price: null,
  stock: 0,
  detail: '',
  status: 1
});

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { max: 200, message: '商品标题长度不能超过200个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '商品价格不能小于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'blur' }
  ]
};

// 表格列定义
const columns = [
  {
    title: '商品ID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
    ellipsis: true
  },
  {
    title: '封面',
    key: 'coverImage',
    slots: { customRender: 'coverImage' },
    width: 100
  },
  {
    title: '商品标题',
    dataIndex: 'title',
    key: 'title',
    width: 200,
    ellipsis: true
  },
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'categoryName',
    width: 120
  },
  {
    title: '价格',
    key: 'price',
    slots: { customRender: 'price' },
    width: 100
  },
  {
    title: '库存',
    key: 'stock',
    slots: { customRender: 'stock' },
    width: 80
  },
  {
    title: '状态',
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
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 240,
    fixed: 'right'
  }
];

// 计算属性
const modalTitle = computed(() => {
  return isEdit.value ? '编辑商品' : '新增商品';
});

// 生命周期
onMounted(() => {
  fetchCategoryList();
  fetchProductList();
});

// 方法
const fetchCategoryList = () => {
  getEnabledCategories({
    onSuccess: (res) => {
      categoryList.value = res || [];
    },
    onError: (error) => {
      message.error('获取分类列表失败：' + error.message);
    }
  });
};

const fetchProductList = () => {
  loading.value = true;
  
  const params = {
    page: pagination.current,
    pageSize: pagination.pageSize,
    title: searchForm.title || null,
    categoryId: searchForm.categoryId,
    status: searchForm.status,
    hasStock: searchForm.hasStock
  };
  
  getProductPage(params, {
    onSuccess: (res) => {
      tableData.value = res.records || [];
      pagination.total = res.total || 0;
      pagination.current = res.current || 1;
      loading.value = false;
    },
    onError: (error) => {
      message.error('获取商品列表失败：' + error.message);
      loading.value = false;
    }
  });
};

const handleSearch = () => {
  pagination.current = 1;
  fetchProductList();
};

const handleReset = () => {
  searchForm.title = '';
  searchForm.categoryId = null;
  searchForm.status = null;
  searchForm.hasStock = null;
  pagination.current = 1;
  fetchProductList();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchProductList();
};

const showCreateModal = () => {
  isEdit.value = false;
  currentEditId.value = null;
  modalVisible.value = true;
  nextTick(() => {
    resetForm();
  });
};

const showEditModal = (record) => {
  isEdit.value = true;
  currentEditId.value = record.id;
  modalVisible.value = true;
  
  // 获取商品详情
  getProductById(record.id, {
    onSuccess: (res) => {
      nextTick(() => {
        Object.assign(formData, {
          title: res.title,
          subtitle: res.subtitle,
          categoryId: res.categoryId,
          price: res.price,
          stock: res.stock,
          detail: res.detail,
          status: res.status
        });
        
        // 设置封面图片
        if (res.coverFilePath) {
          coverFileList.value = [{
            uid: '-1',
            name: 'cover.jpg',
            status: 'done',
            url: res.coverFilePath
          }];
        } else {
          coverFileList.value = [];
        }
        
        // 加载商品图片列表
        loadProductImages(record.id);
        
        if (formRef.value) {
          formRef.value.clearValidate();
        }
      });
    },
    onError: (error) => {
      message.error('获取商品详情失败：' + error.message);
    }
  });
};

const showDetailModal = (record) => {
  getProductById(record.id, {
    onSuccess: (res) => {
      currentProduct.value = res;
      detailVisible.value = true;
    },
    onError: (error) => {
      message.error('获取商品详情失败：' + error.message);
    }
  });
};

const showStockModal = (record) => {
  currentProduct.value = record;
  newStock.value = record.stock;
  stockVisible.value = true;
};

const resetForm = () => {
  Object.assign(formData, {
    title: '',
    subtitle: '',
    categoryId: null,
    price: null,
    stock: 0,
    detail: '',
    status: 1,
    tempProductId: null
  });
  coverFileList.value = [];
  imageFileList.value = [];
  
  if (formRef.value) {
    formRef.value.resetFields();
    formRef.value.clearValidate();
  }
};

// 加载商品图片列表
const loadProductImages = (productId) => {
  getFilesByBusinessField('SHOP_PRODUCT', productId, 'images', {
    onSuccess: (res) => {
      if (res && res.length > 0) {
        imageFileList.value = res.map((file, index) => ({
          uid: `-${index + 2}`,
          name: file.originalName || `image-${index + 1}.jpg`,
          status: 'done',
          url: file.filePath,
          fileId: file.id
        }));
      } else {
        imageFileList.value = [];
      }
    },
    onError: () => {
      imageFileList.value = [];
    }
  });
};

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件！');
    return false;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB！');
    return false;
  }
  return true;
};

const handleCoverUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  
  const businessInfo = {
    businessType: 'SHOP_PRODUCT',
    businessId: currentEditId.value || useBusinessUUID().generateUUID(),
    businessField: 'cover'
  };
  
  uploadBusinessFile(file, businessInfo, false, {
    onSuccess: (res) => {
      coverFileList.value = [{
        uid: file.uid,
        name: file.name,
        status: 'done',
        url: res.filePath
      }];
      onSuccess(res, file);
      message.success('封面上传成功');
    },
    onError: (error) => {
      onError(error);
      message.error('封面上传失败：' + error.message);
    }
  });
};

const handleCoverRemove = () => {
  coverFileList.value = [];
};

const handleImageUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  
  // 确保有商品ID（新增时生成UUID，编辑时使用现有ID）
  let productId = currentEditId.value;
  if (!productId) {
    // 如果是新增商品，需要先生成一个UUID
    if (!formData.tempProductId) {
      formData.tempProductId = useBusinessUUID().generateUUID();
    }
    productId = formData.tempProductId;
  }
  
  const businessInfo = {
    businessType: 'SHOP_PRODUCT',
    businessId: productId,
    businessField: 'images' // 使用 'images' 字段区分商品图片列表
  };
  
  uploadBusinessFile(file, businessInfo, false, {
    onSuccess: (res) => {
      imageFileList.value.push({
        uid: file.uid,
        name: file.name,
        status: 'done',
        url: res.filePath,
        fileId: res.id
      });
      onSuccess(res, file);
      message.success('图片上传成功');
    },
    onError: (error) => {
      onError(error);
      message.error('图片上传失败：' + error.message);
    }
  });
};

const handleImageRemove = (file) => {
  const index = imageFileList.value.findIndex(item => item.uid === file.uid);
  if (index > -1) {
    imageFileList.value.splice(index, 1);
  }
  
  // 如果文件已经上传到服务器，可以选择删除
  // 这里暂时不删除，因为可能用户只是预览时移除，最终还是会提交
  return true;
};

const handleSubmit = () => {
  formRef.value.validate().then(() => {
    submitLoading.value = true;
    
    const params = { ...formData };
    
    const callbacks = {
      successMsg: isEdit.value ? '更新商品成功' : '创建商品成功',
      onSuccess: () => {
        submitLoading.value = false;
        modalVisible.value = false;
        fetchProductList();
        nextTick(() => {
          resetForm();
        });
      },
      onError: (error) => {
        message.error((isEdit.value ? '更新' : '创建') + '商品失败：' + error.message);
        submitLoading.value = false;
      }
    };
    
    // 根据是否编辑调用不同的API
    if (isEdit.value) {
      updateProduct(currentEditId.value, params, callbacks);
    } else {
      // 如果是新增，使用已生成的UUID（上传图片时生成）或生成新的UUID
      params.id = formData.tempProductId || useBusinessUUID().generateUUID();
      createProduct(params, callbacks);
    }
  });
};

const handleCancel = () => {
  modalVisible.value = false;
  nextTick(() => {
    resetForm();
  });
};

const handleStatusChange = (id, status) => {
  const record = tableData.value.find(item => item.id === id);
  if (record) {
    record.statusLoading = true;
  }
  
  const apiCall = status === 1 ? onShelfProduct : offShelfProduct;
  
  apiCall(id, {
    successMsg: status === 1 ? '上架商品成功' : '下架商品成功',
    onSuccess: () => {
      if (record) {
        record.status = status;
        record.statusName = status === 1 ? '上架' : '下架';
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

const handleStockSubmit = () => {
  if (newStock.value === null || newStock.value === undefined) {
    message.error('请输入库存数量');
    return;
  }
  
  stockLoading.value = true;
  
  updateProductStock(currentProduct.value.id, newStock.value, {
    successMsg: '更新库存成功',
    onSuccess: () => {
      stockLoading.value = false;
      stockVisible.value = false;
      fetchProductList();
    },
    onError: (error) => {
      message.error('更新库存失败：' + error.message);
      stockLoading.value = false;
    }
  });
};

const handleDelete = (id) => {
  deleteProduct(id, {
    successMsg: '删除商品成功',
    onSuccess: () => {
      fetchProductList();
    },
    onError: (error) => {
      message.error('删除商品失败：' + error.message);
    }
  });
};
</script>

<style scoped>
.shop-product-page {
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

/* 上传提示文字 */
.upload-tip {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.5;
}

/* 商品详情内容样式 */
.product-detail-content {
  line-height: 1.8;
  color: #262626;
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.product-detail-content :deep(h1) {
  font-size: 24px;
  font-weight: 600;
  margin: 16px 0 12px;
  color: #1a1a1a;
}

.product-detail-content :deep(h2) {
  font-size: 20px;
  font-weight: 600;
  margin: 14px 0 10px;
  color: #1a1a1a;
}

.product-detail-content :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 12px 0 8px;
  color: #1a1a1a;
}

.product-detail-content :deep(p) {
  margin: 8px 0;
}

.product-detail-content :deep(ul),
.product-detail-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.product-detail-content :deep(li) {
  margin: 4px 0;
}

.product-detail-content :deep(strong) {
  font-weight: 600;
  color: #1a1a1a;
}

.product-detail-content :deep(em) {
  font-style: italic;
}

.product-detail-content :deep(u) {
  text-decoration: underline;
}
</style>

