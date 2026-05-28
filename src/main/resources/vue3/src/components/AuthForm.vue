<template>
  <div class="auth-form">
    <!-- 表单标题 -->
    <div class="form-header">
      <h2>{{ title }}</h2>
      <p class="form-subtitle">
        欢迎回来，请登录您的账号
      </p>
    </div>

    <!-- 表单内容 -->
    <form
      ref="formRef"
      class="form-content"
      @submit.prevent="handleSubmit"
    >
      <!-- 动态渲染表单项 -->
      <div
        v-for="field in fields"
        :key="field.prop"
        class="form-item"
        :class="{ 'has-error': errors[field.prop] }"
      >
        <div class="input-wrapper">
          <span
            v-if="field.icon"
            class="input-icon"
          >
            <component :is="field.icon" />
          </span>
          <input
            v-if="field.type !== 'password'"
            v-model="formData[field.prop]"
            :type="field.type || 'text'"
            :placeholder="errors[field.prop] || field.placeholder"
            @keypress.enter="handleSubmit"
            @blur="validateField(field.prop)"
            @input="clearError(field.prop)"
          >
          <input
            v-else
            v-model="formData[field.prop]"
            type="password"
            :placeholder="errors[field.prop] || field.placeholder"
            @keypress.enter="handleSubmit"
            @blur="validateField(field.prop)"
            @input="clearError(field.prop)"
          >
        </div>
        <div
          v-if="errors[field.prop]"
          class="error-message"
        >
          {{ errors[field.prop] }}
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-item submit-item">
        <button
          type="submit"
          class="submit-btn"
          :disabled="loading"
        >
          <span
            v-if="loading"
            class="loading-spinner"
          />
          {{ loading ? "提交中..." : submitText }}
        </button>
      </div>
    </form>

    <!-- 底部链接 -->
    <div
      v-if="links && links.length"
      class="form-links"
    >
      <div
        v-for="link in links"
        :key="link.text"
        class="link-item"
      >
        <router-link :to="link.to">
          {{ link.text }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  fields: {
    type: Array,
    required: true,
  },
  formData: {
    type: Object,
    required: true,
  },
  rules: {
    type: Object,
    required: true,
  },
  submitText: {
    type: String,
    default: "提交",
  },
  loading: {
    type: Boolean,
    default: false,
  },
  links: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["submit"]);

const formRef = ref(null);
const errors = reactive({});

const validateField = (fieldName) => {
  const fieldRules = props.rules[fieldName];
  if (!fieldRules) return true;

  const value = props.formData[fieldName];

  for (const rule of fieldRules) {
    if (rule.required && (!value || value.trim() === "")) {
      errors[fieldName] = rule.message;
      return false;
    }

    if (rule.min && value && value.length < rule.min) {
      errors[fieldName] = rule.message;
      return false;
    }

    if (rule.max && value && value.length > rule.max) {
      errors[fieldName] = rule.message;
      return false;
    }

    if (rule.pattern && value && !rule.pattern.test(value)) {
      errors[fieldName] = rule.message;
      return false;
    }

    if (rule.type === "email" && value) {
      const emailRegex =
        /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
      if (!emailRegex.test(value)) {
        errors[fieldName] = rule.message;
        return false;
      }
    }

    if (rule.validator) {
      try {
        const result = rule.validator(rule, value);
        if (result && typeof result.catch === "function") {
          result.catch((err) => {
            errors[fieldName] = err.message || err;
          });
          return true;
        }
      } catch (error) {
        errors[fieldName] = error.message || error;
        return false;
      }
    }
  }

  delete errors[fieldName];
  return true;
};

const clearError = (fieldName) => {
  delete errors[fieldName];
};

const validateForm = () => {
  let isValid = true;
  Object.keys(props.rules).forEach((fieldName) => {
    if (!validateField(fieldName)) {
      isValid = false;
    }
  });
  return isValid;
};

const handleSubmit = () => {
  if (validateForm()) {
    emit("submit");
  }
};

defineExpose({
  formRef,
  validateForm,
});
</script>

<style scoped>
.auth-form {
  width: 100%;
  font-family: "Inter", "Noto Sans SC", sans-serif;
}

.form-header {
  margin-bottom: 2rem;
}

.form-header h2 {
  margin: 0 0 8px 0;
  font-family: "Inter", "Noto Sans SC", sans-serif;
  font-size: 1.875rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.form-subtitle {
  margin: 0;
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.5;
}

.form-content {
  position: relative;
}

.form-item {
  margin-bottom: 1.25rem;
  position: relative;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  font-size: 16px;
  z-index: 1;
  display: flex;
  align-items: center;
  transition: color 0.2s ease;
}

.input-wrapper input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  padding-left: 44px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: "Inter", "Noto Sans SC", sans-serif;
  color: #0f172a;
  background: white;
  transition: all 0.2s ease;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #94a3b8;
}

.input-wrapper input:hover {
  border-color: #cbd5e1;
}

.input-wrapper input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-wrapper:focus-within .input-icon {
  color: #3b82f6;
}

.error-message {
  margin-top: 6px;
  font-size: 0.875rem;
  color: #ef4444;
  line-height: 1.4;
}

.form-item.has-error .input-wrapper input {
  border-color: #ef4444;
}

.form-item.has-error .input-wrapper input:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.submit-item {
  margin-top: 2rem;
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 6px;
  background: #3b82f6;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  font-family: "Inter", "Noto Sans SC", sans-serif;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.form-links {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.link-item {
  text-align: center;
  margin: 0.75rem 0;
}

.link-item a {
  color: #3b82f6;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: color 0.2s ease;
}

.link-item a:hover {
  color: #2563eb;
}

@media (max-width: 768px) {
  .form-header h2 {
    font-size: 1.5rem;
  }

  .input-wrapper input {
    height: 44px;
  }

  .submit-btn {
    height: 44px;
  }
}
</style>
