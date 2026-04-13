package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Produit;
import com.inphb.icgl.stocks.repository.IProduitRepository;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProduitDAO implements IProduitRepository {
    @Override
    public boolean save(Produit produit) {
        String sql = "INSERT INTO produits(reference, designation, id_categorie, id_fournisseur, prix_unitaire, quantite_stock, stock_minimum, unite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, produit.getReference());
            ps.setString(2, produit.getDesignation());
            if (produit.getIdCategorie() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, produit.getIdCategorie());
            }
            if (produit.getIdFournisseur() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, produit.getIdFournisseur());
            }
            ps.setBigDecimal(5, produit.getPrixUnitaire() == null ? BigDecimal.ZERO : produit.getPrixUnitaire());
            ps.setInt(6, produit.getQuantiteStock());
            ps.setInt(7, produit.getStockMinimum());
            ps.setString(8, produit.getUnite());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean update(Produit produit) {
        String sql = "UPDATE produits SET reference = ?, designation = ?, id_categorie = ?, id_fournisseur = ?, prix_unitaire = ?, quantite_stock = ?, stock_minimum = ?, unite = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, produit.getReference());
            ps.setString(2, produit.getDesignation());
            if (produit.getIdCategorie() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, produit.getIdCategorie());
            }
            if (produit.getIdFournisseur() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, produit.getIdFournisseur());
            }
            ps.setBigDecimal(5, produit.getPrixUnitaire() == null ? BigDecimal.ZERO : produit.getPrixUnitaire());
            ps.setInt(6, produit.getQuantiteStock());
            ps.setInt(7, produit.getStockMinimum());
            ps.setString(8, produit.getUnite());
            ps.setInt(9, produit.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String createArchiveSql = """
                CREATE TABLE IF NOT EXISTS mouvements_archive (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    id_mouvement_origine INT,
                    id_produit INT,
                    type_mouvement VARCHAR(20),
                    quantite INT,
                    motif VARCHAR(255),
                    id_utilisateur INT,
                    date_mouvement TIMESTAMP NULL,
                    date_archivage TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        String archiveSql = """
                INSERT INTO mouvements_archive(id_mouvement_origine, id_produit, type_mouvement, quantite, motif, id_utilisateur, date_mouvement)
                SELECT id, id_produit, type_mouvement, quantite, motif, id_utilisateur, date_mouvement
                FROM mouvements WHERE id_produit = ?
                """;
        String deleteSql = "DELETE FROM produits WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement create = cn.prepareStatement(createArchiveSql);
                 PreparedStatement archive = cn.prepareStatement(archiveSql);
                 PreparedStatement del = cn.prepareStatement(deleteSql)) {
                create.execute();
                archive.setInt(1, id);
                archive.executeUpdate();
                del.setInt(1, id);
                boolean ok = del.executeUpdate() > 0;
                cn.commit();
                return ok;
            } catch (SQLException e) {
                cn.rollback();
                return false;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Produit findById(int id) {
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                WHERE p.id = ?
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    @Override
    public ObservableList<Produit> findAll(int page, int pageSize) {
        ObservableList<Produit> list = FXCollections.observableArrayList();
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                ORDER BY p.id DESC LIMIT ? OFFSET ?
                """;
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
        String sql = "SELECT COUNT(*) FROM produits";
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
    public ObservableList<Produit> search(String motCle, int page, int pageSize) {
        ObservableList<Produit> list = FXCollections.observableArrayList();
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                WHERE p.designation LIKE ? OR c.libelle LIKE ? OR f.nom LIKE ?
                ORDER BY p.id DESC LIMIT ? OFFSET ?
                """;
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            ps.setString(2, "%" + motCle + "%");
            ps.setString(3, "%" + motCle + "%");
            ps.setInt(4, pageSize);
            ps.setInt(5, offset);
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
        String sql = """
                SELECT COUNT(*)
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                WHERE p.designation LIKE ? OR c.libelle LIKE ? OR f.nom LIKE ?
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%" + motCle + "%");
            ps.setString(2, "%" + motCle + "%");
            ps.setString(3, "%" + motCle + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    @Override
    public ObservableList<Produit> findEnAlerte() {
        ObservableList<Produit> list = FXCollections.observableArrayList();
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                WHERE p.quantite_stock <= p.stock_minimum
                ORDER BY (p.stock_minimum - p.quantite_stock) DESC
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countEnAlerte() {
        String sql = "SELECT COUNT(*) FROM produits WHERE quantite_stock <= stock_minimum";
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

    public BigDecimal sumValeurStock() {
        String sql = "SELECT COALESCE(SUM(quantite_stock * prix_unitaire), 0) FROM produits";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException ignored) {
        }
        return BigDecimal.ZERO;
    }

    public ObservableList<Produit> findCritiques(int limit) {
        ObservableList<Produit> list = FXCollections.observableArrayList();
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                WHERE p.quantite_stock <= p.stock_minimum
                ORDER BY (p.quantite_stock - p.stock_minimum) ASC, p.quantite_stock ASC, p.designation ASC
                LIMIT ?
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    public ObservableList<Produit> findAllNoPaging() {
        ObservableList<Produit> list = FXCollections.observableArrayList();
        String sql = """
                SELECT p.id, p.reference, p.designation, p.id_categorie, p.id_fournisseur, p.prix_unitaire, p.quantite_stock, p.stock_minimum, p.unite,
                       c.libelle AS nom_categorie, f.nom AS nom_fournisseur
                FROM produits p
                LEFT JOIN categories c ON c.id = p.id_categorie
                LEFT JOIN fournisseurs f ON f.id = p.id_fournisseur
                ORDER BY p.id DESC
                """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    private Produit map(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("id"));
        p.setReference(rs.getString("reference"));
        p.setDesignation(rs.getString("designation"));
        int cat = rs.getInt("id_categorie");
        p.setIdCategorie(rs.wasNull() ? null : cat);
        int four = rs.getInt("id_fournisseur");
        p.setIdFournisseur(rs.wasNull() ? null : four);
        p.setNomCategorie(rs.getString("nom_categorie"));
        p.setNomFournisseur(rs.getString("nom_fournisseur"));
        p.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        p.setQuantiteStock(rs.getInt("quantite_stock"));
        p.setStockMinimum(rs.getInt("stock_minimum"));
        p.setUnite(rs.getString("unite"));
        return p;
    }
}
