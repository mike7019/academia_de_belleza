package org.example.academia.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de base de datos y JPA.
 *
 * Se encarga de:
 * - Ejecutar migraciones Flyway al inicio (usando la misma URL/credenciales que JPA).
 * - Crear un EntityManagerFactory asociado a la unidad de persistencia definida en persistence.xml.
 * - Exponer métodos estáticos para obtener EntityManager y cerrar recursos al salir de la app.
 */
public class DatabaseConfig {

	private static final String PERSISTENCE_UNIT_NAME = "academiaPU";

	// Permite sobreescribir por variables de entorno si se desea.
	//private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5434/academia_db";
	private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://206.62.139.100:5434/academia_db";
	private static final String DEFAULT_DB_USER   = "academia_user";
	private static final String DEFAULT_DB_PASS   = "academia_pass";

	private static EntityManagerFactory entityManagerFactory;

	static {
		init();
	}

	private static void init() {
		// Permitir configuración por variables de entorno para mayor flexibilidad.
		String url  = getEnvOrDefault("ACADEMIA_DB_URL", DEFAULT_JDBC_URL);
		String user = getEnvOrDefault("ACADEMIA_DB_USER", DEFAULT_DB_USER);
		String pass = getEnvOrDefault("ACADEMIA_DB_PASSWORD", DEFAULT_DB_PASS);

		// Primero ejecutar migraciones Flyway (no hay migraciones V1, se usa baselineOnMigrate)
		Flyway flyway = Flyway.configure()
				.dataSource(url, user, pass)
				.locations("classpath:db/migration")
				.baselineOnMigrate(true)
				.baselineVersion("1")
				.load();
		flyway.migrate();

		// Después inicializar el EntityManagerFactory usando la misma configuración JDBC
		Map<String, Object> props = new HashMap<>();
		props.put("jakarta.persistence.jdbc.url", url);
		props.put("jakarta.persistence.jdbc.user", user);
		props.put("jakarta.persistence.jdbc.password", pass);
		props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");

		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
	}

	private static String getEnvOrDefault(String envName, String defaultValue) {
		String value = System.getenv(envName);
		return (value == null || value.isBlank()) ? defaultValue : value;
	}

	/**
	 * Obtiene el EntityManagerFactory singleton de la aplicación.
	 */
	public static EntityManagerFactory getEntityManagerFactory() {
		if (entityManagerFactory == null) {
			init();
		}
		return entityManagerFactory;
	}

	/**
	 * Crea un nuevo EntityManager asociado a la unidad de persistencia.
	 */
	public static EntityManager createEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}

	/**
	 * Cierra el EntityManagerFactory (llamar al cerrar la aplicación).
	 */
	public static void shutdown() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}
}


