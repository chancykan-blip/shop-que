package com.myshop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
public class TestConnection {

    static final String URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/shop_db?useSSL=false&serverTimezone=UTC";

    static final String USERNAME = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "root";

    static final String PASSWORD = System.getenv("DB_PASS") != null
            ? System.getenv("DB_PASS")
            : "dzsjqlx31";  // ← 这里保留你本地的密码不变
    // 查询用户（支持空条件 = 查全部）
    public static List<String> searchUsers(String region, String status) {
        List<String> list = new ArrayList<>();

        String sql = "SELECT u.id, u.name, u.email, u.region, u.status, " +
                "COALESCE(SUM(o.amount), 0) AS total_amount " +
                "FROM users u " +
                "LEFT JOIN orders o ON u.id = o.user_id " +
                "WHERE (u.region = ? OR ? = '') " +
                "AND (u.status = ? OR ? = '') " +
                "GROUP BY u.id, u.name, u.email, u.region, u.status " +
                "ORDER BY total_amount DESC";

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, region);
            stmt.setString(2, region);
            stmt.setString(3, status);
            stmt.setString(4, status);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String row = rs.getInt("id")          + "|" +
                        rs.getString("name")     + "|" +
                        rs.getString("email")    + "|" +
                        rs.getString("region")   + "|" +
                        rs.getString("status")   + "|" +
                        rs.getDouble("total_amount");
                list.add(row);
            }
        } catch (Exception e) {
            System.out.println("查询出错：" + e.getMessage());
        }
        return list;
    }

    // 查询所有订单
    public static List<String> getOrders() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT o.id, u.name, u.region, o.amount, o.order_date " +
                "FROM orders o JOIN users u ON o.user_id = u.id " +
                "ORDER BY o.order_date DESC";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("id")          + "|" +
                        rs.getString("name")     + "|" +
                        rs.getString("region")   + "|" +
                        rs.getDouble("amount")   + "|" +
                        rs.getString("order_date"));
            }
        } catch (Exception e) {
            System.out.println("出错：" + e.getMessage());
        }
        return list;
    }

    // 新增订单
    public static String addOrder(int userId, double amount, String orderDate) {
        String sql = "INSERT INTO orders (user_id, amount, order_date) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.setString(3, orderDate);
            stmt.executeUpdate();
            return "success";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    // 删除订单
    public static String deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return "success";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    // 获取所有用户（给下拉框用）
    public static List<String> getAllUsers() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT id, name FROM users ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("id") + "|" + rs.getString("name"));
            }
        } catch (Exception e) {
            System.out.println("出错：" + e.getMessage());
        }
        return list;
    }
    // 新增用户
    public static String addUser(String name, String email, String region, String status) {
        String sql = "INSERT INTO users (name, email, region, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, region);
            stmt.setString(4, status);
            stmt.executeUpdate();
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    // 各地区用户数量
    public static List<String> statsByRegion() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT region, COUNT(*) as cnt FROM users GROUP BY region ORDER BY cnt DESC";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("region") + "|" + rs.getInt("cnt"));
            }
        } catch (Exception e) {
            System.out.println("出错：" + e.getMessage());
        }
        return list;
    }

    // 用户状态分布
    public static List<String> statsByStatus() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT status, COUNT(*) as cnt FROM users GROUP BY status";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("status") + "|" + rs.getInt("cnt"));
            }
        } catch (Exception e) {
            System.out.println("出错：" + e.getMessage());
        }
        return list;
    }

    // 每月订单金额
    public static List<String> statsMonthly() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') as month, " +
                "SUM(amount) as total FROM orders " +
                "GROUP BY month ORDER BY month ASC";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("month") + "|" + rs.getDouble("total"));
            }
        } catch (Exception e) {
            System.out.println("出错：" + e.getMessage());
        }
        return list;
    }

    // 删除用户
    public static String deleteUser(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            // 先删订单，再删用户（外键约束）
            PreparedStatement s1 = conn.prepareStatement("DELETE FROM orders WHERE user_id = ?");
            s1.setInt(1, id);
            s1.executeUpdate();

            PreparedStatement s2 = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            s2.setInt(1, id);
            s2.executeUpdate();
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 编辑用户
    public static String updateUser(int id, String name, String email, String region, String status) {
        String sql = "UPDATE users SET name=?, email=?, region=?, status=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, region);
            stmt.setString(4, status);
            stmt.setInt(5, id);
            stmt.executeUpdate();
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    public static String importFromCSV(MultipartFile file) {
        int count = 0;
        String sql = "INSERT INTO users (name, email, region, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             java.io.BufferedReader reader = new java.io.BufferedReader(
                     new java.io.InputStreamReader(file.getInputStream(), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                stmt.setString(1, parts[0].trim());
                stmt.setString(2, parts[1].trim());
                stmt.setString(3, parts[2].trim());
                stmt.setString(4, parts[3].trim());
                stmt.addBatch();
                count++;

                if (count % 500 == 0) stmt.executeBatch();
            }
            stmt.executeBatch();
            return "success:" + count;

        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
}