package com.cloud.identity.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore() throws IOException {
        System.out.println("🔥 Initialisation Firebase via Classpath...");

        try {
            // Charger le fichier de credentials UNE SEULE FOIS
            System.out.println("🔍 Recherche du fichier serviceAccountKey.json dans le classpath...");
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            
            if (!resource.exists()) {
                System.err.println("❌ CRITIQUE : Fichier serviceAccountKey.json introuvable !");
                throw new IOException("Fichier serviceAccountKey.json introuvable dans le classpath !");
            }

            InputStream serviceAccount = resource.getInputStream();
            System.out.println("✅ Fichier serviceAccountKey.json trouvé.");

            // Créer les credentials
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            // Initialiser Firebase Admin SDK UNE SEULE FOIS
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ FirebaseApp initialisé avec succès.");
            } else {
                System.out.println("ℹ️ FirebaseApp déjà initialisé.");
            }

            // Utiliser FirestoreClient qui RÉUTILISE les credentials de FirebaseApp
            // Cela évite de créer une nouvelle instance de credentials qui nécessiterait un
            // token
            Firestore firestore = FirestoreClient.getFirestore();
            System.out.println("✅ Firestore client obtenu avec succès.");
            return firestore;

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'initialisation Firebase : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
