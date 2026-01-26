package com.cloud.identity.dto;

import java.math.BigDecimal;

public class SignalementDTO {
    @com.google.cloud.firestore.annotation.PropertyName("photo_url")
    private String photoUrl;
    
    @com.google.cloud.firestore.annotation.PropertyName("surface_m2")
    private Double surfaceM2;
    
    @com.google.cloud.firestore.annotation.PropertyName("entreprise_concerne")
    private String entrepriseConcerne;

    @com.google.cloud.firestore.annotation.PropertyName("id_firebase")
    private String idFirebase;

    @com.google.cloud.firestore.annotation.PropertyName("date_signalement")
    private Object dateSignalement;

    private String postgresId;
    private Double latitude;
    private Double longitude;
    private String description;
    private BigDecimal budget;
    private String statut;
    private String email; 
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostgresId() {
        return postgresId;
    }

    public void setPostgresId(String postgresId) {
        this.postgresId = postgresId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    @com.google.cloud.firestore.annotation.PropertyName("photo_url")
    public String getPhotoUrl() {
        return photoUrl;
    }

    @com.google.cloud.firestore.annotation.PropertyName("photo_url")
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @com.google.cloud.firestore.annotation.PropertyName("surface_m2")
    public Double getSurfaceM2() {
        return surfaceM2;
    }

    @com.google.cloud.firestore.annotation.PropertyName("surface_m2")
    public void setSurfaceM2(Double surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    @com.google.cloud.firestore.annotation.PropertyName("entreprise_concerne")
    public String getEntrepriseConcerne() {
        return entrepriseConcerne;
    }

    @com.google.cloud.firestore.annotation.PropertyName("entreprise_concerne")
    public void setEntrepriseConcerne(String entrepriseConcerne) {
        this.entrepriseConcerne = entrepriseConcerne;
    }

    @com.google.cloud.firestore.annotation.PropertyName("id_firebase")
    public String getIdFirebase() {
        return idFirebase;
    }

    @com.google.cloud.firestore.annotation.PropertyName("id_firebase")
    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    @com.google.cloud.firestore.annotation.PropertyName("date_signalement")
    public Object getDateSignalement() {
        return dateSignalement;
    }

    @com.google.cloud.firestore.annotation.PropertyName("date_signalement")
    public void setDateSignalement(Object dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }
}
