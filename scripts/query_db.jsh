import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

String url = "jdbc:mysql://localhost:3306/auto?useSSL=false&serverTimezone=UTC";
String user = "root";
String pass = "";

try (Connection conn = DriverManager.getConnection(url, user, pass);
     Statement stmt = conn.createStatement()) {
    System.out.println("Connected to DB");

    try (ResultSet rs = stmt.executeQuery("SELECT id_usuario, correo, contrasena, rol_idrol FROM usuario WHERE correo='admin@test.com'")) {
        while (rs.next()) {
            System.out.println("id=" + rs.getLong("id_usuario") +
                               " correo=" + rs.getString("correo") +
                               " password=" + rs.getString("contrasena") +
                               " rol_id=" + rs.getLong("rol_idrol"));
        }
    }

    try (ResultSet rs = stmt.executeQuery("SELECT id_usuario, correo, contrasena, rol_idrol FROM usuario LIMIT 5")) {
        System.out.println("--- Sample users ---");
        while (rs.next()) {
            System.out.println(rs.getLong("id_usuario") + " | " + rs.getString("correo") + " | " + rs.getString("contrasena") + " | " + rs.getLong("rol_idrol"));
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}
