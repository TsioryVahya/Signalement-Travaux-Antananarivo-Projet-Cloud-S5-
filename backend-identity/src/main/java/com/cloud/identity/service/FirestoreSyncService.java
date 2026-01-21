package com.cloud.identity.service;

import com.cloud.identity.entities.Signalement;
import com.cloud.identity.entities.SignalementsDetail;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirestoreSyncService {

    @Autowired
    private Firestore firestore;

    /**
     * Crée un nouveau signalement dans Firestore à partir d'un signalement PostgreSQL.
     */
    public String createSignalementInFirestore(Signalement signalement, SignalementsDetail details) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("postgresId", signalement.getId().toString());
            data.put("latitude", signalement.getLatitude());
            data.put("longitude", signalement.getLongitude());
            data.put("dateSignalement", signalement.getDateSignalement() != null ? signalement.getDateSignalement().toString() : null);
            
            if (signalement.getStatut() != null) {
                data.put("statut", signalement.getStatut().getNom());
            }
            
            if (signalement.getUtilisateur() != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", signalement.getUtilisateur().getEmail());
                data.put("utilisateur", userMap);
            }

            if (details != null) {
                data.put("description", details.getDescription());
                data.put("surfaceM2", details.getSurfaceM2());
                data.put("budget", details.getBudget());
                data.put("entrepriseConcerne", details.getEntrepriseConcerne());
                data.put("photoUrl", details.getPhotoUrl());
            }

            // Ajouter à Firestore et récupérer l'ID généré
            DocumentReference docRef = firestore.collection("signalements").document();
            docRef.set(data).get(); // .get() pour attendre la fin de l'opération
            
            System.out.println("Signalement créé dans Firestore avec ID : " + docRef.getId());
            return docRef.getId();
        } catch (Exception e) {
            System.err.println("Erreur lors de la création Firestore : " + e.getMessage());
            return null;
        }
    }

    /**
     * Synchronise l'état d'un signalement de PostgreSQL vers Firebase.
     */
    public void syncSignalementToFirebase(Signalement signalement) {
        if (signalement.getIdFirebase() == null) {
            return;
        }

        try {
            Map<String, Object> updates = new HashMap<>();
            if (signalement.getStatut() != null) {
                updates.put("statut", signalement.getStatut().getNom());
            }
            updates.put("postgresId", signalement.getId().toString());
            updates.put("latitude", signalement.getLatitude());
            updates.put("longitude", signalement.getLongitude());
            
            // On met à jour le document Firebase correspondant
            firestore.collection("signalements")
                    .document(signalement.getIdFirebase())
                    .update(updates);
            
            System.out.println("Synchronisation Firebase réussie pour le signalement : " + signalement.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation Firebase : " + e.getMessage());
        }
    }
}
