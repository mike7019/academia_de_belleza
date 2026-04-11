# Migraciones Flyway

Coloca aquí los scripts de migración de esquema gestionados por Flyway, siguiendo la convención:

- `V2__descripcion_corta.sql`
- `V3__otra_migracion.sql`

El bootstrap inicial del esquema se realiza actualmente con el script:

- `db/init/01_init_academia.sql` (ejecutado por Docker al crear el contenedor de PostgreSQL por primera vez).

Flyway está configurado con `baselineOnMigrate=true` y `baselineVersion=1`, por lo que las migraciones deben empezar en la versión `2`.

