<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/dashboard')">
        <span class="logo-icon">✈</span>
        <span v-show="!collapsed" class="logo-text">TripAgency 后台</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="rgba(255,255,255,.7)"
        active-text-color="#fff"
      >
        <el-menu-item v-for="menu in menus" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title !== '首页'">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-dropdown @command="onCommand">
          <div class="user-area">
            <el-avatar :size="32" class="user-avatar">{{ auth.user?.nickname?.charAt(0) || '管' }}</el-avatar>
            <span class="user-name">{{ auth.user?.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>{{ auth.user?.username }}（{{ auth.user?.role }}）</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const collapsed = ref(false)

const menus = [
  { path: '/dashboard', title: '首页', icon: 'Odometer' },
  { path: '/shop', title: '服务商管理', icon: 'Shop' },
  { path: '/university', title: '高校管理', icon: 'School' },
  { path: '/order', title: '订单管理', icon: 'Tickets' }
]

const onCommand = (cmd) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        auth.logout()
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #001529;
  transition: width 0.2s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-icon {
  font-size: 22px;
}
.logo-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}
.el-menu {
  border-right: none;
  padding: 8px;
}
:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  margin-bottom: 4px;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(22, 119, 255, 0.92), rgba(22, 119, 255, 0.55));
  color: #fff !important;
}
:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff !important;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  position: relative;
  z-index: 5;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}
.user-avatar {
  background: #1677ff;
}
.user-name {
  font-size: 14px;
}
.main {
  background: #f0f2f5;
  padding: 0;
}
</style>