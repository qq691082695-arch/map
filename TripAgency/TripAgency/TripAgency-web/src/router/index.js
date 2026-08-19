import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'Odometer' }
      },
      {
        path: 'shop',
        name: 'shop',
        component: () => import('@/views/shop/index.vue'),
        meta: { title: '服务商管理', icon: 'Shop' }
      },
      {
        path: 'university',
        name: 'university',
        component: () => import('@/views/university/index.vue'),
        meta: { title: '高校管理', icon: 'School' }
      },
      {
        path: 'order',
        name: 'order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'Tickets' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  document.title = to.meta.title ? `${to.meta.title} - TripAgency 后台管理系统` : 'TripAgency 后台管理系统'
  if (to.path !== '/login' && !auth.token) {
    next({ path: '/login' })
  } else if (to.path === '/login' && auth.token) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router