package org.example.academia.config;

import jakarta.persistence.EntityManager;

/**
 * Clase utilitaria para verificar la conexión a la base de datos y ejecutar
 * una consulta simple de salud. Se puede ejecutar desde IDE o línea de comandos.
 */
public class DatabaseHealthChecker {

    public static void main(String[] args) {
        System.out.println("Comprobando conexión a la base de datos...");
        EntityManager em = null;
        try {
            em = DatabaseConfig.createEntityManager();
            Object result = em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Conexión OK. Resultado prueba: " + result);
        } catch (Exception ex) {
            System.err.println("Fallo al conectar a la base de datos: " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(2);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }
}

