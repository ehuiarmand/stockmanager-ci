package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Mouvement;
import com.inphb.icgl.stocks.model.MouvementArchive;
import com.inphb.icgl.stocks.repository.IMouvementRepository;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MouvementDAO implements IMouvementRepository {
    @Override
    public boolean save(Mouvement mouvement) {
        String stockSql = "SELECT quantite_stock FROM produits WHERE id = ?";
        String insertSql = "INSERT INTO mouvements(id_produit, type_mouvement, quantite, motif, id_utilisateur) VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE produits SET quantite_stock = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement stockStmt = cn.prepareStatement(stockSql);
                 PreparedStatement insertStmt = cn.prepareStatement(insertSql);
                 PreparedStatement updateStmt = cn.prepareStatement(updateStockSql)) {
                stockStmt.setInt(1, mouvement.getIdProduit());
                int stockActuel;
                try (ResultSet rs = stockStmt.executeQuery()) {
                    if (!rs.next()) {
                        cn.rollback();
                        return false;
                    }
                    stockActuel = rs.getInt(1);
                }

                int nouveauStock = stockActuel;
                if ("SORTIE".equalsIgnoreCase(mouvement.getTypeMouvement())) {
                    if (mouvement.getQuantite() > stockActuel) {
                        cn.rollback();
                        return false;
                    }
                    nouveauStock = stockActuel - mouvement.getQuantite();
                } else {
                    nouveauStock = stockActuel + mouvement.getQuantite();
                }

                insertStmt.setInt(1, mouvement.getIdProduit());
                insertStmt.setString(2, mouvement.getTypeMouvement());
                insertStmt.setInt(3, mouvement.getQuantite());
                insertStmt.setString(4, mouvement.getMotif());
                if (mouvement.getIdUtilisateur() == null) {
                    insertStmt.setNull(5, java.sql.Types.INTEGER);
                } else {
                    insertStmt.setInt(5, mouvement.getIdUtilisateur());
                }
                insertStmt.executeUpdate();

                updateStmt.setInt(1, nouveauStock);
                updateStmt.setInt(2, mouvement.getIdProduit());
                updateStmt.executeUpdate();

                cn.commit();
                return true;
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
    public ObservableList<Mouvement> findAll(int page, int pageSize) {
        return filter(null, null, null, page, pageSize);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM mouvements";
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
    public ObservableList<Mouvement> filter(Integer produitId, LocalDate dateDebut, LocalDate dateFin, int page, int pageSize) {
        ObservableList<Mouvement> list = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder("""
                SELECT m.id, m.id_produit, p.designation AS designation_produit, m.type_mouvement, m.quantite, m.motif,
                       m.id_utilisateur, u.nom_complet AS nom_utilisateur, m.date_mouvement
                FROM mouvements m
                JOIN produits p ON p.id = m.id_produit
                LEFT JOIN utilisateurs u ON u.id = m.id_utilisateur
                WHERE 1=1
                """);
        if (produitId != null) {
            sql.append(" AND m.id_produit = ?");
        }
        if (dateDebut != null) {
            sql.append(" AND DATE(m.date_mouvement) >= ?");
        }
        if (dateFin != null) {
            sql.append(" AND DATE(m.date_mouvement) <= ?");
        }
        sql.append(" ORDER BY m.date_mouvement DESC LIMIT ? OFFSET ?");
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (produitId != null) {
                ps.setInt(idx++, produitId);
            }
            if (dateDebut != null) {
                ps.setDate(idx++, java.sql.Date.valueOf(dateDebut));
            }
            if (dateFin != null) {
                ps.setDate(idx++, java.sql.Date.valueOf(dateFin));
            }
            ps.setInt(idx++, pageSize);
            ps.setInt(idx, offset);
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
    public int countFilter(Integer produitId, LocalDate dateDebut, LocalDate dateFin) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM mouvements m WHERE 1=1");
        if (produitId != null) {
            sql.append(" AND m.id_produit = ?");
        }
        if (dateDebut != null) {
            sql.append(" AND DATE(m.date_mouvement) >= ?");
        }
        if (dateFin != null) {
            sql.append(" AND DATE(m.date_mouvement) <= ?");
        }
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (produitId != null) {
                ps.setInt(idx++, produitId);
            }
            if (dateDebut != null) {
                ps.setDate(idx++, java.sql.Date.valueOf(dateDebut));
            }
            if (dateFin != null) {
                ps.setDate(idx, java.sql.Date.valueOf(dateFin));
            }
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
    public int countToday() {
        String sql = "SELECT COUNT(*) FROM mouvements WHERE DATE(date_mouvement) = CURRENT_DATE()";
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

    public ObservableList<MouvementArchive> findArchives(int page, int pageSize) {
        ObservableList<MouvementArchive> list = FXCollections.observableArrayList();
        String sql = """
                SELECT id, id_mouvement_origine, id_produit, type_mouvement, quantite, motif, id_utilisateur, date_mouvement, date_archivage
                FROM mouvements_archive
                ORDER BY date_archivage DESC
                LIMIT ? OFFSET ?
                """;
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MouvementArchive a = new MouvementArchive();
                    a.setId(rs.getInt("id"));
                    int idMOrig = rs.getInt("id_mouvement_origine");
                    a.setIdMouvementOrigine(rs.wasNull() ? null : idMOrig);
                    a.setIdProduit(rs.getInt("id_produit"));
                    a.setTypeMouvement(rs.getString("type_mouvement"));
                    a.setQuantite(rs.getInt("quantite"));
                    a.setMotif(rs.getString("motif"));
                    int idU = rs.getInt("id_utilisateur");
                    a.setIdUtilisateur(rs.wasNull() ? null : idU);
                    java.sql.Timestamp tm = rs.getTimestamp("date_mouvement");
                    java.sql.Timestamp ta = rs.getTimestamp("date_archivage");
                    a.setDateMouvement(tm == null ? null : tm.toLocalDateTime());
                    a.setDateArchivage(ta == null ? null : ta.toLocalDateTime());
                    list.add(a);
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    public int countArchives() {
        String sql = "SELECT COUNT(*) FROM mouvements_archive";
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

    private Mouvement map(ResultSet rs) throws SQLException {
        Mouvement m = new Mouvement();
        m.setId(rs.getInt("id"));
        m.setIdProduit(rs.getInt("id_produit"));
        m.setDesignationProduit(rs.getString("designation_produit"));
        m.setTypeMouvement(rs.getString("type_mouvement"));
        m.setQuantite(rs.getInt("quantite"));
        m.setMotif(rs.getString("motif"));
        int userId = rs.getInt("id_utilisateur");
        m.setIdUtilisateur(rs.wasNull() ? null : userId);
        m.setNomUtilisateur(rs.getString("nom_utilisateur"));
        java.sql.Timestamp ts = rs.getTimestamp("date_mouvement");
        m.setDateMouvement(ts == null ? null : ts.toLocalDateTime());
        return m;
    }
}
