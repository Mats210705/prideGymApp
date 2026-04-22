<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { formatARS, nombreMes } from '../utils/format'

const resumen = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await http.get('/dashboard')
    resumen.value = data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <h2>Dashboard</h2>

  <div v-if="loading" class="empty">Cargando...</div>

  <div v-else-if="resumen">
    <div class="grid grid-4">
      <div class="card stat">
        <div class="label">Alumnos activos</div>
        <div class="value primary">{{ resumen.alumnosActivos }}</div>
        <div style="font-size:11px;color:var(--muted);margin-top:4px">
          {{ resumen.alumnosTotales }} totales
        </div>
      </div>
      <div class="card stat">
        <div class="label">Ingresos {{ nombreMes(resumen.mes) }}</div>
        <div class="value success">{{ formatARS(resumen.ingresosDelMes) }}</div>
      </div>
      <div class="card stat">
        <div class="label">Cuotas pendientes mes</div>
        <div class="value warning">{{ resumen.cuotasPendientesMes }}</div>
      </div>
      <div class="card stat">
        <div class="label">Cuotas vencidas</div>
        <div class="value" style="color:var(--danger)">{{ resumen.cuotasVencidasTotal }}</div>
      </div>
    </div>

    <div class="grid grid-2" style="margin-top:20px">
      <div class="card">
        <h3 style="margin-bottom:14px">Resumen del mes</h3>
        <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--border)">
          <span>Cuotas pagadas</span>
          <strong style="color:var(--success)">{{ resumen.cuotasPagadasMes }}</strong>
        </div>
        <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--border)">
          <span>Cuotas pendientes</span>
          <strong style="color:var(--warning)">{{ resumen.cuotasPendientesMes }}</strong>
        </div>
        <div style="display:flex;justify-content:space-between;padding:8px 0">
          <span>Disciplinas disponibles</span>
          <strong>{{ resumen.disciplinas }}</strong>
        </div>
      </div>

      <div class="card">
        <h3 style="margin-bottom:14px">Accesos rápidos</h3>
        <div style="display:flex;flex-direction:column;gap:10px">
          <RouterLink to="/cuotas" class="btn" style="text-align:center">
            Gestionar cuotas
          </RouterLink>
          <RouterLink to="/alumnos" class="btn secondary" style="text-align:center">
            Ver alumnos
          </RouterLink>
          <RouterLink to="/disciplinas" class="btn secondary" style="text-align:center">
            Ver disciplinas
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
