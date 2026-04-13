package com.inphb.icgl.stocks.utils;

import com.inphb.icgl.stocks.model.Utilisateur;

public class SessionManager {
    private static Utilisateur utilisateurConnecte;

    private SessionManager() {
    }

    public static void setUtilisateur(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public static boolean isAdmin() {
        return utilisateurConnecte != null && "ADMIN".equals(utilisateurConnecte.getRole());
    }

    public static void logout() {
        utilisateurConnecte = null;
    }
}

