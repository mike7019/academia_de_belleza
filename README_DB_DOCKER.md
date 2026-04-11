# Base de datos y Docker para Academia de Belleza

Este proyecto incluye un `docker-compose.yml` y un script SQL de inicialización para levantar rápidamente una base de datos PostgreSQL lista para usar desde la aplicación JavaFX.

## Estructura

- `docker-compose.yml`: define el servicio `db` (PostgreSQL 15) con volumen de datos persistente.
- `db/init/01_init_academia.sql`: script de inicialización que crea todas las tablas y datos semilla.

## Cómo levantar la base de datos con Docker

Desde la carpeta raíz del proyecto (`academia_de_belleza`), ejecuta en PowerShell:

```powershell
cd "D:\CUN\Programacion Avanzada\academia_de_belleza"
docker compose up -d db
```

Esto hará lo siguiente:

1. Descargar la imagen `postgres:15-alpine` (si no está en tu máquina).
2. Crear el contenedor `academia_belleza_db`.
3. Crear el volumen `academia_pgdata` para persistir datos.
4. Ejecutar automáticamente `db/init/01_init_academia.sql` al crear la base por primera vez.

La base de datos quedará disponible en:

- Host: `localhost`
- Puerto: `5432`
- Base de datos: `academia_db`
- Usuario: `academia_user`
- Password: `academia_pass`

## Conexión desde la aplicación Java

Configura tu `DatabaseConfig`/`persistence.xml` para apuntar a esta BD, por ejemplo:

- URL JDBC: `jdbc:postgresql://localhost:5432/academia_db`
- Usuario: `academia_user`
- Password: `academia_pass`

## Datos semilla incluidos

El script `01_init_academia.sql` crea:

- Tablas de seguridad: `usuario`, `rol`, `permiso`, `usuario_rol`, `rol_permiso`.
- Tablas académicas: `estudiante`, `maestro`, `curso`, `matricula`.
- Tablas financieras: `pago_estudiante`, `movimiento_financiero`.
- Tablas de nómina: `periodo_nomina`, `nomina_profesor`, `detalle_nomina_profesor`.
- Tabla de auditoría: `auditoria`.

Y además inserta:

- Roles: `ADMIN`, `CAJA`, `ACADEMICO`.
- Permisos básicos para estudiantes, maestros, cursos, matrículas, pagos, nómina y seguridad.
- Usuario administrador inicial:
  - `username`: `admin`
  - `password`: `password` (hash BCrypt de ejemplo, cambia en producción).
- Algunos estudiantes, maestros, cursos, una matrícula y un pago de ejemplo.

## Reinicializar la base de datos (opcional)

Si quieres recrear la base desde cero (borrando todos los datos persistidos), puedes hacer:

```powershell
cd "D:\CUN\Programacion Avanzada\academia_de_belleza"
docker compose down -v
docker compose up -d db
```

El parámetro `-v` elimina también el volumen `academia_pgdata`, de modo que al volver a levantar el servicio se volverá a ejecutar el script de inicialización.

