package com.cloud.identity.listeners;

import com.cloud.identity.dto.SignalementDTO;
import com.cloud.identity.service.SignalementService;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirebaseSignalementListener {

    @Autowired
    private SignalementService signalementService;

    @Autowired
    private Firestore firestore;

    @PostConstruct
    public void init() {
        firestore.collection("signalements")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Erreur Firestore: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    System.out.println("Nouveau signalement détecté dans Firestore: " + dc.getDocument().getId());
                                    SignalementDTO dto = dc.getDocument().toObject(SignalementDTO.class);
                                    dto.setIdFirebase(dc.getDocument().getId());
                                    signalementService.enregistrerSignalement(dto);
                                } catch (Exception ex) {
                                    System.err.println("Erreur lors du traitement d'un document Firestore : " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }
}
