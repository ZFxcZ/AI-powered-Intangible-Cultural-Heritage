<template>
  <AuthForm
    title="欢迎回来"
    :fields="loginFields"
    :form-data="loginForm"
    :rules="rules"
    :loading="loading"
    submit-text="登录"
    :links="authLinks"
    @submit="handleLogin"
  />
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api/user'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import AuthForm from '@/components/AuthForm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 表单字段配置
const loginFields = [
  {
    prop: 'username',
    placeholder: '请输入用户名',
    icon: UserOutlined
  },
  {
    prop: 'password',
    type: 'password',
    placeholder: '请输入密码',
    icon: LockOutlined
  }
]

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 底部链接配置
const authLinks = [
  { text: '忘记密码？', to: '/auth/forgot-password' },
  { text: '还没有账号？立即注册', to: '/auth/register' }
]

const handleLogin = async () => {
  loading.value = true
  try {
    // 使用API接口进行登录
    await login(loginForm, {
      successMsg: "登录成功",
      showDefaultMsg: true,
      onSuccess: async (data) => {
        userStore.setUserInfo(data)

        // 根据返回的角色决定跳转路径
        if (data.roleCode !== 'USER') {
          // 直接导航到后台仪表盘
          await router.isReady()
          router.push(route.query.redirect || '/back/dashboard')
        } else {
          // 普通用户登录，直接跳转到前台
          const redirect = route.query.redirect || '/'
          router.push(redirect)
        }
      },
      onError: (error) => {
        console.error('登录失败:', error)
      }
    })
  } finally {
    loading.value = false
  }
}
</script>

