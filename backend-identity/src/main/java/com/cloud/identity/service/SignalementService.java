package com.cloud.identity.service;


import com.cloud.identity.dto.SignalementDTO;
import com.cloud.identity.entities.Signalement;
import com.cloud.identity.entities.SignalementsDetail;
import com.cloud.identity.entities.StatutsSignalement;
import com.cloud.identity.entities.Utilisateur;
import com.cloud.identity.repository.SignalementRepository;
import com.cloud.identity.repository.SignalementsDetailRepository;
import com.cloud.identity.repository.StatutsSignalementRepository;
import com.cloud.identity.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private SignalementsDetailRepository detailsRepository;

    @Autowired
    private StatutsSignalementRepository statutRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private FirestoreSyncService firestoreSyncService;

    public Map<String, Integer> synchroniserDonnees() {
        return firestoreSyncService.syncFromFirestoreToPostgres();
    }

    @Transactional(readOnly = true)
    public List<SignalementDTO> getAllSignalements() {
        return signalementRepository.findAllWithDetails().stream().map(s -> {
            SignalementDTO dto = new SignalementDTO();
            dto.setPostgresId(s.getId().toString());
            dto.setLatitude(s.getLatitude());
            dto.setLongitude(s.getLongitude());
            dto.setIdFirebase(s.getIdFirebase());
            dto.setDateSignalement(s.getDateSignalement());
            
            // On renvoie le NOM du statut pour le dashboard web
            if (s.getStatut() != null) {
                dto.setStatut(s.getStatut().getNom());
            } else {
                dto.setStatut("nouveau");
            }

            // Récupérer les détails via la relation fetchée ou fallback repository
            SignalementsDetail d = s.getDetails();
            if (d == null) {
                // Fallback si la relation n'est pas chargée (peut arriver selon l'état de l'entité)
                d = detailsRepository.findBySignalement(s).orElse(null);
            }

            if (d != null) {
                System.out.println("🔍 Signalement " + s.getId() + " - Description trouvée: " + d.getDescription());
                dto.setDescription(d.getDescription());
                dto.setSurfaceM2(d.getSurfaceM2());
                dto.setBudget(d.getBudget());
                dto.setEntrepriseConcerne(d.getEntrepriseConcerne());
                dto.setPhotoUrl(d.getPhotoUrl());
            } else {
                System.out.println("⚠️ Aucun détail trouvé pour le signalement : " + s.getId());
            }

            // Récupérer l'utilisateur si disponible
            if (s.getUtilisateur() != null) {
                SignalementDTO.UtilisateurDTO userDto = new SignalementDTO.UtilisateurDTO();
                userDto.setEmail(s.getUtilisateur().getEmail());
                dto.setUtilisateur(userDto);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<Signalement> getSignalementById(UUID id) {
        return signalementRepository.findById(id);
    }

    public List<StatutsSignalement> getAllStatuts() {
        return statutRepository.findAll();
    }

    @Transactional
    public void creerSignalement(Double latitude, Double longitude, String description, String email,
                                 Double surfaceM2, BigDecimal budget, String entrepriseConcerne, String photoUrl) {
        System.out.println("📝 Création d'un signalement pour : " + email);
        Signalement s = new Signalement();
        s.setLatitude(latitude);
        s.setLongitude(longitude);
        s.setDateSignalement(java.time.Instant.now());
        
        // Statut par défaut
        StatutsSignalement statut = statutRepository.findByNom("nouveau")
                .orElseGet(() -> {
                    System.out.println("ℹ️ Statut 'nouveau' non trouvé, création...");
                    StatutsSignalement newStatut = new StatutsSignalement();
                    newStatut.setNom("nouveau");
                    return statutRepository.save(newStatut);
                });
        s.setStatut(statut);

        // Utilisateur
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseGet(() -> {
                    System.out.println("ℹ️ Utilisateur non trouvé, création : " + email);
                    Utilisateur newUser = new Utilisateur();
                    newUser.setEmail(email);
                    newUser.setMotDePasse("default_password");
                    return utilisateurRepository.save(newUser);
                });
        s.setUtilisateur(utilisateur);

        s = signalementRepository.save(s);
        System.out.println("✅ Signalement sauvegardé dans Postgres, ID : " + s.getId());

        // Détails
        SignalementsDetail details = new SignalementsDetail();
        details.setSignalement(s);
        details.setDescription(description);
        details.setSurfaceM2(surfaceM2);
        details.setBudget(budget);
        details.setEntrepriseConcerne(entrepriseConcerne);
        details.setPhotoUrl(photoUrl);
        s.setDetails(details);
        detailsRepository.save(details);
        System.out.println("✅ Détails sauvegardés.");

        // Synchronisation vers Firestore
        System.out.println("🔄 Tentative de synchronisation vers Firestore...");
        String firebaseId = firestoreSyncService.createSignalementInFirestore(s, details);
        if (firebaseId != null) {
            s.setIdFirebase(firebaseId);
            signalementRepository.save(s);
            System.out.println("🚀 Synchronisation réussie ! ID Firebase : " + firebaseId);
        } else {
            System.err.println("❌ ÉCHEC de la synchronisation Firestore.");
        }
    }

    @Transactional
    public void modifierSignalement(UUID id, Double latitude, Double longitude, Integer statutId,
                                    String description, Double surfaceM2, BigDecimal budget,
                                    String entrepriseConcerne, String photoUrl) throws Exception {
        Signalement s = signalementRepository.findById(id)
                .orElseThrow(() -> new Exception("Signalement non trouvé"));
        
        StatutsSignalement statut = statutRepository.findById(statutId)
                .orElseThrow(() -> new Exception("Statut non trouvé"));

        s.setLatitude(latitude);
        s.setLongitude(longitude);
        s.setStatut(statut);

        signalementRepository.save(s);

        // Mettre à jour les détails
        SignalementsDetail details = detailsRepository.findBySignalement(s)
                .orElseGet(() -> {
                    SignalementsDetail newDetails = new SignalementsDetail();
                    newDetails.setSignalement(s);
                    return newDetails;
                });
        
        details.setDescription(description);
        details.setSurfaceM2(surfaceM2);
        details.setBudget(budget);
        details.setEntrepriseConcerne(entrepriseConcerne);
        details.setPhotoUrl(photoUrl);
        
        s.setDetails(details);
        detailsRepository.save(details);

        // Synchronisation Firebase
        firestoreSyncService.syncSignalementToFirebase(s);
    }

    @Transactional
    public void supprimerSignalement(UUID id) throws Exception {
        if (!signalementRepository.existsById(id)) {
            throw new Exception("Signalement non trouvé");
        }
        signalementRepository.deleteById(id);
    }

    @Transactional
    public void enregistrerSignalement(SignalementDTO dto) {
        // Vérifier si le signalement existe déjà par son ID Firebase
        if (signalementRepository.findByIdFirebase(dto.getIdFirebase()).isPresent()) {
            System.out.println("Signalement déjà existant dans Postgres : " + dto.getIdFirebase());
            return;
        }

        System.out.println("Enregistrement d'un nouveau signalement depuis Firebase : " + dto.getIdFirebase());

        // 1️⃣ Enregistrer signalement de base
        Signalement s = new Signalement();
        s.setLatitude(dto.getLatitude());
        s.setLongitude(dto.getLongitude());
        s.setIdFirebase(dto.getIdFirebase());
        
        if (dto.getDateSignalement() != null) {
            try {
                Object dateObj = dto.getDateSignalement();
                if (dateObj instanceof com.google.cloud.Timestamp) {
                    s.setDateSignalement(((com.google.cloud.Timestamp) dateObj).toSqlTimestamp().toInstant());
                } else {
                    s.setDateSignalement(Instant.parse(dateObj.toString()));
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing de la date : " + dto.getDateSignalement() + ". Utilisation de la date actuelle.");
                s.setDateSignalement(Instant.now());
            }
        } else {
            s.setDateSignalement(Instant.now());
        }

        // Gérer le statut
        String nomStatut = (dto.getStatut() != null) ? dto.getStatut() : "nouveau";
        StatutsSignalement statut = statutRepository.findByNom(nomStatut)
                .orElseGet(() -> {
                    StatutsSignalement newStatut = new StatutsSignalement();
                    newStatut.setNom(nomStatut);
                    return statutRepository.save(newStatut);
                });
        s.setStatut(statut);

        // Gérer l'utilisateur
        String email = null;
        if (dto.getUtilisateur() != null && dto.getUtilisateur().getEmail() != null) {
            email = dto.getUtilisateur().getEmail();
        } else if (dto.getEmail() != null) {
            email = dto.getEmail();
        }

        if (email != null) {
            final String finalEmail = email;
            Utilisateur utilisateur = utilisateurRepository.findByEmail(finalEmail)
                    .orElseGet(() -> {
                        Utilisateur newUser = new Utilisateur();
                        newUser.setEmail(finalEmail);
                        newUser.setMotDePasse("default_password");
                        return utilisateurRepository.save(newUser);
                    });
            s.setUtilisateur(utilisateur);
        }

        s = signalementRepository.save(s);

        // 2️⃣ Enregistrer détails
        SignalementsDetail details = new SignalementsDetail();
        details.setSignalement(s);
        details.setDescription(dto.getDescription());
        
        // Gestion de la surface (peut être Long ou Double dans Firestore)
        if (dto.getSurfaceM2() != null) {
            try {
                details.setSurfaceM2(Double.valueOf(dto.getSurfaceM2().toString()));
            } catch (Exception e) {
                System.err.println("Erreur conversion surfaceM2 : " + dto.getSurfaceM2());
            }
        }
        
        // Gestion du budget (peut être String ou Number)
        if (dto.getBudget() != null) {
            try {
                details.setBudget(new BigDecimal(dto.getBudget().toString()));
            } catch (Exception e) {
                System.err.println("Erreur conversion budget : " + dto.getBudget());
            }
        }
        
        details.setEntrepriseConcerne(dto.getEntrepriseConcerne());
        details.setPhotoUrl(dto.getPhotoUrl());

        s.setDetails(details);
        detailsRepository.save(details);
        System.out.println("Signalement " + dto.getIdFirebase() + " enregistré avec succès dans Postgres.");
    }

    // Valider un signalement depuis l'admin
    @Transactional
    public void validerSignalement(UUID signalementId) throws Exception {
        Signalement s = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new Exception("Signalement non trouvé"));
        
        StatutsSignalement statutEnCours = statutRepository.findByNom("en cours")
                .orElseGet(() -> {
                    StatutsSignalement newStatut = new StatutsSignalement();
                    newStatut.setNom("en cours");
                    return statutRepository.save(newStatut);
                });
        
        s.setStatut(statutEnCours);
        signalementRepository.save(s);
        // La mise à jour Firebase est maintenant automatique via SignalementEntityListener
    }
}
