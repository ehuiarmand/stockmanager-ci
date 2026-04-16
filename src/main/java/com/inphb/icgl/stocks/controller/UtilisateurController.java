package com.inphb.icgl.stocks.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.inphb.icgl.stocks.dao.UtilisateurDAO;
import com.inphb.icgl.stocks.model.Utilisateur;
import com.inphb.icgl.stocks.repository.IUtilisateurRepository;
import com.inphb.icgl.stocks.utils.SessionManager;
import com.inphb.icgl.stocks.utils.TotpUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class UtilisateurController {
    private static final int PAGE_SIZE = 10;
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
    @FXML private CheckBox chkTwoFactor;
    @FXML private TextField txtTwoFactorSecret;
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
    @FXML private Button btnGenererTwoFactor;

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
        colActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(actif ? "Actif" : "Desactive");
                    setStyle(actif
                            ? "-fx-text-fill:#1e8449; -fx-font-weight:bold; -fx-alignment:CENTER;"
                            : "-fx-text-fill:#c0392b; -fx-font-weight:bold; -fx-alignment:CENTER;");
                }
            }
        });
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "GESTIONNAIRE"));
        cbRole.setValue("GESTIONNAIRE");
        chkTwoFactor.selectedProperty().addListener((obs, oldValue, enabled) -> updateTwoFactorControls());
        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            remplirFormulaire(s);
            updateToggleButton(s);
            mettreAJourEtatActions();
        });
        appliquerStyleBoutons();
        updateToggleButton(null);
        updateTwoFactorControls();
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
        u.setTwoFactorEnabled(chkTwoFactor.isSelected());
        u.setTwoFactorSecret(txtTwoFactorSecret.getText().trim());
        if (dao.save(u)) {
            if (u.isTwoFactorEnabled()) {
                afficherInfosTwoFactor(u);
            }
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
        s.setTwoFactorEnabled(chkTwoFactor.isSelected());
        s.setTwoFactorSecret(txtTwoFactorSecret.getText().trim());
        if (dao.update(s)) {
            if (s.isTwoFactorEnabled()) {
                afficherInfosTwoFactor(s);
            }
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
        chkTwoFactor.setSelected(u.isTwoFactorEnabled());
        txtTwoFactorSecret.setText(u.getTwoFactorSecret() == null ? "" : u.getTwoFactorSecret());
        updateTwoFactorControls();
    }

    private void viderFormulaire() {
        txtNomComplet.clear();
        txtLogin.clear();
        txtMotDePasse.clear();
        cbRole.setValue("GESTIONNAIRE");
        chkActif.setSelected(true);
        chkTwoFactor.setSelected(false);
        txtTwoFactorSecret.clear();
        tableUtilisateurs.getSelectionModel().clearSelection();
        updateToggleButton(null);
        updateTwoFactorControls();
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

    @FXML
    private void handleGenererTwoFactor() {
        if (txtLogin.getText() == null || txtLogin.getText().isBlank()) {
            lblStatut.setText("Renseignez d'abord le login pour generer la cle 2FA.");
            return;
        }
        chkTwoFactor.setSelected(true);
        txtTwoFactorSecret.setText(TotpUtil.generateSecret());
        updateTwoFactorControls();
        lblStatut.setText("Cle 2FA generee. Enregistrez l'utilisateur puis configurez l'application Authenticator.");
    }

    private void updateTwoFactorControls() {
        boolean enabled = chkTwoFactor != null && chkTwoFactor.isSelected();
        if (txtTwoFactorSecret != null) {
            txtTwoFactorSecret.setDisable(!enabled);
        }
        if (!enabled && txtTwoFactorSecret != null) {
            txtTwoFactorSecret.clear();
        }
    }

    private void afficherInfosTwoFactor(Utilisateur utilisateur) {
        String secret = utilisateur.getTwoFactorSecret();
        if (secret == null || secret.isBlank()) {
            return;
        }
        String otpauth = TotpUtil.buildOtpAuthUrl("StockManager CI", utilisateur.getLogin(), secret);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuration 2FA");
        alert.setHeaderText("Activez la double authentification");
        alert.setContentText("Scannez ce QR code dans Google Authenticator ou Microsoft Authenticator.");
        ImageView qrCodeView = buildQrCodeView(otpauth);
        VBox content = new VBox(12);
        content.setPadding(new Insets(8, 0, 0, 0));
        if (qrCodeView != null) {
            content.getChildren().add(qrCodeView);
        }
        alert.getDialogPane().setContent(content);
        TextArea details = new TextArea("Cle secrete : " + secret + System.lineSeparator()
                + "Lien OTP : " + otpauth);
        details.setWrapText(true);
        details.setEditable(false);
        details.setPrefRowCount(4);
        alert.getDialogPane().setExpandableContent(details);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    private ImageView buildQrCodeView(String value) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(value, BarcodeFormat.QR_CODE, 240, 240);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            ImageView imageView = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
            imageView.setFitWidth(220);
            imageView.setFitHeight(220);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (WriterException e) {
            lblStatut.setText("Impossible de generer le QR code 2FA.");
            return null;
        }
    }
}
