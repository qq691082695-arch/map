<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <el-card class="login-card" shadow="always">
      <div class="login-head">
        <div class="logo">✈</div>
        <h1 class="title">TripAgency 后台管理系统</h1>
        <p class="subtitle">商务出行 · 住宿 · 饮食 中介服务平台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>演示账号：admin / 123456</template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login(form)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e) {
      ElMessage.error(e.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #6dd5a0 50%, #5b86e5 100%);
}
.login-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 40%, rgba(255, 255, 255, 0.18), transparent 45%);
}
.login-card {
  width: 420px;
  z-index: 1;
  border-radius: 12px;
}
.login-head {
  text-align: center;
  margin-bottom: 28px;
}
.logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  font-size: 28px;
  line-height: 56px;
}
.title {
  font-size: 22px;
  font-weight: 600;
  color: #001529;
}
.subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #8c8c8c;
}
.submit-btn {
  width: 100%;
}
.login-tip {
  margin-top: 4px;
}
</style>