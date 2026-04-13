package com.inphb.icgl.stocks.utils;

import com.inphb.icgl.stocks.model.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerTest {

    @AfterEach
    void cleanup() {
        SessionManager.logout();
    }

    @Test
    void isAdminRetourneVraiPourUnAdminConnecte() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setRole("ADMIN");

        SessionManager.setUtilisateur(utilisateur);

        assertTrue(SessionManager.isAdmin());
        assertSame(utilisateur, SessionManager.getUtilisateur());
    }

    @Test
    void logoutVideLaSession() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setRole("GESTIONNAIRE");
        SessionManager.setUtilisateur(utilisateur);

        SessionManager.logout();

        assertNull(SessionManager.getUtilisateur());
        assertFalse(SessionManager.isAdmin());
    }
}
