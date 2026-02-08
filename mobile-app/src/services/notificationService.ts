import { getMessaging, getToken, onMessage, Messaging } from 'firebase/messaging';
import { doc, setDoc, collection, query, where, orderBy, onSnapshot, Timestamp, updateDoc } from 'firebase/firestore';
import { db, auth } from '../firebase/config';
import { ref, Ref } from 'vue';

export interface Notification {
  id: string;
  userId: string;
  signalementId: string;
  titre: string;
  message: string;
  type: 'status_change' | 'other';
  oldStatus?: string;
  newStatus: string;
  dateCreation: any;
  lu: boolean;
}

class NotificationService {
  private messaging: Messaging | null = null;
  private unsubscribeSnapshot: (() => void) | null = null;
  public notifications: Ref<Notification[]> = ref([]);
  public unreadCount: Ref<number> = ref(0);

  async initialize() {
    try {
      // Initialiser Firebase Messaging
      this.messaging = getMessaging();
      
      // Demander la permission pour les notifications
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        console.log('✅ Permission de notification accordée');
        await this.saveFCMToken();
        this.setupMessageListener();
      } else {
        console.warn('⚠️ Permission de notification refusée');
      }
    } catch (error) {
      console.error('❌ Erreur lors de l\'initialisation des notifications:', error);
    }
  }

  private async saveFCMToken() {
    try {
      if (!this.messaging) {
        console.warn('⚠️ Messaging non initialisé');
        return;
      }

      if (!auth.currentUser) {
        console.warn('⚠️ Utilisateur non connecté, impossible de sauvegarder le token');
        return;
      }
      
      console.log('🔐 Tentative d\'obtention du FCM token...');
      
      const currentToken = await getToken(this.messaging, {
        vapidKey: 'BMjmtEyox-Cq7673l2i68KbFeQQNRF6trQeuN4tfYHvwMBFbPoMtMgUL2FdX4MDd0XLm-PdCQLM-mZunRByy9tI'
      });

      if (currentToken) {
        console.log('📱 FCM Token obtenu:', currentToken);
        console.log('👤 Utilisateur:', auth.currentUser.email);
        console.log('🆔 UID:', auth.currentUser.uid);
        console.log('📋 Token complet à copier:');
        console.log('─'.repeat(80));
        console.log(currentToken);
        console.log('─'.repeat(80));
        
        // Sauvegarder le token dans Firestore
        const userDocRef = doc(db, 'users', auth.currentUser.uid);
        await setDoc(userDocRef, {
          fcmToken: currentToken,
          lastTokenUpdate: Timestamp.now()
        }, { merge: true });
        
        console.log('✅ FCM Token sauvegardé dans Firestore pour l\'utilisateur:', auth.currentUser.uid);
        console.log('✅ Vérifiez Firebase Console > Firestore > users >', auth.currentUser.uid);
      } else {
        console.warn('⚠️ Impossible d\'obtenir le FCM token');
        console.warn('💡 Cela peut être normal en développement local (localhost)');
        console.warn('💡 Les notifications FCM nécessitent HTTPS en production');
      }
    } catch (error: any) {
      console.error('❌ Erreur lors de la sauvegarde du FCM token:', error.code || error.message);
      
      if (error.code === 'messaging/permission-blocked') {
        console.error('🚫 Permission de notification bloquée par l\'utilisateur');
        console.error('💡 Réinitialisez les permissions du site dans les paramètres du navigateur');
      } else if (error.message?.includes('AbortError') || error.message?.includes('push service error')) {
        console.warn('⚠️ Erreur du service push (normal en localhost)');
        console.warn('💡 Les notifications FCM nécessitent:');
        console.warn('   1. HTTPS (ou localhost avec certificat)');
        console.warn('   2. Service worker correctement enregistré');
        console.warn('   3. Configuration VAPID valide');
        console.warn('💡 En développement, vous pouvez ignorer cette erreur');
        console.warn('💡 Les notifications fonctionneront en production avec HTTPS');
      } else {
        console.error('💡 Détails de l\'erreur:', error);
      }
    }
  }

  private setupMessageListener() {
    if (!this.messaging) return;

    // Écouter les messages en premier plan
    onMessage(this.messaging, (payload) => {
      console.log('📬 Message reçu:', payload);
      
      // Afficher une notification locale
      if (payload.notification) {
        new Notification(payload.notification.title || 'Nouvelle notification', {
          body: payload.notification.body,
          icon: '/assets/icon/favicon.png'
        });
      }
      
      // Recharger les notifications
      this.loadNotifications();
    });
  }

  async loadNotifications() {
    try {
      if (!auth.currentUser) {
        console.warn('⚠️ Utilisateur non connecté, impossible de charger les notifications');
        this.notifications.value = [];
        this.unreadCount.value = 0;
        return;
      }

      // Se désabonner de l'ancien listener si existant
      if (this.unsubscribeSnapshot) {
        this.unsubscribeSnapshot();
      }

      console.log('📬 Chargement des notifications pour:', auth.currentUser.uid);

      // Créer une requête pour les notifications de l'utilisateur
      const notificationsRef = collection(db, 'notifications');
      
      // Requête simple sans orderBy pour éviter l'erreur d'index
      const q = query(
        notificationsRef,
        where('userId', '==', auth.currentUser.uid)
      );

      // Écouter les changements en temps réel
      this.unsubscribeSnapshot = onSnapshot(q, (snapshot) => {
        const notifs: Notification[] = [];
        let unreadCount = 0;

        snapshot.forEach((doc) => {
          const data = doc.data();
          const notif: Notification = {
            id: doc.id,
            userId: data.userId,
            signalementId: data.signalementId,
            titre: data.titre,
            message: data.message,
            type: data.type || 'other',
            oldStatus: data.oldStatus,
            newStatus: data.newStatus,
            dateCreation: data.dateCreation,
            lu: data.lu || false
          };
          
          notifs.push(notif);
          if (!notif.lu) {
            unreadCount++;
          }
        });

        // Trier manuellement par date (plus récent en premier)
        notifs.sort((a, b) => {
          const dateA = a.dateCreation?.toDate?.() || new Date(a.dateCreation);
          const dateB = b.dateCreation?.toDate?.() || new Date(b.dateCreation);
          return dateB.getTime() - dateA.getTime();
        });

        this.notifications.value = notifs;
        this.unreadCount.value = unreadCount;
        
        console.log(`📬 ${notifs.length} notifications chargées (${unreadCount} non lues)`);
      }, (error: any) => {
        console.error('❌ Erreur lors de l\'écoute des notifications:', error);
        
        if (error.code === 'failed-precondition' && error.message?.includes('index')) {
          console.warn('⚠️ Index Firestore manquant');
          console.warn('💡 Cliquez sur le lien dans l\'erreur pour créer l\'index automatiquement');
          console.warn('💡 Ou créez l\'index manuellement dans Firebase Console');
          console.warn('💡 En attendant, les notifications fonctionnent sans tri par date');
        }
      });
    } catch (error) {
      console.error('❌ Erreur lors du chargement des notifications:', error);
    }
  }

  async markAsRead(notificationId: string) {
    try {
      const notifRef = doc(db, 'notifications', notificationId);
      await updateDoc(notifRef, {
        lu: true
      });
      console.log(`✅ Notification ${notificationId} marquée comme lue`);
    } catch (error) {
      console.error('❌ Erreur lors du marquage de la notification:', error);
    }
  }

  async markAllAsRead() {
    try {
      const unreadNotifs = this.notifications.value.filter(n => !n.lu);
      
      for (const notif of unreadNotifs) {
        await this.markAsRead(notif.id);
      }
      
      console.log(`✅ ${unreadNotifs.length} notifications marquées comme lues`);
    } catch (error) {
      console.error('❌ Erreur lors du marquage de toutes les notifications:', error);
    }
  }

  cleanup() {
    if (this.unsubscribeSnapshot) {
      this.unsubscribeSnapshot();
      this.unsubscribeSnapshot = null;
    }
    this.notifications.value = [];
    this.unreadCount.value = 0;
  }

  // Méthode pour réessayer la sauvegarde du token si l'utilisateur s'est connecté après l'init
  async retryTokenSave() {
    if (auth.currentUser && this.messaging) {
      console.log('🔄 Tentative de sauvegarde du FCM token pour l\'utilisateur connecté');
      await this.saveFCMToken();
    }
  }
}

export const notificationService = new NotificationService();
