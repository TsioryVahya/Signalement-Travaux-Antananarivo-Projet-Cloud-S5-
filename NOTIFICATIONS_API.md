# 🔔 API et Interactions des Notifications FCM

## Vue d'ensemble

Ce document décrit les interactions entre les différents composants du système de notifications.

---

## 🔄 Diagramme de Flux

```
┌─────────────────┐
│  Admin Web App  │
└────────┬────────┘
         │ 1. Modifie statut
         ↓
┌─────────────────────────────┐
│  Backend (Spring Boot)      │
│  - SignalementService       │
│  - FcmNotificationService   │
└────────┬────────────────────┘
         │ 2. Détecte changement
         │ 3. Récupère FCM token
         ↓
┌─────────────────────────────┐
│  Firebase (Firestore)       │
│  - Collection: users        │
│  - Champ: fcmToken          │
└────────┬────────────────────┘
         │ 4. Envoie notification
         ↓
┌─────────────────────────────┐
│  Firebase Cloud Messaging   │
└────────┬────────────────────┘
         │ 5. Push notification
         ↓
┌─────────────────────────────┐
│  Mobile App                 │
│  - notificationService      │
│  - NotificationsView        │
└─────────────────────────────┘
         │ 6. Crée enregistrement
         ↓
┌─────────────────────────────┐
│  Firebase (Firestore)       │
│  - Collection: notifications│
└─────────────────────────────┘
```

---

## 📡 Backend (Java)

### Service: FcmNotificationService

#### Méthode: `notifyStatusChange`

**Description:** Point d'entrée principal pour envoyer une notification de changement de statut

**Signature:**
```java
public void notifyStatusChange(
    String signalementId,
    String oldStatus,
    String newStatus,
    String userId
)
```

**Paramètres:**
- `signalementId` : ID Firebase du signalement
- `oldStatus` : Ancien statut (peut être null)
- `newStatus` : Nouveau statut
- `userId` : ID Firebase de l'utilisateur

**Flux:**
1. Log de l'événement
2. Appel à `sendStatusChangeNotification()`

---

#### Méthode: `sendStatusChangeNotification`

**Description:** Récupère le token FCM et envoie la notification

**Signature:**
```java
public void sendStatusChangeNotification(
    String userId,
    String signalementId,
    String oldStatus,
    String newStatus
)
```

**Flux:**
1. Récupère le document utilisateur dans Firestore
2. Extrait le champ `fcmToken`
3. Prépare le titre et le message
4. Crée un Map avec les données supplémentaires
5. Appelle `sendNotification()`
6. Appelle `createNotificationRecord()`

**Exemple de données:**
```java
Map<String, String> data = {
    "type": "status_change",
    "signalementId": "abc123",
    "oldStatus": "nouveau",
    "newStatus": "en cours"
}
```

---

#### Méthode: `sendNotification`

**Description:** Envoie la notification FCM via Firebase Admin SDK

**Signature:**
```java
public void sendNotification(
    String fcmToken,
    String titre,
    String corps,
    Map<String, String> data
)
```

**Utilise:**
```java
Message message = Message.builder()
    .setToken(fcmToken)
    .setNotification(notification)
    .putAllData(data)
    .build();

String response = FirebaseMessaging.getInstance().send(message);
```

**Logs:**
- ✅ `Notification FCM envoyée avec succès: [response]`
- ❌ `Erreur lors de l'envoi de la notification FCM`

---

#### Méthode: `createNotificationRecord`

**Description:** Crée un enregistrement dans Firestore pour l'historique

**Signature:**
```java
private void createNotificationRecord(
    String userId,
    String signalementId,
    String oldStatus,
    String newStatus
)
```

**Document créé dans `notifications`:**
```json
{
  "userId": "abc123",
  "signalementId": "def456",
  "titre": "Changement de statut",
  "message": "Votre signalement est maintenant \"en cours\"",
  "type": "status_change",
  "oldStatus": "nouveau",
  "newStatus": "en cours",
  "dateCreation": Timestamp.now(),
  "lu": false
}
```

---

### Service: SignalementService

#### Méthode: `modifierSignalement`

**Modifications:**

**Avant:**
```java
public void modifierSignalement(...) {
    // ... code existant ...
    s.setStatut(statut);
    signalementRepository.save(s);
    // Fin
}
```

**Après:**
```java
public void modifierSignalement(...) {
    // Récupérer l'ancien statut
    String oldStatut = s.getStatut() != null ? s.getStatut().getNom() : null;
    
    // ... code existant ...
    s.setStatut(statut);
    signalementRepository.save(s);
    
    // Envoyer notification si changement
    String newStatut = statut.getNom();
    if (oldStatut != null && !oldStatut.equals(newStatut)) {
        sendStatusChangeNotification(s, oldStatut, newStatut);
    }
}
```

---

#### Méthode: `validerSignalement`

**Modifications:**

**Avant:**
```java
public void validerSignalement(UUID signalementId) {
    // ... change statut ...
    signalementRepository.save(s);
    eventPublisher.publishEvent(new SignalementSavedEvent(this, s));
}
```

**Après:**
```java
public void validerSignalement(UUID signalementId) {
    String oldStatut = s.getStatut() != null ? s.getStatut().getNom() : null;
    
    // ... change statut ...
    signalementRepository.save(s);
    eventPublisher.publishEvent(new SignalementSavedEvent(this, s));
    
    // Notification
    sendStatusChangeNotification(s, oldStatut, "en cours");
}
```

---

#### Nouvelle Méthode: `sendStatusChangeNotification`

**Description:** Coordonne l'envoi de notification pour un signalement

**Signature:**
```java
private void sendStatusChangeNotification(
    Signalement signalement,
    String oldStatus,
    String newStatus
)
```

**Flux:**
1. Vérifie que le signalement a un utilisateur et un idFirebase
2. Récupère l'email de l'utilisateur
3. Récupère l'ID Firebase de l'utilisateur via `getUserFirebaseId()`
4. Appelle `fcmNotificationService.notifyStatusChange()`

---

#### Nouvelle Méthode: `getUserFirebaseId`

**Description:** Récupère l'ID Firebase d'un utilisateur depuis son email

**Signature:**
```java
private String getUserFirebaseId(String email)
```

**Requête Firestore:**
```java
db.collection("users")
  .whereEqualTo("email", email)
  .limit(1)
  .get()
```

**Retourne:**
- L'ID du document si trouvé
- `null` si non trouvé ou erreur

---

## 📱 Frontend (Mobile)

### Service: notificationService

#### Méthode: `initialize`

**Description:** Initialise Firebase Messaging et demande les permissions

**Flux:**
1. Initialise `getMessaging()`
2. Demande permission via `Notification.requestPermission()`
3. Si accordée : appelle `saveFCMToken()` et `setupMessageListener()`

**Logs:**
- ✅ `Permission de notification accordée`
- ⚠️ `Permission de notification refusée`

---

#### Méthode: `saveFCMToken`

**Description:** Obtient et sauvegarde le token FCM dans Firestore

**Flux:**
1. Appelle `getToken(messaging, { vapidKey })`
2. Sauvegarde dans `users/{userId}` :
```typescript
{
  fcmToken: token,
  lastTokenUpdate: Timestamp.now()
}
```

**Logs:**
- 📱 `FCM Token obtenu: [token]`
- ✅ `FCM Token sauvegardé dans Firestore`

---

#### Méthode: `setupMessageListener`

**Description:** Écoute les messages en premier plan

**Utilise:**
```typescript
onMessage(messaging, (payload) => {
  // Affiche une notification locale
  new Notification(payload.notification.title, {
    body: payload.notification.body,
    icon: '/assets/icon/favicon.png'
  });
  
  // Recharge les notifications
  loadNotifications();
});
```

---

#### Méthode: `loadNotifications`

**Description:** Charge les notifications depuis Firestore en temps réel

**Requête:**
```typescript
const q = query(
  collection(db, 'notifications'),
  where('userId', '==', auth.currentUser.uid),
  orderBy('dateCreation', 'desc')
);

onSnapshot(q, (snapshot) => {
  // Traite les documents
});
```

**État mis à jour:**
- `notifications.value` : Array de Notification
- `unreadCount.value` : Nombre de notifications non lues

**Logs:**
- 📬 `X notifications chargées (Y non lues)`

---

#### Méthode: `markAsRead`

**Description:** Marque une notification comme lue

**Signature:**
```typescript
async markAsRead(notificationId: string)
```

**Opération:**
```typescript
updateDoc(doc(db, 'notifications', notificationId), {
  lu: true
});
```

---

#### Méthode: `markAllAsRead`

**Description:** Marque toutes les notifications non lues comme lues

**Flux:**
1. Filtre les notifications non lues
2. Pour chaque notification, appelle `markAsRead()`

---

### Vue: NotificationsView

#### Structure

**Template:**
- Header avec titre et bouton "Tout marquer comme lu"
- État vide si aucune notification
- Liste des notifications avec :
  - Item glissant (swipe)
  - Badge "Nouveau" si non lu
  - Icône colorée selon le statut
  - Titre, message, date, chip de statut

**Script:**
```typescript
onMounted(() => {
  notificationService.loadNotifications();
});

const handleNotificationClick = async (notif) => {
  if (!notif.lu) {
    await notificationService.markAsRead(notif.id);
  }
  router.push('/tabs/my-reports');
};
```

---

## 🔥 Firebase Firestore

### Collection: `users`

**Document ID:** UID Firebase de l'utilisateur

**Champs:**
```json
{
  "email": "user@example.com",
  "fcmToken": "eXampleT0ken...",
  "lastTokenUpdate": Timestamp
}
```

**Indexation:** Aucun index nécessaire

---

### Collection: `notifications`

**Document ID:** Auto-généré par Firestore

**Champs:**
```json
{
  "userId": "abc123",
  "signalementId": "def456",
  "titre": "Changement de statut",
  "message": "Votre signalement est maintenant \"en cours\"",
  "type": "status_change",
  "oldStatus": "nouveau",
  "newStatus": "en cours",
  "dateCreation": Timestamp,
  "lu": false
}
```

**Index composé requis:**
- `userId` (Ascending) + `dateCreation` (Descending)

**Règles de sécurité:**
```javascript
match /notifications/{notificationId} {
  allow read: if request.auth.uid == resource.data.userId;
  allow update: if request.auth.uid == resource.data.userId &&
                   request.resource.data.diff(resource.data).affectedKeys()
                     .hasOnly(['lu']);
}
```

---

## 🔐 Sécurité

### Backend

- **Firebase Admin SDK** : Utilise `serviceAccountKey.json`
- **Bypass des règles Firestore** : Le backend a tous les droits
- **Validation des données** : Vérifie l'existence de l'utilisateur

### Frontend

- **Authentification requise** : Doit être connecté pour recevoir des notifications
- **Règles Firestore strictes** : Ne peut lire que ses propres notifications
- **Token sécurisé** : Le token FCM n'est pas exposé côté client (stocké dans Firestore)

---

## 📊 Métriques et Monitoring

### Logs à surveiller

**Backend:**
- Nombre de notifications envoyées
- Erreurs d'envoi FCM
- Tokens invalides

**Frontend:**
- Permissions refusées
- Erreurs de sauvegarde de token
- Notifications non reçues

### Firebase Console

**Cloud Messaging:**
- Statistiques d'envoi
- Taux de succès/échec
- Tokens actifs

**Firestore:**
- Taille de la collection `notifications`
- Requêtes par seconde
- Coût des opérations

---

## 🎯 Optimisations Possibles

### Backend

1. **Cache des tokens FCM**
   - Éviter de requêter Firestore à chaque envoi
   - Invalider le cache après X minutes

2. **Envoi par batch**
   - Grouper plusieurs notifications
   - Utiliser `sendMulticast()` de FCM

3. **Retry avec backoff**
   - Réessayer en cas d'échec
   - Exponentiel backoff

### Frontend

1. **Lazy loading des notifications**
   - Charger par pages
   - Infinite scroll

2. **Cache local**
   - Utiliser IndexedDB
   - Réduire les requêtes Firestore

3. **Debounce des mises à jour**
   - Grouper les marquages "lu"
   - Réduire les écritures

---

## 🧪 Tests

### Tests Unitaires Backend

```java
@Test
public void testNotifyStatusChange() {
    // Arrange
    String userId = "test-user";
    String signalementId = "test-signal";
    
    // Act
    fcmNotificationService.notifyStatusChange(
        signalementId, "nouveau", "en cours", userId
    );
    
    // Assert
    // Vérifier que la notification a été créée dans Firestore
}
```

### Tests Frontend

```typescript
describe('notificationService', () => {
  it('should save FCM token', async () => {
    await notificationService.saveFCMToken();
    // Vérifier que le token existe dans Firestore
  });
  
  it('should load notifications', async () => {
    await notificationService.loadNotifications();
    expect(notificationService.notifications.value.length).toBeGreaterThan(0);
  });
});
```

---

## 📚 Références

- [Firebase Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Ionic Vue Documentation](https://ionicframework.com/docs/vue/overview)
