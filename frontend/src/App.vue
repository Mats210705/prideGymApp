<script setup>
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { computed, ref, watch } from 'vue'

const route = useRoute()
const router = useRouter()

const isPublic = computed(() => route.meta.public)
const user = computed(() => {
  const raw = localStorage.getItem('pridegym_user')
  return raw ? JSON.parse(raw) : null
})

const mobileMenuOpen = ref(false)

// Cerrar menu mobile al navegar
watch(() => route.fullPath, () => {
  mobileMenuOpen.value = false
})

function logout() {
  localStorage.removeItem('pridegym_token')
  localStorage.removeItem('pridegym_user')
  router.push({ name: 'login' })
}

function closeMenu() {
  mobileMenuOpen.value = false
}
</script>

<template>
  <div v-if="isPublic">
    <RouterView />
  </div>
  <div v-else class="app-layout">
    <!-- Topbar mobile -->
    <header class="mobile-topbar">
      <button class="hamburger" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="Menu">
        <span></span><span></span><span></span>
      </button>
      <div class="mobile-brand">
        <strong>PRIDE GYM</strong>
        <span>Resistencia</span>
      </div>
    </header>

    <!-- Overlay mobile -->
    <div
      v-if="mobileMenuOpen"
      class="sidebar-overlay"
      @click="closeMenu"
    ></div>

    <aside class="sidebar" :class="{ open: mobileMenuOpen }">
      <div class="brand">
        <h1>PRIDE GYM</h1>
        <div class="sub">Resistencia</div>
      </div>
      <nav class="nav">
        <RouterLink to="/dashboard" active-class="active" @click="closeMenu">Dashboard</RouterLink>
        <RouterLink to="/alumnos" active-class="active" @click="closeMenu">Alumnos</RouterLink>
        <RouterLink to="/disciplinas" active-class="active" @click="closeMenu">Disciplinas</RouterLink>
        <RouterLink to="/cuotas" active-class="active" @click="closeMenu">Cuotas</RouterLink>
        <RouterLink to="/rutinas" active-class="active" @click="closeMenu">Rutinas</RouterLink>
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
