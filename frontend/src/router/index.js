import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentUser } from '../api/auth'
import AdminLayout from '../layout/AdminLayout.vue'
import { authState, clearAuth, setCurrentUser } from '../stores/auth'
import HomeView from '../views/home/HomeView.vue'
import InboundView from '../views/inbound/InboundView.vue'
import LoginView from '../views/login/LoginView.vue'
import OutboundView from '../views/outbound/OutboundView.vue'
import ProductView from '../views/product/ProductView.vue'
import ReportView from '../views/report/ReportView.vue'
import StockView from '../views/stock/StockView.vue'
import StockcheckView from '../views/stockcheck/StockcheckView.vue'
import SystemView from '../views/system/SystemView.vue'
import UserView from '../views/user/UserView.vue'

async function ensureAuthenticated() {
  if (authState.isAuthenticated) {
    return true
  }

  if (!authState.token) {
    clearAuth()
    return false
  }

  try {
    const currentUser = await getCurrentUser({
      redirectOnUnauthorized: false,
    })
    setCurrentUser(currentUser)
    return true
  } catch {
    clearAuth()
    return false
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        public: true,
      },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView,
        },
        {
          path: 'users',
          name: 'users',
          component: UserView,
        },
        {
          path: 'products',
          name: 'products',
          component: ProductView,
        },
        {
          path: 'stocks',
          name: 'stocks',
          component: StockView,
        },
        {
          path: 'inbounds',
          name: 'inbounds',
          component: InboundView,
        },
        {
          path: 'outbounds',
          name: 'outbounds',
          component: OutboundView,
        },
        {
          path: 'stockchecks',
          name: 'stockchecks',
          component: StockcheckView,
        },
        {
          path: 'reports',
          name: 'reports',
          component: ReportView,
        },
        {
          path: 'system',
          name: 'system',
          component: SystemView,
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (to.path === '/login' && authState.token) {
      const authenticated = await ensureAuthenticated()
      if (authenticated) {
        return { path: '/' }
      }
    }
    return true
  }

  if (to.meta.requiresAuth) {
    const authenticated = await ensureAuthenticated()
    if (!authenticated) {
      return {
        path: '/login',
        query: {
          redirect: to.fullPath,
        },
      }
    }
  }

  return true
})

export default router
