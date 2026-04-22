<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../api/http'
import { formatARS, formatFecha, MESES, nombreMes } from '../utils/format'

const cuotas = ref([])
const loading = ref(true)
const filtroEstado = ref('')
const filtroMes = ref('')
const filtroAnio = ref(new Date().getFullYear())
const filtroTexto = ref('')
let debounceTimer = null

const showPago = ref(false)
const cuotaSeleccionada = ref(null)
const pagoForm = ref({ fechaPago: '', metodoPago: 'EFECTIVO', observaciones: '' })

const showGenerar = ref(false)
const generarForm = ref({
  mes: new Date().getMonth() + 1,
  anio: new Date().getFullYear(),
  fechaVencimiento: ''
})

const anios = [2024, 2025, 2026, 2027]
const metodos = ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'MERCADOPAGO', 'OTRO']

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filtroMes.value) params.mes = filtroMes.value
    if (filtroAnio.value) params.anio = filtroAnio.value
    if (filtroEstado.value) params.estado = filtroEstado.value
    if (filtroTexto.value && filtroTexto.value.trim()) params.texto = filtroTexto.value.trim()
    const { data } = await http.get('/cuotas', { params })
    cuotas.value = data
  } finally {
    loading.value = false
  }
}

function loadDebounced() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(load, 300)
}

function limpiarFiltros() {
  filtroMes.value = ''
  filtroAnio.value = new Date().getFullYear()
  filtroEstado.value = ''
  filtroTexto.value = ''
  load()
}

const totalFiltrado = computed(() =>
  cuotas.value.reduce((s, c) => s + Number(c.monto || 0), 0)
)

const totalPagado = computed(() =>
  cuotas.value.filter(c => c.estado === 'PAGADA').reduce((s, c) => s + Number(c.monto || 0), 0)
)

function abrirPago(c) {
  cuotaSeleccionada.value = c
  pagoForm.value = {
    fechaPago: new Date().toISOString().slice(0, 10),
    metodoPago: 'EFECTIVO',
    observaciones: ''
  }
  showPago.value = true
}

async function registrarPago() {
  await http.post(`/cuotas/${cuotaSeleccionada.value.id}/pagar`, pagoForm.value)
  showPago.value = false
  await load()
}

async function revertir(id) {
  if (!confirm('¿Revertir el pago?')) return
  await http.post(`/cuotas/${id}/revertir-pago`)
  await load()
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta cuota?')) return
  await http.delete(`/cuotas/${id}`)
  await load()
}

async function generarCuotasMes() {
  const body = { ...generarForm.value }
  if (!body.fechaVencimiento) body.fechaVencimiento = null
  const { data } = await http.post('/cuotas/generar-mes', body)
  alert(`Generación completa:
  - Creadas: ${data.creadas}
  - Ya existían: ${data.existentes}
  - Alumnos activos: ${data.alumnosActivos}`)
  showGenerar.value = false
  await load()
}

onMounted(load)
</script>

<template>
  <h2>Cuotas</h2>

  <div class="toolbar">
    <input
      v-model="filtroTexto"
      @input="loadDebounced"
      placeholder="Buscar alumno (nombre, apellido, DNI)..."
      style="max-width:280px"
    />
    <select v-model="filtroMes" @change="load" style="max-width:160px">
      <option value="">Todos los meses</option>
      <option v-for="(m, i) in MESES" :key="i" :value="i + 1">{{ m }}</option>
    </select>
    <select v-model="filtroAnio" @change="load" style="max-width:140px">
      <option value="">Todos los años</option>
      <option v-for="a in anios" :key="a" :value="a">{{ a }}</option>
    </select>
    <select v-model="filtroEstado" @change="load" style="max-width:160px">
      <option value="">Todos los estados</option>
      <option value="PENDIENTE">Pendientes</option>
      <option value="PAGADA">Pagadas</option>
      <option value="VENCIDA">Vencidas</option>
    </select>
    <button class="secondary" @click="limpiarFiltros" title="Limpiar filtros">Limpiar</button>
    <div class="spacer"></div>
    <button @click="showGenerar = true">Generar cuotas del mes</button>
  </div>

  <div class="grid grid-2" style="margin-bottom:20px">
    <div class="card stat">
      <div class="label">Total filtrado</div>
      <div class="value">{{ formatARS(totalFiltrado) }}</div>
    </div>
    <div class="card stat">
      <div class="label">Ya cobrado (filtro)</div>
      <div class="value success">{{ formatARS(totalPagado) }}</div>
    </div>
  </div>

  <div v-if="loading" class="empty">Cargando...</div>
  <div v-else-if="cuotas.length === 0" class="empty">No hay cuotas con ese filtro</div>

  <table v-else>
    <thead>
      <tr>
        <th>Alumno</th>
        <th>Período</th>
        <th>Monto</th>
        <th>Vencimiento</th>
        <th>Estado</th>
        <th>Pago</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="c in cuotas" :key="c.id">
        <td>
          <strong>{{ c.alumno?.apellido }}, {{ c.alumno?.nombre }}</strong>
          <div style="font-size:11px;color:var(--muted)">DNI: {{ c.alumno?.dni }}</div>
        </td>
        <td>{{ nombreMes(c.mes) }} {{ c.anio }}</td>
        <td><strong>{{ formatARS(c.monto) }}</strong></td>
        <td>{{ formatFecha(c.fechaVencimiento) }}</td>
        <td>
          <span :class="['badge', c.estado.toLowerCase()]">{{ c.estado }}</span>
        </td>
        <td>
          <div v-if="c.fechaPago">
            {{ formatFecha(c.fechaPago) }}
            <div style="font-size:11px;color:var(--muted)">{{ c.metodoPago }}</div>
          </div>
          <span v-else style="color:var(--muted)">—</span>
        </td>
        <td style="text-align:right;white-space:nowrap">
          <button v-if="c.estado !== 'PAGADA'" class="small success" @click="abrirPago(c)">Cobrar</button>
          <button v-else class="small secondary" @click="revertir(c.id)">Revertir</button>
          <button class="small danger" style="margin-left:6px" @click="eliminar(c.id)">Borrar</button>
        </td>
      </tr>
    </tbody>
  </table>

  <!-- Modal pago -->
  <div v-if="showPago" class="modal-overlay" @click.self="showPago = false">
    <form class="modal" @submit.prevent="registrarPago">
      <h3>Registrar pago</h3>
      <p style="margin-bottom:16px;color:var(--muted)">
        {{ cuotaSeleccionada?.alumno?.apellido }}, {{ cuotaSeleccionada?.alumno?.nombre }} —
        {{ nombreMes(cuotaSeleccionada?.mes) }} {{ cuotaSeleccionada?.anio }} —
        <strong style="color:var(--text)">{{ formatARS(cuotaSeleccionada?.monto) }}</strong>
      </p>

      <div class="field">
        <label>Fecha de pago</label>
        <input v-model="pagoForm.fechaPago" type="date" required />
      </div>
      <div class="field">
        <label>Método de pago</label>
        <select v-model="pagoForm.metodoPago">
          <option v-for="m in metodos" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>
      <div class="field">
        <label>Observaciones</label>
        <textarea v-model="pagoForm.observaciones" rows="2"></textarea>
      </div>

      <div class="actions">
        <button type="button" class="secondary" @click="showPago = false">Cancelar</button>
        <button type="submit" class="success">Registrar pago</button>
      </div>
    </form>
  </div>

  <!-- Modal generar cuotas -->
  <div v-if="showGenerar" class="modal-overlay" @click.self="showGenerar = false">
    <form class="modal" @submit.prevent="generarCuotasMes">
      <h3>Generar cuotas del mes</h3>
      <p style="margin-bottom:16px;color:var(--muted);font-size:13px">
        Se crearán cuotas para todos los alumnos activos que aún no tengan cuota en el período.
        El monto se calcula sumando los precios de las disciplinas que practica cada alumno.
      </p>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
        <div class="field">
          <label>Mes</label>
          <select v-model.number="generarForm.mes" required>
            <option v-for="(m, i) in MESES" :key="i" :value="i + 1">{{ m }}</option>
          </select>
        </div>
        <div class="field">
          <label>Año</label>
          <select v-model.number="generarForm.anio" required>
            <option v-for="a in anios" :key="a" :value="a">{{ a }}</option>
          </select>
        </div>
      </div>

      <div class="field">
        <label>Fecha de vencimiento (opcional)</label>
        <input v-model="generarForm.fechaVencimiento" type="date" />
        <div style="font-size:11px;color:var(--muted);margin-top:4px">
          Si no se indica, se usa el día 10 del mes.
        </div>
      </div>

      <div class="actions">
        <button type="button" class="secondary" @click="showGenerar = false">Cancelar</button>
        <button type="submit">Generar</button>
      </div>
    </form>
  </div>
</template>
