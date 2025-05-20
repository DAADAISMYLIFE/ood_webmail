/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.model.Addrbook;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author keyrb
 */
@Service
@Slf4j
public class AddrbookService {

    // DB 연결 정보
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    // 주소록 목록
    public List<Addrbook> getAddrbookList(String user) {
        List<Addrbook> list = new ArrayList<>();
        String sql = "SELECT email, name, phone FROM addrbook WHERE user = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
            log.error("주소록 불러오기 중 오류 발생", e);
        }

        return list;
    }

    // 주소록 등록
    public boolean registerAddrbook(String user, String email, String name, String phone) {
        String sql = "INSERT INTO addrbook (user, email, name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, email);
            pstmt.setString(3, name);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            log.error("주소록 등록 처리 중 오류 발생", e);
            return false;
        }
    }

    // 주소록 삭제
    public boolean deleteAddrbook(String user, String email) {
        String sql = "DELETE FROM addrbook WHERE user = ? AND email = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error("주소록 삭제 처리 중 오류 발생", e);
            return false;
        }
    }

    // 주소록 검색
    public List<Addrbook> searchAddrbookList(String user, String keyword) {
        List<Addrbook> list = new ArrayList<>();
        String sql = "SELECT email, name, phone FROM addrbook WHERE user = ? AND (email LIKE ? OR name LIKE ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, "%" + keyword + "%");
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Addrbook entry = new Addrbook();
                entry.setEmail(rs.getString("email"));
                entry.setName(rs.getString("name"));
                entry.setPhone(rs.getString("phone"));
                list.add(entry);
            }

        } catch (SQLException e) {
            log.error("주소록 검색 중 오류 발생", e);
        }
        return list;
    }
}
