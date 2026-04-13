package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.MouvementDAO;
import com.inphb.icgl.stocks.model.MouvementArchive;
import com.inphb.icgl.stocks.repository.IMouvementRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class ArchiveController {
    private static final int PAGE_SIZE = 15;
    private final IMouvementRepository dao = new MouvementDAO();
    private int currentPage = 1;

    @FXML
    private TableView<MouvementArchive> tableArchives;
    @FXML
    private TableColumn<MouvementArchive, Integer> colId;
    @FXML
    private TableColumn<MouvementArchive, Integer> colIdOrigine;
    @FXML
    private TableColumn<MouvementArchive, Integer> colIdProduit;
    @FXML
    private TableColumn<MouvementArchive, String> colType;
    @FXML
    private TableColumn<MouvementArchive, Integer> colQuantite;
    @FXML
    private TableColumn<MouvementArchive, String> colMotif;
    @FXML
    private TableColumn<MouvementArchive, Integer> colIdUtilisateur;
    @FXML
    private TableColumn<MouvementArchive, LocalDateTime> colDateMouvement;
    @FXML
    private TableColumn<MouvementArchive, LocalDateTime> colDateArchivage;
    @FXML
    private Label lblPagination;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdOrigine.setCellValueFactory(new PropertyValueFactory<>("idMouvementOrigine"));
        colIdProduit.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeMouvement"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colIdUtilisateur.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colDateMouvement.setCellValueFactory(new PropertyValueFactory<>("dateMouvement"));
        colDateArchivage.setCellValueFactory(new PropertyValueFactory<>("dateArchivage"));
        charger();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            charger();
        }
    }

    @FXML
    private void handleNextPage() {
        int total = dao.countArchives();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage < totalPages) {
            currentPage++;
            charger();
        }
    }

    private void charger() {
        tableArchives.setItems(dao.findArchives(currentPage, PAGE_SIZE));
        int total = dao.countArchives();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " archives");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }
}
