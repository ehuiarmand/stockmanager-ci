package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.UtilisateurDAO;
import com.inphb.icgl.stocks.model.Utilisateur;
import com.inphb.icgl.stocks.repository.IUtilisateurRepository;
import com.inphb.icgl.stocks.utils.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtMotDePasse;
    @FXML private TextField txtMotDePasseVisible;
    @FXML private TextField txtCodeTwoFactor;
    @FXML private Label lblTwoFactorHint;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnConnexion;
    @FXML private Button btnQuitter;
    @FXML private Label lblMessage;
    @FXML private ProgressIndicator progressIndicator;

    private final IUtilisateurRepository utilisateurDAO = new UtilisateurDAO();
    private int tentatives = 0;

    private static final int MAX_TENTATIVES = 3;
    private static final int BLOCAGE_SECONDES = 30;

    private static final String BTN_OK_NORMAL =
            "-fx-background-color:#1e8449; -fx-text-fill:white; " +
            "-fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-padding:11 0; -fx-background-radius:7; -fx-cursor:hand;";
    private static final String BTN_OK_HOVER =
            "-fx-background-color:#17692f; -fx-text-fill:white; " +
            "-fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-padding:11 0; -fx-background-radius:7; -fx-cursor:hand;";

    private static final String BTN_QUIT_NORMAL =
            "-fx-background-color:#c0392b; -fx-text-fill:white; " +
            "-fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-padding:11 0; -fx-background-radius:7; -fx-cursor:hand;";
    private static final String BTN_QUIT_HOVER =
            "-fx-background-color:#962d22; -fx-text-fill:white; " +
            "-fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-padding:11 0; -fx-background-radius:7; -fx-cursor:hand;";

    private static final String FIELD_NORMAL =
            "-fx-font-size:14px; -fx-padding:10 12; " +
            "-fx-background-color:white; " +
            "-fx-border-color:#CBD5E1; -fx-border-radius:6; " +
            "-fx-background-radius:6; -fx-pref-height:42px;";
    private static final String FIELD_FOCUS =
            "-fx-font-size:14px; -fx-padding:10 12; " +
            "-fx-background-color:white; " +
            "-fx-border-color:#2E5EAA; -fx-border-width:2; " +
            "-fx-border-radius:6; -fx-background-radius:6; -fx-pref-height:42px;";

    private boolean passwordVisible;
    private Utilisateur pendingTwoFactorUser;

    @FXML
    public void initialize() {
        lblMessage.setText("");
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        btnConnexion.setOnMouseEntered(e -> btnConnexion.setStyle(BTN_OK_HOVER));
        btnConnexion.setOnMouseExited(e -> btnConnexion.setStyle(BTN_OK_NORMAL));

        btnQuitter.setOnMouseEntered(e -> btnQuitter.setStyle(BTN_QUIT_HOVER));
        btnQuitter.setOnMouseExited(e -> btnQuitter.setStyle(BTN_QUIT_NORMAL));

        txtLogin.focusedProperty().addListener((obs, oldValue, focused) ->
                txtLogin.setStyle(Boolean.TRUE.equals(focused) ? FIELD_FOCUS : FIELD_NORMAL));
        txtMotDePasse.focusedProperty().addListener((obs, oldValue, focused) ->
                txtMotDePasse.setStyle(Boolean.TRUE.equals(focused) ? FIELD_FOCUS : FIELD_NORMAL));
        txtMotDePasseVisible.focusedProperty().addListener((obs, oldValue, focused) ->
                txtMotDePasseVisible.setStyle(Boolean.TRUE.equals(focused) ? FIELD_FOCUS : FIELD_NORMAL));
        txtCodeTwoFactor.focusedProperty().addListener((obs, oldValue, focused) ->
                txtCodeTwoFactor.setStyle(Boolean.TRUE.equals(focused) ? FIELD_FOCUS : FIELD_NORMAL));

        txtMotDePasseVisible.textProperty().bindBidirectional(txtMotDePasse.textProperty());
        updatePasswordToggleIcon();

        txtLogin.textProperty().addListener((obs, oldValue, newValue) -> resetTwoFactorState(false));
        txtMotDePasse.textProperty().addListener((obs, oldValue, newValue) -> {
            if (pendingTwoFactorUser != null) {
                resetTwoFactorState(false);
            }
        });

        txtLogin.setOnAction(e -> txtMotDePasse.requestFocus());
        txtMotDePasse.setOnAction(e -> handleConnexion());
        txtMotDePasseVisible.setOnAction(e -> handleConnexion());
        txtCodeTwoFactor.setOnAction(e -> handleConnexion());
        resetTwoFactorState(false);
    }

    @FXML
    private void handleConnexion() {
        if (pendingTwoFactorUser != null) {
            verifierDeuxiemeFacteur();
            return;
        }

        String login = txtLogin.getText() == null ? "" : txtLogin.getText().trim();
        String mdp = txtMotDePasse.getText() == null ? "" : txtMotDePasse.getText();

        if (login.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur utilisateur = utilisateurDAO.authenticate(login, mdp);
        String authError = utilisateurDAO.getLastErrorMessage();
        if (authError != null) {
            afficherErreur(authError);
            return;
        }

        if (utilisateur != null) {
            if (utilisateur.isTwoFactorEnabled()) {
                pendingTwoFactorUser = utilisateur;
                txtCodeTwoFactor.clear();
                txtCodeTwoFactor.setVisible(true);
                txtCodeTwoFactor.setManaged(true);
                lblTwoFactorHint.setVisible(true);
                lblTwoFactorHint.setManaged(true);
                btnConnexion.setText("Verifier le code");
                lblMessage.setText("Deuxieme facteur requis. Saisissez le code a 6 chiffres.");
                lblMessage.setStyle("-fx-text-fill:#1B3A6B; -fx-font-weight:bold;");
                txtCodeTwoFactor.requestFocus();
                return;
            }
            SessionManager.setUtilisateur(utilisateur);
            tentatives = 0;
            lblMessage.setText("Connexion reussie...");
            lblMessage.setStyle("-fx-text-fill:#1e8449; -fx-font-weight:bold;");
            ouvrirMainLayout();
            return;
        }

        tentatives++;
        int restantes = MAX_TENTATIVES - tentatives;
        if (tentatives >= MAX_TENTATIVES) {
            bloquerTemporairement();
        } else {
            afficherErreur("Identifiants incorrects - " + restantes + " tentative(s) restante(s).");
            txtMotDePasse.clear();
            txtMotDePasse.requestFocus();
        }
    }

    private void bloquerTemporairement() {
        btnConnexion.setDisable(true);
        txtLogin.setDisable(true);
        txtMotDePasse.setDisable(true);
        txtMotDePasseVisible.setDisable(true);
        txtCodeTwoFactor.setDisable(true);
        btnTogglePassword.setDisable(true);

        final int[] secondesRestantes = {BLOCAGE_SECONDES};
        PauseTransition countDown = new PauseTransition(Duration.seconds(1));
        countDown.setOnFinished(e -> {
            secondesRestantes[0]--;
            if (secondesRestantes[0] > 0) {
                afficherErreur("Compte bloque - reessayez dans " + secondesRestantes[0] + " s.");
                countDown.play();
            } else {
                tentatives = 0;
                btnConnexion.setDisable(false);
                txtLogin.setDisable(false);
                txtMotDePasse.setDisable(false);
                txtMotDePasseVisible.setDisable(false);
                txtCodeTwoFactor.setDisable(false);
                btnTogglePassword.setDisable(false);
                txtMotDePasse.clear();
                resetTwoFactorState(false);
                lblMessage.setText("Vous pouvez reessayer.");
                lblMessage.setStyle("-fx-text-fill:#1e8449; -fx-font-weight:bold;");
            }
        });

        afficherErreur("Trop de tentatives - blocage " + BLOCAGE_SECONDES + " s.");
        countDown.play();
    }

    private void ouvrirMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage mainStage = new Stage();
            String nom = SessionManager.getUtilisateur() == null
                    ? "Utilisateur"
                    : SessionManager.getUtilisateur().getNomComplet();
            mainStage.setTitle("StockManager CI - " + nom);
            mainStage.setScene(scene);
            mainStage.setMaximized(true);

            Stage loginStage = (Stage) btnConnexion.getScene().getWindow();
            loginStage.close();
            mainStage.show();
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuitter() {
        Platform.exit();
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold; -fx-font-size:12px;");
    }

    private void verifierDeuxiemeFacteur() {
        String code = txtCodeTwoFactor.getText() == null ? "" : txtCodeTwoFactor.getText().trim();
        if (!code.matches("\\d{6}")) {
            afficherErreur("Entrez un code 2FA a 6 chiffres.");
            return;
        }
        if (!utilisateurDAO.verifyTwoFactorCode(pendingTwoFactorUser, code)) {
            String message = utilisateurDAO.getLastErrorMessage();
            afficherErreur(message != null ? message : "Code 2FA invalide.");
            txtCodeTwoFactor.requestFocus();
            txtCodeTwoFactor.selectAll();
            return;
        }
        SessionManager.setUtilisateur(pendingTwoFactorUser);
        tentatives = 0;
        lblMessage.setText("Connexion reussie...");
        lblMessage.setStyle("-fx-text-fill:#1e8449; -fx-font-weight:bold;");
        resetTwoFactorState(false);
        ouvrirMainLayout();
    }

    private void resetTwoFactorState(boolean clearCode) {
        pendingTwoFactorUser = null;
        txtCodeTwoFactor.setVisible(false);
        txtCodeTwoFactor.setManaged(false);
        lblTwoFactorHint.setVisible(false);
        lblTwoFactorHint.setManaged(false);
        btnConnexion.setText("Se connecter");
        if (clearCode) {
            txtCodeTwoFactor.clear();
        }
    }

    @FXML
    private void handleTogglePassword() {
        togglePasswordVisibility(!passwordVisible);
    }

    private void togglePasswordVisibility(boolean afficher) {
        passwordVisible = afficher;
        txtMotDePasse.setVisible(!afficher);
        txtMotDePasse.setManaged(!afficher);
        txtMotDePasseVisible.setVisible(afficher);
        txtMotDePasseVisible.setManaged(afficher);
        updatePasswordToggleIcon();

        if (afficher) {
            txtMotDePasseVisible.requestFocus();
            txtMotDePasseVisible.positionCaret(txtMotDePasseVisible.getText().length());
        } else {
            txtMotDePasse.requestFocus();
            txtMotDePasse.positionCaret(txtMotDePasse.getText().length());
        }
    }

    private void updatePasswordToggleIcon() {
        if (btnTogglePassword != null) {
            btnTogglePassword.setText(passwordVisible ? "🙈" : "👁");
        }
    }
}
