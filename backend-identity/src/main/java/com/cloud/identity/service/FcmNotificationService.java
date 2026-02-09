package com.cloud.identity.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FcmNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FcmNotificationService.class);

    /**
     * Envoie une notification FCM à un utilisateur
     * 
     * @param fcmToken Token FCM de l'utilisateur
     * @param titre    Titre de la notification
     * @param corps    Corps de la notification
     * @param data     Données supplémentaires
     */
    public void sendNotification(String fcmToken, String titre, String corps, Map<String, String> data) {
        try {
            if (fcmToken == null || fcmToken.isEmpty()) {
                logger.warn("⚠️ Token FCM vide, impossible d'envoyer la notification");
                return;
            }

            // Construire la notification
            Notification notification = Notification.builder()
                    .setTitle(titre)
                    .setBody(corps)
                    .build();

            // Construire le message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            // Ajouter les données si présentes
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // Envoyer le message
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("✅ Notification FCM envoyée avec succès: {}", response);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de la notification FCM", e);
        }
    }

    /**
     * Envoie une notification de changement de statut
     * 
     * @param userId        ID de l'utilisateur Firebase (UID)
     * @param userEmail     Email de l'utilisateur (utilisé comme fallback pour
     *                      trouver l'UID si nécessaire)
     * @param signalementId ID du signalement
     * @param oldStatus     Ancien statut
     * @param newStatus     Nouveau statut
     */
    public void sendStatusChangeNotification(String userId, String userEmail, String signalementId,
            String oldStatus, String newStatus) {
        try {
            logger.info("📬 Préparation notification pour userId={}, email={}, signalement={}, {} -> {}",
                    userId, userEmail, signalementId, oldStatus, newStatus);

            Firestore db = FirestoreClient.getFirestore();
            String finalUserId = userId;
            String fcmToken = null;

            // 1. Tenter de récupérer par UID
            if (finalUserId != null && !finalUserId.isEmpty()) {
                try {
                    DocumentReference userDoc = db.collection("users").document(finalUserId);
                    Map<String, Object> userData = userDoc.get().get().getData();
                    if (userData != null && userData.containsKey("fcmToken")) {
                        fcmToken = (String) userData.get("fcmToken");
                        logger.info("✅ FCM token trouvé via UID: {}", finalUserId);
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Impossible de trouver l'utilisateur par UID: {}", finalUserId);
                }
            }

            // 2. Si non trouvé par UID, tenter par Email
            if ((fcmToken == null || fcmToken.isEmpty()) && userEmail != null && !userEmail.isEmpty()) {
                logger.info("🔍 Recherche du FCM token via email: {}", userEmail);
                try {
                    com.google.cloud.firestore.QuerySnapshot query = db.collection("users")
                            .whereEqualTo("email", userEmail)
                            .limit(1)
                            .get()
                            .get();

                    if (!query.isEmpty()) {
                        com.google.cloud.firestore.QueryDocumentSnapshot doc = query.getDocuments().get(0);
                        finalUserId = doc.getId(); // On met à jour l'UID pour l'enregistrement Firestore
                        fcmToken = doc.getString("fcmToken");
                        logger.info("✅ FCM token trouvé via Email. UID mis à jour: {}", finalUserId);
                    }
                } catch (Exception e) {
                    logger.error("❌ Erreur lors de la recherche par email", e);
                }
            }

            // 3. Toujours créer l'enregistrement dans la collection notifications Firestore
            // On utilise le finalUserId (soit l'UID original, soit celui trouvé par email)
            createNotificationRecord(finalUserId, signalementId, oldStatus, newStatus);

            // 4. Tenter d'envoyer la notification push FCM
            if (fcmToken == null || fcmToken.isEmpty()) {
                logger.warn("⚠️ Aucun FCM token trouvé pour l'utilisateur {}. Push non envoyé.", finalUserId);
                return;
            }

            // Préparer le titre et le message
            String titre = "Changement de statut";
            String corps = String.format("Votre signalement est maintenant \"%s\"", newStatus);

            // Préparer les données supplémentaires
            Map<String, String> data = new HashMap<>();
            data.put("type", "status_change");
            data.put("signalementId", signalementId);
            data.put("oldStatus", oldStatus != null ? oldStatus : "");
            data.put("newStatus", newStatus);

            // Envoyer la notification FCM
            sendNotification(fcmToken, titre, corps, data);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de la notification de changement de statut", e);
        }
    }

    /**
     * Crée un enregistrement de notification dans Firestore
     */
    private void createNotificationRecord(String userId, String signalementId,
            String oldStatus, String newStatus) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", userId);
            notificationData.put("signalementId", signalementId);
            notificationData.put("titre", "Changement de statut");
            notificationData.put("message", String.format("Votre signalement est maintenant \"%s\"", newStatus));
            notificationData.put("type", "status_change");
            notificationData.put("oldStatus", oldStatus != null ? oldStatus : "");
            notificationData.put("newStatus", newStatus);
            notificationData.put("dateCreation", com.google.cloud.Timestamp.now());
            notificationData.put("lu", false);

            // Ajouter à la collection notifications
            db.collection("notifications").add(notificationData).get();

            logger.info("✅ Notification enregistrée dans Firestore pour userId={}", userId);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'enregistrement de la notification dans Firestore", e);
        }
    }

    /**
     * Envoie une notification à tous les utilisateurs ayant signalé un problème
     */
    public void notifyStatusChange(String signalementId, String oldStatus, String newStatus, String userId,
            String userEmail) {
        logger.info("🔔 Notification de changement de statut: {} -> {} pour signalement {} (user: {}, email: {})",
                oldStatus, newStatus, signalementId, userId, userEmail);

        if ((userId != null && !userId.isEmpty()) || (userEmail != null && !userEmail.isEmpty())) {
            sendStatusChangeNotification(userId, userEmail, signalementId, oldStatus, newStatus);
        } else {
            logger.warn("⚠️ UserId et Email manquants, impossible d'envoyer la notification");
        }
    }
}
