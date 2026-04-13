package com.inphb.icgl.stocks.repository;

import com.inphb.icgl.stocks.model.Mouvement;
import com.inphb.icgl.stocks.model.MouvementArchive;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public interface IMouvementRepository {
    boolean save(Mouvement mouvement);

    ObservableList<Mouvement> findAll(int page, int pageSize);

    int countAll();

    ObservableList<Mouvement> filter(Integer produitId, LocalDate dateDebut, LocalDate dateFin, int page, int pageSize);

    int countFilter(Integer produitId, LocalDate dateDebut, LocalDate dateFin);

    int countToday();

    ObservableList<MouvementArchive> findArchives(int page, int pageSize);

    int countArchives();
}
