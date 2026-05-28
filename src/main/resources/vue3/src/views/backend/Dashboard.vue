<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <a-card class="welcome-card">
      <template #title>
        <div class="welcome-header">
          <a-avatar :size="64" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0) }}
          </a-avatar>
          <div class="welcome-info">
            <h2>欢迎回来, {{ userInfo?.name || userInfo?.username }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>
      </template>
      <div class="role-info">
        <a-tag>{{ roleLabel }}</a-tag>
      </div>
    </a-card>

    <!-- 数据统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :loading="loading">
          <a-statistic
            title="总用户数"
            :value="statistics.totalUsers"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">今日新增: {{ statistics.todayNewUsers }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :loading="loading">
          <a-statistic
            title="总订单数"
            :value="statistics.totalOrders"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <ShoppingOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">今日订单: {{ statistics.todayOrders }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :loading="loading">
          <a-statistic
            title="总销售额"
            :value="statistics.totalSales"
            :precision="2"
            prefix="¥"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <DollarOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">今日销售: ¥{{ formatMoney(statistics.todaySales) }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card" :loading="loading">
          <a-statistic
            title="非遗项目"
            :value="statistics.totalHeritageItems"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <BookOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">
            课程: {{ statistics.totalCourses }} | 活动: {{ statistics.totalActivities }}
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="charts-row">
      <!-- 订单趋势图 -->
      <a-col :xs="24" :lg="12">
        <a-card title="近7天订单趋势" :loading="loading">
          <div ref="orderChartRef" style="width: 100%; height: 300px;"></div>
        </a-card>
      </a-col>
      
      <!-- 销售趋势图 -->
      <a-col :xs="24" :lg="12">
        <a-card title="近7天销售趋势" :loading="loading">
          <div ref="salesChartRef" style="width: 100%; height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="charts-row">
      <!-- 订单状态分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="订单状态分布" :loading="loading">
          <div ref="orderStatusChartRef" style="width: 100%; height: 300px;"></div>
        </a-card>
      </a-col>
      
      <!-- 非遗项目类别分布 -->
      <a-col :xs="24" :lg="12">
        <a-card title="非遗项目类别分布" :loading="loading">
          <div ref="heritageCategoryChartRef" style="width: 100%; height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  ShoppingOutlined, 
  DollarOutlined, 
  BookOutlined 
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStatistics } from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'USER': '普通用户'
  }
  return roleMap[userInfo.value?.userType] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar;
})

// 当前时间
const currentTime = ref('')
let timeInterval = null // 保存定时器引用

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

// 统计数据
const loading = ref(false)
const statistics = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  totalOrders: 0,
  todayOrders: 0,
  totalSales: 0,
  todaySales: 0,
  totalHeritageItems: 0,
  totalCourses: 0,
  totalActivities: 0,
  totalProducts: 0,
  totalInheritors: 0,
  orderStatusDistribution: {},
  heritageCategoryDistribution: {},
  last7DaysOrders: [],
  last7DaysSales: []
})

// 图表引用
const orderChartRef = ref(null)
const salesChartRef = ref(null)
const orderStatusChartRef = ref(null)
const heritageCategoryChartRef = ref(null)

// ECharts实例
let orderChart = null
let salesChart = null
let orderStatusChart = null
let heritageCategoryChart = null

// 格式化金额
const formatMoney = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2)
}

// 获取统计数据
const fetchStatistics = () => {
  loading.value = true
  getDashboardStatistics({
    onSuccess: (data) => {
      statistics.value = data
      loading.value = false
      // 等待DOM更新后初始化图表
      nextTick(() => {
        initCharts()
      })
    },
    onError: (error) => {
      console.error('获取统计数据失败:', error)
      message.error('获取统计数据失败')
      loading.value = false
    }
  })
}

// 初始化所有图表
const initCharts = () => {
  initOrderChart()
  initSalesChart()
  initOrderStatusChart()
  initHeritageCategoryChart()
}

// 初始化订单趋势图
const initOrderChart = () => {
  if (!orderChartRef.value) return
  
  if (orderChart) {
    orderChart.dispose()
  }
  
  orderChart = echarts.init(orderChartRef.value)
  
  const dates = statistics.value.last7DaysOrders.map(item => item.date)
  const counts = statistics.value.last7DaysOrders.map(item => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '订单数',
      type: 'line',
      data: counts,
      smooth: true,
      itemStyle: {
        color: '#1890ff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(24, 144, 255, 0.3)'
          }, {
            offset: 1,
            color: 'rgba(24, 144, 255, 0.05)'
          }]
        }
      }
    }]
  }
  
  orderChart.setOption(option)
}

// 初始化销售趋势图
const initSalesChart = () => {
  if (!salesChartRef.value) return
  
  if (salesChart) {
    salesChart.dispose()
  }
  
  salesChart = echarts.init(salesChartRef.value)
  
  const dates = statistics.value.last7DaysSales.map(item => item.date)
  const amounts = statistics.value.last7DaysSales.map(item => Number(item.amount || 0))
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        return params[0].name + '<br/>' + 
               params[0].marker + '销售额: ¥' + params[0].value.toFixed(2)
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [{
      name: '销售额',
      type: 'bar',
      data: amounts,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: '#cf1322'
          }, {
            offset: 1,
            color: '#ff7875'
          }]
        }
      }
    }]
  }
  
  salesChart.setOption(option)
}

// 初始化订单状态分布图
const initOrderStatusChart = () => {
  if (!orderStatusChartRef.value) return
  
  if (orderStatusChart) {
    orderStatusChart.dispose()
  }
  
  orderStatusChart = echarts.init(orderStatusChartRef.value)
  
  const data = Object.entries(statistics.value.orderStatusDistribution || {})
    .map(([name, value]) => ({ name, value }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '订单状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: data
    }]
  }
  
  orderStatusChart.setOption(option)
}

// 初始化非遗项目类别分布图
const initHeritageCategoryChart = () => {
  if (!heritageCategoryChartRef.value) return
  
  if (heritageCategoryChart) {
    heritageCategoryChart.dispose()
  }
  
  heritageCategoryChart = echarts.init(heritageCategoryChartRef.value)
  
  const data = Object.entries(statistics.value.heritageCategoryDistribution || {})
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 20,
      bottom: 20
    },
    series: [{
      name: '非遗类别',
      type: 'pie',
      radius: '60%',
      center: ['40%', '50%'],
      data: data,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  heritageCategoryChart.setOption(option)
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  orderChart?.resize()
  salesChart?.resize()
  orderStatusChart?.resize()
  heritageCategoryChart?.resize()
}

onMounted(() => {
  updateTime()
  // 每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)
  
  // 获取统计数据
  fetchStatistics()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
  
  // 销毁图表实例
  orderChart?.dispose()
  salesChart?.dispose()
  orderStatusChart?.dispose()
  heritageCategoryChart?.dispose()
  
  // 移除窗口监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.welcome-card {
  margin-bottom: 20px;
  
  .welcome-header {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .welcome-info {
      flex: 1;
      
      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #262626;
      }
      
      p {
        margin: 8px 0 0 0;
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }
  
  .role-info {
    margin-top: 16px;
  }
}

.stats-row {
  margin-bottom: 20px;
  
  .stat-card {
    text-align: center;
    
    .stat-sub {
      margin-top: 12px;
      font-size: 13px;
      color: #8c8c8c;
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }
  
  .welcome-header {
    flex-direction: column;
    align-items: flex-start !important;
    
    .welcome-info h2 {
      font-size: 20px;
    }
  }
  
  .stats-row {
    .a-col {
      margin-bottom: 12px;
    }
  }
}
</style>
 