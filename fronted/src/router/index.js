import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/dashboard', component: () => import('@/views/Dashboard.vue') },
  { path: '/editor/:id', component: () => import('@/views/Editor.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const isPublic = ['/login', '/register'].includes(to.path)

  if (!auth.token && !isPublic) {
    return '/login'
  }

  if (auth.token && !auth.user) {
    try {
      await auth.fetchProfile()
    } catch (error) {
      auth.logout()
      return '/login'
    }
  }

  if (auth.token && isPublic) {
    return '/dashboard'
  }

  return true
})

export default router
