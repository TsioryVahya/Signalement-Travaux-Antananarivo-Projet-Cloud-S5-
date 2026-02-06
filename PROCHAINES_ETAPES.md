# 🚀 Prochaines Étapes - Notifications Persistantes

## ✅ Implémentation Terminée

Le système de notifications persistantes est maintenant **complètement implémenté** côté mobile. Voici ce qui fonctionne déjà :

1. ✅ **Fonction de vérification** : `checkUnreadNotifications()` dans MapView.vue
2. ✅ **Appel automatique** : Se déclenche à chaque connexion utilisateur
3. ✅ **Affichage toast** : Chaque notification s'affiche pendant 5 secondes
4. ✅ **Marquage lu** : Les notifications sont automatiquement marquées comme lues

---

## 🔧 Actions Requises (Backend)

### 1. Déployer l'index Firestore (OBLIGATOIRE)

Sans cet index, les requêtes échoueront.

```bash
cd d:/ITU/S5/M.Rojo/Final\ S5/Projet-Cloud-S5-routier
firebase firestore:indexes:deploy
```

**Ou manuellement** dans Firebase Console :
1. Allez dans **Firestore Database** > **Indexes**
2. Cliquez **Create Index**
3. Collection : `notifications`
4. Ajoutez les champs :
   - `userEmail` : Ascending
   - `lue` : Ascending
   - `dateCreation` : Descending
5. Créez l'index (peut prendre 5-10 minutes)

---

### 2. Déployer les règles de sécurité (OBLIGATOIRE)

```bash
firebase deploy --only firestore:rules
```

Cela applique les règles définies dans `firestore.rules` qui :
- Autorisent les utilisateurs à lire leurs propres notifications
- Autorisent les utilisateurs à marquer leurs notifications comme lues
- Interdisent la création/suppression côté client (réservé au backend)

---

### 3. Intégrer le service dans le backend

**Option A** : Copier le fichier créé

```bash
# Le fichier est déjà créé ici :
backend-identity/src/main/java/com/cloud/services/NotificationPersistanceService.java
```

Copiez-le dans votre package `com.cloud.services` si nécessaire.

**Option B** : Créer manuellement

Consultez le fichier `EXEMPLE_NotificationController.java` pour voir comment l'utiliser.

---

### 4. Modifier votre contrôleur de signalements

Dans votre contrôleur qui gère le changement de statut :

**Avant** :
```java
@PutMapping("/{id}/statut")
public ResponseEntity<?> changerStatut(@PathVariable Integer id, ...) {
    // Changer le statut dans la BD
    signalementService.updateStatut(id, nouveauStatut);
    
    // Envoyer notification FCM
    fcmService.sendNotification(userFcmToken, "Statut modifié", message);
    
    return ResponseEntity.ok().build();
}
```

**Après** :
```java
@Autowired
private NotificationPersistanceService notificationService;

@PutMapping("/{id}/statut")
public ResponseEntity<?> changerStatut(@PathVariable Integer id, ...) {
    // Changer le statut dans la BD
    signalementService.updateStatut(id, nouveauStatut);
    
    // Envoyer notification FCM + Persister dans Firestore
    notificationService.notifierChangementStatut(
        userEmail,
        userFcmToken,  // Peut être null si l'utilisateur est hors ligne
        id,
        nouveauStatut
    );
    
    return ResponseEntity.ok().build();
}
```

---

## 🧪 Tester le Système

### Test 1 : Avec la page de test (Rapide)

1. **Démarrez l'app mobile** :
   ```bash
   cd mobile-app
   npm run dev
   ```

2. **Ouvrez la page de test** :
   ```
   http://localhost:5173/test-notification-persistante.html
   ```

3. **Créez une notification** :
   - Email : L'email d'un utilisateur de test (ex: `test@example.com`)
   - Titre : `✅ Signalement Approuvé`
   - Message : `Votre signalement #123 a été approuvé`
   - Cliquez **Créer Notification**

4. **Sur l'app mobile** :
   - Déconnectez-vous (si connecté)
   - Reconnectez-vous avec l'email `test@example.com`
   - **Résultat attendu** : Un toast s'affiche immédiatement avec le message !

---

### Test 2 : Avec le backend (Réaliste)

1. **Assurez-vous que le backend utilise le nouveau service**

2. **Sur le backoffice web** :
   - Connectez-vous en tant qu'admin
   - Trouvez un signalement d'un utilisateur mobile
   - Changez son statut (ex: EN_ATTENTE → APPROUVE)

3. **Vérifiez dans Firestore Console** :
   - Allez dans **Firestore Database**
   - Collection `notifications`
   - Un nouveau document devrait apparaître avec `lue: false`

4. **Sur l'app mobile** :
   - L'utilisateur se déconnecte (ou était déjà déconnecté)
   - Il se reconnecte
   - **Résultat attendu** : Toast avec "Signalement Approuvé"

---

## 📊 Vérifier que ça fonctionne

### Dans la Console Navigateur (F12)

Quand l'utilisateur se connecte, vous devriez voir :

```
🔔 Vérification des notifications non lues pour: user@example.com
📬 2 notification(s) non lue(s) trouvée(s)
📩 Affichage notification: {titre: "...", message: "..."}
✅ Toutes les notifications ont été affichées et marquées comme lues
```

### Dans Firestore Console

Après la connexion, les documents doivent avoir :
```json
{
  "lue": true,
  "dateLecture": "2026-02-06T15:30:00Z"
}
```

---

## 🎯 Scénarios Couverts

| Scénario | Comportement |
|----------|--------------|
| **Utilisateur connecté** | Reçoit la notification FCM en temps réel + notification persistée dans Firestore |
| **Utilisateur hors ligne** | Notification persistée dans Firestore uniquement |
| **Utilisateur se reconnecte** | Toutes les notifications non lues s'affichent en toasts |
| **Plusieurs notifications** | Affichées une par une avec un délai de 500ms entre chaque |

---

## 🔍 Dépannage

### "The query requires an index"

**Cause** : L'index composite n'a pas été déployé.

**Solution** :
```bash
firebase firestore:indexes:deploy
```

Attendez 5-10 minutes que l'index soit créé.

---

### Aucune notification ne s'affiche

**Vérifications** :

1. **Console navigateur** : Y a-t-il des erreurs ?
2. **Firestore Console** : Y a-t-il des documents avec `lue: false` et le bon `userEmail` ?
3. **Email** : L'email dans Firestore correspond-il exactement à celui de l'utilisateur ?
4. **Index** : L'index composite est-il en statut "Enabled" dans Firebase Console ?

---

### Permission denied

**Cause** : Les règles Firestore n'ont pas été déployées.

**Solution** :
```bash
firebase deploy --only firestore:rules
```

---

### Les toasts ne s'affichent pas

**Cause possible** : L'import Ionic est incorrect.

**Vérification** : Dans MapView.vue, vous devez avoir :
```typescript
import { toastController } from '@ionic/vue';
```

---

## 📚 Documentation

Consultez les fichiers suivants pour plus de détails :

| Fichier | Description |
|---------|-------------|
| `RESUME_NOTIFICATIONS.md` | Vue d'ensemble rapide |
| `NOTIFICATIONS_PERSISTANTES.md` | Guide complet d'implémentation |
| `NotificationPersistanceService.java` | Code du service backend |
| `EXEMPLE_NotificationController.java` | Exemple d'utilisation |
| `test-notification-persistante.html` | Page de test interactive |
| `firestore.indexes.json` | Configuration des index |
| `firestore.rules` | Règles de sécurité |

---

## ✅ Checklist de Déploiement

Avant de considérer le système comme terminé, cochez :

- [ ] L'index Firestore est déployé et en statut "Enabled"
- [ ] Les règles Firestore sont déployées
- [ ] Le service `NotificationPersistanceService` est intégré dans le backend
- [ ] Le contrôleur de signalements utilise le nouveau service
- [ ] Test manuel avec `test-notification-persistante.html` réussi
- [ ] Test end-to-end (backoffice → notification → app mobile) réussi
- [ ] Les logs montrent "✅ Toutes les notifications ont été affichées"

---

**Prêt à tester ?** Commencez par le Test 1 avec la page de test ! 🚀

---

**Date** : 06 Février 2026  
**Auteur** : GitHub Copilot
