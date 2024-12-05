package Server;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt; // 비밀번호 해시 라이브러리 사용

public class JDBCConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/ChatApp";
    private static final String USER = "root";
    private static final String PASSWORD = "0000"; // MySQL 비밀번호 입력

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DEBUG] 데이터베이스 연결 성공: " + conn);
            return conn;
        } catch (SQLException e) {
            System.out.println("[ERROR] 데이터베이스 연결 실패: " + e.getMessage());
            e.printStackTrace(); // 상세 오류 출력
            return null; // 실패 시 null 반환
        }
    }


    public boolean registerUser(String userName, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // 비밀번호 해싱
        String query = "INSERT INTO users (userName, password) VALUES (?, ?)";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("[DEBUG] 연결 성공: " + conn); // 연결 확인
            System.out.println("[DEBUG] 실행할 쿼리: " + query);
            System.out.println("[DEBUG] userName: " + userName);
            System.out.println("[DEBUG] hashedPassword: " + hashedPassword);

            stmt.setString(1, userName);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();

            System.out.println("[DEBUG] 회원가입 성공: " + userName);
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[ERROR] 중복된 사용자 이름: " + userName);
        } catch (SQLException e) {
            System.out.println("[ERROR] SQL 오류: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean loginUser(String userName, String password) {
        String query = "SELECT password FROM users WHERE userName = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return BCrypt.checkpw(password, hashedPassword); // 해시 비밀번호 검증
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUser(String userName, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // 비밀번호 해싱
        String query = "UPDATE users SET password = ? WHERE userName = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, userName);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(String userName) {
        String query = "DELETE FROM users WHERE userName = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}