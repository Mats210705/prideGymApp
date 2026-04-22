# Pride Gym - Administración

Web app para administrar alumnos, disciplinas y cuotas mensuales de **Pride Gym Resistencia** (boxeo, MMA, Muay Thai, BJJ, kickboxing, funcional).

## Stack

- **Backend**: Spring Boot 3.3 + Java 17 + Spring Security (JWT) + Spring Data JPA + H2 en memoria
- **Frontend**: Vue 3 + Vite + Vue Router + Axios + CSS puro
- **DB**: H2 en memoria — se reinicia al reiniciar el backend (ideal para demo/pruebas)

## Requisitos

- Java 17
- Maven 3.9+ (o usar el wrapper si lo agregás después)
- Node.js 18+ y npm

## Cómo ejecutar

### 1) Backend

```bash
cd backend
mvn spring-boot:run
```

Levanta en **http://localhost:8080**

- API: `http://localhost:8080/api`
- Consola H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:pridegymdb`
  - User: `sa`
  - Password: *(vacío)*

### 2) Frontend

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abrí **http://localhost:5173**

## Credenciales demo

- Usuario: `admin`
- Contraseña: `admin123`

## Datos de ejemplo precargados

Al iniciar el backend se cargan automáticamente:

- **6 disciplinas**: Boxeo, MMA, Muay Thai, Brazilian Jiu-Jitsu, Kickboxing, Funcional
- **8 alumnos** con sus disciplinas asignadas
- **Cuotas** del mes anterior (todas pagadas) y del mes actual (mitad pagadas, mitad pendientes)

## Funcionalidades

- **Dashboard**: alumnos activos, ingresos del mes, cuotas pendientes, cuotas vencidas
- **Alumnos**: CRUD, filtro de búsqueda, asignación de disciplinas con chips
- **Disciplinas**: CRUD con precio mensual en ARS
- **Cuotas**:
  - Listado con filtros por mes, año y estado
  - Totales calculados en vivo
  - Registrar pago (fecha, método)
  - Revertir pago
  - **Generación masiva de cuotas del mes** para todos los alumnos activos
  - Marcado automático de vencidas al consultar

## Estructura

```
pridegym-app/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/pridegym/
│       ├── PrideGymApplication.java
│       ├── config/        (SecurityConfig, DataLoader)
│       ├── controller/    (Auth, Alumno, Disciplina, Cuota, Dashboard)
│       ├── dto/
│       ├── model/         (Usuario, Alumno, Disciplina, Cuota, enums)
│       ├── repository/
│       └── security/      (JwtService, JwtAuthFilter)
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/
        ├── api/           (axios con interceptor JWT)
        ├── utils/         (format ARS, fechas)
        ├── views/         (Login, Dashboard, Alumnos, Disciplinas, Cuotas)
        └── assets/main.css
```

## Notas de producción

Para pasar a producción habría que:

1. Cambiar H2 por Postgres/MySQL (basta con ajustar `application.properties` y `pom.xml`)
2. Mover `app.jwt.secret` a variable de entorno
3. Endurecer CORS (hoy está abierto a `localhost:5173`)
4. Agregar roles/usuarios adicionales y gestión de usuarios desde la UI
5. Hashear contraseñas al crear usuarios desde la UI (ya se hace en `DataLoader` con BCrypt)
