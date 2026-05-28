<template>
  <div class="order-detail-wrapper">
    <div class="order-detail-container">
      <a-card v-if="orderDetail" class="order-detail-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <h2>订单详情</h2>
          <a-tag :color="getStatusColor(orderDetail.status)" style="font-size: 16px; padding: 4px 12px;">
            {{ orderDetail.statusName }}
          </a-tag>
        </div>

        <!-- 订单信息 -->
        <a-descriptions title="订单信息" bordered :column="2">
          <a-descriptions-item label="订单号">{{ orderDetail.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="订单状态">{{ orderDetail.statusName }}</a-descriptions-item>
          <a-descriptions-item label="下单时间">{{ formatDate(orderDetail.createTime) }}</a-descriptions-item>
          <a-descriptions-item label="支付时间">
            {{ orderDetail.payTime ? formatDate(orderDetail.payTime) : '未支付' }}
          </a-descriptions-item>
          <a-descriptions-item label="支付方式">
            {{ orderDetail.payTypeName || '未支付' }}
          </a-descriptions-item>
          <a-descriptions-item label="物流单号">
            {{ orderDetail.logisticsNo || '暂无' }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 收货地址 -->
        <a-descriptions title="收货地址" bordered :column="1" style="margin-top: 24px;" v-if="orderDetail.address">
          <a-descriptions-item label="收货人">{{ orderDetail.address.receiver }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ orderDetail.address.phone }}</a-descriptions-item>
          <a-descriptions-item label="收货地址">{{ orderDetail.address.fullAddress }}</a-descriptions-item>
        </a-descriptions>

        <!-- 商品明细 -->
        <div class="order-items" style="margin-top: 24px;">
          <h3>商品明细</h3>
          <a-table
            :dataSource="orderDetail.items"
            :columns="itemColumns"
            :pagination="false"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'title'">
                <div>
                  <div>{{ record.title }}</div>
                  <div style="color: #999; font-size: 12px;" v-if="record.skuTitle">
                    {{ record.skuTitle }}
                  </div>
                </div>
              </template>
              <template v-else-if="column.key === 'price'">
                <span style="color: #ff4d4f;">¥{{ record.price }}</span>
              </template>
              <template v-else-if="column.key === 'subtotal'">
                <span style="color: #ff4d4f; font-weight: bold;">¥{{ record.subtotal }}</span>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 金额统计 -->
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">商品总额:</span>
            <span class="value">¥{{ orderDetail.totalAmount }}</span>
          </div>
          <div class="summary-item total">
            <span class="label">实付金额:</span>
            <span class="value">¥{{ orderDetail.payAmount }}</span>
          </div>
        </div>

        <!-- 订单备注 -->
        <div class="order-remark" v-if="orderDetail.remark">
          <h3>订单备注</h3>
          <p>{{ orderDetail.remark }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <a-button @click="goBack">返回</a-button>
          <a-button
            v-if="orderDetail.status === 0"
            type="primary"
            @click="handlePayOrder"
            style="margin-left: auto;"
          >
            <i class="fas fa-credit-card"></i>
            立即支付
          </a-button>
          <a-button
            v-if="orderDetail.status === 0 || orderDetail.status === 1"
            danger
            @click="handleCancelOrder"
          >
            取消订单
          </a-button>
          <a-button
            v-if="orderDetail.status === 2"
            type="primary"
            @click="handleConfirmOrder"
          >
            确认收货
          </a-button>
        </div>
      </a-card>

      <a-spin v-else class="loading-spinner" size="large" />
    </div>

    <!-- 支付二维码弹窗 -->
    <a-modal
      v-model:open="qrVisible"
      title="扫码支付"
      width="400px"
      :footer="null"
      @cancel="handleQrClose"
    >
      <div class="qr-code-container">
        <div id="qrCodeContainer" class="qr-code-wrapper"></div>
        <p class="qr-tip">请使用支付宝扫描二维码进行支付</p>
        <p class="qr-tip-sub">支付完成后，页面将自动跳转</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Modal, message } from 'ant-design-vue';
import { getOrderDetail, cancelOrder, confirmOrder, generatePayQrCode } from '@/api/OrderApi';
import { useUserStore } from '@/store/user';
import QRCode from 'qrcode';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 数据
const orderDetail = ref(null);
const qrVisible = ref(false);
let checkPaymentTimer = null;
let currentPayUrl = null;
let currentOrderId = null;

// 表格列定义
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
    width: 120
  },
  {
    title: '数量',
    key: 'quantity',
    dataIndex: 'quantity',
    width: 100
  },
  {
    title: '小计',
    key: 'subtotal',
    dataIndex: 'subtotal',
    width: 120
  }
];

// 生成支付二维码
const generateQRCode = async (payUrl) => {
  console.log('开始生成二维码，URL:', payUrl);
  
  // 等待 DOM 更新
  await nextTick();
  
  const qrContainer = document.getElementById('qrCodeContainer');
  if (!qrContainer) {
    console.error('找不到二维码容器');
    message.error('二维码容器加载失败');
    return false;
  }
  
  // 清空容器
  qrContainer.innerHTML = '';
  
  try {
    // 创建 canvas 元素
    const canvas = document.createElement('canvas');
    canvas.width = 280;
    canvas.height = 280;
    canvas.style.width = '280px';
    canvas.style.height = '280px';
    qrContainer.appendChild(canvas);
    
    // 生成二维码
    await QRCode.toCanvas(canvas, payUrl, {
      width: 280,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      },
      errorCorrectionLevel: 'M'
    });
    
    console.log('✅ 二维码生成成功');
    return true;
  } catch (error) {
    console.error('❌ 二维码生成失败:', error);
    qrContainer.innerHTML = `<div style="color: red; text-align: center; padding: 20px;">二维码生成失败<br/>${error.message}</div>`;
    message.error('二维码生成失败：' + error.message);
    return false;
  }
};

// 监听弹窗打开状态
watch(qrVisible, (newVal) => {
  console.log('弹窗状态变化:', newVal);
  if (newVal && currentPayUrl) {
    // 弹窗打开时生成二维码
    console.log('弹窗已打开，准备生成二维码');
    setTimeout(() => {
      generateQRCode(currentPayUrl);
    }, 100);
  }
});

const showQrCode = async () => {
  const orderId = orderDetail.value.id;
  currentOrderId = orderId;
  
  try {
    console.log('=== 开始获取支付链接 ===');
    console.log('订单 ID:', orderId);
    
    await generatePayQrCode(orderId, 'ALI', {
      onSuccess: (payUrl) => {
        console.log('=== 获取支付链接成功 ===');
        console.log('后端返回的 payUrl:', payUrl);
        
        // 获取用户 token
        const token = userStore.token;
        if (!token) {
          message.error('用户未登录，请重新登录');
          return;
        }
        
        // 🔥 关键修改：使用后端真实地址，而不是前端地址
        // 从环境变量或配置中获取后端基础 URL
        const backendBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8889';
        
        let fullPayUrl;
        if (typeof payUrl === 'string') {
          // 确保 payUrl 以 / 开头
          const normalizedPayUrl = payUrl.startsWith('/') ? payUrl : `/${payUrl}`;
          // 直接使用后端地址，不再拼接 /api
          fullPayUrl = `${backendBaseUrl}${normalizedPayUrl}&token=${token}`;
        } else {
          console.error('payUrl 格式错误:', payUrl);
          message.error('支付链接格式错误');
          return;
        }
        
        console.log('完整支付 URL:', fullPayUrl);
        
        // 保存支付 URL
        currentPayUrl = fullPayUrl;
        
        // 打开二维码弹窗
        console.log('准备打开弹窗');
        qrVisible.value = true;
        console.log('弹窗已设置为打开');
        
        // 开始轮询检查支付状态
        startPaymentCheck(orderId);
      },
      onError: (error) => {
        console.error('=== 获取支付链接失败 ===', error);
        message.error('获取支付链接失败：' + (error.message || '未知错误'));
      }
    });
  } catch (error) {
    console.error('=== 获取支付链接异常 ===', error);
    message.error('获取支付链接失败');
  }
};

// 开始检查支付状态
const startPaymentCheck = (orderId) => {
  // // 清除之前的定时器
  // if (checkPaymentTimer) {
  //   clearInterval(checkPaymentTimer);
  // }
  
  // const checkPayment = async () => {
  //   try {
  //     const res = await getOrderDetail(orderId);
  //     orderDetail.value = res;
      
  //     // 如果已支付（status !== 0 表示已支付或已发货等状态）
  //     if (res.status !== 0) {
  //       console.log('订单已支付，关闭弹窗');
  //       handleQrClose();
  //       message.success('支付成功');
  //       loadOrderDetail();
  //     }
  //   } catch (error) {
  //     console.error('检查支付状态失败:', error);
  //   }
  // };
  
  // // 每 3 秒检查一次
  // checkPaymentTimer = setInterval(checkPayment, 3000);
  // console.log('开始轮询支付状态，订单 ID:', orderId);

  if (checkPaymentTimer) {
    clearInterval(checkPaymentTimer);
  }
  
  let checkCount = 0;
  const maxCheckCount = 20;
  
  const checkPayment = async () => {
    checkCount++;
    console.log(`第${checkCount}次检查支付状态...`);
    
    try {
      const res = await getOrderDetail(orderId);
      
      if (res.status !== 0) {
        console.log('订单已支付');
        handlePaymentSuccess(); // 调用统一的成功处理方法
        return;
      }
      
      if (checkCount >= maxCheckCount) {
        console.log('支付检查超时');
        clearInterval(checkPaymentTimer);
        checkPaymentTimer = null;
        message.warning('支付超时，请确认支付状态');
      }
    } catch (error) {
      console.error('检查支付状态失败:', error);
    }
  };
  
  checkPaymentTimer = setInterval(checkPayment, 3000);
  console.log('开始轮询支付状态，订单 ID:', orderId);
};

// 关闭二维码弹窗
const handleQrClose = () => {
  console.log('关闭二维码弹窗');
  qrVisible.value = false;
  if (checkPaymentTimer) {
    clearInterval(checkPaymentTimer);
    checkPaymentTimer = null;
  }
  // 清空保存的支付 URL 和订单 ID
  currentPayUrl = null;
  currentOrderId = null;
};

// 支付订单
const handlePayOrder = () => {
  console.log('点击支付按钮，订单状态:', orderDetail.value?.status);
  
  if (!orderDetail.value) {
    message.error('订单数据不存在');
    return;
  }
  
  if (orderDetail.value.status !== 0) {
    message.warning('该订单无需支付');
    return;
  }
  
  showQrCode();
};

// 取消订单
const handleCancelOrder = () => {
  Modal.confirm({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    onOk: () => {
      cancelOrder(orderDetail.value.id, {
        successMsg: '订单已取消',
        onSuccess: () => {
          loadOrderDetail();
        }
      });
    }
  });
};

// 确认收货
const handleConfirmOrder = () => {
  Modal.confirm({
    title: '确认收货',
    content: '确定已收到商品吗？',
    onOk: () => {
      confirmOrder(orderDetail.value.id, {
        successMsg: '已确认收货',
        onSuccess: () => {
          loadOrderDetail();
        }
      });
    }
  });
};

// 加载订单详情
const loadOrderDetail = () => {
  const orderId = route.params.id;
  getOrderDetail(orderId, {
    onSuccess: (res) => {
      orderDetail.value = res;
    },
    onError: () => {
      router.push('/orders');
    }
  });
};

// 返回
const goBack = () => {
  // 获取来源页面标识
  const from = route.query.from;
  
  // 如果订单已支付或已完成，跳转到订单列表
  if (orderDetail.value && orderDetail.value.status !== 0) {
    router.push('/orders');
    return;
  }
  
  // 如果是从创建订单页来的，跳转到订单列表
  if (from === 'create-order') {
    router.push('/orders');
    return;
  }
  
  // 其他情况正常返回上一页
  router.back();
};

const handlePaymentSuccess = () => {
  message.success('支付成功！');
  
  // 关闭弹窗和停止轮询
  if (checkPaymentTimer) {
    clearInterval(checkPaymentTimer);
    checkPaymentTimer = null;
  }
  qrVisible.value = false;
  currentPayUrl = null;
  currentOrderId = null;
  
  // 延迟1.5秒后跳转到订单列表
  setTimeout(() => {
    router.push('/orders');
  }, 1500);
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
  loadOrderDetail();
});

// 组件卸载时清除定时器
onUnmounted(() => {
  if (checkPaymentTimer) {
    clearInterval(checkPaymentTimer);
  }
});
</script>

<style scoped>
.order-detail-wrapper {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
}

.order-detail-container {
  padding: 32px 20px;
}

.order-detail-card {
  max-width: 1200px;
  margin: 0 auto;
  box-shadow: 0 4px 20px rgba(139, 69, 19, 0.12);
  border: 1px solid #e8e8e8;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e8e8e8;
}

.order-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c2c2c;
  font-family: 'SimSun', '宋体', serif;
  letter-spacing: 3px;
}

.order-items h3,
.order-remark h3 {
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 1px;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 600;
  color: #666;
  letter-spacing: 0.5px;
  background: rgba(139, 69, 19, 0.03);
}

:deep(.ant-descriptions-item-content) {
  color: #2c2c2c;
  font-weight: 500;
}

.order-summary {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.04) 0%, rgba(197, 165, 114, 0.06) 100%);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.summary-item {
  display: flex;
  gap: 20px;
  font-size: 15px;
}

.summary-item.total {
  font-size: 20px;
  font-weight: 700;
}

.summary-item .label {
  color: #666;
  font-weight: 500;
}

.summary-item .value {
  color: #8b4513;
  font-weight: 700;
}

.order-remark {
  margin-top: 24px;
  padding: 20px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.order-remark p {
  margin: 0;
  color: #666;
  line-height: 1.8;
}

.order-actions {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.order-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
}

.order-actions :deep(.ant-btn-primary):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
}

.order-actions :deep(.ant-btn-primary) i {
  margin-right: 6px;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fff;
  border-radius: 8px;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.qr-code-wrapper {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  min-height: 280px;
}

.qr-tip {
  font-size: 16px;
  color: #333;
  margin: 10px 0 5px;
  text-align: center;
  font-weight: 500;
}

.qr-tip-sub {
  font-size: 13px;
  color: #999;
  text-align: center;
  margin: 0;
}
</style>