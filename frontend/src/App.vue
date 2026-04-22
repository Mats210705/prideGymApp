<script setup>
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const router = useRouter()

const isPublic = computed(() => route.meta.public)
const user = computed(() => {
  const raw = localStorage.getItem('pridegym_user')
  return raw ? JSON.parse(raw) : null
})

function logout() {
  localStorage.removeItem('pridegym_token')
  localStorage.removeItem('pridegym_user')
  router.push({ name: 'login' })
}
</script>

<template>
  <div v-if="isPublic">
    <RouterView />
  </div>
  <div v-else class="app-layout">
    <aside class="sidebar">
      <div class="brand">
        <h1>PRIDE GYM</h1>
        <div class="sub">Resistencia</div>
      </div>
      <nav class="nav">
        <RouterLink to="/dashboard" active-class="active">Dashboard</RouterLink>
        <RouterLink to="/alumnos" active-class="active">Alumnos</RouterLink>
        <RouterLink to="/disciplinas" active-class="active">Disciplinas</RouterLink>
        <RouterLink to="/cuotas" active-class="active">Cuotas</RouterLink>
      </nav>
      <div class="sidebar-footer">
        <div>{{ user?.nombre || user?.username }}</div>
        <button class="secondary" @click="logout">Cerrar sesión</button>
      </div>
    </aside>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>
