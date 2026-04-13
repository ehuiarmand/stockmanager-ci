package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Fournisseur;
import com.inphb.icgl.stocks.repository.IFournisseurRepository;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FournisseurDAO implements IFournisseurRepository {
    @Override
    public boolean save(Fournisseur fournisseur) {
        String sql = "INSERT INTO fournisseurs(nom, telephone, email, adresse, ville) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fournisseur.getNom());
            ps.setString(2, fournisseur.getTelephone());
            ps.setString(3, fournisseur.getEmail());
            ps.setString(4, fournisseur.getAdresse());
            ps.setString(5, fournisseur.getVille());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean update(Fournisseur fournisseur) {
        String sql = "UPDATE fournisseurs SET nom = ?, telephone = ?, email = ?, adresse = ?, ville = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fournisseur.getNom());
            ps.setString(2, fournisseur.getTelephone());
            ps.setString(3, fournisseur.getEmail());
            ps.setString(4, fournisseur.getAdresse());
            ps.setString(5, fournisseur.getVille());
            ps.setInt(6, fournisseur.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String checkSql = "SELECT COUNT(*) FROM produits WHERE id_fournisseur = ?";
        String deleteSql = "DELETE FROM fournisseurs WHERE id = ?";
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
    public Fournisseur findById(int id) {
        String sql = "SELECT id, nom, telephone, email, adresse, ville FROM fournisseurs WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Fournisseur f = map(rs);
                    return f;
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    @Override
    public ObservableList<Fournisseur> findAll(int page, int pageSize) {
        ObservableList<Fournisseur> list = FXCollections.observableArrayList();
        String sql = "SELECT id, nom, telephone, email, adresse, ville FROM fournisseurs ORDER BY id DESC LIMIT ? OFFSET ?";
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM fournisseurs";
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
    public ObservableList<Fournisseur> search(String motCle, int page, int pageSize) {
        ObservableList<Fournisseur> list = FXCollections.observableArrayList();
        String sql = "SELECT id, nom, telephone, email, adresse, ville FROM fournisseurs WHERE nom LIKE ? OR telephone LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?";
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            ps.setString(2, "%" + motCle + "%");
            ps.setInt(3, pageSize);
            ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countSearch(String motCle) {
        String sql = "SELECT COUNT(*) FROM fournisseurs WHERE nom LIKE ? OR telephone LIKE ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            ps.setString(2, "%" + motCle + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    private Fournisseur map(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id"));
        f.setNom(rs.getString("nom"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        f.setVille(rs.getString("ville"));
        return f;
    }
}
