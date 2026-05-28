<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <a-tabs v-model:activeKey="activeTab">
        <!-- 基本信息 Tab -->
        <a-tab-pane key="basic" tab="基本信息">
          <div class="profile-info">
            <div class="avatar-container">
              <a-avatar :size="100" :src="avatarUrl">
                {{ userForm.name?.charAt(0) || 'U' }}
              </a-avatar>
              <a-upload
                class="avatar-uploader"
                :action="uploadAction"
                :show-upload-list="false"
                :custom-request="customUploadAvatar"
                :before-upload="beforeAvatarUpload"
              >
                <a-button size="small" type="primary">更换头像</a-button>
              </a-upload>
            </div>

            <div class="info-form">
              <a-form
                ref="userFormRef"
                :model="userForm"
                :rules="rules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-form-item label="用户名" name="username">
                  <a-input v-model:value="userForm.username" disabled />
                </a-form-item>

                <a-form-item label="姓名" name="name">
                  <a-input v-model:value="userForm.name" />
                </a-form-item>

                <a-form-item label="性别" name="sex">
                  <a-radio-group v-model:value="userForm.sex">
                    <a-radio value="男">男</a-radio>
                    <a-radio value="女">女</a-radio>
                  </a-radio-group>
                </a-form-item>

                <a-form-item label="电子邮箱" name="email">
                  <a-input v-model:value="userForm.email" />
                </a-form-item>

                <a-form-item label="手机号码" name="phone">
                  <a-input v-model:value="userForm.phone" />
                </a-form-item>

                <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
                  <a-button type="primary" @click="submitUserInfo">
                    保存修改
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <!-- 我的作品 Tab -->
        <a-tab-pane key="heritage" tab="我的作品">
          <div class="heritage-section">
            <!-- 操作栏 -->
            <div class="heritage-actions">
              <a-space>
                <a-button type="primary" @click="showCreateModal = true">
                  <i class="fas fa-plus"></i>
                  发布作品
                </a-button>
                <a-select
                  v-model:value="heritageFilter.status"
                  placeholder="筛选状态"
                  style="width: 120px"
                  allow-clear
                  @change="fetchMyHeritage"
                >
                  <a-select-option :value="0">草稿</a-select-option>
                  <a-select-option :value="1">待审</a-select-option>
                  <a-select-option :value="2">已发布</a-select-option>
                  <a-select-option :value="3">下架</a-select-option>
                </a-select>
                <a-input-search
                  v-model:value="heritageFilter.keyword"
                  placeholder="搜索我的作品"
                  style="width: 200px"
                  @search="fetchMyHeritage"
                />
              </a-space>
            </div>

            <!-- 作品列表 -->
            <div class="heritage-list">
              <a-spin :spinning="heritageLoading">
                <div v-if="myHeritageList.length > 0" class="heritage-grid">
                  <div
                    v-for="item in myHeritageList"
                    :key="item.id"
                    class="heritage-item"
                  >
                    <div class="item-cover">
                      <img
                        v-if="getItemCoverUrl(item)"
                        :src="getItemCoverUrl(item)"
                        :alt="item.title"
                        @error="handleImageError"
                      />
                      <div v-else class="no-image">
                        <i class="fas fa-image"></i>
                      </div>
                      <div class="item-status">
                        <HeritageStatusTag
                          :status="item.status"
                          :status-name="item.statusName"
                        />
                      </div>
                    </div>
                    <div class="item-content">
                      <h4 class="item-title" :title="item.title">{{ item.title }}</h4>
                      <p class="item-category">{{ item.category }}</p>
                      <p class="item-time">{{ formatDate(item.createTime) }}</p>
                      <div class="item-actions">
                        <a-space>
                          <a-button size="small" @click="handleViewItem(item)">
                            查看
                          </a-button>
                          <a-button
                            size="small"
                            type="primary"
                            @click="handleEditItem(item)"
                            v-if="canEdit(item)"
                          >
                            编辑
                          </a-button>
                          <a-popconfirm
                            title="确定要删除这个作品吗？"
                            @confirm="handleDeleteItem(item)"
                            v-if="canDelete(item)"
                          >
                            <a-button size="small" danger>删除</a-button>
                          </a-popconfirm>
                        </a-space>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-heritage">
                  <a-empty description="还没有发布任何作品">
                    <a-button type="primary" @click="showCreateModal = true">
                      立即发布
                    </a-button>
                  </a-empty>
                </div>
              </a-spin>
            </div>

            <!-- 分页 -->
            <div v-if="heritageTotal > 0" class="heritage-pagination">
              <a-pagination
                v-model:current="heritageFilter.currentPage"
                v-model:pageSize="heritageFilter.size"
                :total="heritageTotal"
                show-size-changer
                show-total
                @change="fetchMyHeritage"
                @showSizeChange="fetchMyHeritage"
              />
            </div>
          </div>
        </a-tab-pane>

        <!-- 收货地址 Tab -->
        <a-tab-pane key="address" tab="收货地址">
          <div class="address-section">
            <!-- 地址列表 -->
            <div class="address-list">
              <a-card
                v-for="address in addressList"
                :key="address.id"
                class="address-card"
                :class="{ 'default-address': address.isDefault }"
              >
                <div class="address-header">
                  <div class="address-info">
                    <span class="receiver">{{ address.receiver }}</span>
                    <span class="phone">{{ address.phone }}</span>
                    <a-tag v-if="address.isDefault" color="green">默认地址</a-tag>
                  </div>
                  <div class="address-actions">
                    <a-button type="link" size="small" @click="handleEditAddress(address)">编辑</a-button>
                    <a-button type="link" size="small" danger @click="handleDeleteAddress(address.id)">删除</a-button>
                    <a-button
                      v-if="!address.isDefault"
                      type="link"
                      size="small"
                      @click="handleSetDefaultAddress(address.id)"
                    >
                      设为默认
                    </a-button>
                  </div>
                </div>
                <div class="address-detail">
                  {{ address.fullAddress }}
                </div>
              </a-card>
              
              <!-- 添加地址按钮 -->
              <a-card class="add-address-card" @click="handleAddAddress">
                <div class="add-address-content">
                  <i class="fa fa-plus-circle"></i>
                  <span>添加新地址</span>
                </div>
              </a-card>
            </div>
          </div>
        </a-tab-pane>

        <!-- 修改密码 Tab -->
        <a-tab-pane key="password" tab="修改密码">
          <a-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 18 }"
            style="max-width: 500px; margin: 0 auto"
          >
            <a-form-item label="旧密码" name="oldPassword">
              <a-input-password
                v-model:value="passwordForm.oldPassword"
                placeholder="请输入旧密码"
              />
            </a-form-item>

            <a-form-item label="新密码" name="newPassword">
              <a-input-password
                v-model:value="passwordForm.newPassword"
                placeholder="请输入新密码"
              />
            </a-form-item>

            <a-form-item label="确认新密码" name="confirmPassword">
              <a-input-password
                v-model:value="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
              <a-button type="primary" @click="submitPassword">
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 创建作品模态框 -->
    <HeritageItemCreate
      v-model:open="showCreateModal"
      mode="user"
      @success="handleCreateSuccess"
    />

    <!-- 编辑作品模态框 -->
    <HeritageItemEdit
      v-model:open="showEditModal"
      :item-id="currentEditItemId"
      mode="user"
      @success="handleEditSuccess"
    />

    <!-- 地址编辑对话框 -->
    <a-modal
      v-model:open="addressModalVisible"
      :title="isEditAddress ? '编辑地址' : '添加地址'"
      @ok="handleSubmitAddress"
      @cancel="handleCancelAddress"
      width="600px"
    >
      <a-form
        ref="addressFormRef"
        :model="addressFormData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item
          label="收货人"
          name="receiver"
          :rules="[{ required: true, message: '请输入收货人姓名' }]"
        >
          <a-input v-model:value="addressFormData.receiver" placeholder="请输入收货人姓名" />
        </a-form-item>

        <a-form-item
          label="手机号"
          name="phone"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
          ]"
        >
          <a-input v-model:value="addressFormData.phone" placeholder="请输入手机号" />
        </a-form-item>

        <a-form-item
          label="省"
          name="province"
          :rules="[{ required: true, message: '请输入省份' }]"
        >
          <a-input v-model:value="addressFormData.province" placeholder="请输入省份" />
        </a-form-item>

        <a-form-item
          label="市"
          name="city"
          :rules="[{ required: true, message: '请输入城市' }]"
        >
          <a-input v-model:value="addressFormData.city" placeholder="请输入城市" />
        </a-form-item>

        <a-form-item
          label="区/县"
          name="district"
          :rules="[{ required: true, message: '请输入区/县' }]"
        >
          <a-input v-model:value="addressFormData.district" placeholder="请输入区/县" />
        </a-form-item>

        <a-form-item
          label="详细地址"
          name="detail"
          :rules="[{ required: true, message: '请输入详细地址' }]"
        >
          <a-textarea
            v-model:value="addressFormData.detail"
            placeholder="请输入详细地址"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="设为默认" name="isDefault">
          <a-switch v-model:checked="addressFormData.isDefault" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { message, Modal } from "ant-design-vue";
import { useUserStore } from "@/store/user";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { getCurrentUser, updateUser, updatePassword } from '@/api/user';
import { 
  getHeritageItemPage, 
  deleteHeritageItem 
} from '@/api/HeritageApi';
import {
  getUserAddressList,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/AddressApi';
import { formatLocalDate } from '@/utils/dateUtils';
import HeritageItemCreate from '@/components/common/HeritageItemCreate.vue';
import HeritageItemEdit from '@/components/common/HeritageItemEdit.vue';
import HeritageStatusTag from '@/components/common/HeritageStatusTag.vue';

const uploadAction = "#";
const userStore = useUserStore();
const router = useRouter();
const activeTab = ref("basic");

// ========== 我的作品相关数据 ==========
const showCreateModal = ref(false);
const showEditModal = ref(false);
const currentEditItemId = ref(null);
const heritageLoading = ref(false);
const myHeritageList = ref([]);
const heritageTotal = ref(0);

const heritageFilter = reactive({
  currentPage: 1,
  size: 12,
  status: null,
  keyword: '',
  creatorId: null // 将在获取用户信息后设置
});

// ========== 收货地址相关数据 ==========
const addressList = ref([]);
const addressModalVisible = ref(false);
const isEditAddress = ref(false);
const addressFormRef = ref(null);
const addressFormData = reactive({
  id: null,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
});

// 表单引用
const userFormRef = ref(null);
const passwordFormRef = ref(null);

// 用户表单数据
const userForm = reactive({
  id: "",
  username: "",
  name: "",
  email: "",
  phone: "",
  sex: "",
  avatar: "",
});

// 头像地址
const avatarUrl = computed(() => {
  return userForm.avatar;
});

// 密码表单数据
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  phone: [
    { required: false, trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: ["blur", "change"],
    },
  ],
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: ["blur", "change"],
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 从后端获取最新用户信息
    const data = await getCurrentUser();
    
    // 更新store
    userStore.updateUserInfo(data);
    
    // 更新表单数据
    userForm.id = data.id || "";
    userForm.username = data.username || "";
    userForm.name = data.nickname || data.name || "";
    userForm.email = data.email || "";
    userForm.phone = data.phone || "";
    userForm.sex = data.sex || "男";
    userForm.avatar = data.avatar || "";

    // 设置作品筛选的创建人ID
    heritageFilter.creatorId = data.id;

    console.log("用户信息加载成功:", userForm);
  } catch (error) {
    console.error("获取用户信息失败", error);
    message.error("获取用户信息失败");
  }
};

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    message.error("头像只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    message.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义头像上传方法（使用策略C：直接业务绑定上传）
const customUploadAvatar = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);
    formData.append("businessType", "USER_AVATAR");
    formData.append("businessId", userForm.id.toString());
    formData.append("businessField", "avatar");
    formData.append("replaceOld", "true"); // 替换旧头像

    // 发送上传请求
    const response = await request.post("/file/upload", formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${userStore.token}`,
      }
    });

    // 更新用户头像路径
    const avatarPath = response.filePath || response.path;
    userForm.avatar = avatarPath;

    // 更新用户信息到后端
    await updateUserAvatar(avatarPath);

    // 通知上传成功
    options.onSuccess(response);
    message.success("头像上传成功");
  } catch (error) {
    options.onError(error);
    console.error("头像上传过程发生错误:", error);
    message.error("头像上传失败: " + (error.message || "未知错误"));
  }
};

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    // 更新用户信息到后端
    const response = await updateUser(userForm.id, { avatar: avatarPath });
    
    // 更新本地store
    userStore.updateUserInfo(response);
    
    console.log("头像信息已保存到后端");
  } catch (error) {
    console.error("头像信息保存失败", error);
    message.error("头像信息保存失败");
    throw error;
  }
};

// 提交用户信息更新
const submitUserInfo = async () => {
  if (!userFormRef.value) return;

  try {
    // 表单验证
    await userFormRef.value.validate();

    // 更新用户信息到后端
    const response = await updateUser(userForm.id, {
      name: userForm.name,
      email: userForm.email,
      phone: userForm.phone,
      sex: userForm.sex,
    });
    
    // 更新本地store
    userStore.updateUserInfo(response);
    
    message.success("个人信息更新成功!");

  } catch (error) {
    console.error("保存个人信息失败", error);
    message.error("保存个人信息失败: " + (error.message || "未知错误"));
  }
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    // 表单验证
    await passwordFormRef.value.validate();

    // 调用后端修改密码接口
    await updatePassword(userForm.id, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    }, {
      successMsg: "密码修改成功，即将跳转到登录页",
      showDefaultMsg: true
    });
    
    // 清空表单
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";

    // 显示提示信息（不可取消）
    Modal.info({
      title: "密码修改成功",
      content: "为了您的账户安全，请重新登录",
      okText: "确定",
      onOk: async () => {
        // 清除用户信息并跳转到登录页
        await userStore.logout();
        window.location.href = "/auth/login";
      }
    });

    // 3秒后自动跳转（即使用户不点击确定按钮）
    setTimeout(async () => {
      await userStore.logout();
      window.location.href = "/auth/login";
    }, 3000);

  } catch (error) {
    console.error("密码修改失败", error);
    if (!error.errorFields) {
      message.error(error.message || "密码修改失败");
    }
  }
};

// 监听用户表单数据变化
watch(
  userForm,
  (newVal) => {
    console.log("用户表单数据变化:", newVal);
  },
  { deep: true }
);

// ========== 我的作品相关方法 ==========

/**
 * 获取我的作品列表
 */
const fetchMyHeritage = () => {
  if (!heritageFilter.creatorId) {
    console.log('用户ID未设置，跳过获取作品列表');
    return;
  }
  
  heritageLoading.value = true;
  
  const params = {
    currentPage: heritageFilter.currentPage,
    size: heritageFilter.size,
    creatorId: heritageFilter.creatorId,
    title: heritageFilter.keyword || undefined,
    status: heritageFilter.status !== null ? heritageFilter.status : undefined,
    orderBy: 'create_time',
    orderDirection: 'desc'
  };
  
  getHeritageItemPage(params, {
    onSuccess: (res) => {
      myHeritageList.value = res.records || [];
      heritageTotal.value = res.total || 0;
      heritageLoading.value = false;
      console.log(`获取到 ${myHeritageList.value.length} 个作品`);
    },
    onError: (error) => {
      message.error('获取作品列表失败');
      heritageLoading.value = false;
    }
  });
};

/**
 * 格式化日期显示
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    return formatLocalDate(new Date(dateStr));
  } catch (error) {
    return '';
  }
};

/**
 * 获取作品封面URL
 */
const getItemCoverUrl = (item) => {
  // 优先使用 coverImage（完整路径）
  if (item.coverImage) {
    return item.coverImage;
  }
  // 其次使用 coverFileId
  if (item.coverFileId) {
    return `/api/file/preview/${item.coverFileId}`;
  }
  // 再尝试使用第一个媒体文件
  if (item.mediaList && item.mediaList.length > 0) {
    return item.mediaList[0].filePath;
  }
  // 没有图片返回 null
  return null;
};

/**
 * 处理图片加载错误
 */
const handleImageError = (event) => {
  event.target.style.display = 'none';
  const noImageDiv = event.target.nextElementSibling;
  if (noImageDiv && noImageDiv.classList.contains('no-image')) {
    noImageDiv.style.display = 'flex';
  }
};

/**
 * 判断是否可以编辑
 */
const canEdit = (item) => {
  // 草稿和待审状态可以编辑
  return item.status === 0 || item.status === 1;
};

/**
 * 判断是否可以删除
 */
const canDelete = (item) => {
  // 草稿和下架状态可以删除
  return item.status === 0 || item.status === 3;
};

/**
 * 查看作品详情
 */
const handleViewItem = (item) => {
  router.push(`/heritage/${item.id}`);
};

/**
 * 编辑作品
 */
const handleEditItem = (item) => {
  currentEditItemId.value = item.id;
  showEditModal.value = true;
};

/**
 * 删除作品
 */
const handleDeleteItem = (item) => {
  deleteHeritageItem({ itemId: item.id }, {
    successMsg: '作品删除成功',
    onSuccess: () => {
      fetchMyHeritage(); // 重新加载列表
    }
  });
};

/**
 * 创建成功回调
 */
const handleCreateSuccess = () => {
  fetchMyHeritage(); // 重新加载列表
};

/**
 * 编辑成功回调
 */
const handleEditSuccess = () => {
  fetchMyHeritage(); // 重新加载列表
};

/**
 * 监听activeTab变化，切换到我的作品时加载数据
 */
watch(activeTab, (newTab) => {
  if (newTab === 'heritage' && heritageFilter.creatorId) {
    fetchMyHeritage();
  } else if (newTab === 'address') {
    loadAddressList();
  }
});

// ========== 收货地址相关方法 ==========

/**
 * 加载地址列表
 */
const loadAddressList = () => {
  getUserAddressList({
    onSuccess: (res) => {
      addressList.value = res || [];
    }
  });
};

/**
 * 添加地址
 */
const handleAddAddress = () => {
  isEditAddress.value = false;
  resetAddressForm();
  addressModalVisible.value = true;
};

/**
 * 编辑地址
 */
const handleEditAddress = (address) => {
  isEditAddress.value = true;
  addressFormData.id = address.id;
  addressFormData.receiver = address.receiver;
  addressFormData.phone = address.phone;
  addressFormData.province = address.province;
  addressFormData.city = address.city;
  addressFormData.district = address.district;
  addressFormData.detail = address.detail;
  addressFormData.isDefault = address.isDefault;
  addressModalVisible.value = true;
};

/**
 * 删除地址
 */
const handleDeleteAddress = (addressId) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该收货地址吗？',
    onOk: () => {
      deleteAddress(addressId, {
        successMsg: '删除成功',
        onSuccess: () => {
          loadAddressList();
        }
      });
    }
  });
};

/**
 * 设置默认地址
 */
const handleSetDefaultAddress = (addressId) => {
  setDefaultAddress(addressId, {
    successMsg: '设置成功',
    onSuccess: () => {
      loadAddressList();
    }
  });
};

/**
 * 提交地址表单
 */
const handleSubmitAddress = () => {
  addressFormRef.value.validate().then(() => {
    const params = {
      receiver: addressFormData.receiver,
      phone: addressFormData.phone,
      province: addressFormData.province,
      city: addressFormData.city,
      district: addressFormData.district,
      detail: addressFormData.detail,
      isDefault: addressFormData.isDefault
    };

    if (isEditAddress.value) {
      updateAddress(addressFormData.id, params, {
        successMsg: '更新成功',
        onSuccess: () => {
          addressModalVisible.value = false;
          loadAddressList();
        }
      });
    } else {
      createAddress(params, {
        successMsg: '添加成功',
        onSuccess: () => {
          addressModalVisible.value = false;
          loadAddressList();
        }
      });
    }
  });
};

/**
 * 取消地址编辑
 */
const handleCancelAddress = () => {
  addressModalVisible.value = false;
  resetAddressForm();
};

/**
 * 重置地址表单
 */
const resetAddressForm = () => {
  addressFormData.id = null;
  addressFormData.receiver = '';
  addressFormData.phone = '';
  addressFormData.province = '';
  addressFormData.city = '';
  addressFormData.district = '';
  addressFormData.detail = '';
  addressFormData.isDefault = false;
  if (addressFormRef.value) {
    addressFormRef.value.resetFields();
  }
};

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
  
  // 检查URL参数，如果有tab参数则切换到对应tab
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');
  if (tabParam && ['basic', 'heritage', 'address', 'password'].includes(tabParam)) {
    activeTab.value = tabParam;
  }
});
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
  padding: 32px 20px;
}

.profile-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(139, 69, 19, 0.12);
  border: 1px solid #e8e8e8;

  :deep(.ant-card-head) {
    background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
    border-bottom: 2px solid rgba(197, 165, 114, 0.3);
  }

  :deep(.ant-card-head-title) {
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    font-family: 'SimSun', '宋体', serif;
    letter-spacing: 3px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #fff;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

@media (min-width: 768px) {
  .profile-info {
    flex-direction: row;
  }
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: rgba(139, 69, 19, 0.03);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.info-form {
  flex: 1;
}

.info-form :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.ant-tabs {
  margin-top: 0;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 28px;
}

:deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #666;

  &:hover {
    color: #8b4513;
  }
}

:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #8b4513;
}

:deep(.ant-tabs-ink-bar) {
  background: #8b4513;
}

/* ========== 我的作品样式 ========== */
.heritage-section {
  padding: 24px 0;
}

.heritage-actions {
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(139, 69, 19, 0.03);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.heritage-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }

  i {
    margin-right: 6px;
  }
}

.heritage-list {
  min-height: 400px;
}

.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.heritage-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
}

.heritage-item:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
  border-color: #c5a572;
}

.item-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #faf8f3;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.heritage-item:hover .item-cover img {
  transform: scale(1.08);
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
  font-size: 56px;
}

.item-status {
  position: absolute;
  top: 12px;
  right: 12px;
}

.item-content {
  padding: 18px;
  background: rgba(139, 69, 19, 0.02);
}

.item-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #2c2c2c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.item-category {
  font-size: 14px;
  color: #666;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.item-time {
  font-size: 13px;
  color: #999;
  margin: 0 0 14px 0;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.item-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.empty-heritage {
  text-align: center;
  padding: 80px 20px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 1px dashed #e8e8e8;
}

.empty-heritage :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.heritage-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 24px 0;
}

.heritage-pagination :deep(.ant-pagination-item-active) {
  border-color: #8b4513;
  background: #8b4513;
}

.heritage-pagination :deep(.ant-pagination-item-active a) {
  color: #fff;
}

/* ========== 收货地址样式 ========== */
.address-section {
  padding: 24px 0;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.address-card {
  cursor: default;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.address-card :deep(.ant-card-body) {
  padding: 16px;
}

.address-card:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
}

.default-address {
  border-color: #8b4513;
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.02) 0%, rgba(197, 165, 114, 0.04) 100%);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.receiver {
  font-weight: 600;
  font-size: 16px;
  color: #2c2c2c;
  letter-spacing: 0.5px;
}

.phone {
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 6px;
}

.address-actions :deep(.ant-btn-link) {
  color: #8b4513;

  &:hover {
    color: #a0522d;
  }

  &.ant-btn-dangerous {
    color: #c5322d;

    &:hover {
      color: #ff4d4f;
    }
  }
}

.address-detail {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

.add-address-card {
  cursor: pointer;
  border: 2px dashed #c5a572;
  transition: all 0.3s;
  background: rgba(139, 69, 19, 0.02);
}

.add-address-card :deep(.ant-card-body) {
  padding: 32px 16px;
}

.add-address-card:hover {
  border-color: #8b4513;
  background: rgba(139, 69, 19, 0.06);
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
}

.add-address-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8b4513;
}

.add-address-content i {
  font-size: 40px;
  margin-bottom: 10px;
}

.add-address-content span {
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .heritage-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .heritage-actions {
    padding: 12px;
  }
  
  .heritage-actions :deep(.ant-space) {
    flex-direction: column;
    width: 100%;
  }
  
  .heritage-actions :deep(.ant-space-item) {
    width: 100%;
  }
  
  .heritage-actions :deep(.ant-btn),
  .heritage-actions :deep(.ant-select),
  .heritage-actions :deep(.ant-input-search) {
    width: 100% !important;
  }

  .address-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style> 