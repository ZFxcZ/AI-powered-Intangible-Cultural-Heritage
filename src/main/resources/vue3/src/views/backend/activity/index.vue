<template>
  <div class="activity-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>活动管理</h2>
      <a-button
        type="primary"
        @click="showCreateModal"
      >
        <template #icon>
          <i class="fas fa-plus" />
        </template>
        新增活动
      </a-button>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <a-form
        :model="searchForm"
        layout="inline"
      >
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.title"
            placeholder="请输入活动标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="展演">
              展演
            </a-select-option>
            <a-select-option value="展览">
              展览
            </a-select-option>
            <a-select-option value="培训">
              培训
            </a-select-option>
            <a-select-option value="比赛">
              比赛
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
            <a-select-option :value="0">
              草稿
            </a-select-option>
            <a-select-option :value="1">
              报名中
            </a-select-option>
            <a-select-option :value="2">
              进行中
            </a-select-option>
            <a-select-option :value="3">
              已结束
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              @click="handleSearch"
            >
              <template #icon>
                <i class="fas fa-search" />
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <i class="fas fa-redo" />
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
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'coverFileId'">
            <img
              v-if="record.coverFilePath"
              :src="getFullImagePath(record.coverFilePath)"
              alt="封面"
              style="
               width: 60px;
                height: 60px;
                object-fit: cover;
                border-radius: 4px;
              "
            />
            <span v-else>暂无封面</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'time'">
            <div>
              <div>开始: {{ formatDate(record.startTime) }}</div>
              <div>结束: {{ formatDate(record.endTime) }}</div>
            </div>
               
               
               
              
          </template>

          <template v-e
               lse-if="colu
               mn.key === 'a
               ction'">
              
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleViewSignups(record)"
              >
                报名管理
              </a-button>
              <a-button t
                 ype="link" s
                 ize="sm
                a ll" @click="
                handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除此活动吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small"> 删除 </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 创建/编辑活动弹窗 -->
    <a-modal
      v-model:open="isModalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleModalOk"
      @cancel="handl
e         ModalCancel"
         
        
    >
           
           
         
      <a-form
        :model="formData"
        :label-col="
{          span: 4 }"
         
        
        :wrapper-co
l           ="{ span: 20 }"
           
          
      >
              
            
        <a-form-item label="活动标题" requir
              ed
            >
          <a-input
              
            
            v-model:value="formData.titl
              e"
            
            placeholder="请输入活动标题"
          />
        </a-form-item>

         
         
        
        <a-form-item label="活动类型" required>
          <a-select v-model:value="formData.type" placeholder="请选择活动类型">
            <a-select-option value="展演">展演</a-select-option>
            <a-select-option value="展览">展览</a-select-option>
            <a-select-option value="培训">培训</a-select-option>
            <a-select-option value="比赛">比赛</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-it
e           m label="活动时间" required>
           
         
          <a-range-picker
            v-model:value="timeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="活动地点">
          <a-input
            v-model:value="formData.location"
            placeholder="请输入活动地点"
          />
        </a-form-item>

        <a-form-item label="活动描述">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入活动描述"
            :rows="4"
          />
        </a-form-item> 

                
              
        <a-form-item label="活动封面">
          <a-upload
            v-model:file-list="coverFileList"
            :before-upload="handleCoverBeforeUpload"
            :remove="handleRemoveCover"
            accept="image/*"
            :max-count="1"
            list-type="picture-card"
            :show-u
p           load-list="{ showPreviewIcon: f
a           lse }"
          
          >
              
            
            <div v-if="coverFileList.len
              gth
             < 1">
              <i class="fas fa-upload"><
              /i>
            
              <div style="margin-top: 8p
              x">
            上传封面</div>
            </div>
          </a-upload>
          <div style="color: #999; font-size: 12px; margin-top: 8px">
            支持JPG、PNG格式，建议尺寸1200x400像素，文件大小不超过5MB
          </div>
        </a-form-item>

        <a-form-item label="活动状态">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">报名中</a-select-option>
            <a-select-option :value="2">进行中</a-select-option>
            <a-select-option :value="3">已结束</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>"
        row-key="id
弹
    <a-modal
      v-model:open="isSignupModalVisible"
      title="报名管理"
      width="900px"
      :footer="null"
    >
      <a-table
        :columns="signupColumns"
        :data-source="signupData"
        :loading="signupLoading"
        :pagination="signupPagination"
        @change="handleSignupTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getSignupStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status === 0"
                type="link"
                size="small"
                @click="handleApproveSignup(record.id)"
              >
                通过
              </a-button>
              <a-button
                v-if="record.status === 0"
                type="link"
                danger
                size="small"
                @click="handleRejectSignup(record.id)"
              >
                拒绝
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleCheckIn(record.id)"
              >
                签到
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { generateUUID } from "@/composables/useBusinessUUID";
import {
  getActivityPage,
  createActivity,
  updateActivity,
  deleteActivity,
  getActivitySignups,
  approveSignup,
  rejectSignup,
  checkInSignup,
} from "@/api/ActivityApi";
import { uploadBusinessFile, deleteFile } from "@/api/FileApi";

// 搜索表单
const searchForm = reactive({
  title: "",
  type: undefined,
  status: undefined,
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条数据`,
});

// 表格列定义
const columns = [
  { title: "活动ID", dataIndex: "id", key: "id", width: 180 },
  { title: "标题", dataIndex: "title", key: "title" },
  { title: "类型", dataIndex: "type", key: "type", width: 100 },
  { title: "封面", key: "coverFileId", width: 100 },
  { title: "活动时间", key: "time", width: 180 },
  { title: "地点", dataIndex: "location", key: "location" },
  { title: "状态", key: "status", width: 100 },
  { title: "操作", key: "action", fixed: "right", width: 220 },
];

// 弹窗相关
const isModalVisible = ref(false);
const modalTitle = ref("新增活动");
const isEdit = ref(false);
const formData = reactive({
  id: "",
  title: "",
  type: "",
  location: "",
  description: "",
  status: 0,
  coverFileId: null,
});
const timeRange = ref([]);

// 封面上传相关
const coverFileList = ref([]);
const uploadingCover = ref(false);

// 报名管理相关
const isSignupModalVisible = ref(false);
const signupData = ref([]);
const signupLoading = ref(false);
const signupPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});
const currentActivityId = ref(null);

const signupColumns = [
  { title: "报名ID", dataIndex: "id", key: "id", width: 100 },
  { title: "活动标题", dataIndex: "activityTitle", key: "activityTitle" },
  { title: "用户名", dataIndex: "username", key: "username", width: 150 },
  { title: "状态", key: "status", width: 100 },
  { title: "报名时间", dataIndex: "createTime", key: "createTime", width: 180 },
  { title: "操作", key: "action", width: 200 },
];

// 加载数据
const loadData = () => {
  loading.value = true;
  getActivityPage(
    {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm,
    },
    {
      onSuccess: (data) => {
        tableData.value = data.records || [];
        pagination.total = data.total || 0;
        loading.value = false;
      },
      onError: () => {
        loading.value = false;
      },
    },
  );
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

// 重置
const handleReset = () => {
  searchForm.title = "";
  searchForm.type = undefined;
  searchForm.status = undefined;
  pagination.current = 1;
  loadData();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false;
  modalTitle.value = "新增活动";
  resetForm();
  isModalVisible.value = true;
};

// 编辑
const handleEdit = (record) => {
  isEdit.value = true;
  modalTitle.value = "编辑活动";
  Object.assign(formData, record);
  if (record.startTime && record.endTime) {
    timeRange.value = [dayjs(record.startTime), dayjs(record.endTime)];
  }
  // 加载活动封面
  loadActivityCover(record);
  isModalVisible.value = true;
};

// 弹窗确认
const handleModalOk = () => {
  if (!formData.title || !formData.type) {
    message.error("请填写必填项");
    return;
  }

  if (timeRange.value && timeRange.value.length === 2) {
    formData.startTime = timeRange.value[0].format("YYYY-MM-DD HH:mm:ss");
    formData.endTime = timeRange.value[1].format("YYYY-MM-DD HH:mm:ss");
  }

  if (isEdit.value) {
    updateActivity(
      { activityId: formData.id, ...formData },
      {
        onSuccess: () => {
          message.success("更新成功");
          isModalVisible.value = false;
          loadData();
        },
        successMsg: false,
      },
    );
  } else {
    formData.id = generateUUID();
    createActivity(formData, {
      onSuccess: () => {
        message.success("创建成功");
        isModalVisible.value = false;
        loadData();
      },
      successMsg: false,
    });
  }
};

// 弹窗取消
const handleModalCancel = () => {
  isModalVisible.value = false;
  resetForm();
};

// 重置表单
const resetForm = () => {
  formData.id = "";
  formData.title = "";
  formData.type = "";
  formData.location = "";
  formData.description = "";
  formData.status = 0;
  formData.coverFileId = null;
  timeRange.value = [];
  coverFileList.value = [];
};

// 加载活动封面
const loadActivityCover = (activity) => {
  coverFileList.value = [];
  if (activity.coverFileId) {
    // 如果有封面文件ID，构造文件信息用于显示
    coverFileList.value = [
      {
        uid: activity.coverFileId,
        name: "活动封面",
        status: "done",
        url: activity.coverFilePath || "",
        fileId: activity.coverFileId,
      },
    ];
  }
};

// 封面上传前处理
const handleCoverBeforeUpload = async (file) => {
  // 文件大小验证（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error("封面文件大小不能超过 5MB!");
    return false;
  }

  // 文件类型验证
  const isImage = file.type.startsWith("image/");
  if (!isImage) {
    message.error("只能上传图片文件!");
    return false;
  }

  try {
    uploadingCover.value = true;

    // 生成临时文件ID用于显示上传状态
    const tempFileId = Date.now();

    // 添加上传中的文件到列表
    coverFileList.value = [
      {
        uid: tempFileId,
        name: file.name,
        status: "uploading",
        percent: 0,
      },
    ];

    // 如果是新建活动，需要先生成UUID
    if (!formData.id) {
      formData.id = generateUUID();
    }

    // 上传封面文件
    await uploadBusinessFile(
      file,
      {
        businessType: "ACTIVITY",
        businessId: formData.id,
        businessField: "cover",
      },
      false,
      {
        onSuccess: (data) => {
          // 移除上传中的临时文件
          coverFileList.value = [];

          // 添加上传成功的文件
          coverFileList.value = [
            {
              uid: data.id,
              name: data.originalName || file.name,
              status: "done",
              url: data.filePath,
              fileId: data.id,
            },
          ];

          // 更新表单数据
          formData.coverFileId = data.id;
          message.success("封面上传成功");
          uploadingCover.value = false;
        },
        onError: () => {
          // 移除上传失败的文件
          coverFileList.value = [];
          message.error("封面上传失败");
          uploadingCover.value = false;
        },
        successMsg: false,
      },
    );
  } catch (error) {
    console.error("上传失败:", error);
    coverFileList.value = [];
    uploadingCover.value = false;
  }

  return false; // 阻止默认上传行为
};

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
              coverFileList.value = [];
              formData.coverFileId = null;
              message.success("封面删除成功");
              resolve(true); // 允许Ant Design移除
            },
            onError: () => {
              message.error("删除失败");
              reject(false); // 阻止Ant Design移除
            },
            successMsg: false,
          },
        );
      } else {
        // 没有fileId（可能是上传中或上传失败的文件），直接移除
        coverFileList.value = [];
        formData.coverFileId = null;
        message.success("文件移除成功");
        resolve(true); // 允许Ant Design移除
      }
    } catch (error) {
      console.error("删除封面异常:", error);
      message.error("删除失败");
      reject(false); // 阻止Ant Design移除
    }
  });
};

// 删除
const handleDelete = (id) => {
  deleteActivity(
    { activityId: id },
    {
      onSuccess: () => {
        message.success("删除成功");
        loadData();
      },
      successMsg: false,
    },
  );
};

// 查看报名
const handleViewSignups = (record) => {
  currentActivityId.value = record.id;
  isSignupModalVisible.value = true;
  loadSignups();
};

// 加载报名数据
const loadSignups = () => {
  signupLoading.value = true;
  getActivitySignups(
    {
      activityId: currentActivityId.value,
      current: signupPagination.current,
      size: signupPagination.pageSize,
    },
    {
      onSuccess: (data) => {
        signupData.value = data.records || [];
        signupPagination.total = data.total || 0;
        signupLoading.value = false;
      },
      onError: () => {
        signupLoading.value = false;
      },
    },
  );
};

// 报名表格变化
const handleSignupTableChange = (pag) => {
  signupPagination.current = pag.current;
  signupPagination.pageSize = pag.pageSize;
  loadSignups();
};

// 审核通过
const handleApproveSignup = (id) => {
  approveSignup(
    { signupId: id },
    {
      onSuccess: () => {
        message.success("审核通过");
        loadSignups();
      },
      successMsg: false,
    },
  );
};

// 审核拒绝
const handleRejectSignup = (id) => {
  rejectSignup(
    { signupId: id },
    {
      onSuccess: () => {
        message.success("已拒绝");
        loadSignups();
      },
      successMsg: false,
    },
  );
};

// 签到
const handleCheckIn = (id) => {
  checkInSignup(
    { signupId: id },
    {
      onSuccess: () => {
        message.success("签到成功");
        loadSignups();
      },
      successMsg: false,
    },
  );
};

// 工具函数
const getStatusColor = (status) => {
  const colors = {
    0: "default",
    1: "blue",
    2: "green",
    3: "gray",
  };
  return colors[status] || "default";
};

const getSignupStatusColor = (status) => {
  const colors = {
    0: "orange",
    1: "green",
    2: "red",
    3: "blue",
  };
  return colors[status] || "default";
};

const formatDate = (dateStr) => {
  return dateStr ? dayjs(dateStr).format("YYYY-MM-DD HH:mm") : "-";
};

const getFullImagePath = (path) => {
  if (!path) return "";
  if (path.startsWith("http")) return path;
  // 静态资源直接访问，不需要API前缀
  return path.startsWith("/") ? path : "/" + path;
};

// 页面加载时获取数据
onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.activity-management {
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
</style>
