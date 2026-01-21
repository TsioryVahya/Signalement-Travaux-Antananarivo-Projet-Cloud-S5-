package com.cloud.identity.dto;

import java.math.BigDecimal;

public class SignalementDTO {
    private String idFirebase;
    private Double latitude;
    private Double longitude;
    private String dateSignalement;
    private String description;
    private Double surfaceM2;
    private BigDecimal budget;
    private String entrepriseConcerne;
    private String photoUrl;
    private UtilisateurDTO utilisateur;

    public static class UtilisateurDTO {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(String dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(Double surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getEntrepriseConcerne() {
        return entrepriseConcerne;
    }

    public void setEntrepriseConcerne(String entrepriseConcerne) {
        this.entrepriseConcerne = entrepriseConcerne;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }
}
