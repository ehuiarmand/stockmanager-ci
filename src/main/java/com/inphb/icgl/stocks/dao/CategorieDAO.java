package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Categorie;
import com.inphb.icgl.stocks.repository.ICategorieRepository;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategorieDAO implements ICategorieRepository {
    @Override
    public boolean save(Categorie categorie) {
        String sql = "INSERT INTO categories(libelle, description) VALUES (?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categorie.getLibelle());
            ps.setString(2, categorie.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean update(Categorie categorie) {
        String sql = "UPDATE categories SET libelle = ?, description = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categorie.getLibelle());
            ps.setString(2, categorie.getDescription());
            ps.setInt(3, categorie.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String checkSql = "SELECT COUNT(*) FROM produits WHERE id_categorie = ?";
        String deleteSql = "DELETE FROM categories WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement check = cn.prepareStatement(checkSql)) {
            check.setInt(1, id);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
            try (PreparedStatement del = cn.prepareStatement(deleteSql)) {
                del.setInt(1, id);
                return del.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Categorie findById(int id) {
        String sql = "SELECT id, libelle, description FROM categories WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categorie c = new Categorie();
                    c.setId(rs.getInt("id"));
                    c.setLibelle(rs.getString("libelle"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    @Override
    public ObservableList<Categorie> findAll(int page, int pageSize) {
        ObservableList<Categorie> list = FXCollections.observableArrayList();
        String sql = "SELECT id, libelle, description FROM categories ORDER BY id DESC LIMIT ? OFFSET ?";
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categorie c = new Categorie();
                    c.setId(rs.getInt("id"));
                    c.setLibelle(rs.getString("libelle"));
                    c.setDescription(rs.getString("description"));
                    list.add(c);
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    @Override
    public ObservableList<Categorie> search(String motCle, int page, int pageSize) {
        ObservableList<Categorie> list = FXCollections.observableArrayList();
        String sql = "SELECT id, libelle, description FROM categories WHERE libelle LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?";
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categorie c = new Categorie();
                    c.setId(rs.getInt("id"));
                    c.setLibelle(rs.getString("libelle"));
                    c.setDescription(rs.getString("description"));
                    list.add(c);
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countSearch(String motCle) {
        String sql = "SELECT COUNT(*) FROM categories WHERE libelle LIKE ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
