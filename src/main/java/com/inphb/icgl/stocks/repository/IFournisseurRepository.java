package com.inphb.icgl.stocks.repository;

import com.inphb.icgl.stocks.model.Fournisseur;
import javafx.collections.ObservableList;

public interface IFournisseurRepository {
    boolean save(Fournisseur fournisseur);

    boolean update(Fournisseur fournisseur);

    boolean delete(int id);

    Fournisseur findById(int id);

    ObservableList<Fournisseur> findAll(int page, int pageSize);

    int countAll();

    ObservableList<Fournisseur> search(String motCle, int page, int pageSize);

    int countSearch(String motCle);
}

