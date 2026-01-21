package com.cloud.identity.controller;

import com.cloud.identity.entities.Signalement;
import com.cloud.identity.entities.StatutsSignalement;
import com.cloud.identity.service.SignalementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/signalements")
public class SignalementController {

    @Autowired
    private SignalementService signalementService;

    @Autowired
    private com.cloud.identity.repository.SignalementsDetailRepository detailsRepository;

    @GetMapping
    public String listeSignalements(Model model) {
        model.addAttribute("signalements", signalementService.getAllSignalements());
        return "listeSignalements";
    }

    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable UUID id, Model model) throws Exception {
        Signalement signalement = signalementService.getSignalementById(id)
                .orElseThrow(() -> new Exception("Signalement non trouvé"));
        List<StatutsSignalement> statuts = signalementService.getAllStatuts();
        
        // Récupérer les détails pour les afficher
        com.cloud.identity.entities.SignalementsDetail details = detailsRepository.findBySignalement(signalement)
                .orElse(new com.cloud.identity.entities.SignalementsDetail());

        model.addAttribute("signalement", signalement);
        model.addAttribute("details", details);
        model.addAttribute("statuts", statuts);
        return "modifierSignalement";
    }

    @GetMapping("/nouveau")
    public String afficherFormulaireCreation() {
        return "nouveauSignalement";
    }

    @PostMapping("/nouveau")
    public String creerSignalement(@RequestParam Double latitude,
                                   @RequestParam Double longitude,
                                   @RequestParam String description,
                                   @RequestParam String email,
                                   @RequestParam(required = false) Double surfaceM2,
                                   @RequestParam(required = false) BigDecimal budget,
                                   @RequestParam(required = false) String entrepriseConcerne,
                                   @RequestParam(required = false) String photoUrl) {
        signalementService.creerSignalement(latitude, longitude, description, email, surfaceM2, budget, entrepriseConcerne, photoUrl);
        return "redirect:/signalements";
    }

    @PostMapping("/modifier")
    public String modifierSignalement(@RequestParam UUID id,
                                      @RequestParam Double latitude,
                                      @RequestParam Double longitude,
                                      @RequestParam Integer statutId,
                                      @RequestParam String description,
                                      @RequestParam(required = false) Double surfaceM2,
                                      @RequestParam(required = false) BigDecimal budget,
                                      @RequestParam(required = false) String entrepriseConcerne,
                                      @RequestParam(required = false) String photoUrl) throws Exception {
        signalementService.modifierSignalement(id, latitude, longitude, statutId, description, surfaceM2, budget, entrepriseConcerne, photoUrl);
        return "redirect:/signalements";
    }
}
