package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform;

public class MainLayoutController {

    @FXML private StackPane mainPane;
    @FXML private Label lblUtilisateur;
    @FXML private Label lblTitrePage;
    @FXML private Button btnAccueil;
    @FXML private MenuButton menuStock;
    @FXML private MenuButton menuAdministration;
    @FXML private MenuButton menuSession;
    @FXML private MenuItem menuUtilisateurs;
    @FXML private MenuItem menuArchives;

    @FXML
    public void initialize() {
        boolean admin = SessionManager.isAdmin();

        if (lblUtilisateur != null && SessionManager.getUtilisateur() != null) {
            lblUtilisateur.setText(SessionManager.getUtilisateur().getNomComplet()
                    + " [" + SessionManager.getUtilisateur().getRole() + "]");
        }
        if (menuAdministration != null) {
            menuAdministration.setVisible(admin);
            menuAdministration.setManaged(admin);
        }
        if (menuUtilisateurs != null) {
            menuUtilisateurs.setVisible(admin);
        }
        if (menuArchives != null) {
            menuArchives.setVisible(admin);
        }
        chargerVue("/fxml/Dashboard.fxml", "Tableau de Bord");
    }

    @FXML public void ouvrirDashboard()    { chargerVue("/fxml/Dashboard.fxml",   "Tableau de Bord"); }
    @FXML public void ouvrirCategories()   { chargerVue("/fxml/Categorie.fxml",   "Categories"); }
    @FXML public void ouvrirFournisseurs() { chargerVue("/fxml/Fournisseur.fxml", "Fournisseurs"); }
    @FXML public void ouvrirProduits()     { chargerVue("/fxml/Produit.fxml",     "Produits"); }
    @FXML public void ouvrirMouvements()   { chargerVue("/fxml/Mouvement.fxml",   "Mouvements de Stock"); }
    @FXML public void ouvrirUtilisateurs() {
        if (SessionManager.isAdmin())
            chargerVue("/fxml/Utilisateur.fxml", "Utilisateurs");
    }
    @FXML public void ouvrirArchives() {
        if (SessionManager.isAdmin())
            chargerVue("/fxml/Archive.fxml", "Archives des Mouvements");
    }

    @FXML
    public void handleDeconnexion() {
        try {
            SessionManager.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Scene loginScene = new javafx.scene.Scene(loader.load(), 480, 360);
            loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setMaximized(false);
            stage.setWidth(480);
            stage.setHeight(360);
            stage.centerOnScreen();
            stage.setTitle("StockManager CI - Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleQuitter() {
        Platform.exit();
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            mainPane.getChildren().setAll((javafx.scene.Node) loader.load());
            if (lblTitrePage != null) {
                lblTitrePage.setText(titre);
            }
            updateActiveMenu(fxmlPath);
        } catch (Exception e) {
            System.err.println("[MainLayout] Erreur chargement vue " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveMenu(String fxmlPath) {
        setNavActive(btnAccueil, false);
        setNavActive(menuStock, false);
        setNavActive(menuAdministration, false);
        setNavActive(menuSession, false);

        if ("/fxml/Dashboard.fxml".equals(fxmlPath)) {
            setNavActive(btnAccueil, true);
        } else if ("/fxml/Categorie.fxml".equals(fxmlPath)
                || "/fxml/Fournisseur.fxml".equals(fxmlPath)
                || "/fxml/Produit.fxml".equals(fxmlPath)
                || "/fxml/Mouvement.fxml".equals(fxmlPath)) {
            setNavActive(menuStock, true);
        } else if ("/fxml/Utilisateur.fxml".equals(fxmlPath)
                || "/fxml/Archive.fxml".equals(fxmlPath)) {
            setNavActive(menuAdministration, true);
        }
    }

    private void setNavActive(Control control, boolean active) {
        if (control == null) {
            return;
        }
        control.getStyleClass().remove("main-nav-item-active");
        if (active && !control.getStyleClass().contains("main-nav-item-active")) {
            control.getStyleClass().add("main-nav-item-active");
        }
    }
}
