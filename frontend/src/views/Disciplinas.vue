<script setup>
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { formatARS } from '../utils/format'

const disciplinas = ref([])
const loading = ref(true)
const showModal = ref(false)
const editing = ref(null)
const form = ref({ nombre: '', precioMensual: 0, descripcion: '', activa: true })

async function load() {
  loading.value = true
  try {
    const { data } = await http.get('/disciplinas')
    disciplinas.value = data
  } finally {
    loading.value = false
  }
}

function nuevo() {
  editing.value = null
  form.value = { nombre: '', precioMensual: 0, descripcion: '', activa: true }
  showModal.value = true
}

function editar(d) {
  editing.value = d.id
  form.value = { ...d }
  showModal.value = true
}

async function guardar() {
  if (editing.value) {
    await http.put(`/disciplinas/${editing.value}`, form.value)
  } else {
    await http.post('/disciplinas', form.value)
  }
  showModal.value = false
  await load()
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta disciplina?')) return
  try {
    await http.delete(`/disciplinas/${id}`)
    await load()
  } catch (e) {
    alert('No se puede eliminar: la disciplina está asignada a alumnos.')
  }
}

onMounted(load)
</script>

<template>
  <h2>Disciplinas</h2>

  <div class="toolbar">
    <div class="spacer"></div>
    <button @click="nuevo">+ Nueva disciplina</button>
  </div>

  <div v-if="loading" class="empty">Cargando...</div>
  <div v-else-if="disciplinas.length === 0" class="empty">No hay disciplinas cargadas</div>

  <table v-else>
    <thead>
      <tr>
        <th>Nombre</th>
        <th>Precio mensual</th>
        <th>Descripción</th>
        <th>Estado</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="d in disciplinas" :key="d.id">
        <td><strong>{{ d.nombre }}</strong></td>
        <td>{{ formatARS(d.precioMensual) }}</td>
        <td style="color:var(--muted)">{{ d.descripcion }}</td>
        <td>
          <span :class="['badge', d.activa ? 'pagada' : 'vencida']">
            {{ d.activa ? 'Activa' : 'Inactiva' }}
          </span>
        </td>
        <td style="text-align:right;white-space:nowrap">
          <button class="small secondary" @click="editar(d)">Editar</button>
          <button class="small danger" style="margin-left:6px" @click="eliminar(d.id)">Borrar</button>
        </td>
      </tr>
    </tbody>
  </table>

  <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
    <form class="modal" @submit.prevent="guardar">
      <h3>{{ editing ? 'Editar disciplina' : 'Nueva disciplina' }}</h3>

      <div class="field">
        <label>Nombre</label>
        <input v-model="form.nombre" required />
      </div>
      <div class="field">
        <label>Precio mensual (ARS)</label>
        <input v-model.number="form.precioMensual" type="number" step="0.01" required />
      </div>
      <div class="field">
        <label>Descripción</label>
        <textarea v-model="form.descripcion" rows="3"></textarea>
      </div>
      <div class="field">
        <label>
          <input type="checkbox" v-model="form.activa" style="width:auto;margin-right:8px" />
          Activa
        </label>
      </div>

      <div class="actions">
        <button type="button" class="secondary" @click="showModal = false">Cancelar</button>
        <button type="submit">Guardar</button>
      </div>
    </form>
  </div>
</template>
