package com.example.demostracion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbMigrator {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/auto?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = ""; // cambiar si tu MySQL usa contraseña

        String[] statements = new String[] {
            // Cambiar los tipos de columna a INT(11) para coincidir con usuario.id_usuario (int)
            "ALTER TABLE mensaje MODIFY destinatario_id INT(11) DEFAULT NULL",
            "ALTER TABLE mensaje MODIFY remitente_id INT(11) DEFAULT NULL",
            // Añadir constraints si no existen
            "ALTER TABLE mensaje ADD CONSTRAINT FK_mensaje_remitente FOREIGN KEY (remitente_id) REFERENCES usuario(id_usuario)",
            "ALTER TABLE mensaje ADD CONSTRAINT FK_mensaje_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario(id_usuario)"
        };

        try (Connection conn = DriverManager.getConnection(url, user, pass); Statement st = conn.createStatement()) {
            System.out.println("Conectado a BD: " + url);

            for (String s : statements) {
                try {
                    System.out.println("Ejecutando: " + s);
                    st.execute(s);
                    System.out.println("OK");
                } catch (Exception e) {
                    System.err.println("Error ejecutando statement: " + e.getMessage());
                }
            }

            System.out.println("\n-- SHOW CREATE TABLE mensaje --");
            try (ResultSet rs = st.executeQuery("SHOW CREATE TABLE mensaje")) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString(1));
                    System.out.println(rs.getString(2));
                }
            }

        } catch (Exception e) {
            System.err.println("Fallo migración: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.out.println("Migración finalizada.");
    }
}
