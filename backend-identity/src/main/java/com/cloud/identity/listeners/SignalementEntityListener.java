package com.cloud.identity.listeners;
import com.cloud.identity.config.SpringContextHelper;
import com.cloud.identity.entities.Signalement;
import com.cloud.identity.service.FirestoreSyncService;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PostPersist;

public class SignalementEntityListener {

    @PostUpdate
    @PostPersist
    public void onPostUpdate(Signalement signalement) {
        // Récupérer le service via le helper car le listener n'est pas géré par Spring
        FirestoreSyncService syncService = SpringContextHelper.getBean(FirestoreSyncService.class);
        syncService.syncSignalementToFirebase(signalement);
    }
}
