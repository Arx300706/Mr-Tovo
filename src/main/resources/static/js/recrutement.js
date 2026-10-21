// Recrutement (FR1-05) : contrôle du CV dans le navigateur et confirmation des actions sensibles.
// Le serveur refait toujours les mêmes contrôles (CandidatureService.erreurCv).
document.addEventListener("DOMContentLoaded", () => {
	document.querySelectorAll("input[data-controle-cv]").forEach((champ) => {
		const formulaire = champ.form;
		const message = formulaire.querySelector("[data-erreur-cv][hidden], [data-erreur-cv]");
		const tailleMax = Number(champ.dataset.tailleMax || 5 * 1024 * 1024);
		const extensions = (champ.dataset.extensions || "pdf,doc,docx").split(",");

		const verifier = () => {
			const fichier = champ.files[0];
			let erreur = "";
			if (!fichier) {
				erreur = "Le CV est obligatoire";
			} else {
				const extension = fichier.name.includes(".") ? fichier.name.split(".").pop().toLowerCase() : "";
				if (!extensions.includes(extension)) {
					erreur = "Le CV doit être un fichier PDF ou Word (.pdf, .doc, .docx)";
				} else if (fichier.size > tailleMax) {
					const taille = (fichier.size / 1024 / 1024).toFixed(1).replace(".", ",");
					erreur = `Le CV dépasse 5 Mo (${taille} Mo)`;
				}
			}
			champ.setCustomValidity(erreur);
			if (message) {
				message.textContent = erreur;
				message.hidden = erreur === "";
			}
			return erreur === "";
		};

		champ.addEventListener("change", verifier);
		formulaire.addEventListener("submit", (evenement) => {
			if (!verifier()) {
				evenement.preventDefault();
				champ.focus();
			}
		});
	});

	document.querySelectorAll("form[data-confirmer]").forEach((formulaire) => {
		formulaire.addEventListener("submit", (evenement) => {
			if (!window.confirm(formulaire.dataset.confirmer)) {
				evenement.preventDefault();
			}
		});
	});
});
