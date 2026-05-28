<template>
  <div v-if="!resetSuccess">
    <AuthForm
      title="找回密码"
      :fields="forgotFields"
      :form-data="forgotForm"
      :rules="rules"
      :loading="loading"
      submit-text="重置密码"
      :links="authLinks"
      @submit="handleResetPassword"
    />
  </div>

  <!-- 成功提示 -->
  <div v-else class="success-message">
    <div class="success-icon">✓</div>
    <h2>密码重置成功</h2>
    <p>您的密码已成功重置</p>
    <p>请使用新密码登录，继续您的非遗传承之旅</p>

    <div class="back-links">
      <router-link to="/auth/login" class="login-btn">
        立即登录
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, MailOutlined, PhoneOutlined, LockOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AuthForm from '@/components/AuthForm.vue'
import { forgetPassword } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const resetSuccess = ref(false)

const forgotForm = reactive({
  username: '',
  email: '',
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单字段配置
const forgotFields = [
  {
    prop: 'username',
    placeholder: '请输入用户名',
    icon: UserOutlined
  },
  {
    prop: 'email',
    placeholder: '请输入邮箱地址',
    icon: MailOutlined
  },
  {
    prop: 'phone',
    placeholder: '请输入手机号',
    icon: PhoneOutlined
  },
  {
    prop: 'newPassword',
    type: 'password',
    placeholder: '请输入新密码',
    icon: LockOutlined
  },
  {
    prop: 'confirmPassword',
    type: 'password',
    placeholder: '请确认新密码',
    icon: LockOutlined
  }
]

// 密码确认验证
const validateConfirmPassword = async (rule, value) => {
  if (value !== forgotForm.newPassword) {
    return Promise.reject('两次输入密码不一致!')
  }
  return Promise.resolve()
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 底部链接配置
const authLinks = [
  { text: '返回登录', to: '/auth/login' },
  { text: '还没有账号？立即注册', to: '/auth/register' }
]

// 重置密码
const handleResetPassword = async () => {
  loading.value = true
  try {
    // 调用重置密码API
    await forgetPassword({
      username: forgotForm.username,
      email: forgotForm.email,
      phone: forgotForm.phone,
      newPassword: forgotForm.newPassword
    }, {
      successMsg: '密码重置成功',
      showDefaultMsg: true
    })

    resetSuccess.value = true
  } catch (error) {
    // 错误已由request拦截器处理
    console.error('密码重置失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 引入思源字体 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;700&family=Noto+Sans+SC:wght@400;500&display=swap');

.success-message {
  text-align: center;
  padding: 3rem 2rem;
  background: linear-gradient(135deg, rgba(248, 245, 240, 0.95) 0%, rgba(255, 255, 255, 0.95) 100%);
  border: 2px solid #C5A572;
  position: relative;
  animation: successFadeIn 0.6s ease-out;
}

@keyframes successFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, #7BA4A8, #5A8A8E);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: white;
  font-weight: bold;
  box-shadow: 0 4px 16px rgba(123, 164, 168, 0.3);
  animation: iconPop 0.5s ease-out 0.2s both;
}

@keyframes iconPop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-message h2 {
  font-family: 'Noto Serif SC', '思源宋体', serif;
  color: #2C2C2C;
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: 0.2rem;
}

.success-message p {
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  color: #666666;
  margin: 0.5rem 0;
  line-height: 1.8;
  font-size: 0.95rem;
  letter-spacing: 0.05rem;
}

.success-message strong {
  color: #D4282D;
  font-weight: 500;
}

.back-links {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(197, 165, 114, 0.3);
}

.login-btn {
  display: inline-block;
  padding: 12px 48px;
  background: linear-gradient(135deg, #D4282D, #B8252A);
  color: white;
  text-decoration: none;
  border-radius: 2px;
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 0.1rem;
  font-family: 'Noto Sans SC', '思源黑体', sans-serif;
  box-shadow: 0 4px 12px rgba(212, 40, 45, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #B8252A, #D4282D);
  box-shadow: 0 6px 16px rgba(212, 40, 45, 0.4);
  transform: translateY(-2px);
  color: white;
}
</style>
