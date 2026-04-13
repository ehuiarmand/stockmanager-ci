package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.CategorieDAO;
import com.inphb.icgl.stocks.model.Categorie;
import com.inphb.icgl.stocks.repository.ICategorieRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class CategorieController {
    private static final int PAGE_SIZE = 15;
    private static final String BTN_ADD =
            "-fx-background-color:#7FBB9A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_EDIT =
            "-fx-background-color:#4A90CC; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_DELETE =
            "-fx-background-color:#D4820A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";

    private final ICategorieRepository dao = new CategorieDAO();
    private int currentPage = 1;

    @FXML private TextField txtLibelle;
    @FXML private TextField txtDescription;
    @FXML private TextField txtRecherche;
    @FXML private Label lblStatut;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colId;
    @FXML private TableColumn<Categorie, String> colLibelle;
    @FXML private TableColumn<Categorie, String> colDescription;
    @FXML private Label lblPagination;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableCategories.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            remplirFormulaire(s);
            mettreAJourEtatActions();
        });
        txtRecherche.textProperty().addListener((obs, o, n) -> {
            currentPage = 1;
            charger();
        });
        appliquerStyleBoutons();
        mettreAJourEtatActions();
        charger();
        Platform.runLater(() -> {
            tableCategories.getSelectionModel().clearSelection();
            mettreAJourEtatActions();
        });
    }

    @FXML
    private void handleAjouter() {
        tableCategories.getSelectionModel().clearSelection();
        if (txtLibelle.getText() == null || txtLibelle.getText().isBlank()) {
            lblStatut.setText("Libelle obligatoire.");
            return;
        }
        Categorie c = new Categorie();
        c.setLibelle(txtLibelle.getText().trim());
        c.setDescription(txtDescription.getText().trim());
        if (dao.save(c)) {
            viderFormulaire();
            charger();
            lblStatut.setText("Categorie ajoutee avec succes.");
        } else {
            lblStatut.setText("Echec ajout : le libelle existe peut-etre deja.");
        }
    }

    @FXML
    private void handleModifier() {
        Categorie s = tableCategories.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez une categorie a modifier.");
            return;
        }
        if (txtLibelle.getText() == null || txtLibelle.getText().isBlank()) {
            lblStatut.setText("Libelle obligatoire.");
            return;
        }
        if (!confirmer("Confirmation de modification",
                "Voulez-vous modifier la categorie \"" + s.getLibelle() + "\" ?")) {
            lblStatut.setText("Modification annulee.");
            return;
        }
        s.setLibelle(txtLibelle.getText().trim());
        s.setDescription(txtDescription.getText().trim());
        if (dao.update(s)) {
            charger();
            lblStatut.setText("Categorie modifiee avec succes.");
        } else {
            lblStatut.setText("Echec de la modification.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Categorie s = tableCategories.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez une categorie a supprimer.");
            return;
        }
        if (!confirmer("Confirmation de suppression",
                "Voulez-vous supprimer la categorie \"" + s.getLibelle() + "\" ?\n" +
                "Attention : impossible si des produits utilisent cette categorie.")) {
            lblStatut.setText("Suppression annulee.");
            return;
        }
        if (dao.delete(s.getId())) {
            viderFormulaire();
            charger();
            lblStatut.setText("Categorie supprimee avec succes.");
        } else {
            lblStatut.setText("Suppression impossible : categorie utilisee par des produits.");
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) { currentPage--; charger(); }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount() / PAGE_SIZE));
        if (currentPage < totalPages) { currentPage++; charger(); }
    }

    private void charger() {
        String search = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();
        tableCategories.setItems(search.isBlank()
                ? dao.findAll(currentPage, PAGE_SIZE)
                : dao.search(search, currentPage, PAGE_SIZE));
        tableCategories.getSelectionModel().clearSelection();
        refreshPagination();
        mettreAJourEtatActions();
    }

    private int totalCount() {
        String search = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();
        return search.isBlank() ? dao.countAll() : dao.countSearch(search);
    }

    private void refreshPagination() {
        int total = totalCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " categories");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    private void remplirFormulaire(Categorie c) {
        if (c == null) { lblStatut.setText(""); return; }
        txtLibelle.setText(c.getLibelle());
        txtDescription.setText(c.getDescription());
    }

    private void viderFormulaire() {
        txtLibelle.clear();
        txtDescription.clear();
        tableCategories.getSelectionModel().clearSelection();
        mettreAJourEtatActions();
    }

    private void mettreAJourEtatActions() {
        if (btnAjouter != null) btnAjouter.setDisable(false);
        if (btnModifier != null) btnModifier.setDisable(false);
        if (btnSupprimer != null) btnSupprimer.setDisable(false);
    }

    private void appliquerStyleBoutons() {
        if (btnAjouter != null) btnAjouter.setStyle(BTN_ADD);
        if (btnModifier != null) btnModifier.setStyle(BTN_EDIT);
        if (btnSupprimer != null) btnSupprimer.setStyle(BTN_DELETE);
    }

    private boolean confirmer(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> resultat = alert.showAndWait();
        return resultat.isPresent() && resultat.get() == ButtonType.OK;
    }
}
