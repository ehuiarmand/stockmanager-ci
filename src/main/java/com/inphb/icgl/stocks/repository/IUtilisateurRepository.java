package com.inphb.icgl.stocks.repository;

import com.inphb.icgl.stocks.model.Utilisateur;
import javafx.collections.ObservableList;

public interface IUtilisateurRepository {
    Utilisateur findByLogin(String login);

    Utilisateur authenticate(String login, String motDePasseClair);

    boolean verifyTwoFactorCode(Utilisateur utilisateur, String code);

    String getLastErrorMessage();

    boolean save(Utilisateur utilisateur);

    boolean update(Utilisateur utilisateur);

    boolean delete(int id);

    boolean disable(int id);

    boolean setActiveState(int id, boolean actif);

    ObservableList<Utilisateur> findAll(int page, int pageSize);

    int countAll();
}
