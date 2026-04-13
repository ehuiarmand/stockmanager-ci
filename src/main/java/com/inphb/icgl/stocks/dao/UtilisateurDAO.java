package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Utilisateur;
import com.inphb.icgl.stocks.repository.IUtilisateurRepository;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurDAO implements IUtilisateurRepository {
    private String lastErrorMessage;

    @Override
    public Utilisateur findByLogin(String login) {
        lastErrorMessage = null;
        String sql = "SELECT id, nom_complet, login, mot_de_passe, role, actif FROM utilisateurs WHERE login = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUtilisateur(rs);
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = buildDatabaseErrorMessage(e);
        }
        return null;
    }

    @Override
    public Utilisateur authenticate(String login, String motDePasseClair) {
        lastErrorMessage = null;
        try {
            Utilisateur utilisateur = findByLogin(login);
            if (lastErrorMessage != null) {
                return null;
            }
            if (utilisateur == null || !utilisateur.isActif()) {
                return null;
            }
            String hashInput = hashSha256(motDePasseClair);
            if (!hashInput.equalsIgnoreCase(utilisateur.getMotDePasse())) {
                return null;
            }
            return utilisateur;
        } catch (RuntimeException e) {
            lastErrorMessage = "Erreur interne pendant l'authentification.";
            return null;
        }
    }

    @Override
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public boolean save(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateurs(nom_complet, login, mot_de_passe, role, actif) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, utilisateur.getNomComplet());
            ps.setString(2, utilisateur.getLogin());
            ps.setString(3, hashSha256(utilisateur.getMotDePasse()));
            ps.setString(4, utilisateur.getRole());
            ps.setBoolean(5, utilisateur.isActif());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean update(Utilisateur utilisateur) {
        boolean updatePassword = utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isBlank();
        String sql = updatePassword
                ? "UPDATE utilisateurs SET nom_complet = ?, login = ?, mot_de_passe = ?, role = ?, actif = ? WHERE id = ?"
                : "UPDATE utilisateurs SET nom_complet = ?, login = ?, role = ?, actif = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, utilisateur.getNomComplet());
            ps.setString(2, utilisateur.getLogin());
            if (updatePassword) {
                ps.setString(3, hashSha256(utilisateur.getMotDePasse()));
                ps.setString(4, utilisateur.getRole());
                ps.setBoolean(5, utilisateur.isActif());
                ps.setInt(6, utilisateur.getId());
            } else {
                ps.setString(3, utilisateur.getRole());
                ps.setBoolean(4, utilisateur.isActif());
                ps.setInt(5, utilisateur.getId());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean disable(int id) {
        return setActiveState(id, false);
    }

    @Override
    public boolean setActiveState(int id, boolean actif) {
        String sql = "UPDATE utilisateurs SET actif = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public ObservableList<Utilisateur> findAll(int page, int pageSize) {
        ObservableList<Utilisateur> list = FXCollections.observableArrayList();
        String sql = "SELECT id, nom_complet, login, mot_de_passe, role, actif FROM utilisateurs LIMIT ? OFFSET ?";
        int offset = Math.max(0, page - 1) * pageSize;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUtilisateur(rs));
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM utilisateurs";
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

    private String hashSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 indisponible", e);
        }
    }

    private Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setNomComplet(rs.getString("nom_complet"));
        utilisateur.setLogin(rs.getString("login"));
        utilisateur.setMotDePasse(rs.getString("mot_de_passe"));
        utilisateur.setRole(rs.getString("role"));
        utilisateur.setActif(rs.getBoolean("actif"));
        return utilisateur;
    }

    private String buildDatabaseErrorMessage(SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState != null && sqlState.startsWith("08")) {
            return "Base de donnees indisponible. Demarrez MySQL puis reessayez.";
        }
        String message = e.getMessage();
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("communications link failure")
                    || lower.contains("connection refused")
                    || lower.contains("cannot connect")
                    || lower.contains("access denied for user")) {
                return "Connexion MySQL impossible. Verifiez que MySQL est demarre.";
            }
        }
        return "Erreur d'acces a la base de donnees.";
    }
}
