package com.example.demostracion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DbInspector {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/auto?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = ""; // cambiar si tu MySQL usa contraseña

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement st = conn.createStatement()) {

            System.out.println("Conectado a la BD: " + url);

            String[] queries = new String[] {
                "SHOW CREATE TABLE usuario",
                "SHOW CREATE TABLE mensaje",
                "SELECT TABLE_NAME, ENGINE, TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'auto' AND TABLE_NAME IN ('rol','usuario','mensaje','adjunto')"
            };

            for (String q : queries) {
                System.out.println("\n-- Ejecutando: " + q);
                try (ResultSet rs = st.executeQuery(q)) {
                    ResultSetMetaData md = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String label = md.getColumnLabel(i);
                            String value = rs.getString(i);
                            System.out.println(label + ": " + value);
                        }
                        System.out.println("----");
                    }
                } catch (Exception e) {
                    System.err.println("Error ejecutando query: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }

        } catch (Exception e) {
            System.err.println("No se pudo conectar o ejecutar consultas: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
