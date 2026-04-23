<script setup>
import { ref, onMounted, computed } from 'vue'
import http from '../api/http'
import { formatFecha } from '../utils/format'

const rutinas = ref([])
const disciplinas = ref([])
const alumnos = ref([])
const loading = ref(true)

// Filtros
const filtroNivel = ref('')
const filtroDisciplinaId = ref('')
const filtroAlumnoId = ref('')
const filtroTexto = ref('')
const soloActivas = ref(false)
let debounceTimer = null

const niveles = ['PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO']

// Modal edicion/creacion
const showModal = ref(false)
const editando = ref(null) // null = crear, objeto = editar
const form = ref(nuevaRutinaVacia())

function nuevaRutinaVacia() {
  return {
    id: null,
    nombre: '',
    descripcion: '',
    nivel: 'PRINCIPIANTE',
    disciplina: null,
    alumno: null,
    activa: true,
    ejercicios: []
  }
}

function ejercicioVacio() {
  return { nombre: '', series: null, repeticiones: '', descanso: '', notas: '' }
}

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filtroNivel.value) params.nivel = filtroNivel.value
    if (filtroDisciplinaId.value) params.disciplinaId = filtroDisciplinaId.value
    if (filtroAlumnoId.value) params.alumnoId = filtroAlumnoId.value
    if (filtroTexto.value && filtroTexto.value.trim()) params.texto = filtroTexto.value.trim()
    if (soloActivas.value) params.soloActivas = true

    const { data } = await http.get('/rutinas', { params })
    rutinas.value = data
  } finally {
    loading.value = false
  }
}

function loadDebounced() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(load, 300)
}

function limpiarFiltros() {
  filtroNivel.value = ''
  filtroDisciplinaId.value = ''
  filtroAlumnoId.value = ''
  filtroTexto.value = ''
  soloActivas.value = false
  load()
}

async function loadCatalogos() {
  const [d, a] = await Promise.all([
    http.get('/disciplinas'),
    http.get('/alumnos')
  ])
  disciplinas.value = d.data
  alumnos.value = a.data
}

const totalRutinas = computed(() => rutinas.value.length)
const activasCount = computed(() => rutinas.value.filter(r => r.activa).length)

function abrirCrear() {
  editando.value = null
  form.value = nuevaRutinaVacia()
  showModal.value = true
}

function abrirEditar(r) {
  editando.value = r
  form.value = {
    id: r.id,
    nombre: r.nombre,
    descripcion: r.descripcion || '',
    nivel: r.nivel,
    disciplina: r.disciplina ? { id: r.disciplina.id } : null,
    alumno: r.alumno ? { id: r.alumno.id } : null,
    activa: r.activa,
    ejercicios: (r.ejercicios || []).map(e => ({
      nombre: e.nombre || '',
      series: e.series,
      repeticiones: e.repeticiones || '',
      descanso: e.descanso || '',
      notas: e.notas || ''
    }))
  }
  showModal.value = true
}

function agregarEjercicio() {
  form.value.ejercicios.push(ejercicioVacio())
}

function quitarEjercicio(i) {
  form.value.ejercicios.splice(i, 1)
}

function moverEjercicio(i, delta) {
  const j = i + delta
  if (j < 0 || j >= form.value.ejercicios.length) return
  const arr = form.value.ejercicios
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}

async function guardar() {
  const payload = {
    ...form.value,
    disciplina: form.value.disciplina?.id ? { id: form.value.disciplina.id } : null,
    alumno: form.value.alumno?.id ? { id: form.value.alumno.id } : null,
    ejercicios: form.value.ejercicios.map((e, idx) => ({ ...e, orden: idx + 1 }))
  }
  if (editando.value) {
    await http.put(`/rutinas/${editando.value.id}`, payload)
  } else {
    await http.post('/rutinas', payload)
  }
  showModal.value = false
  await load()
}

async function duplicar(id) {
  await http.post(`/rutinas/${id}/duplicar`)
  await load()
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta rutina? Esta accion no se puede deshacer.')) return
  await http.delete(`/rutinas/${id}`)
  await load()
}

function nombreDisciplina(id) {
  return disciplinas.value.find(d => d.id === id)?.nombre || '-'
}

function nombreAlumno(a) {
  if (!a) return null
  return `${a.apellido}, ${a.nombre}`
}

onMounted(async () => {
  await loadCatalogos()
  await load()
})
</script>

<template>
  <h2>Rutinas</h2>

  <div class="toolbar">
    <input
      v-model="filtroTexto"
      @input="loadDebounced"
      placeholder="Buscar por nombre o descripcion..."
      style="max-width:260px"
    />
    <select v-model="filtroNivel" @change="load" style="max-width:160px">
      <option value="">Todos los niveles</option>
      <option v-for="n in niveles" :key="n" :value="n">{{ n }}</option>
    </select>
    <select v-model="filtroDisciplinaId" @change="load" style="max-width:180px">
      <option value="">Todas las disciplinas</option>
      <option v-for="d in disciplinas" :key="d.id" :value="d.id">{{ d.nombre }}</option>
    </select>
    <select v-model="filtroAlumnoId" @change="load" style="max-width:200px">
      <option value="">Todos los alumnos</option>
      <option v-for="a in alumnos" :key="a.id" :value="a.id">
        {{ a.apellido }}, {{ a.nombre }}
      </option>
    </select>
    <label style="display:flex;align-items:center;gap:6px;font-size:12px;text-transform:none;letter-spacing:0">
      <input type="checkbox" v-model="soloActivas" @change="load" style="width:auto" />
      Solo activas
    </label>
    <button class="secondary" @click="limpiarFiltros">Limpiar</button>
    <div class="spacer"></div>
    <button @click="abrirCrear">Nueva rutina</button>
  </div>

  <div class="grid grid-2" style="margin-bottom:20px">
    <div class="card stat">
      <div class="label">Total rutinas</div>
      <div class="value">{{ totalRutinas }}</div>
    </div>
    <div class="card stat">
      <div class="label">Activas</div>
      <div class="value success">{{ activasCount }}</div>
    </div>
  </div>

  <div v-if="loading" class="empty">Cargando...</div>
  <div v-else-if="rutinas.length === 0" class="empty">No hay rutinas con ese filtro</div>

  <div v-else class="grid" style="grid-template-columns:repeat(auto-fill, minmax(360px, 1fr))">
    <div v-for="r in rutinas" :key="r.id" class="card" style="display:flex;flex-direction:column;gap:10px">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:10px">
        <div style="flex:1">
          <strong style="font-size:15px">{{ r.nombre }}</strong>
          <div style="font-size:11px;color:var(--muted);margin-top:2px">
            Creada: {{ formatFecha(r.fechaCreacion) }}
          </div>
        </div>
        <span v-if="!r.activa" class="badge vencida">INACTIVA</span>
      </div>

      <div class="chip-group">
        <span class="chip" :class="{ 'selected': true }">{{ r.nivel }}</span>
        <span v-if="r.disciplina" class="chip">{{ r.disciplina.nombre }}</span>
        <span v-if="r.alumno" class="chip" style="background:rgba(230,57,70,0.15);border-color:var(--primary);color:var(--primary)">
          {{ nombreAlumno(r.alumno) }}
        </span>
        <span v-else class="chip">Generica</span>
      </div>

      <div v-if="r.descripcion" style="font-size:13px;color:var(--muted);line-height:1.4">
        {{ r.descripcion }}
      </div>

      <div v-if="r.ejercicios && r.ejercicios.length" style="border-top:1px solid var(--border);padding-top:10px">
        <div style="font-size:11px;color:var(--muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:6px">
          Ejercicios ({{ r.ejercicios.length }})
        </div>
        <ol style="padding-left:20px;font-size:13px;line-height:1.6">
          <li v-for="(e, i) in r.ejercicios" :key="i">
            <strong>{{ e.nombre }}</strong>
            <span v-if="e.series || e.repeticiones" style="color:var(--muted)">
              — {{ e.series ? e.series + 'x' : '' }}{{ e.repeticiones }}
            </span>
            <span v-if="e.descanso && e.descanso !== '-'" style="color:var(--muted)">
              · descanso {{ e.descanso }}
            </span>
            <div v-if="e.notas" style="font-size:11px;color:var(--muted);font-style:italic">
              {{ e.notas }}
            </div>
          </li>
        </ol>
      </div>

      <div style="display:flex;gap:6px;margin-top:auto;flex-wrap:wrap">
        <button class="small secondary" @click="abrirEditar(r)">Editar</button>
        <button class="small secondary" @click="duplicar(r.id)">Duplicar</button>
        <button class="small danger" @click="eliminar(r.id)">Borrar</button>
      </div>
    </div>
  </div>

  <!-- Modal edicion/creacion -->
  <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
    <form class="modal" style="width:720px" @submit.prevent="guardar">
      <h3>{{ editando ? 'Editar rutina' : 'Nueva rutina' }}</h3>

      <div class="field">
        <label>Nombre</label>
        <input v-model="form.nombre" required maxlength="150" />
      </div>

      <div class="field">
        <label>Descripcion</label>
        <textarea v-model="form.descripcion" rows="2" maxlength="1000"></textarea>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px">
        <div class="field">
          <label>Nivel</label>
          <select v-model="form.nivel" required>
            <option v-for="n in niveles" :key="n" :value="n">{{ n }}</option>
          </select>
        </div>
        <div class="field">
          <label>Disciplina</label>
          <select :value="form.disciplina?.id || ''"
                  @change="e => form.disciplina = e.target.value ? { id: Number(e.target.value) } : null">
            <option value="">(ninguna)</option>
            <option v-for="d in disciplinas" :key="d.id" :value="d.id">{{ d.nombre }}</option>
          </select>
        </div>
        <div class="field">
          <label>Alumno asignado</label>
          <select :value="form.alumno?.id || ''"
                  @change="e => form.alumno = e.target.value ? { id: Number(e.target.value) } : null">
            <option value="">(generica)</option>
            <option v-for="a in alumnos" :key="a.id" :value="a.id">
              {{ a.apellido }}, {{ a.nombre }}
            </option>
          </select>
        </div>
      </div>

      <div class="field">
        <label style="display:flex;align-items:center;gap:8px;text-transform:none;letter-spacing:0">
          <input type="checkbox" v-model="form.activa" style="width:auto" />
          Rutina activa
        </label>
      </div>

      <div style="border-top:1px solid var(--border);padding-top:16px;margin-top:8px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
          <strong>Ejercicios</strong>
          <button type="button" class="small" @click="agregarEjercicio">+ Agregar</button>
        </div>

        <div v-if="form.ejercicios.length === 0" class="empty" style="padding:20px">
          Todavia no agregaste ejercicios.
        </div>

        <div v-for="(e, i) in form.ejercicios" :key="i" class="card" style="padding:12px;margin-bottom:10px;background:var(--panel-2)">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
            <strong style="font-size:13px">#{{ i + 1 }}</strong>
            <div style="display:flex;gap:4px">
              <button type="button" class="small secondary" @click="moverEjercicio(i, -1)" :disabled="i === 0">↑</button>
              <button type="button" class="small secondary" @click="moverEjercicio(i, 1)" :disabled="i === form.ejercicios.length - 1">↓</button>
              <button type="button" class="small danger" @click="quitarEjercicio(i)">Quitar</button>
            </div>
          </div>

          <div class="field" style="margin-bottom:8px">
            <label>Nombre del ejercicio</label>
            <input v-model="e.nombre" required maxlength="200" placeholder="Ej: Saltar soga" />
          </div>

          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px">
            <div class="field" style="margin-bottom:0">
              <label>Series</label>
              <input v-model.number="e.series" type="number" min="0" placeholder="3" />
            </div>
            <div class="field" style="margin-bottom:0">
              <label>Repeticiones</label>
              <input v-model="e.repeticiones" maxlength="50" placeholder="10-12 o 30 seg" />
            </div>
            <div class="field" style="margin-bottom:0">
              <label>Descanso</label>
              <input v-model="e.descanso" maxlength="50" placeholder="60s" />
            </div>
          </div>

          <div class="field" style="margin-top:8px;margin-bottom:0">
            <label>Notas (opcional)</label>
            <input v-model="e.notas" maxlength="300" placeholder="Tecnica, peso, observaciones" />
          </div>
        </div>
      </div>

      <div class="actions">
        <button type="button" class="secondary" @click="showModal = false">Cancelar</button>
        <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear rutina' }}</button>
      </div>
    </form>
  </div>
</template>
