package com.inphb.icgl.stocks.model;

import java.time.LocalDateTime;

public class MouvementArchive {
    private int id;
    private Integer idMouvementOrigine;
    private int idProduit;
    private String typeMouvement;
    private int quantite;
    private String motif;
    private Integer idUtilisateur;
    private LocalDateTime dateMouvement;
    private LocalDateTime dateArchivage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdMouvementOrigine() {
        return idMouvementOrigine;
    }

    public void setIdMouvementOrigine(Integer idMouvementOrigine) {
        this.idMouvementOrigine = idMouvementOrigine;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(String typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public LocalDateTime getDateArchivage() {
        return dateArchivage;
    }

    public void setDateArchivage(LocalDateTime dateArchivage) {
        this.dateArchivage = dateArchivage;
    }
}

