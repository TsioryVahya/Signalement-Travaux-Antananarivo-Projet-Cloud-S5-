# ✅ Checklist de Configuration des Notifications FCM

## 📋 Avant de commencer

- [ ] Avoir accès à la console Firebase (https://console.firebase.google.com)
- [ ] Avoir les droits d'admin sur le projet Firebase
- [ ] Avoir un compte Firebase actif

---

## 🔧 Configuration Firebase Console

### 1. Activer Cloud Messaging

- [ ] Ouvrir la console Firebase
- [ ] Sélectionner le projet `projet-cloud-s5-routier`
- [ ] Aller dans **Paramètres du projet** (icône engrenage)
- [ ] Onglet **Cloud Messaging**
- [ ] Vérifier que l'API Cloud Messaging est activée

### 2. Générer la clé VAPID

- [ ] Dans **Cloud Messaging**, section **Configuration Web**
- [ ] Cliquer sur **Générer une paire de clés**
- [ ] Copier la **clé publique** (commence par "B...")
- [ ] Sauvegarder cette clé dans un endroit sûr

### 3. Mettre à jour le code avec la clé VAPID

- [ ] Ouvrir `mobile-app/src/services/notificationService.ts`
- [ ] Ligne ~48, remplacer `YOUR_VAPID_KEY` par la clé copiée
  ```typescript
  vapidKey: 'BVotre-Cle-VAPID-Ici...'
  ```
- [ ] Sauvegarder le fichier

### 4. Configurer les règles Firestore

- [ ] Aller dans **Firestore Database**
- [ ] Onglet **Règles**
- [ ] Ajouter/Fusionner les règles depuis `mobile-app/firestore.rules`
- [ ] Publier les règles

### 5. Vérifier serviceAccountKey.json

- [ ] Confirmer que `backend-identity/src/main/resources/serviceAccountKey.json` existe
- [ ] Vérifier que ce fichier contient les bonnes credentials
- [ ] Si absent, télécharger depuis Firebase Console :
  - Paramètres du projet > Comptes de service
  - Générer une nouvelle clé privée
  - Renommer en `serviceAccountKey.json`
  - Placer dans `backend-identity/src/main/resources/`

---

## 🚀 Déploiement

### Backend

- [ ] Recompiler le backend Java
  ```powershell
  cd backend-identity
  mvn clean package
  ```
- [ ] Redémarrer le conteneur Docker
  ```powershell
  docker-compose restart backend
  ```
- [ ] Vérifier les logs pour les erreurs
  ```powershell
  docker-compose logs -f backend
  ```

### Frontend Mobile

- [ ] Installer les dépendances (si nécessaire)
  ```powershell
  cd mobile-app
  npm install
  ```
- [ ] Lancer en mode développement
  ```powershell
  npm run dev
  ```
- [ ] Ou builder pour production
  ```powershell
  npm run build
  ```

---

## 🧪 Tests

### Test 1 : Permission de notification

- [ ] Ouvrir l'application mobile dans le navigateur
- [ ] Se connecter avec un compte utilisateur
- [ ] Vérifier qu'une popup demande la permission de notification
- [ ] Accepter la permission
- [ ] Ouvrir la console DevTools (F12)
- [ ] Vérifier le log : `✅ Permission de notification accordée`

### Test 2 : Sauvegarde du FCM Token

- [ ] Dans la console DevTools, chercher : `📱 FCM Token obtenu:`
- [ ] Copier le token (pour débug)
- [ ] Vérifier le log : `✅ FCM Token sauvegardé dans Firestore`
- [ ] Dans Firebase Console > Firestore Database
- [ ] Collection `users` > Document de l'utilisateur
- [ ] Vérifier la présence du champ `fcmToken`

### Test 3 : Création d'un signalement

- [ ] Dans l'app mobile, créer un nouveau signalement
- [ ] Noter l'ID du signalement créé
- [ ] Vérifier qu'il apparaît dans Firestore > `signalements`
- [ ] Vérifier qu'il apparaît dans PostgreSQL (backend logs)

### Test 4 : Changement de statut depuis le dashboard

- [ ] Se connecter au dashboard web en tant qu'admin
- [ ] Trouver le signalement créé précédemment
- [ ] Changer son statut (ex: de "nouveau" à "en cours")
- [ ] Sauvegarder la modification

### Test 5 : Réception de la notification

- [ ] Retourner à l'application mobile
- [ ] Vérifier la réception d'une notification push
- [ ] Vérifier le badge rouge sur l'onglet Notifications
- [ ] Cliquer sur l'onglet Notifications
- [ ] Vérifier que la notification apparaît dans la liste

### Test 6 : Notification en arrière-plan

- [ ] Avec l'app mobile ouverte, minimiser la fenêtre
- [ ] Depuis le dashboard web, changer le statut d'un signalement
- [ ] Vérifier la réception d'une notification système
- [ ] Cliquer sur la notification
- [ ] Vérifier que l'app s'ouvre sur la page Notifications

### Test 7 : Marquer comme lu

- [ ] Dans l'app mobile, onglet Notifications
- [ ] Glisser une notification vers la gauche
- [ ] Cliquer sur l'icône de validation
- [ ] Vérifier que la notification change d'apparence (fond blanc)
- [ ] Vérifier que le badge se met à jour

### Test 8 : Tout marquer comme lu

- [ ] Avoir plusieurs notifications non lues
- [ ] Cliquer sur "Tout marquer comme lu"
- [ ] Vérifier que toutes les notifications deviennent blanches
- [ ] Vérifier que le badge disparaît

---

## 🔍 Vérifications dans Firebase Console

### Collection `users`

- [ ] Ouvrir Firestore Database
- [ ] Collection `users`
- [ ] Sélectionner un utilisateur
- [ ] Vérifier les champs :
  - `email` : présent
  - `fcmToken` : présent (longue chaîne)
  - `lastTokenUpdate` : timestamp récent

### Collection `notifications`

- [ ] Collection `notifications` existe
- [ ] Des documents sont présents après les tests
- [ ] Chaque document contient :
  - `userId` : ID de l'utilisateur
  - `signalementId` : ID du signalement
  - `titre` : "Changement de statut"
  - `message` : Description
  - `type` : "status_change"
  - `newStatus` : Le nouveau statut
  - `dateCreation` : Timestamp
  - `lu` : boolean

---

## 📊 Monitoring

### Logs Backend

- [ ] Vérifier les logs du backend :
  ```powershell
  docker-compose logs -f backend | Select-String "notification"
  ```
- [ ] Chercher les messages :
  - `📬 Préparation notification pour userId=...`
  - `✅ Notification FCM envoyée avec succès`
  - `✅ Notification enregistrée dans Firestore`

### Logs Frontend

- [ ] Ouvrir DevTools (F12) > Console
- [ ] Chercher les messages :
  - `✅ Permission de notification accordée`
  - `📱 FCM Token obtenu:`
  - `✅ FCM Token sauvegardé dans Firestore`
  - `📬 X notifications chargées (Y non lues)`
  - `📬 Message reçu:` (lors de la réception)

### Firebase Console Messaging

- [ ] Aller dans **Messaging** dans la console
- [ ] Vérifier les statistiques d'envoi (peut prendre du temps)
- [ ] Voir le nombre de notifications envoyées

---

## 🐛 Dépannage

### Problème : Permission refusée

**Solution :**
- [ ] Vérifier les paramètres du navigateur
- [ ] Autoriser les notifications pour localhost
- [ ] Chrome : chrome://settings/content/notifications
- [ ] Redémarrer le navigateur

### Problème : Token non sauvegardé

**Solution :**
- [ ] Vérifier que l'utilisateur est bien connecté
- [ ] Vérifier la connexion à Firebase
- [ ] Vérifier les règles Firestore
- [ ] Regarder les erreurs dans la console

### Problème : Notification non reçue

**Solution :**
- [ ] Vérifier que le token existe dans Firestore
- [ ] Vérifier les logs backend pour les erreurs
- [ ] Vérifier que la clé VAPID est correcte
- [ ] Vérifier que le service worker est enregistré
  - DevTools > Application > Service Workers

### Problème : Service worker non enregistré

**Solution :**
- [ ] Vérifier que `firebase-messaging-sw.js` est dans `public/`
- [ ] Vérifier qu'il est accessible via `/firebase-messaging-sw.js`
- [ ] Nettoyer le cache du navigateur
- [ ] Réenregistrer le service worker

### Problème : Badge ne se met pas à jour

**Solution :**
- [ ] Rafraîchir la page
- [ ] Vérifier les logs Firestore listener
- [ ] Vérifier que le champ `lu` est bien mis à jour dans Firestore

---

## ✅ Validation finale

- [ ] Les notifications sont reçues en temps réel
- [ ] Le badge affiche le bon nombre
- [ ] Les notifications peuvent être marquées comme lues
- [ ] Le clic sur une notification fonctionne
- [ ] Les notifications en arrière-plan fonctionnent
- [ ] Aucune erreur dans les logs backend
- [ ] Aucune erreur dans la console frontend
- [ ] Les règles Firestore sont appliquées
- [ ] Documentation lue et comprise

---

## 📚 Ressources

- **Documentation complète** : `mobile-app/FCM_NOTIFICATIONS.md`
- **Résumé des modifications** : `NOTIFICATIONS_IMPLEMENTATION.md`
- **Script de test** : `test-notifications.ps1`
- **Règles Firestore** : `mobile-app/firestore.rules`

---

## 🎉 Félicitations !

Si tous les points sont cochés, votre système de notifications FCM est opérationnel ! 🚀

Pour toute question ou problème, consultez la documentation ou les logs.
