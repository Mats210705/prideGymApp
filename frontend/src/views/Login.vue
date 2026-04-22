<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const username = ref('admin')
const password = ref('admin123')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await http.post('/auth/login', {
      username: username.value,
      password: password.value
    })
    localStorage.setItem('pridegym_token', data.token)
    localStorage.setItem('pridegym_user', JSON.stringify({
      username: data.username,
      nombre: data.nombre
    }))
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.response?.data?.error || 'No se pudo iniciar sesión'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-bg">
    <form class="login-box" @submit.prevent="submit">
      <h1>PRIDE GYM</h1>
      <div class="sub">Administración</div>

      <div class="field">
        <label>Usuario</label>
        <input v-model="username" autofocus />
      </div>
      <div class="field">
        <label>Contraseña</label>
        <input v-model="password" type="password" />
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Ingresando...' : 'Ingresar' }}
      </button>

      <div v-if="error" class="error">{{ error }}</div>

      <div class="hint">
        Demo: <strong>admin</strong> / <strong>admin123</strong>
      </div>
    </form>
  </div>
</template>
