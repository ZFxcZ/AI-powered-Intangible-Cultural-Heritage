<template>
  <div class="order-manage-container">
    <h2>订单管理</h2>

    <!-- 查询条件 -->
    <a-card class="search-card">
      <a-form layout="inline">
        <a-form-item label="订单状态">
          <a-select v-model:value="searchParams.status" style="width: 150px;" placeholder="全部状态">
            <a-select-option :value="null">全部状态</a-select-option>
            <a-select-option :value="0">待支付</a-select-option>
            <a-select-option :value="1">已支付</a-select-option>
            <a-select-option :value="2">已发货</a-select-option>
            <a-select-option :value="3">已完成</a-select-option>
            <a-select-option :value="4">已关闭</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="订单号">
          <a-input v-model:value="searchParams.orderNo" placeholder="请输入订单号" style="width: 200px;" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px;" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 订单列表 -->
    <a-card class="table-card">
      <a-table
        :dataSource="orderList"
        :columns="columns"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`
        }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            <a-button type="link" @click="viewOrderDetail(record.id)">{{ record.orderNo }}</a-button>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ record.statusName }}</a-tag>
          </template>
          <template v-else-if="column.key === 'payAmount'">
            <span style="color: #ff4d4f; font-weight: bold;">¥{{ record.payAmount }}</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewOrderDetail(record.id)">查看</a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleShip(record)"
              >
                发货
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 发货对话框 -->
    <a-modal
      v-model:open="shipModalVisible"
      title="订单发货"
      @ok="handleShipSubmit"
      @cancel="shipModalVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="订单号">
          <span>{{ currentOrder?.orderNo }}</span>
        </a-form-item>
        <a-form-item label="物流单号" required>
          <a-input v-model:value="logisticsNo" placeholder="请输入物流单号" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 订单详情对话框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="订单详情"
      width="800px"
      :footer="null"
    >
      <div v-if="orderDetail" class="order-detail-content">
        <!-- 订单基本信息 -->
        <a-descriptions title="订单信息" :column="2" bordered>
          <a-descriptions-item label="订单号">{{ orderDetail.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(orderDetail.status)">{{ orderDetail.statusName }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="下单时间">{{ formatDate(orderDetail.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="支付时间">{{ orderDetail.payTime ? formatDate(orderDetail.payTime) : '-' }}</a-descriptions-item>
          <a-descriptions-item label="支付方式">{{ orderDetail.payTypeName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="物流单号">{{ orderDetail.logisticsNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="订单备注" :span="2">{{ orderDetail.remark || '-' }}</a-descriptions-item>
        </a-descriptions>

        <!-- 收货地址信息 -->
        <a-descriptions title="收货信息" :column="2" bordered style="margin-top: 20px;">
          <a-descriptions-item label="收货人">{{ orderDetail.address?.receiver }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ orderDetail.address?.phone }}</a-descriptions-item>
          <a-descriptions-item label="收货地址" :span="2">{{ orderDetail.address?.fullAddress }}</a-descriptions-item>
        </a-descriptions>

        <!-- 商品信息 -->
        <div style="margin-top: 20px;">
          <h4>商品清单</h4>
          <a-table
            :dataSource="orderDetail.items"
            :columns="itemColumns"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'subtotal'">
                <span style="color: #ff4d4f; font-weight: bold;">¥{{ record.subtotal }}</span>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 金额信息 -->
        <div style="margin-top: 20px; text-align: right;">
          <a-space direction="vertical" style="width: 300px;">
            <div style="display: flex; justify-content: space-between; font-size: 16px;">
              <span>订单总额：</span>
              <span style="font-weight: bold;">¥{{ orderDetail.totalAmount }}</span>
            </div>
            <a-divider style="margin: 8px 0;" />
            <div style="display: flex; justify-content: space-between; font-size: 16px; font-weight: bold;">
              <span>实付金额：</span>
              <span style="color: #ff4d4f;">¥{{ orderDetail.payAmount }}</span>
            </div>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getAdminOrderPage, shipOrder, getOrderDetail } from '@/api/OrderApi';

const router = useRouter();

// 数据
const orderList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const searchParams = reactive({
  status: null,
  orderNo: ''
});

const shipModalVisible = ref(false);
const currentOrder = ref(null);
const logisticsNo = ref('');

// 订单详情相关
const detailModalVisible = ref(false);
const orderDetail = ref(null);

// 表格列定义
const columns = [
  {
    title: '订单号',
    key: 'orderNo',
    dataIndex: 'orderNo',
    width: 200
  },
  {
    title: '商品信息',
    key: 'mainProductTitle',
    dataIndex: 'mainProductTitle'
  },
  {
    title: '数量',
    key: 'itemCount',
    dataIndex: 'itemCount',
    width: 80
  },
  {
    title: '实付金额',
    key: 'payAmount',
    dataIndex: 'payAmount',
    width: 120
  },
  {
    title: '订单状态',
    key: 'status',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '下单时间',
    key: 'createTime',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

// 订单明细表格列定义
const itemColumns = [
  {
    title: '商品名称',
    key: 'title',
    dataIndex: 'title'
  },
  {
    title: '单价',
    key: 'price',
    dataIndex: 'price',
    width: 100
  },
  {
    title: '数量',
    key: 'quantity',
    dataIndex: 'quantity',
    width: 80
  },
  {
    title: '小计',
    key: 'subtotal',
    dataIndex: 'subtotal',
    width: 120
  }
];

// 加载订单列表
const loadOrderList = () => {
  loading.value = true;
  
  const params = {
    current: currentPage.value,
    size: pageSize.value
  };
  
  if (searchParams.status !== null) {
    params.status = searchParams.status;
  }
  
  if (searchParams.orderNo) {
    params.orderNo = searchParams.orderNo;
  }
  
  getAdminOrderPage(params, {
    onSuccess: (res) => {
      orderList.value = res.records || [];
      total.value = res.total || 0;
      loading.value = false;
    },
    onError: () => {
      loading.value = false;
    }
  });
};

// 查询
const handleSearch = () => {
  currentPage.value = 1;
  loadOrderList();
};

// 重置
const handleReset = () => {
  searchParams.status = null;
  searchParams.orderNo = '';
  currentPage.value = 1;
  loadOrderList();
};

// 表格变化
const handleTableChange = (pagination) => {
  currentPage.value = pagination.current;
  pageSize.value = pagination.pageSize;
  loadOrderList();
};

// 查看详情
const viewOrderDetail = (orderId) => {
  getOrderDetail(orderId, {
    onSuccess: (data) => {
      orderDetail.value = data;
      detailModalVisible.value = true;
    },
    onError: (error) => {
      message.error('获取订单详情失败：' + error.message);
    }
  });
};

// 发货
const handleShip = (order) => {
  currentOrder.value = order;
  logisticsNo.value = '';
  shipModalVisible.value = true;
};

// 发货提交
const handleShipSubmit = () => {
  if (!logisticsNo.value) {
    message.error('请输入物流单号');
    return;
  }
  
  shipOrder(currentOrder.value.id, logisticsNo.value, {
    successMsg: '发货成功',
    onSuccess: () => {
      shipModalVisible.value = false;
      loadOrderList();
    }
  });
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    0: 'orange',
    1: 'blue',
    2: 'cyan',
    3: 'green',
    4: 'default'
  };
  return colorMap[status] || 'default';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};

// 页面加载
onMounted(() => {
  loadOrderList();
});
</script>

<style scoped>
.order-manage-container {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  background-color: #fff;
}

/* 订单详情对话框样式 */
.order-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.order-detail-content h4 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.order-detail-content :deep(.ant-descriptions-title) {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.order-detail-content :deep(.ant-descriptions-item-label) {
  font-weight: 500;
  background-color: #fafafa;
}
</style>

