/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author keyrb
 */
public class AddrbookService {

    // DB 연결 정보
    private static final String URL = "jdbc:mysql://localhost:13308/webmail";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // 주소록 목록
    public List<Addrbook> getAddrbookList(String user) {
        List<Addrbook> list = new ArrayList<>();
        String sql = "SELECT email, name, phone FROM addrbook WHERE user = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Addrbook entry = new Addrbook();
                entry.setEmail(rs.getString("email"));
                entry.setName(rs.getString("name"));
                entry.setPhone(rs.getString("phone"));
                list.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    // 주소록 등록
    public boolean registerAddrbook(String user, String email, String name, String phone) {
        String sql = "INSERT INTO addrbook (user, email, name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, email);
            pstmt.setString(3, name);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 주소록 삭제
    public boolean deleteAddrbook(String user, String email) {
        String sql = "DELETE FROM addrbook WHERE user = ? AND email = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
