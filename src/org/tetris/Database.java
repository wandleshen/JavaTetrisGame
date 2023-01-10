package org.tetris;

import java.sql.*;

public class Database {
    private static final String url = "jdbc:mysql://localhost:3306/jvav";
    private static final String username = "root";
    private static final String password = "root";
    private static Connection conn;

    static {
        // 加载驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot load JDBC driver");
        }
        // 链接数据库
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cannot connect to database");
        }
    }

    public static boolean login(String username, String password) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean register(String username, String password) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO user (username, password) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void recordBattle(String player1, String player2, String score1, String score2, String winner) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO battles (player1, player2, player1score, player2score, winner) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, player1);
            ps.setString(2, player2);
            ps.setString(3, score1);
            ps.setString(4, score2);
            ps.setString(5, winner);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
    public static int getWinCount(String username) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM battles WHERE winner = ?");
            ps.setString(1, username);
            ResultSet result = ps.executeQuery();
            return result.next() ? result.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
    public static int getLoseCount(String username) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM battles WHERE (player1 = ? OR player2 = ?) AND winner != ?");
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ResultSet result = ps.executeQuery();
            return result.next() ? result.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
