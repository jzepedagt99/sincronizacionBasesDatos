
package com.proyecto.mavenproject2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Javier Zepeda
 */
public class ConexionBD {
    
    // Conexion a Mysql
    public static Connection getConnectionMysql() {
        
        Connection conMysql = null;
        String URL = "jdbc:mysql://localhost:3306/prestamo";
        String USER = "root";
        String PASSWORD = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conMysql = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos");
        } catch (Exception e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conMysql;
    }
    
    
    // Conexión PostgreSQL
    public static Connection getPostgresConnection() {
        String url = "jdbc:postgresql://localhost:5432/mi_postgres";
        String user = "postgres";
        String password = "12345";

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
