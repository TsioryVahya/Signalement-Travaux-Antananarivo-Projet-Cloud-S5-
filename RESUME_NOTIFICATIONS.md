# 📋 Résumé : Système de Notifications Persistantes

## ✅ Ce qui a été implémenté

### 1. Côté Mobile (Mobile-App)

**Fichier modifié** : `src/views/MapView.vue`

**Nouvelle fonction** : `checkUnreadNotifications(userEmail)`
- Récupère les notifications non lues de l'utilisateur depuis Firestore
- Affiche chaque notification dans un toast (5 secondes)
- Marque automatiquement les notifications comme lues
- S'exécute automatiquement à chaque connexion

**Flux** :
```
Utilisateur se connecte 
  → handleLogin() réussit
  → checkUnreadNotifications(userEmail) est appelée
  → Requête Firestore : WHERE userEmail == email AND lue == false
  → Pour chaque notification :
      → Afficher un toast
      → Marquer comme lue (lue: true, dateLecture: timestamp)
```

---

### 2. Côté Backend (Backend-Identity)

**Nouveau service** : `NotificationPersistanceService.java`

**Méthodes principales** :
- `envoyerNotificationAvecPersistance()` : Envoie FCM + Persiste dans Firestore
- `notifierChangementStatut()` : Helper pour changements de statut

**Exemple d'utilisation** : `EXEMPLE_NotificationController.java`
- Endpoint : `PUT /api/signalements/{id}/statut`
- Change le statut ET envoie la notification
- Fonctionne même si l'utilisateur est hors ligne

---

### 3. Structure Firestore

**Collection** : `notifications`

```json
{
  "userEmail": "user@example.com",
  "titre": "✅ Signalement Approuvé",
  "message": "Votre signalement #123 a été approuvé",
  "signalementId": 123,
  "lue": false,
  "dateCreation": "2026-02-06T10:30:00Z",
  "dateLecture": null
}
```

**Index composite requis** :
- `userEmail` (Ascending)
- `lue` (Ascending)
- `dateCreation` (Descending)

Fichier de configuration : `firestore.indexes.json`

---

### 4. Sécurité Firestore

**Fichier** : `firestore.rules`

**Règles pour `notifications`** :
- ✅ Lecture : Uniquement l'utilisateur concerné
- ✅ Update : Uniquement pour marquer comme lue
- ❌ Create : Réservé au backend (Admin SDK)
- ❌ Delete : Interdit

---

## 🧪 Comment tester

### Test Manuel (Recommandé)

1. **Ouvrez le fichier de test** :
   ```
   http://localhost:5173/test-notification-persistante.html
   ```

2. **Remplissez le formulaire** :
   - Email : L'email d'un utilisateur existant
   - Titre : Choisissez un preset ou personnalisez
   - Message : Ex: "Votre signalement #123 a été approuvé"
   - ID Signalement : 123 (optionnel)

3. **Cliquez sur "Créer Notification"**

4. **Sur l'app mobile** :
   - Si vous êtes connecté, déconnectez-vous
   - Reconnectez-vous avec le même email
   - Un toast devrait s'afficher immédiatement !

### Test Automatique (Via Backend)

Lorsque l'admin change le statut d'un signalement dans le backoffice web, une notification est automatiquement créée dans Firestore ET envoyée via FCM.

---

## 📁 Fichiers créés/modifiés

### Nouveaux fichiers :
```
✅ mobile-app/NOTIFICATIONS_PERSISTANTES.md (Documentation complète)
✅ mobile-app/public/test-notification-persistante.html (Page de test)
✅ backend-identity/src/main/java/com/cloud/services/NotificationPersistanceService.java
✅ backend-identity/EXEMPLE_NotificationController.java
✅ firestore.indexes.json (Configuration index)
✅ firestore.rules (Règles de sécurité)
```

### Fichiers modifiés :
```
✅ mobile-app/src/views/MapView.vue
   - Ajout de checkUnreadNotifications()
   - Appel lors de la connexion (ligne ~505)
   - Import de toastController
```

---

## 🚀 Déploiement

### 1. Déployer l'index Firestore

Dans le terminal :
```bash
cd d:/ITU/S5/M.Rojo/Final\ S5/Projet-Cloud-S5-routier
firebase firestore:indexes:deploy
```

### 2. Déployer les règles Firestore

Dans le terminal :
```bash
firebase deploy --only firestore:rules
```

### 3. Redémarrer le backend

```bash
cd backend-identity
mvn clean install
mvn spring-boot:run
```

### 4. Redémarrer l'app mobile

```bash
cd mobile-app
npm run dev
```

---

## 🎯 Avantages de ce système

| Avantage | Description |
|----------|-------------|
| **Aucune notification perdue** | Même hors ligne, la notification est sauvegardée |
| **Expérience utilisateur améliorée** | L'utilisateur voit toutes ses notifications à la connexion |
| **Audit complet** | Historique de toutes les notifications envoyées |
| **Double livraison** | FCM (temps réel) + Firestore (persistance) |
| **Facile à déboguer** | Toutes les notifications visibles dans Firebase Console |

---

## 🔍 Dépannage

### "The query requires an index"

**Solution** : Déployez l'index avec `firebase firestore:indexes:deploy`

### Les notifications ne s'affichent pas

**Vérifications** :
1. Console navigateur → Y a-t-il des logs `🔔 Vérification des notifications` ?
2. Firebase Console → Y a-t-il des documents avec `lue: false` ?
3. L'email dans Firestore correspond-il à celui de l'utilisateur connecté ?

### Permission denied

**Solution** : Déployez les règles Firestore avec `firebase deploy --only firestore:rules`

---

## 📚 Documentation complète

Pour plus de détails, consultez :
- **NOTIFICATIONS_PERSISTANTES.md** : Guide complet d'implémentation
- **test-notification-persistante.html** : Page de test interactive

---

**Date** : 06 Février 2026  
**Auteur** : GitHub Copilot
