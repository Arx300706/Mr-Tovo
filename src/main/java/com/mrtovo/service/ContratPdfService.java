package com.mrtovo.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mrtovo.entity.Compte;
import com.mrtovo.entity.Contrat;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Met en forme le contrat de travail en PDF, à partir des données enregistrées. */
@Service
public class ContratPdfService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Séparateurs fixes : la police du PDF ne sait pas afficher les espaces insécables de la locale. */
    private static final DecimalFormat FORMAT_MONTANT = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));

    public byte[] generer(Contrat c) {
        Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font texte = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font petit = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 60, 60, 60, 60);
        try {
            PdfWriter.getInstance(document, sortie);
            document.open();

            Paragraph entete = new Paragraph("CONTRAT DE TRAVAIL", titre);
            entete.setAlignment(Element.ALIGN_CENTER);
            document.add(entete);
            Paragraph reference = new Paragraph("Contrat n°" + c.getId() + " — " + c.getTypeContrat().getLibelle(), texte);
            reference.setAlignment(Element.ALIGN_CENTER);
            reference.setSpacingAfter(20);
            document.add(reference);

            document.add(new Paragraph("Entre les soussignés", section));
            Compte compte = c.getCandidature().getCompte();
            document.add(new Paragraph("L'employeur : Mr Tovo.", texte));
            document.add(new Paragraph("Le salarié : " + compte.getNomComplet()
                    + (compte.getAdresse() != null ? ", demeurant à " + compte.getAdresse() : "")
                    + ", e-mail : " + compte.getEmail() + ".", texte));
            document.add(espace());

            document.add(new Paragraph("Article 1 — Emploi", section));
            document.add(new Paragraph("Le salarié est engagé au poste de « " + c.getPoste().getLibelle()
                    + " » au sein du département " + c.getCandidature().getOffre().getDepartement().getLibelle()
                    + ", service " + c.getCandidature().getOffre().getService().getLibelle() + ".", texte));
            document.add(espace());

            document.add(new Paragraph("Article 2 — Durée", section));
            String fin = c.getDateFin() != null ? "jusqu'au " + c.getDateFin().format(FORMAT_DATE) : "durée indéterminée";
            document.add(new Paragraph("Le contrat prend effet le " + c.getDateDebut().format(FORMAT_DATE)
                    + " et est conclu " + fin + ".", texte));
            document.add(espace());

            document.add(new Paragraph("Article 3 — Rémunération", section));
            document.add(new Paragraph("Le salaire mensuel est fixé à " + montant(c.getSalaire()) + " Ar.", texte));
            document.add(espace());

            document.add(new Paragraph("Article 4 — Signatures", section));
            document.add(new Paragraph("Le présent contrat est établi en deux exemplaires et prend effet après signature des deux parties.", texte));
            document.add(espace());

            PdfPTable signatures = new PdfPTable(2);
            signatures.setWidthPercentage(100);
            signatures.addCell(celluleSignature("Pour l'employeur (Mr Tovo)", c.getDateSignatureRh()));
            signatures.addCell(celluleSignature("Le salarié", c.getDateSignatureCandidat()));
            document.add(signatures);

            document.add(espace());
            document.add(new Paragraph("Statut du contrat : " + c.getStatut().getLibelle()
                    + ". Document généré le " + LocalDate.now().format(FORMAT_DATE) + ".", petit));

            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Génération du contrat en PDF impossible", e);
        }
        return sortie.toByteArray();
    }

    /** Montant en Ariary avec espaces comme séparateurs de milliers (ex. 2 000 000). */
    static String montant(BigDecimal valeur) {
        return FORMAT_MONTANT.format(valeur).replace(',', ' ');
    }

    private static Paragraph espace() {
        return new Paragraph(" ");
    }

    private static PdfPCell celluleSignature(String libelle, LocalDateTime signeLe) {
        Font texte = FontFactory.getFont(FontFactory.HELVETICA, 10);
        String mention = signeLe != null ? "Signé le " + signeLe.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Signature :";
        PdfPCell cellule = new PdfPCell(new Paragraph(libelle + "\n\n" + mention + "\n\n\n", texte));
        cellule.setBorder(PdfPCell.NO_BORDER);
        cellule.setPadding(8);
        return cellule;
    }
}
