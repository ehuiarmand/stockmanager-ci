package com.inphb.icgl.stocks.repository;

import com.inphb.icgl.stocks.model.Produit;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

public interface IProduitRepository {
    boolean save(Produit produit);

    boolean update(Produit produit);

    boolean delete(int id);

    Produit findById(int id);

    ObservableList<Produit> findAll(int page, int pageSize);

    int countAll();

    ObservableList<Produit> search(String motCle, int page, int pageSize);

    int countSearch(String motCle);

    ObservableList<Produit> findEnAlerte();

    int countEnAlerte();

    ObservableList<Produit> findAllNoPaging();

    BigDecimal sumValeurStock();

    ObservableList<Produit> findCritiques(int limit);
}
