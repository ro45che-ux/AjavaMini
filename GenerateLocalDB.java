import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class GenerateLocalDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cloud_db", "root", "root123");
            
            // Inserting a super obvious row into the existing table (alertType column)
            PreparedStatement ps = conn.prepareStatement("INSERT INTO alerts (alertId, alertType, message, status) VALUES (?, ?, ?, ?)");
            ps.setString(1, "A_NEW_123");
            ps.setString(2, "SUPER_ALERT");
            ps.setString(3, "HEY! THIS IS THE BRAND NEW ROW ADDED RIGHT NOW!");
            ps.setString(4, "ACTIVE");
            ps.executeUpdate();
            
            conn.close();
            System.out.println("Super obvious row added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
