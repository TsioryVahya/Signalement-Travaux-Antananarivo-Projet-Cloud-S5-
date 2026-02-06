# 🔔 Notifications Persistantes - Guide d'Implémentation

## 📋 Vue d'ensemble

Ce système permet aux utilisateurs de **recevoir les notifications manquées** lorsqu'ils se reconnectent à l'application, même s'ils étaient hors ligne au moment de l'envoi.

---

## 🗄️ Structure Firestore

### Collection `notifications`

Chaque notification envoyée doit être **enregistrée dans Firestore** avec la structure suivante :

```javascript
{
  "userEmail": "user@example.com",        // Email de l'utilisateur destinataire
  "titre": "Statut modifié",              // Titre de la notification
  "message": "Votre signalement #123 a été approuvé", // Message détaillé
  "signalementId": 123,                   // ID du signalement concerné (optionnel)
  "lue": false,                           // Statut de lecture
  "dateCreation": "2026-02-06T10:30:00Z", // Date de création (ISO string)
  "dateLecture": null                     // Date de lecture (null si non lue)
}
```

---

## 🔧 Implémentation Backend (Java Spring Boot)

### 1. Créer un document dans Firestore lors de l'envoi d'une notification

Lorsque l'admin change le statut d'un signalement, le backend doit :

1. **Envoyer la notification FCM** (comme actuellement)
2. **Créer un document dans Firestore** pour persistance

```java
import com.google.cloud.firestore.Firestore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    
    private final Firestore firestore;
    
    public void envoyerNotificationAvecPersistance(
        String userEmail, 
        String titre, 
        String message, 
        Integer signalementId
    ) throws Exception {
        
        // 1. Envoyer la notification FCM (code existant)
        envoyerNotificationFCM(userEmail, titre, message);
        
        // 2. Persister dans Firestore
        Map<String, Object> notification = new HashMap<>();
        notification.put("userEmail", userEmail);
        notification.put("titre", titre);
        notification.put("message", message);
        notification.put("signalementId", signalementId);
        notification.put("lue", false);
        notification.put("dateCreation", Instant.now().toString());
        notification.put("dateLecture", null);
        
        firestore.collection("notifications").add(notification);
        
        System.out.println("✅ Notification persistée pour: " + userEmail);
    }
}
```

### 2. Créer un index composite Firestore

Pour que les requêtes fonctionnent, créez un **index composite** dans Firebase Console :

1. Allez dans **Firestore Database** > **Indexes**
2. Créez un index avec :
   - Collection : `notifications`
   - Champs :
     - `userEmail` : Ascending
     - `lue` : Ascending
     - `dateCreation` : Descending

Ou utilisez cette commande Firebase CLI :

```bash
firebase firestore:indexes:deploy
```

Avec ce fichier `firestore.indexes.json` :

```json
{
  "indexes": [
    {
      "collectionGroup": "notifications",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userEmail", "order": "ASCENDING" },
        { "fieldPath": "lue", "order": "ASCENDING" },
        { "fieldPath": "dateCreation", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## 📱 Fonctionnement Côté Mobile

### Flux de Connexion

```
1. Utilisateur se connecte
   ↓
2. handleLogin() s'exécute
   ↓
3. Authentification réussie
   ↓
4. checkUnreadNotifications() est appelée
   ↓
5. Requête Firestore : notifications non lues
   ↓
6. Affichage des toasts (5 secondes chacun)
   ↓
7. Marquage des notifications comme lues
```

### Code Mobile (Déjà Implémenté)

La fonction `checkUnreadNotifications()` dans `MapView.vue` :

```typescript
const checkUnreadNotifications = async (userEmail: string) => {
  // Récupère les notifications où lue === false
  const q = query(
    collection(db, 'notifications'),
    where('userEmail', '==', userEmail),
    where('lue', '==', false),
    orderBy('dateCreation', 'desc')
  );
  
  const snapshot = await getDocs(q);
  
  // Affiche chaque notification avec un toast
  for (const docSnap of snapshot.docs) {
    const notif = docSnap.data();
    
    const toast = await toastController.create({
      message: `${notif.titre}: ${notif.message}`,
      duration: 5000,
      position: 'top',
      color: 'primary'
    });
    
    await toast.present();
    
    // Marque comme lue
    await updateDoc(doc(db, 'notifications', docSnap.id), {
      lue: true,
      dateLecture: new Date().toISOString()
    });
  }
};
```

---

## 🧪 Test du Système

### Scénario de Test

1. **Déconnectez l'utilisateur** de l'application mobile
2. **Connectez-vous au backoffice web**
3. **Changez le statut d'un signalement** de cet utilisateur
4. **Vérifiez dans Firestore** qu'un document a été créé dans `notifications` avec `lue: false`
5. **Reconnectez l'utilisateur** sur l'app mobile
6. **Vérifiez** qu'un toast s'affiche avec le titre et le message
7. **Vérifiez dans Firestore** que `lue` est passé à `true` et `dateLecture` est rempli

---

## 📊 Exemples de Notifications

### Signalement Approuvé

```javascript
{
  "userEmail": "agent@example.com",
  "titre": "✅ Signalement Approuvé",
  "message": "Votre signalement #456 (Nid de poule) a été approuvé",
  "signalementId": 456,
  "lue": false,
  "dateCreation": "2026-02-06T14:20:00Z",
  "dateLecture": null
}
```

### Signalement Rejeté

```javascript
{
  "userEmail": "agent@example.com",
  "titre": "❌ Signalement Rejeté",
  "message": "Votre signalement #789 a été rejeté. Raison: Doublon",
  "signalementId": 789,
  "lue": false,
  "dateCreation": "2026-02-06T15:45:00Z",
  "dateLecture": null
}
```

### Signalement Résolu

```javascript
{
  "userEmail": "agent@example.com",
  "titre": "🎉 Signalement Résolu",
  "message": "Votre signalement #321 (Feu cassé) a été résolu",
  "signalementId": 321,
  "lue": false,
  "dateCreation": "2026-02-06T16:10:00Z",
  "dateLecture": null
}
```

---

## 🔒 Sécurité Firestore (Rules)

Ajoutez ces règles de sécurité dans Firebase Console :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Notifications : lecture/écriture uniquement pour l'utilisateur concerné
    match /notifications/{notificationId} {
      allow read: if request.auth != null && 
                     resource.data.userEmail == request.auth.token.email;
      
      allow update: if request.auth != null && 
                       resource.data.userEmail == request.auth.token.email &&
                       request.resource.data.diff(resource.data).affectedKeys()
                         .hasOnly(['lue', 'dateLecture']);
      
      // Seul le backend peut créer des notifications
      allow create: if false;
      allow delete: if false;
    }
  }
}
```

---

## 🚀 Avantages de ce Système

✅ **Notifications persistantes** : Aucune notification perdue  
✅ **Expérience utilisateur améliorée** : L'utilisateur voit toutes ses notifications au moment de se connecter  
✅ **Audit complet** : Historique de toutes les notifications envoyées  
✅ **Temps réel + Différé** : Fonctionne en ligne (FCM) et hors ligne (Firestore)  
✅ **Facile à déboguer** : Toutes les notifications sont visibles dans Firestore  

---

## 📝 Maintenance

### Nettoyer les anciennes notifications (optionnel)

Créez une Cloud Function pour supprimer les notifications lues de plus de 30 jours :

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.cleanOldNotifications = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    const db = admin.firestore();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    
    const snapshot = await db.collection('notifications')
      .where('lue', '==', true)
      .where('dateLecture', '<', thirtyDaysAgo.toISOString())
      .get();
    
    const batch = db.batch();
    snapshot.docs.forEach(doc => batch.delete(doc.ref));
    
    await batch.commit();
    console.log(`Supprimé ${snapshot.size} notifications obsolètes`);
  });
```

---

## 🆘 Dépannage

### Les notifications ne s'affichent pas

1. **Vérifiez l'index Firestore** : Firebase Console > Firestore > Indexes
2. **Vérifiez les logs** : Console du navigateur > `🔔 Vérification des notifications`
3. **Vérifiez Firestore** : Y a-t-il des documents avec `lue: false` et le bon `userEmail` ?
4. **Vérifiez les permissions** : L'utilisateur est-il authentifié Firebase Auth ?

### L'index composite n'existe pas

Erreur : `The query requires an index`

**Solution** : Cliquez sur le lien dans l'erreur ou créez manuellement l'index comme décrit ci-dessus.

---

## 📚 Documentation Connexe

- [FIREBASE_SYNC_FIX.md](../backend-identity/FIREBASE_SYNC_FIX.md) - Synchronisation Firebase
- [ACTIVATE_FCM_API.md](./ACTIVATE_FCM_API.md) - Activation de l'API FCM
- [Firebase Firestore Queries](https://firebase.google.com/docs/firestore/query-data/queries)
- [Ionic Toast Controller](https://ionicframework.com/docs/api/toast)

---

**Auteur** : GitHub Copilot  
**Date** : 06 Février 2026  
**Version** : 1.0
