<template>
  <div class="user-orders-page">
    <!-- 页面标题 - 使用卷轴样式 -->
    <div class="scroll-header">
      <div class="scroll-header-content">
        <h1 class="scroll-header-title">我的订单</h1>
        <p class="scroll-header-subtitle">查看订单状态 · 管理我的购买</p>
        <div class="scroll-header-seal">订单</div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="user-orders-container">
      <!-- 订单状态筛选 -->
      <div class="order-tabs">
      <a-radio-group v-model:value="currentStatus" button-style="solid" @change="handleStatusChange">
        <a-radio-button :value="null">全部订单</a-radio-button>
        <a-radio-button :value="0">待支付</a-radio-button>
        <a-radio-button :value="1">已支付</a-radio-button>
        <a-radio-button :value="2">已发货</a-radio-button>
        <a-radio-button :value="3">已完成</a-radio-button>
        <a-radio-button :value="4">已关闭</a-radio-button>
      </a-radio-group>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <a-card v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <a-tag :color="getStatusColor(order.status)">{{ order.statusName }}</a-tag>
        </div>
        
        <div class="order-content">
          <div class="order-product">
            <span class="product-title">{{ order.mainProductTitle }}</span>
            <span class="product-count">共{{ order.itemCount }}件商品</span>
          </div>
          
          <div class="order-amount">
            <span class="amount-label">实付金额:</span>
            <span class="amount-value">¥{{ order.payAmount }}</span>
          </div>
          
          <div class="order-actions">
            <a-button type="link" @click="viewOrderDetail(order.id)">查看详情</a-button>
            <a-button
              v-if="order.status === 0 || order.status === 1"
              type="link"
              danger
              @click="handleCancelOrder(order.id)"
            >
              取消订单
            </a-button>
            <a-button
              v-if="order.status === 2"
              type="primary"
              size="small"
              @click="handleConfirmOrder(order.id)"
            >
              确认收货
            </a-button>
          </div>
        </div>
        
        <div class="order-footer">
          <span class="order-time">下单时间: {{ formatDate(order.createTime) }}</span>
        </div>
      </a-card>
      
      <a-empty v-if="orderList.length === 0" description="暂无订单" />
    </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <a-pagination
          v-model:current="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          show-size-changer
          @change="loadOrderList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Modal } from 'ant-design-vue';
import { getUserOrderPage, cancelOrder, confirmOrder } from '@/api/OrderApi';
import '@/styles/scroll-header.css';

const router = useRouter();

// 数据
const orderList = ref([]);
const currentStatus = ref(null);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 加载订单列表
const loadOrderList = () => {
  const params = {
    current: currentPage.value,
    size: pageSize.value
  };
  
  if (currentStatus.value !== null) {
    params.status = currentStatus.value;
  }
  
  getUserOrderPage(params, {
    onSuccess: (res) => {
      orderList.value = res.records || [];
      total.value = res.total || 0;
    }
  });
};

// 状态改变
const handleStatusChange = () => {
  currentPage.value = 1;
  loadOrderList();
};

// 查看详情
const viewOrderDetail = (orderId) => {
  router.push(`/orders/${orderId}`);
};

// 取消订单
const handleCancelOrder = (orderId) => {
  Modal.confirm({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    onOk: () => {
      cancelOrder(orderId, {
        successMsg: '订单已取消',
        onSuccess: () => {
          loadOrderList();
        }
      });
    }
  });
};

// 确认收货
const handleConfirmOrder = (orderId) => {
  Modal.confirm({
    title: '确认收货',
    content: '确定已收到商品吗？',
    onOk: () => {
      confirmOrder(orderId, {
        successMsg: '已确认收货',
        onSuccess: () => {
          loadOrderList();
        }
      });
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
/* 新中式配色 */
.user-orders-page {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.user-orders-container {
  padding: 32px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.order-tabs {
  margin-bottom: 24px;
}

.order-tabs :deep(.ant-radio-button-wrapper) {
  border-color: #e8e8e8;
  color: #666;
  font-weight: 500;

  &:hover {
    color: #8b4513;
  }
}

.order-tabs :deep(.ant-radio-button-wrapper-checked) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border-color: #8b4513;
  color: #fff;

  &:hover {
    color: #fff;
  }
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.order-card:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 14px;
  border-bottom: 2px solid #e8e8e8;
  margin-bottom: 14px;
}

.order-no {
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

.order-content {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 20px;
  align-items: center;
  padding: 14px 0;
}

.order-product {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-title {
  font-size: 17px;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 0.5px;
}

.product-count {
  font-size: 14px;
  color: #999;
}

.order-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.amount-label {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

.amount-value {
  font-size: 22px;
  font-weight: 700;
  color: #8b4513;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.order-actions :deep(.ant-btn-link) {
  color: #8b4513;

  &:hover {
    color: #a0522d;
  }

  &.ant-btn-dangerous {
    color: #c5322d;
  }
}

.order-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.order-footer {
  padding-top: 14px;
  border-top: 2px solid #e8e8e8;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.pagination {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

.pagination :deep(.ant-pagination-item-active) {
  border-color: #8b4513;
  background: #8b4513;
}

.pagination :deep(.ant-pagination-item-active a) {
  color: #fff;
}
</style>

