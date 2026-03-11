package library.dao;

import library.model.Member;
import library.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) members.add(mapRow(rs));
        }
        return members;
    }

    public List<Member> searchMembers(String query) throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY name";
        String q = "%" + query + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) members.add(mapRow(rs));
            }
        }
        return members;
    }

    public void addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone, address, membership_type, expiry_date, status) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getAddress());
            ps.setString(5, member.getMembershipType());
            ps.setString(6, member.getExpiryDate());
            ps.setString(7, member.getStatus());
            ps.executeUpdate();
        }
    }

    public void updateMember(Member member) throws SQLException {
        String sql = "UPDATE members SET name=?, email=?, phone=?, address=?, membership_type=?, expiry_date=?, status=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getAddress());
            ps.setString(5, member.getMembershipType());
            ps.setString(6, member.getExpiryDate());
            ps.setString(7, member.getStatus());
            ps.setInt(8, member.getId());
            ps.executeUpdate();
        }
    }

    public void deleteMember(int id) throws SQLException {
        String sql = "DELETE FROM members WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int getTotalMembers() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members WHERE status='Active'")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("membership_type"),
            rs.getString("membership_date"),
            rs.getString("expiry_date"),
            rs.getString("status")
        );
    }
}
