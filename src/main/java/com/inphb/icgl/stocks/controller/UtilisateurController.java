package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.UtilisateurDAO;
import com.inphb.icgl.stocks.model.Utilisateur;
import com.inphb.icgl.stocks.repository.IUtilisateurRepository;
import com.inphb.icgl.stocks.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class UtilisateurController {
    private static final int PAGE_SIZE = 15;
    private static final String BTN_ADD =
            "-fx-background-color:#7FBB9A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_EDIT =
            "-fx-background-color:#4A90CC; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_TOGGLE =
            "-fx-background-color:#E28A00; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_DELETE =
            "-fx-background-color:#D4820A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";

    private final IUtilisateurRepository dao = new UtilisateurDAO();
    private int currentPage = 1;

    @FXML private TextField txtNomComplet;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtMotDePasse;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox chkActif;
    @FXML private Label lblStatut;
    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNomComplet;
    @FXML private TableColumn<Utilisateur, String> colLogin;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;
    @FXML private Label lblPagination;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnToggleActif;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdmin()) {
            lblStatut.setText("Acces reserve aux ADMIN.");
            return;
        }
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomComplet.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "GESTIONNAIRE"));
        cbRole.setValue("GESTIONNAIRE");
        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            remplirFormulaire(s);
            updateToggleButton(s);
            mettreAJourEtatActions();
        });
        appliquerStyleBoutons();
        updateToggleButton(null);
        mettreAJourEtatActions();
        charger();
        Platform.runLater(() -> {
            tableUtilisateurs.getSelectionModel().clearSelection();
            mettreAJourEtatActions();
        });
    }

    @FXML
    private void handleAjouter() {
        tableUtilisateurs.getSelectionModel().clearSelection();
        if (!SessionManager.isAdmin()) { lblStatut.setText("Acces refuse."); return; }
        if (txtNomComplet.getText().isBlank() || txtLogin.getText().isBlank() || txtMotDePasse.getText().isBlank()) {
            lblStatut.setText("Nom, login et mot de passe obligatoires.");
            return;
        }
        Utilisateur u = new Utilisateur();
        u.setNomComplet(txtNomComplet.getText().trim());
        u.setLogin(txtLogin.getText().trim());
        u.setMotDePasse(txtMotDePasse.getText().trim());
        u.setRole(cbRole.getValue());
        u.setActif(chkActif.isSelected());
        if (dao.save(u)) {
            viderFormulaire();
            charger();
            lblStatut.setText("Utilisateur ajoute avec succes.");
        } else {
            lblStatut.setText("Echec ajout : le login existe peut-etre deja.");
        }
    }

    @FXML
    private void handleModifier() {
        Utilisateur s = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (s == null) { lblStatut.setText("Selectionnez un utilisateur a modifier."); return; }
        if (txtNomComplet.getText().isBlank() || txtLogin.getText().isBlank()) {
            lblStatut.setText("Nom et login obligatoires.");
            return;
        }
        if (!confirmer("Confirmation de modification",
                "Voulez-vous modifier l'utilisateur \"" + s.getNomComplet() + "\" ?")) {
            lblStatut.setText("Modification annulee.");
            return;
        }
        String nouveauMotDePasse = txtMotDePasse.getText() == null ? "" : txtMotDePasse.getText().trim();
        s.setNomComplet(txtNomComplet.getText().trim());
        s.setLogin(txtLogin.getText().trim());
        s.setMotDePasse(nouveauMotDePasse);
        s.setRole(cbRole.getValue());
        s.setActif(chkActif.isSelected());
        if (dao.update(s)) {
            charger();
            lblStatut.setText(nouveauMotDePasse.isBlank()
                    ? "Utilisateur modifie avec succes."
                    : "Utilisateur modifie avec nouveau mot de passe.");
        } else {
            lblStatut.setText("Echec de la modification.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Utilisateur s = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (s == null) { lblStatut.setText("Selectionnez un utilisateur a supprimer."); return; }
        if (SessionManager.getUtilisateur() != null && SessionManager.getUtilisateur().getId() == s.getId()) {
            lblStatut.setText("Impossible de supprimer votre propre compte.");
            return;
        }
        if (!confirmer("Confirmation de suppression",
                "Voulez-vous supprimer definitivement l'utilisateur \"" + s.getNomComplet() + "\" ?\n" +
                "Cette action est irreversible.")) {
            lblStatut.setText("Suppression annulee.");
            return;
        }
        if (dao.delete(s.getId())) {
            viderFormulaire();
            charger();
            lblStatut.setText("Utilisateur supprime avec succes.");
        } else {
            lblStatut.setText("Echec de la suppression.");
        }
    }

    @FXML
    private void handleDesactiver() {
        Utilisateur s = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (s == null) { lblStatut.setText("Selectionnez un utilisateur."); return; }
        if (SessionManager.getUtilisateur() != null && SessionManager.getUtilisateur().getId() == s.getId()) {
            lblStatut.setText("Un ADMIN ne peut pas se desactiver lui-meme.");
            return;
        }
        boolean nouvelEtat = !s.isActif();
        String action = nouvelEtat ? "activer" : "desactiver";
        if (!confirmer("Confirmation", "Voulez-vous " + action + " l'utilisateur \"" + s.getNomComplet() + "\" ?")) {
            return;
        }
        if (dao.setActiveState(s.getId(), nouvelEtat)) {
            charger();
            lblStatut.setText(nouvelEtat ? "Utilisateur active." : "Utilisateur desactive.");
        } else {
            lblStatut.setText("Echec mise a jour statut.");
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) { currentPage--; charger(); }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = Math.max(1, (int) Math.ceil((double) dao.countAll() / PAGE_SIZE));
        if (currentPage < totalPages) { currentPage++; charger(); }
    }

    private void charger() {
        tableUtilisateurs.setItems(dao.findAll(currentPage, PAGE_SIZE));
        tableUtilisateurs.getSelectionModel().clearSelection();
        int total = dao.countAll();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " utilisateurs");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
        updateToggleButton(null);
        mettreAJourEtatActions();
    }

    private void remplirFormulaire(Utilisateur u) {
        if (u == null) { lblStatut.setText(""); return; }
        txtNomComplet.setText(u.getNomComplet());
        txtLogin.setText(u.getLogin());
        txtMotDePasse.clear();
        cbRole.setValue(u.getRole());
        chkActif.setSelected(u.isActif());
    }

    private void viderFormulaire() {
        txtNomComplet.clear();
        txtLogin.clear();
        txtMotDePasse.clear();
        cbRole.setValue("GESTIONNAIRE");
        chkActif.setSelected(true);
        tableUtilisateurs.getSelectionModel().clearSelection();
        updateToggleButton(null);
        mettreAJourEtatActions();
    }

    private void updateToggleButton(Utilisateur u) {
        if (btnToggleActif == null) return;
        btnToggleActif.setText(u != null && !u.isActif() ? "Activer" : "Desactiver");
    }

    private void mettreAJourEtatActions() {
        if (btnAjouter != null) btnAjouter.setDisable(false);
        if (btnModifier != null) btnModifier.setDisable(false);
        if (btnSupprimer != null) btnSupprimer.setDisable(false);
        if (btnToggleActif != null) btnToggleActif.setDisable(false);
    }

    private void appliquerStyleBoutons() {
        if (btnAjouter != null) btnAjouter.setStyle(BTN_ADD);
        if (btnModifier != null) btnModifier.setStyle(BTN_EDIT);
        if (btnSupprimer != null) btnSupprimer.setStyle(BTN_DELETE);
        if (btnToggleActif != null) btnToggleActif.setStyle(BTN_TOGGLE);
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
