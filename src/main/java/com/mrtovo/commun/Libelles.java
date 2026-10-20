package com.mrtovo.commun;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Petits outils sur les libellés des tables de référence.
 */
public final class Libelles {

	private Libelles() {
	}

	/** « en entretien » → « en-entretien », « clôturée » → « cloturee » (utilisable en classe CSS). */
	public static String code(String libelle) {
		if (libelle == null) {
			return "";
		}
		String sansAccent = Normalizer.normalize(libelle, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return sansAccent.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-");
	}

}
