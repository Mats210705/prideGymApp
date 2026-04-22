import axios from 'axios'
import router from '../router'

const http = axios.create({
  baseURL: (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api'
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('pridegym_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  r => r,
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('pridegym_token')
      localStorage.removeItem('pridegym_user')
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(err)
  }
)

export default http
