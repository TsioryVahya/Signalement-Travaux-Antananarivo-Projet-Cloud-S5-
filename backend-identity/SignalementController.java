package com.cloud.controllers;

import com.cloud.services.NotificationPersistanceService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Exemple d'utilisation du service de notifications persistantes
 * 
 * Ce contrôleur montre comment envoyer une notification lors du changement
 * de statut d'un signalement.
 */
@RestController
@RequestMapping("/api/signalements")
@CrossOrigin(origins = "*")
public class SignalementController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private NotificationPersistanceService notificationService;

    /**
     * Endpoint pour changer le statut d'un signalement
     * 
     * Exemple d'appel :
     * PUT /api/signalements/123/statut
     * Body: { "statut": "APPROUVE" }
     */
    @PutMapping("/{signalementId}/statut")
    public ResponseEntity<?> changerStatut(
            @PathVariable Integer signalementId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String nouveauStatut = body.get("statut");
            
            // 1. Récupérer le signalement pour obtenir l'email de l'auteur
            DocumentReference signalementRef = firestore
                    .collection("signalements")
                    .document(signalementId.toString());
            
            Map<String, Object> signalement = signalementRef.get().get().getData();
            
            if (signalement == null) {
                return ResponseEntity.notFound().build();
            }
            
            String userEmail = (String) signalement.get("email");
            
            // 2. Mettre à jour le statut dans Firestore
            signalementRef.update("statut", nouveauStatut).get();
            
            // 3. Récupérer le token FCM de l'utilisateur depuis la collection "users"
            String fcmToken = null;
            try {
                Map<String, Object> userData = firestore
                        .collection("users")
                        .document(userEmail)
                        .get()
                        .get()
                        .getData();
                
                if (userData != null) {
                    fcmToken = (String) userData.get("fcmToken");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de récupérer le token FCM: " + e.getMessage());
            }
            
            // 4. Envoyer la notification (FCM + Firestore persistance)
            notificationService.notifierChangementStatut(
                    userEmail,
                    fcmToken,
                    signalementId,
                    nouveauStatut
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Statut mis à jour et notification envoyée",
                    "signalementId", signalementId,
                    "nouveauStatut", nouveauStatut,
                    "notificationPersistee", true,
                    "notificationFcmEnvoyee", fcmToken != null
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors du changement de statut",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint pour envoyer une notification personnalisée
     * 
     * Exemple d'appel :
     * POST /api/signalements/notification
     * Body: {
     *   "userEmail": "user@example.com",
     *   "titre": "Test",
     *   "message": "Ceci est un test",
     *   "signalementId": 123
     * }
     */
    @PostMapping("/notification")
    public ResponseEntity<?> envoyerNotificationPersonnalisee(
            @RequestBody Map<String, Object> body
    ) {
        try {
            String userEmail = (String) body.get("userEmail");
            String titre = (String) body.get("titre");
            String message = (String) body.get("message");
            Integer signalementId = body.get("signalementId") != null 
                    ? Integer.parseInt(body.get("signalementId").toString()) 
                    : null;
            
            // Récupérer le token FCM
            String fcmToken = null;
            try {
                Map<String, Object> userData = firestore
                        .collection("users")
                        .document(userEmail)
                        .get()
                        .get()
                        .getData();
                
                if (userData != null) {
                    fcmToken = (String) userData.get("fcmToken");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de récupérer le token FCM: " + e.getMessage());
            }
            
            // Envoyer la notification
            notificationService.envoyerNotificationAvecPersistance(
                    userEmail,
                    fcmToken,
                    titre,
                    message,
                    signalementId
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Notification envoyée",
                    "notificationPersistee", true,
                    "notificationFcmEnvoyee", fcmToken != null
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de l'envoi de la notification",
                    "details", e.getMessage()
            ));
        }
    }
}
