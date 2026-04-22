<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../api/http'
import { formatFecha } from '../utils/format'

const alumnos = ref([])
const disciplinas = ref([])
const loading = ref(true)
const filtro = ref('')
const showModal = ref(false)
const editing = ref(null)
const form = ref(vacio())

function vacio() {
  return {
    nombre: '', apellido: '', dni: '', email: '', telefono: '',
    fechaNacimiento: '', fechaAlta: '', contactoEmergencia: '',
    activo: true, disciplinaIds: []
  }
}

async function load() {
  loading.value = true
  try {
    const [a, d] = await Promise.all([
      http.get('/alumnos'),
      http.get('/disciplinas')
    ])
    alumnos.value = a.data
    disciplinas.value = d.data
  } finally {
    loading.value = false
  }
}

const filtrados = computed(() => {
  if (!filtro.value) return alumnos.value
  const q = filtro.value.toLowerCase()
  return alumnos.value.filter(a =>
    `${a.nombre} ${a.apellido} ${a.dni || ''} ${a.email || ''}`.toLowerCase().includes(q)
  )
})

function nuevo() {
  editing.value = null
  form.value = vacio()
  form.value.fechaAlta = new Date().toISOString().slice(0, 10)
  showModal.value = true
}

function editar(a) {
  editing.value = a.id
  form.value = {
    nombre: a.nombre, apellido: a.apellido, dni: a.dni, email: a.email, telefono: a.telefono,
    fechaNacimiento: a.fechaNacimiento || '',
    fechaAlta: a.fechaAlta || '',
    contactoEmergencia: a.contactoEmergencia || '',
    activo: a.activo,
    disciplinaIds: (a.disciplinas || []).map(d => d.id)
  }
  showModal.value = true
}

function toggleDisciplina(id) {
  const i = form.value.disciplinaIds.indexOf(id)
  if (i >= 0) form.value.disciplinaIds.splice(i, 1)
  else form.value.disciplinaIds.push(id)
}

async function guardar() {
  const body = { ...form.value }
  if (!body.fechaNacimiento) body.fechaNacimiento = null
  if (!body.fechaAlta) body.fechaAlta = null

  if (editing.value) {
    await http.put(`/alumnos/${editing.value}`, body)
  } else {
    await http.post('/alumnos', body)
  }
  showModal.value = false
  await load()
}

async function eliminar(id) {
  if (!confirm('¿Eliminar este alumno? Se borrarán también sus cuotas.')) return
  await http.delete(`/alumnos/${id}`)
  await load()
}

onMounted(load)
</script>

<template>
  <h2>Alumnos</h2>

  <div class="toolbar">
    <input v-model="filtro" placeholder="Buscar por nombre, apellido, DNI o email..." style="max-width:400px" />
    <div class="spacer"></div>
    <button @click="nuevo">+ Nuevo alumno</button>
  </div>

  <div v-if="loading" class="empty">Cargando...</div>
  <div v-else-if="filtrados.length === 0" class="empty">Sin resultados</div>

  <table v-else>
    <thead>
      <tr>
        <th>Nombre</th>
        <th>DNI</th>
        <th>Contacto</th>
        <th>Disciplinas</th>
        <th>Alta</th>
        <th>Estado</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="a in filtrados" :key="a.id">
        <td><strong>{{ a.apellido }}, {{ a.nombre }}</strong></td>
        <td>{{ a.dni }}</td>
        <td>
          <div>{{ a.telefono }}</div>
          <div style="font-size:11px;color:var(--muted)">{{ a.email }}</div>
        </td>
        <td>
          <div class="chip-group">
            <span v-for="d in a.disciplinas" :key="d.id" class="chip">{{ d.nombre }}</span>
          </div>
        </td>
        <td>{{ formatFecha(a.fechaAlta) }}</td>
        <td>
          <span :class="['badge', a.activo ? 'pagada' : 'vencida']">
            {{ a.activo ? 'Activo' : 'Inactivo' }}
          </span>
        </td>
        <td style="text-align:right;white-space:nowrap">
          <button class="small secondary" @click="editar(a)">Editar</button>
          <button class="small danger" style="margin-left:6px" @click="eliminar(a.id)">Borrar</button>
        </td>
      </tr>
    </tbody>
  </table>

  <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
    <form class="modal" @submit.prevent="guardar">
      <h3>{{ editing ? 'Editar alumno' : 'Nuevo alumno' }}</h3>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
        <div class="field">
          <label>Nombre</label>
          <input v-model="form.nombre" required />
        </div>
        <div class="field">
          <label>Apellido</label>
          <input v-model="form.apellido" required />
        </div>
        <div class="field">
          <label>DNI</label>
          <input v-model="form.dni" />
        </div>
        <div class="field">
          <label>Teléfono</label>
          <input v-model="form.telefono" />
        </div>
        <div class="field" style="grid-column:span 2">
          <label>Email</label>
          <input v-model="form.email" type="email" />
        </div>
        <div class="field">
          <label>Fecha nacimiento</label>
          <input v-model="form.fechaNacimiento" type="date" />
        </div>
        <div class="field">
          <label>Fecha de alta</label>
          <input v-model="form.fechaAlta" type="date" />
        </div>
        <div class="field" style="grid-column:span 2">
          <label>Contacto de emergencia</label>
          <input v-model="form.contactoEmergencia" />
        </div>
      </div>

      <div class="field">
        <label>Disciplinas</label>
        <div class="chip-group">
          <span
            v-for="d in disciplinas"
            :key="d.id"
            :class="['chip', 'selectable', form.disciplinaIds.includes(d.id) ? 'selected' : '']"
            @click="toggleDisciplina(d.id)"
          >
            {{ d.nombre }}
          </span>
        </div>
      </div>

      <div class="field">
        <label>
          <input type="checkbox" v-model="form.activo" style="width:auto;margin-right:8px" />
          Activo
        </label>
      </div>

      <div class="actions">
        <button type="button" class="secondary" @click="showModal = false">Cancelar</button>
        <button type="submit">Guardar</button>
      </div>
    </form>
  </div>
</template>
