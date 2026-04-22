# Deploy - Pride Gym App

Guía para deployar el backend en **Render** y el frontend en **Vercel**, ambos desde el repo `https://github.com/Mats210705/prideGymApp`.

## Orden recomendado

1. Primero Render (backend) — necesitás la URL pública para configurar Vercel
2. Después Vercel (frontend) con esa URL
3. Finalmente volver a Render y setear `CORS_ORIGINS` con la URL de Vercel

---

## 1. Backend en Render

### 1.1 — Crear el servicio

1. Entrar a https://render.com y loguearse con GitHub (autorizar acceso al repo `prideGymApp`)
2. Dashboard → **New +** → **Web Service**
3. Seleccionar el repo `Mats210705/prideGymApp`
4. Configurar así:

| Campo | Valor |
|---|---|
| **Name** | `pridegym-backend` |
| **Region** | Oregon (o el más cercano) |
| **Branch** | `main` |
| **Root Directory** | `backend` |
| **Runtime / Environment** | `Docker` |
| **Dockerfile Path** | `./Dockerfile` (relativo al root directory) |
| **Instance Type** | `Free` |

5. **NO** hacer clic en "Create Web Service" todavía — primero configurar variables de entorno (sección siguiente)

### 1.2 — Variables de entorno (Environment Variables)

En la sección **Environment** agregar:

| Key | Value |
|---|---|
| `JWT_SECRET` | Generá uno largo (mín 32 chars). Ejemplo: `pridegym-prod-jwt-secret-minimo-256-bits-para-hs256-xxxxxxxxx` |
| `CORS_ORIGINS` | Dejá vacío por ahora — lo seteás después de tener la URL de Vercel |

> ⚠️ Hasta que no configures `CORS_ORIGINS`, el frontend no va a poder hablar con el backend.

### 1.3 — Deploy

1. Click **Create Web Service**
2. Render empieza a buildear el Docker (tarda **5–10 min** la primera vez, luego 2–3 min)
3. Cuando esté listo vas a tener una URL tipo `https://pridegym-backend.onrender.com`
4. Verificá que levantó: abrí `https://pridegym-backend.onrender.com/api/auth/login` en el navegador — debería devolver `Method 'GET' is not supported` (eso significa que el endpoint existe, solo que solo acepta POST)

### ⚠️ Aviso plan Free de Render

- El servicio **se duerme tras 15 min de inactividad**. La primera request después del sleep tarda ~30-60 seg en responder mientras se reinicia.
- **Cada reinicio borra todos los datos** (porque H2 está en memoria) y vuelve a cargar los de DataLoader.
- Para demo al cliente está perfecto. Para uso real, ver sección "Migrar a Postgres" al final.

---

## 2. Frontend en Vercel

### 2.1 — Importar el proyecto

1. Entrar a https://vercel.com y loguearse con GitHub
2. **Add New...** → **Project**
3. Importar el repo `Mats210705/prideGymApp`
4. Configurar:

| Campo | Valor |
|---|---|
| **Project Name** | `pridegym-app` |
| **Framework Preset** | `Vite` (debería detectarse solo) |
| **Root Directory** | `frontend` (importante — click en "Edit" y seleccionar) |
| **Build Command** | `npm run build` (auto) |
| **Output Directory** | `dist` (auto) |
| **Install Command** | `npm install` (auto) |

### 2.2 — Variable de entorno

En la sección **Environment Variables**:

| Key | Value |
|---|---|
| `VITE_API_URL` | `https://pridegym-backend.onrender.com` (la URL que te dio Render, **sin barra al final**) |

### 2.3 — Deploy

1. Click **Deploy**
2. Tarda 1–2 min
3. Te da una URL tipo `https://pridegym-app.vercel.app`

---

## 3. Cerrar el círculo — Configurar CORS en Render

Ahora que tenés la URL de Vercel, actualizá el backend:

1. Volver al servicio en Render → **Environment**
2. Agregar/editar la variable:

| Key | Value |
|---|---|
| `CORS_ORIGINS` | `https://pridegym-app.vercel.app,https://*.vercel.app` |

> El patrón `https://*.vercel.app` permite también los deploys de preview que Vercel genera por cada PR/push.

3. Guardar → Render redeployea automáticamente (tarda 2–3 min)

---

## 4. Probar

1. Abrir `https://pridegym-app.vercel.app`
2. Si hace varios minutos que nadie usa el backend, el login va a tardar 30-60 seg (Render wake-up). Después vuelve a ir rápido.
3. Login: `admin` / `admin123`
4. Debería cargar el dashboard con los datos demo

### Si algo falla

- **Login da error de red / CORS**: revisá que `CORS_ORIGINS` en Render incluya exactamente la URL de Vercel (con `https://`, sin barra final)
- **Login queda girando mucho**: backend está dormido, esperar ~1 min y reintentar
- **Ver logs del backend**: Render → servicio → pestaña **Logs**
- **Ver build del frontend**: Vercel → proyecto → pestaña **Deployments** → abrir el último

---

## 5. (Opcional) Migrar a Postgres para que los datos persistan

Cuando el cliente quiera empezar a cargar alumnos reales, migrar así:

### Render — crear Postgres free

1. Dashboard → **New +** → **PostgreSQL**
2. Name: `pridegym-db`, Plan: `Free`
3. Una vez creado, copiar el **Internal Database URL** (formato `postgres://user:pass@host/db`)

### Backend — agregar dependencia

En `backend/pom.xml`, dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Render — setear env vars del backend

| Key | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | (el Internal Database URL, pero empezando con `jdbc:postgresql://` en vez de `postgres://`) |
| `SPRING_DATASOURCE_USERNAME` | (el user que da Render) |
| `SPRING_DATASOURCE_PASSWORD` | (el password) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
| `SPRING_JPA_DATABASE_PLATFORM` | `org.hibernate.dialect.PostgreSQLDialect` |

### Ajustar DataLoader

El `DataLoader` ya checkea si hay datos (`count() == 0`) antes de insertar, así que con Postgres solo se van a cargar los demos la primera vez. Después podés borrar los demos desde la UI y cargar los reales.

> Free tier Postgres de Render: 1GB de storage, se borra a los 90 días si no tenés plan pago. Para producción real conviene plan pago ($7/mes) o usar Supabase / Neon (ambos con free tier más generoso).
