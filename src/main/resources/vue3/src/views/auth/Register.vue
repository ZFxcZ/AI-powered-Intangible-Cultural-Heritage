<template>
  <AuthForm
    title="加入我们"
    :fields="registerFields"
    :form-data="registerForm"
    :rules="rules"
    :loading="loading"
    submit-text="注册"
    :links="authLinks"
    @submit="handleRegister"
  />
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined } from '@ant-design/icons-vue'
import { register } from '@/api/user'
import AuthForm from '@/components/AuthForm.vue'

const router = useRouter()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  name: '',
  userType: 'USER', // 默认注册为普通用户，值为USER或ADMIN
})

// 表单字段配置
const registerFields = [
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
  },
  {
    prop: 'confirmPassword',
    type: 'password',
    placeholder: '请确认密码',
    icon: LockOutlined
  },
  {
    prop: 'email',
    placeholder: '请输入邮箱',
    icon: MailOutlined
  },
  {
    prop: 'phone',
    placeholder: '请输入手机号',
    icon: PhoneOutlined
  },
  {
    prop: 'name',
    placeholder: '请输入姓名',
    icon: UserOutlined
  }
]

const validatePass2 = async (rule, value) => {
  if (value !== registerForm.password) {
    return Promise.reject('两次输入密码不一致!')
  }
  return Promise.resolve()
}

const validateEmail = async (rule, value) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    return Promise.reject('邮箱格式不正确')
  }
  return Promise.resolve()
}

const validatePhone = async (rule, value) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    return Promise.reject('手机号格式不正确')
  }
  return Promise.resolve()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  name: [
    { required: false }
  ]
}

// 底部链接配置
const authLinks = [
  { text: '已有账号？立即登录', to: '/auth/login' }
]

const handleRegister = async () => {
  loading.value = true
  try {
    // 保留confirmPassword字段，后端需要验证
    await register(registerForm, {
      successMsg: "注册成功",
      showDefaultMsg: true,
      onSuccess: () => {
        router.push('/auth/login')
      }
    })
  } finally {
    loading.value = false
  }
}
</script>

