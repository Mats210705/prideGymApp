import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Alumnos from '../views/Alumnos.vue'
import Disciplinas from '../views/Disciplinas.vue'
import Cuotas from '../views/Cuotas.vue'

const routes = [
  { path: '/login', name: 'login', component: Login, meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: Dashboard },
  { path: '/alumnos', name: 'alumnos', component: Alumnos },
  { path: '/disciplinas', name: 'disciplinas', component: Disciplinas },
  { path: '/cuotas', name: 'cuotas', component: Cuotas }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('pridegym_token')
  if (!to.meta.public && !token) {
    return next({ name: 'login' })
  }
  if (to.name === 'login' && token) {
    return next({ name: 'dashboard' })
  }
  next()
})

export default router
