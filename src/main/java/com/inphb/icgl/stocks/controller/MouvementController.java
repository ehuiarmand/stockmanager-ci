package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.MouvementDAO;
import com.inphb.icgl.stocks.dao.ProduitDAO;
import com.inphb.icgl.stocks.model.Mouvement;
import com.inphb.icgl.stocks.model.Produit;
import com.inphb.icgl.stocks.repository.IMouvementRepository;
import com.inphb.icgl.stocks.repository.IProduitRepository;
import com.inphb.icgl.stocks.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MouvementController {
    private static final int PAGE_SIZE = 15;
    private final IMouvementRepository mouvementDAO = new MouvementDAO();
    private final IProduitRepository produitDAO = new ProduitDAO();
    private int currentPage = 1;

    @FXML
    private ComboBox<Produit> cbProduit;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtMotif;
    @FXML
    private Label lblStatut;
    @FXML
    private ComboBox<Produit> cbFiltreProduit;
    @FXML
    private DatePicker dpDebut;
    @FXML
    private DatePicker dpFin;
    @FXML
    private TableView<Mouvement> tableMouvements;
    @FXML
    private TableColumn<Mouvement, Integer> colId;
    @FXML
    private TableColumn<Mouvement, String> colProduit;
    @FXML
    private TableColumn<Mouvement, String> colType;
    @FXML
    private TableColumn<Mouvement, Integer> colQuantite;
    @FXML
    private TableColumn<Mouvement, String> colMotif;
    @FXML
    private TableColumn<Mouvement, String> colUtilisateur;
    @FXML
    private TableColumn<Mouvement, LocalDateTime> colDate;
    @FXML
    private Label lblPagination;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduit.setCellValueFactory(new PropertyValueFactory<>("designationProduit"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeMouvement"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMouvement"));
        cbType.setItems(FXCollections.observableArrayList("ENTREE", "SORTIE"));
        cbType.setValue("ENTREE");
        var produits = produitDAO.findAllNoPaging();
        cbProduit.setItems(produits);
        cbFiltreProduit.setItems(produits);
        charger();
    }

    @FXML
    private void handleEnregistrer() {
        Produit produit = cbProduit.getValue();
        if (produit == null) {
            lblStatut.setText("Selectionnez un produit.");
            return;
        }
        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantite.getText().trim());
        } catch (Exception e) {
            lblStatut.setText("Quantite invalide.");
            return;
        }
        if (quantite <= 0) {
            lblStatut.setText("Quantite doit etre > 0.");
            return;
        }
        Mouvement m = new Mouvement();
        m.setIdProduit(produit.getId());
        m.setTypeMouvement(cbType.getValue());
        m.setQuantite(quantite);
        m.setMotif(txtMotif.getText() == null ? "" : txtMotif.getText().trim());
        if (SessionManager.getUtilisateur() != null) {
            m.setIdUtilisateur(SessionManager.getUtilisateur().getId());
        }
        if (mouvementDAO.save(m)) {
            txtQuantite.clear();
            txtMotif.clear();
            lblStatut.setText("Mouvement enregistre.");
            charger();
            cbProduit.setItems(produitDAO.findAllNoPaging());
            cbFiltreProduit.setItems(produitDAO.findAllNoPaging());
        } else {
            lblStatut.setText("Echec: stock insuffisant ou erreur BD.");
        }
    }

    @FXML
    private void handleFiltrer() {
        currentPage = 1;
        charger();
    }

    @FXML
    private void handleResetFiltre() {
        cbFiltreProduit.setValue(null);
        dpDebut.setValue(null);
        dpFin.setValue(null);
        currentPage = 1;
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
        int total = totalCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage < totalPages) {
            currentPage++;
            charger();
        }
    }

    private void charger() {
        Integer produitId = cbFiltreProduit.getValue() == null ? null : cbFiltreProduit.getValue().getId();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();
        tableMouvements.setItems(mouvementDAO.filter(produitId, debut, fin, currentPage, PAGE_SIZE));
        refreshPagination();
    }

    private int totalCount() {
        Integer produitId = cbFiltreProduit.getValue() == null ? null : cbFiltreProduit.getValue().getId();
        return mouvementDAO.countFilter(produitId, dpDebut.getValue(), dpFin.getValue());
    }

    private void refreshPagination() {
        int total = totalCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        lblPagination.setText("Page " + currentPage + " / " + totalPages + " - " + total + " mouvements");
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }
}
