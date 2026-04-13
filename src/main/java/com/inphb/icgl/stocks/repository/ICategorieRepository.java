package com.inphb.icgl.stocks.repository;

import com.inphb.icgl.stocks.model.Categorie;
import javafx.collections.ObservableList;

public interface ICategorieRepository {
    boolean save(Categorie categorie);

    boolean update(Categorie categorie);

    boolean delete(int id);

    Categorie findById(int id);

    ObservableList<Categorie> findAll(int page, int pageSize);

    int countAll();

    ObservableList<Categorie> search(String motCle, int page, int pageSize);

    int countSearch(String motCle);
}

