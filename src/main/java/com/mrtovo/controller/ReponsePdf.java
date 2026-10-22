package com.mrtovo.controller;

import com.mrtovo.entity.Cv;
import com.mrtovo.exception.RessourceIntrouvableException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/** Réponse HTTP affichant un fichier PDF dans le navigateur (CV, contrat). */
final class ReponsePdf {

    private ReponsePdf() {
    }

    static ResponseEntity<byte[]> inline(byte[] contenu, String nomFichier) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nomFichier).build().toString())
                .body(contenu);
    }

    /** CV déposé avec la candidature ; 404 si le candidat n'en a pas fourni. */
    static ResponseEntity<byte[]> cv(Optional<Cv> cv) {
        Cv trouve = cv.orElseThrow(() -> new RessourceIntrouvableException("Aucun CV n'a été déposé pour cette candidature"));
        return inline(trouve.getFichier(), trouve.getNomFichier());
    }
}
