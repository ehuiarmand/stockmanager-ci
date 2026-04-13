package com.inphb.icgl.stocks.dao;

import com.inphb.icgl.stocks.model.Categorie;
import com.inphb.icgl.stocks.utils.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategorieDAOTest {

    private final CategorieDAO dao = new CategorieDAO();

    @BeforeAll
    static void configureDatabase() throws Exception {
        System.setProperty("stockmanager.db.url", "jdbc:h2:mem:stockmanager_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        System.setProperty("stockmanager.db.user", "sa");
        System.setProperty("stockmanager.db.password", "");
        DatabaseConnection.closeConnection();

        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        libelle VARCHAR(100) NOT NULL UNIQUE,
                        description VARCHAR(255)
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS produits (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        id_categorie INT
                    )
                    """);
        }
    }

    @BeforeEach
    void resetData() throws Exception {
        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement()) {
            st.execute("DELETE FROM produits");
            st.execute("DELETE FROM categories");
            st.execute("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1");
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        DatabaseConnection.closeConnection();
        System.clearProperty("stockmanager.db.url");
        System.clearProperty("stockmanager.db.user");
        System.clearProperty("stockmanager.db.password");
    }

    @Test
    void saveAjouteUneCategorie() {
        Categorie categorie = new Categorie();
        categorie.setLibelle("Quincaillerie");
        categorie.setDescription("Materiel divers");

        boolean saved = dao.save(categorie);

        assertTrue(saved);
        assertEquals(1, dao.countAll());
        assertEquals("Quincaillerie", dao.findAll(1, 15).getFirst().getLibelle());
    }

    @Test
    void findAllRetourneLesCategoriesPaginees() {
        for (int i = 1; i <= 3; i++) {
            Categorie categorie = new Categorie();
            categorie.setLibelle("Categorie " + i);
            categorie.setDescription("Desc " + i);
            assertTrue(dao.save(categorie));
        }

        var resultats = dao.findAll(1, 2);

        assertEquals(2, resultats.size());
        assertNotNull(resultats.getFirst().getId());
    }

    @Test
    void deleteSupprimeUneCategorieNonReferencee() {
        Categorie categorie = new Categorie();
        categorie.setLibelle("Papeterie");
        categorie.setDescription("Fournitures");
        assertTrue(dao.save(categorie));

        int id = dao.findAll(1, 10).getFirst().getId();

        assertTrue(dao.delete(id));
        assertEquals(0, dao.countAll());
    }

    @Test
    void deleteRefuseUneCategorieReferencee() throws Exception {
        Categorie categorie = new Categorie();
        categorie.setLibelle("Electronique");
        categorie.setDescription("Accessoires");
        assertTrue(dao.save(categorie));
        int id = dao.findAll(1, 10).getFirst().getId();

        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement()) {
            st.execute("INSERT INTO produits(id_categorie) VALUES (" + id + ")");
        }

        assertFalse(dao.delete(id));
        assertEquals(1, dao.countAll());
    }
}
