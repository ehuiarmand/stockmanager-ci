package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.CategorieDAO;
import com.inphb.icgl.stocks.dao.FournisseurDAO;
import com.inphb.icgl.stocks.dao.ProduitDAO;
import com.inphb.icgl.stocks.model.Categorie;
import com.inphb.icgl.stocks.model.Fournisseur;
import com.inphb.icgl.stocks.model.Produit;
import com.inphb.icgl.stocks.repository.ICategorieRepository;
import com.inphb.icgl.stocks.repository.IFournisseurRepository;
import com.inphb.icgl.stocks.repository.IProduitRepository;
import com.inphb.icgl.stocks.utils.ExportUtil;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;

public class ProduitController {
    private static final int PAGE_SIZE = 15;
    private static final PseudoClass STOCK_ALERT = PseudoClass.getPseudoClass("stock-alert");
    private static final String BTN_ADD =
            "-fx-background-color:#7FBB9A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_EDIT =
            "-fx-background-color:#4A90CC; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";
    private static final String BTN_DELETE =
            "-fx-background-color:#D4820A; -fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold; " +
            "-fx-background-radius:8; -fx-border-radius:8; -fx-padding:8 18;";

    private final IProduitRepository produitDAO = new ProduitDAO();
    private final ICategorieRepository categorieDAO = new CategorieDAO();
    private final IFournisseurRepository fournisseurDAO = new FournisseurDAO();
    private int currentPage = 1;

    @FXML private TextField txtReference;
    @FXML private TextField txtDesignation;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private ComboBox<Fournisseur> cbFournisseur;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private TextField txtStockMin;
    @FXML private TextField txtUnite;
    @FXML private TextField txtRecherche;
    @FXML private Label lblAlerte;
    @FXML private Label lblStatut;
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colReference;
    @FXML private TableColumn<Produit, String> colDesignation;
    @FXML private TableColumn<Produit, String> colCategorie;
    @FXML private TableColumn<Produit, String> colFournisseur;
    @FXML private TableColumn<Produit, BigDecimal> colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, Integer> colStockMin;
    @FXML private TableColumn<Produit, String> colUnite;
    @FXML private Label lblPagination;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVider;

    @FXML
    public void initialize() {
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        colFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimum"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colQuantite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(item));
                Produit p = getTableRow() == null ? null : getTableRow().getItem();
                if (p != null && p.getQuantiteStock() <= p.getStockMinimum())
                    setStyle("-fx-background-color:#ffb3b3; -fx-text-fill:#7a0000; -fx-font-weight:700;");
                else setStyle("");
            }
        });
        colStockMin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(item));
                Produit p = getTableRow() == null ? null : getTableRow().getItem();
                if (p != null && p.getQuantiteStock() <= p.getStockMinimum())
                    setStyle("-fx-background-color:#ffd1d1; -fx-text-fill:#7a0000; -fx-font-weight:700;");
                else setStyle("");
            }
        });
        tableProduits.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produit p, boolean empty) {
                super.updateItem(p, empty);
                pseudoClassStateChanged(STOCK_ALERT, !empty && p != null && p.getQuantiteStock() <= p.getStockMinimum());
            }
        });
        tableProduits.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            remplirFormulaire(s);
            mettreAJourEtatActions();
        });
        txtRecherche.textProperty().addListener((obs, o, n) -> { currentPage = 1; charger(); });
        cbCategorie.setItems(categorieDAO.findAll(1, 1000));
        cbFournisseur.setItems(fournisseurDAO.findAll(1, 1000));
        appliquerStyleBoutons();
        mettreAJourEtatActions();
        charger();
        Platform.runLater(() -> {
            tableProduits.getSelectionModel().clearSelection();
            mettreAJourEtatActions();
        });
    }

    @FXML
    private void handleAjouter() {
        tableProduits.getSelectionModel().clearSelection();
        Produit p = lireFormulaire();
        if (p == null) return;
        if (produitDAO.save(p)) {
            viderFormulaire();
            charger();
            lblStatut.setText("Produit ajoute avec succes.");
        } else {
            lblStatut.setText("Echec ajout : la reference existe peut-etre deja.");
        }
    }

    @FXML
    private void handleModifier() {
        Produit s = tableProduits.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez un produit a modifier.");
            return;
        }
        Produit p = lireFormulaire();
        if (p == null) return;
        if (!confirmer("Confirmation de modification",
                "Voulez-vous modifier le produit \"" + s.getDesignation() + "\" ?")) {
            lblStatut.setText("Modification annulee.");
            return;
        }
        p.setId(s.getId());
        if (produitDAO.update(p)) {
            charger();
            lblStatut.setText("Produit modifie avec succes.");
        } else {
            lblStatut.setText("Echec de la modification.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Produit s = tableProduits.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblStatut.setText("Selectionnez un produit a supprimer.");
            return;
        }
        if (!confirmer("Confirmation de suppression",
                "Voulez-vous supprimer le produit \"" + s.getDesignation() + "\" ?\n" +
                "Les mouvements associes seront archives.")) {
            lblStatut.setText("Suppression annulee.");
            return;
        }
        if (produitDAO.delete(s.getId())) {
            viderFormulaire();
            charger();
            lblStatut.setText("Produit supprime (mouvements archives).");
        } else {
            lblStatut.setText("Echec de la suppression.");
        }
    }

    @FXML
    private void handleExporter() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le fichier Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
        chooser.setInitialFileName("produits_stock.xlsx");
        Stage stage = (Stage) tableProduits.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            ExportUtil.exporterXLSX(produitDAO.findAllNoPaging(), file);
            lblStatut.setText("Export reussi : " + file.getAbsolutePath());
        } catch (Exception e) {
            lblStatut.setText("Echec export : " + e.getMessage());
        }
    }

    @FXML
    private void handleExporterPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le fichier PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        chooser.setInitialFileName("produits_stock.pdf");
        Stage stage = (Stage) tableProduits.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            ExportUtil.exporterPDF(produitDAO.findAllNoPaging(), file);
            lblStatut.setText("Export PDF reussi : " + file.getAbsolutePath());
        } catch (Exception e) {
            lblStatut.setText("Echec export PDF : " + e.getMessage());
        }
    }

    @FXML
    private void handleVider() {
        viderFormulaire();
        lblStatut.setText("Formulaire vide.");
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
        tableProduits.setItems(search.isBlank()
                ? produitDAO.findAll(currentPage, PAGE_SIZE)
                : produitDAO.search(search, currentPage, PAGE_SIZE));
        tableProduits.getSelectionModel().clearSelection();
        lblAlerte.setText(String.valueOf(produitDAO.countEnAlerte()));
        refreshPagination();
        mettreAJourEtatActions();
    }

    private int totalCount() {
        String search = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();
        return search.isBlank() ? produitDAO.countAll() : produitDAO.countSearch(search);
    }

    private void refreshPagination() {
        int total = totalCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " produits");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    private Produit lireFormulaire() {
        try {
            Produit p = new Produit();
            p.setReference(txtReference.getText().trim());
            p.setDesignation(txtDesignation.getText().trim());
            Categorie c = cbCategorie.getValue();
            Fournisseur f = cbFournisseur.getValue();
            p.setIdCategorie(c == null ? null : c.getId());
            p.setIdFournisseur(f == null ? null : f.getId());
            p.setPrixUnitaire(new BigDecimal(txtPrix.getText().trim().isBlank() ? "0" : txtPrix.getText().trim()));
            p.setQuantiteStock(Integer.parseInt(txtQuantite.getText().trim()));
            p.setStockMinimum(Integer.parseInt(txtStockMin.getText().trim()));
            p.setUnite(txtUnite.getText().trim().isBlank() ? "piece" : txtUnite.getText().trim());
            if (p.getReference().isBlank() || p.getDesignation().isBlank()) {
                lblStatut.setText("Reference et designation obligatoires.");
                return null;
            }
            return p;
        } catch (Exception e) {
            lblStatut.setText("Valeurs numeriques invalides (quantite, stock min, prix).");
            return null;
        }
    }

    private void remplirFormulaire(Produit p) {
        if (p == null) { lblStatut.setText(""); return; }
        txtReference.setText(p.getReference());
        txtDesignation.setText(p.getDesignation());
        txtPrix.setText(p.getPrixUnitaire() == null ? "0" : p.getPrixUnitaire().toPlainString());
        txtQuantite.setText(String.valueOf(p.getQuantiteStock()));
        txtStockMin.setText(String.valueOf(p.getStockMinimum()));
        txtUnite.setText(p.getUnite());
        if (p.getIdCategorie() != null)
            cbCategorie.getItems().stream().filter(c -> c.getId() == p.getIdCategorie()).findFirst().ifPresent(cbCategorie::setValue);
        else cbCategorie.setValue(null);
        if (p.getIdFournisseur() != null)
            cbFournisseur.getItems().stream().filter(f -> f.getId() == p.getIdFournisseur()).findFirst().ifPresent(cbFournisseur::setValue);
        else cbFournisseur.setValue(null);
    }

    private void viderFormulaire() {
        txtReference.clear(); txtDesignation.clear(); txtPrix.clear();
        txtQuantite.clear(); txtStockMin.clear(); txtUnite.clear();
        cbCategorie.setValue(null); cbFournisseur.setValue(null);
        tableProduits.getSelectionModel().clearSelection();
        mettreAJourEtatActions();
    }

    private void mettreAJourEtatActions() {
        if (btnAjouter != null) btnAjouter.setDisable(false);
        if (btnModifier != null) btnModifier.setDisable(false);
        if (btnSupprimer != null) btnSupprimer.setDisable(false);
        if (btnVider != null) btnVider.setDisable(false);
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
