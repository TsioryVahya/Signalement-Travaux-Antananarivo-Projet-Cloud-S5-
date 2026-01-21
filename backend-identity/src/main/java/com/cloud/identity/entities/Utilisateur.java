package com.cloud.identity.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statut_actuel_id")
    private StatutsUtilisateur statutActuel;

    @ColumnDefault("0")
    @Column(name = "tentatives_connexion")
    private Integer tentativesConnexion;

    @Column(name = "derniere_connexion")
    private Instant derniereConnexion;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_creation")
    private Instant dateCreation;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public StatutsUtilisateur getStatutActuel() {
        return statutActuel;
    }

    public void setStatutActuel(StatutsUtilisateur statutActuel) {
        this.statutActuel = statutActuel;
    }

    public Integer getTentativesConnexion() {
        return tentativesConnexion;
    }

    public void setTentativesConnexion(Integer tentativesConnexion) {
        this.tentativesConnexion = tentativesConnexion;
    }

    public Instant getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(Instant derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statutActuel != null ? statutActuel.getNom() : null;
    }

    public void setStatut(String statut) {
        if (statut != null) {
            StatutsUtilisateur s = new StatutsUtilisateur();
            s.setNom(statut);
            this.statutActuel = s;
        }
    }

}