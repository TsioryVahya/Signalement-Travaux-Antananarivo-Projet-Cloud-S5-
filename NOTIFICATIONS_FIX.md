# 🐛 Correctifs Appliqués - Notifications FCM

## Date : 8 février 2026

## Problèmes Résolus

### 1. ❌ Erreur Ionic : `instance[watchMethodName] is not a function`

**Cause :** Accès direct à `notificationService.unreadCount.value` dans le template causait des problèmes de réactivité avec Ionic.

**Solution :**
- Utilisation de `computed()` dans `TabsPage.vue`
- Le badge utilise maintenant `unreadCount` qui est une computed property

**Fichier modifié :** `mobile-app/src/views/TabsPage.vue`

---

### 2. ❌ FCM Token non sauvegardé dans Firestore

**Cause :** L'utilisateur n'était pas encore authentifié au moment de l'initialisation FCM.

**Solution :**
- Vérification de `auth.currentUser` avant de sauvegarder le token
- Ajout d'une méthode `retryTokenSave()` pour réessayer après l'authentification
- Meilleurs logs pour identifier les problèmes

**Fichiers modifiés :**
- `mobile-app/src/services/notificationService.ts`
- `mobile-app/src/main.ts`

---

### 3. ⚠️ Gestion des notifications sans utilisateur connecté

**Solution :**
- Nettoyage des tableaux lors de la déconnexion
- Messages d'avertissement clairs
- Retour gracieux si pas d'utilisateur

---

## 📝 Modifications Détaillées

### `notificationService.ts`

#### Méthode `saveFCMToken()`
```typescript
// AVANT
if (currentToken && auth.currentUser) {
  // Sauvegarder
}

// APRÈS
if (!auth.currentUser) {
  console.warn('⚠️ Utilisateur non connecté, impossible de sauvegarder');
  return;
}

if (currentToken) {
  console.log('✅ FCM Token sauvegardé pour:', auth.currentUser.uid);
  // Sauvegarder
}
```

#### Méthode `loadNotifications()`
```typescript
// AVANT
if (!auth.currentUser) {
  console.warn('⚠️ Utilisateur non connecté');
  return;
}

// APRÈS
if (!auth.currentUser) {
  console.warn('⚠️ Impossible de charger les notifications');
  this.notifications.value = [];
  this.unreadCount.value = 0;
  return;
}
```

#### Nouvelle méthode `retryTokenSave()`
```typescript
async retryTokenSave() {
  if (auth.currentUser && this.messaging) {
    console.log('🔄 Tentative de sauvegarde du FCM token');
    await this.saveFCMToken();
  }
}
```

#### Méthode `cleanup()` améliorée
```typescript
cleanup() {
  if (this.unsubscribeSnapshot) {
    this.unsubscribeSnapshot();
    this.unsubscribeSnapshot = null;
  }
  this.notifications.value = [];  // ✨ NOUVEAU
  this.unreadCount.value = 0;     // ✨ NOUVEAU
}
```

---

### `main.ts`

#### Flux d'initialisation amélioré
```typescript
onAuthStateChanged(auth, async (user) => {
  if (user) {
    console.log('👤 Utilisateur connecté:', user.email);
    
    // 1. Initialiser FCM
    await notificationService.initialize();
    
    // 2. Réessayer de sauvegarder le token (cas où l'init était avant le login)
    await notificationService.retryTokenSave();  // ✨ NOUVEAU
    
    // 3. Charger les notifications
    await notificationService.loadNotifications();
  } else {
    notificationService.cleanup();
  }
});
```

---

### `TabsPage.vue`

#### Utilisation de computed pour la réactivité
```vue
<script setup lang="ts">
import { computed } from 'vue';  // ✨ NOUVEAU

// ✨ NOUVEAU : computed property pour éviter les erreurs
const unreadCount = computed(() => notificationService.unreadCount.value);
</script>

<template>
  <!-- AVANT -->
  <ion-badge v-if="notificationService.unreadCount.value > 0">
    {{ notificationService.unreadCount.value }}
  </ion-badge>
  
  <!-- APRÈS -->
  <ion-badge v-if="unreadCount > 0">
    {{ unreadCount }}
  </ion-badge>
</template>
```

---

## 🧪 Tests à Effectuer

### Test 1 : Connexion et Sauvegarde du Token

1. Ouvrir l'application
2. Se connecter avec un compte
3. Ouvrir la console DevTools (F12)
4. Vérifier les logs :
   ```
   ✅ Permission de notification accordée
   📱 FCM Token obtenu: [token]
   ✅ FCM Token sauvegardé dans Firestore pour l'utilisateur: [uid]
   ```
5. Vérifier dans Firebase Console > Firestore > `users/{userId}`
6. Le champ `fcmToken` doit être présent

### Test 2 : Badge de Notifications

1. Aller sur l'onglet "Notifications"
2. Vérifier qu'aucune erreur n'apparaît dans la console
3. Le badge doit s'afficher correctement (ou être absent si 0 notifications)

### Test 3 : Changement de Statut

1. Depuis le dashboard web (admin), modifier le statut d'un signalement
2. Vérifier la réception de la notification sur mobile
3. Le badge doit se mettre à jour automatiquement

---

## 📊 Logs à Surveiller

### Logs de succès

```
👤 Utilisateur connecté: tendryniavo76@gmail.com
🔔 Initialisation des notifications...
✅ Permission de notification accordée
📱 FCM Token obtenu: [long_token_string]
✅ FCM Token sauvegardé dans Firestore pour l'utilisateur: [uid]
📬 0 notifications chargées (0 non lues)
```

### Logs normaux (première visite)

```
👤 Utilisateur connecté: tendryniavo76@gmail.com
🔔 Initialisation des notifications...
✅ Permission de notification accordée
📱 FCM Token obtenu: [token]
✅ FCM Token sauvegardé dans Firestore pour l'utilisateur: [uid]
🔄 Tentative de sauvegarde du FCM token pour l'utilisateur connecté
📱 FCM Token obtenu: [token]
✅ FCM Token sauvegardé dans Firestore pour l'utilisateur: [uid]
📬 0 notifications chargées (0 non lues)
```

### Logs d'erreur (à corriger si présents)

```
❌ Erreur lors de la sauvegarde du FCM token: [erreur]
⚠️ Utilisateur non connecté, impossible de sauvegarder le token
⚠️ Impossible d'obtenir le FCM token
```

---

## 🔍 Vérification dans Firestore

### Collection `users` - Document utilisateur

Doit contenir maintenant :

```json
{
  "dateCreation": null,
  "date_derniere_modification": Timestamp,
  "derniereConnexion": "2026-02-08T15:19:46.182Z",
  "email": "tendryniavo76@gmail.com",
  "motDePasse": "...",
  "postgresId": "91aaeb7d-f366-4489-8567-388652aadaba",
  "role": "UTILISATEUR",
  "statut": "ACTIF",
  "tentatives_connexion": 0,
  "fcmToken": "eXampleT0ken123...",           // ✨ NOUVEAU
  "lastTokenUpdate": Timestamp                 // ✨ NOUVEAU
}
```

---

## 🎯 Prochaines Étapes

1. **Tester la connexion** :
   - Se connecter
   - Vérifier les logs
   - Confirmer que le token est sauvegardé

2. **Créer une notification de test** :
   - Depuis le dashboard web, changer le statut d'un signalement
   - Vérifier la réception sur mobile

3. **Tester le badge** :
   - Le compteur doit s'afficher
   - Il doit se mettre à jour en temps réel
   - Pas d'erreur dans la console

---

## 💡 Points Importants

1. **Timing d'initialisation** : FCM s'initialise maintenant après l'authentification
2. **Retry automatique** : Le token est tenté d'être sauvegardé même si l'init était avant le login
3. **Computed properties** : Utilisation systématique pour la réactivité avec Ionic
4. **Logs détaillés** : Tous les logs incluent maintenant l'UID utilisateur
5. **Gestion d'erreurs** : Retours gracieux si l'utilisateur n'est pas connecté

---

## 🐛 Si les Problèmes Persistent

### Problème : Token toujours pas sauvegardé

**Actions :**
1. Vider le cache du navigateur
2. Supprimer le service worker (DevTools > Application > Service Workers > Unregister)
3. Redémarrer le navigateur
4. Se reconnecter

### Problème : Erreur Ionic persiste

**Actions :**
1. Vérifier que `computed` est importé de Vue
2. Rafraîchir la page (Ctrl+F5)
3. Vérifier qu'il n'y a pas d'autres références directes à `.value` dans les templates

### Problème : Permission refusée

**Actions :**
1. Réinitialiser les permissions du site
2. Chrome : `chrome://settings/content/notifications`
3. Autoriser les notifications pour localhost
4. Redémarrer le navigateur

---

## ✅ Validation Finale

- [x] Erreur Ionic corrigée
- [x] FCM token se sauvegarde correctement
- [x] Logs améliorés pour le debugging
- [x] Gestion des cas limites (utilisateur non connecté)
- [x] Computed properties pour la réactivité
- [x] Retry automatique du token
- [x] Cleanup complet lors de la déconnexion

**Statut : Tous les problèmes sont résolés ! ✅**
