package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.FournisseurDAO;
import com.inphb.icgl.stocks.model.Fournisseur;
import com.inphb.icgl.stocks.repository.IFournisseurRepository;
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

public class FournisseurController {
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

    private final IFournisseurRepository dao = new FournisseurDAO();
    private int currentPage = 1;
    private boolean modeAjout = true;
    private Fournisseur fournisseurSelectionne;

    @FXML private TextField txtNom;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtRecherche;
    @FXML private Label lblStatut;
    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Integer> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colTelephone;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colAdresse;
    @FXML private TableColumn<Fournisseur, String> colVille;
    @FXML private Label lblPagination;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));

        tableFournisseurs.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            fournisseurSelectionne = s;
            modeAjout = (s == null);
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
            tableFournisseurs.getSelectionModel().clearSelection();
            mettreAJourEtatActions();
        });
    }

    @FXML
    private void handleAjouter() {
        tableFournisseurs.getSelectionModel().clearSelection();
        modeAjout = true;
        if (txtNom.getText() == null || txtNom.getText().isBlank()) {
            lblStatut.setText("Nom obligatoire.");
            return;
        }
        Fournisseur f = lireFormulaire();
        if (dao.save(f)) {
            viderFormulaire();
            charger();
            lblStatut.setText("Fournisseur ajoute.");
        } else {
            lblStatut.setText("Echec ajout fournisseur.");
        }
    }

    @FXML
    private void handleModifier() {
        Fournisseur s = fournisseurSelectionne != null
                ? fournisseurSelectionne
                : tableFournisseurs.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez un fournisseur a modifier.");
            return;
        }
        if (txtNom.getText() == null || txtNom.getText().isBlank()) {
            lblStatut.setText("Nom obligatoire.");
            return;
        }

        // Boite de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de modification");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous confirmer la modification du fournisseur \"" + s.getNom() + "\" ?");
        Optional<ButtonType> resultat = alert.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            lblStatut.setText("Modification annulee.");
            return;
        }

        Fournisseur f = lireFormulaire();
        f.setId(s.getId());
        if (dao.update(f)) {
            viderFormulaire();
            charger();
            lblStatut.setText("Fournisseur modifie avec succes.");
        } else {
            lblStatut.setText("Echec de la modification.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Fournisseur s = fournisseurSelectionne != null
                ? fournisseurSelectionne
                : tableFournisseurs.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez un fournisseur a supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous supprimer le fournisseur \"" + s.getNom() + "\" ?");
        Optional<ButtonType> resultat = alert.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            lblStatut.setText("Suppression annulee.");
            return;
        }

        if (dao.delete(s.getId())) {
            viderFormulaire();
            charger();
            lblStatut.setText("Fournisseur supprime.");
        } else {
            lblStatut.setText("Suppression impossible (fournisseur reference par des produits).");
        }
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
        int total = totalCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage < totalPages) {
            currentPage++;
            charger();
        }
    }

    private void charger() {
        String search = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();
        if (search.isBlank()) {
            tableFournisseurs.setItems(dao.findAll(currentPage, PAGE_SIZE));
        } else {
            tableFournisseurs.setItems(dao.search(search, currentPage, PAGE_SIZE));
        }
        fournisseurSelectionne = null;
        tableFournisseurs.getSelectionModel().clearSelection();
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
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " fournisseurs");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    private Fournisseur lireFormulaire() {
        Fournisseur f = new Fournisseur();
        f.setNom(txtNom.getText().trim());
        f.setTelephone(txtTelephone.getText().trim());
        f.setEmail(txtEmail.getText().trim());
        f.setAdresse(txtAdresse.getText().trim());
        f.setVille(txtVille.getText().trim());
        return f;
    }

    private void remplirFormulaire(Fournisseur f) {
        if (f == null) {
            lblStatut.setText("");
            return;
        }
        txtNom.setText(f.getNom());
        txtTelephone.setText(f.getTelephone());
        txtEmail.setText(f.getEmail());
        txtAdresse.setText(f.getAdresse());
        txtVille.setText(f.getVille());
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtAdresse.clear();
        txtVille.clear();
        fournisseurSelectionne = null;
        tableFournisseurs.getSelectionModel().clearSelection();
        modeAjout = true;
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
}
