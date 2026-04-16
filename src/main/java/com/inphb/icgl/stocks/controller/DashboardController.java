package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.dao.MouvementDAO;
import com.inphb.icgl.stocks.dao.ProduitDAO;
import com.inphb.icgl.stocks.model.Mouvement;
import com.inphb.icgl.stocks.model.Produit;
import com.inphb.icgl.stocks.repository.IMouvementRepository;
import com.inphb.icgl.stocks.repository.IProduitRepository;
import com.inphb.icgl.stocks.utils.SessionManager;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblBienvenue;
    @FXML private Label lblTotalProduits;
    @FXML private Label lblProduitsAlerte;
    @FXML private Label lblMouvementsJour;
    @FXML private Label lblValeurStock;
    @FXML private Label lblChartResume;
    @FXML private Label lblChartNormal;
    @FXML private Label lblChartAlert;
    @FXML private PieChart chartStockStatus;

    @FXML private TableView<Produit>             tableAlertes;
    @FXML private TableColumn<Produit, String>   colDesignation;
    @FXML private TableColumn<Produit, Integer>  colQteStock;
    @FXML private TableColumn<Produit, Integer>  colStockMin;
    @FXML private TableColumn<Produit, String>   colCategorie;
    @FXML private TableColumn<Produit, String>   colFournisseur;
    @FXML private TableColumn<Produit, String>   colStatut;
    @FXML private TableColumn<Produit, Void>     colAction;

    private final IProduitRepository produitDAO = new ProduitDAO();
    private final IMouvementRepository mouvementDAO = new MouvementDAO();

    @FXML
    public void initialize() {
        configurerTableau();
        chargerIndicateurs();
    }

    private void configurerTableau() {
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colQteStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimum"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        colFournisseur.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));

        colStatut.setCellValueFactory(data -> {
            Produit p = data.getValue();
            int diff = p.getQuantiteStock() - p.getStockMinimum();
            String statut = (diff < 0) ? "CRITIQUE" : (diff == 0) ? "ALERTE" : "OK";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });

        colStatut.setCellFactory(col -> new TableCell<Produit, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (statut == null || empty) {
                    setText(""); setStyle("");
                } else if (statut.contains("CRITIQUE")) {
                    setText(statut);
                    setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold; -fx-alignment:CENTER;");
                } else if (statut.contains("ALERTE")) {
                    setText(statut);
                    setStyle("-fx-text-fill:#e67e22; -fx-font-weight:bold; -fx-alignment:CENTER;");
                } else {
                    setText(statut);
                    setStyle("-fx-text-fill:#1e8449; -fx-font-weight:bold; -fx-alignment:CENTER;");
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnApprovisionner = new Button("Ajouter stock");

            {
                btnApprovisionner.setStyle(
                        "-fx-background-color:#2F80ED; -fx-border-color:#1F5FB9; " +
                        "-fx-border-width:1.2; -fx-text-fill:white; -fx-font-size:12px; " +
                        "-fx-font-weight:700; -fx-background-radius:7; -fx-border-radius:7;"
                );
                btnApprovisionner.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    approvisionnerProduit(produit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnApprovisionner);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setStyle("-fx-alignment:CENTER;");
                }
            }
        });

        tableAlertes.setRowFactory(tv -> new TableRow<Produit>() {
            @Override
            protected void updateItem(Produit p, boolean empty) {
                super.updateItem(p, empty);
                if (p == null || empty) {
                    setStyle("");
                } else {
                    int diff = p.getQuantiteStock() - p.getStockMinimum();
                    if (diff < 0) {
                        setStyle("-fx-background-color:#FFE0E0;");
                    } else if (diff == 0) {
                        setStyle("-fx-background-color:#FFF3E0;");
                    } else {
                        setStyle("-fx-background-color:#F0FFF4;");
                    }
                }
            }
        });

        centerColumn(colQteStock);
        centerColumn(colStockMin);
    }

    private <T> void centerColumn(TableColumn<Produit, T> col) {
        col.setCellFactory(c -> new TableCell<Produit, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setText(""); }
                else { setText(item.toString()); setStyle("-fx-alignment:CENTER;"); }
            }
        });
    }

    public void chargerIndicateurs() {
        if (lblBienvenue != null && SessionManager.getUtilisateur() != null) {
            lblBienvenue.setText(SessionManager.getUtilisateur().getNomComplet()
                    + "  [" + SessionManager.getUtilisateur().getRole() + "]");
        }

        int totalProduits = produitDAO.countAll();
        lblTotalProduits.setText(String.valueOf(totalProduits));

        int nbAlertes = produitDAO.countEnAlerte();
        lblProduitsAlerte.setText(String.valueOf(nbAlertes));

        int nbMvt = mouvementDAO.countToday();
        lblMouvementsJour.setText(String.valueOf(nbMvt));

        double valeurTotale = calculerValeurStock();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        lblValeurStock.setText(nf.format((long) valeurTotale) + " FCFA");

        chargerGraphique(totalProduits, nbAlertes);

        ObservableList<Produit> alertes = produitDAO.findEnAlerte();
        if (alertes.size() > 5) {
            tableAlertes.setItems(FXCollections.observableArrayList(alertes.subList(0, 5)));
        } else {
            tableAlertes.setItems(alertes);
        }
    }

    private void chargerGraphique(int totalProduits, int nbAlertes) {
        if (chartStockStatus == null) {
            return;
        }
        int stockNormal = Math.max(0, totalProduits - nbAlertes);
        ObservableList<PieChart.Data> data;
        if (nbAlertes <= 0) {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("Stock normal", Math.max(1, totalProduits))
            );
        } else if (stockNormal <= 0) {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("En alerte", Math.max(1, nbAlertes))
            );
        } else {
            data = FXCollections.observableArrayList(
                    new PieChart.Data("Stock normal", stockNormal),
                    new PieChart.Data("En alerte", nbAlertes)
            );
        }
        chartStockStatus.setData(data);
        chartStockStatus.setClockwise(true);
        chartStockStatus.setLabelLineLength(0);
        chartStockStatus.setStartAngle(90);

        int pourcentageStable = totalProduits == 0 ? 0 : (stockNormal * 100 / totalProduits);
        if (lblChartResume != null) {
            lblChartResume.setText(pourcentageStable + "% du stock est stable");
        }
        if (lblChartNormal != null) {
            lblChartNormal.setText("Stock normal : " + stockNormal);
        }
        if (lblChartAlert != null) {
            lblChartAlert.setText("En alerte : " + nbAlertes);
        }
    }

    private double calculerValeurStock() {
        ObservableList<Produit> tous = produitDAO.findAllNoPaging();
        return tous.stream()
                .mapToDouble(p -> BigDecimal.valueOf(p.getQuantiteStock())
                        .multiply(p.getPrixUnitaire() == null ? BigDecimal.ZERO : p.getPrixUnitaire())
                        .doubleValue())
                .sum();
    }

    private void approvisionnerProduit(Produit produit) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Approvisionnement");
        dialog.setHeaderText("Approvisionner : " + produit.getDesignation());
        dialog.setContentText("Quantite a ajouter :");
        dialog.showAndWait().ifPresent(saisie -> {
            try {
                int quantite = Integer.parseInt(saisie.trim());
                if (quantite <= 0) {
                    afficherMessage("Quantite invalide", "La quantite doit etre superieure a 0.", Alert.AlertType.WARNING);
                    return;
                }

                Mouvement mouvement = new Mouvement();
                mouvement.setIdProduit(produit.getId());
                mouvement.setTypeMouvement("ENTREE");
                mouvement.setQuantite(quantite);
                mouvement.setMotif("Approvisionnement rapide depuis le tableau de bord");
                if (SessionManager.getUtilisateur() != null) {
                    mouvement.setIdUtilisateur(SessionManager.getUtilisateur().getId());
                }

                if (mouvementDAO.save(mouvement)) {
                    afficherMessage("Approvisionnement", "Stock mis a jour avec succes.", Alert.AlertType.INFORMATION);
                    chargerIndicateurs();
                } else {
                    afficherMessage("Echec", "Impossible d'approvisionner ce produit.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                afficherMessage("Quantite invalide", "Entrez un nombre entier valide.", Alert.AlertType.WARNING);
            }
        });
    }

    private void afficherMessage(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
