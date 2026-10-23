package com.mrtovo.security;

import com.mrtovo.entity.Compte;
import com.mrtovo.entity.Departement;
import com.mrtovo.entity.Employe;
import com.mrtovo.entity.Poste;
import com.mrtovo.repository.CompteRepository;
import com.mrtovo.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Un seul formulaire de connexion : un identifiant contenant « @ » est l'e-mail d'un candidat,
 * sinon c'est le matricule d'un employé.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {

    private final EmployeRepository employeRepository;
    private final CompteRepository compteRepository;

    @Override
    @Transactional(readOnly = true)
    public UtilisateurConnecte loadUserByUsername(String identifiant) {
        String valeur = identifiant == null ? "" : identifiant.trim();
        if (valeur.contains("@")) {
            Compte compte = compteRepository.findByEmailIgnoreCase(valeur)
                    .orElseThrow(() -> new UsernameNotFoundException("Compte inconnu"));
            return pourCandidat(compte);
        }
        Employe employe = employeRepository.findByMatricule(valeur.toUpperCase())
                .orElseThrow(() -> new UsernameNotFoundException("Matricule inconnu"));
        return pourEmploye(employe);
    }

    public UtilisateurConnecte pourCandidat(Compte compte) {
        return new UtilisateurConnecte(UtilisateurConnecte.Type.CANDIDAT, compte.getId(), compte.getEmail(),
                compte.getMotDePasse(), compte.getNomComplet(), null, null, null, Set.of(Roles.CANDIDAT));
    }

    private UtilisateurConnecte pourEmploye(Employe employe) {
        Set<String> roles = new HashSet<>();
        roles.add(Roles.EMPLOYE);
        Poste poste = employe.getPosteActuel();
        Departement departement = poste == null ? null : poste.getDepartement();
        if (poste != null) {
            if (poste.isEstDirecteur()) {
                // Le directeur peut tout voir et tout faire
                roles.addAll(Set.of(Roles.DIRECTEUR, Roles.MANAGER, Roles.RH, Roles.FINANCE));
            }
            if (poste.isEstManager()) {
                roles.add(Roles.MANAGER);
            }
            if (departement != null && Departement.CODE_RH.equals(departement.getCode())) {
                roles.add(Roles.RH);
            }
            if (departement != null && Departement.CODE_FINANCE.equals(departement.getCode())) {
                roles.add(Roles.FINANCE);
            }
        }
        return new UtilisateurConnecte(UtilisateurConnecte.Type.EMPLOYE, employe.getId(), employe.getMatricule(),
                employe.getMotDePasse(), employe.getNomComplet(),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getLibelle(),
                poste == null ? null : poste.getLibelle(), roles);
    }
}
