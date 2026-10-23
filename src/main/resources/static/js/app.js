document.addEventListener("DOMContentLoaded", () => {
	// Menu latéral repliable sur téléphone (fragments/entete.html + fragments/menu.html)
	const bouton = document.querySelector("[data-bascule-menu]");
	const menu = document.getElementById("menu-lateral");
	if (bouton && menu) {
		bouton.addEventListener("click", () => menu.classList.toggle("ouvert"));
	}
});

// Mr Tovo — petits comportements d'interface (sans dépendance)

document.addEventListener("DOMContentLoaded", () => {
    // Affiche un bloc seulement si une condition est vraie : data-afficher-si="nomDuChamp=valeur1|valeur2"
    const blocs = document.querySelectorAll("[data-afficher-si]");
    const majBlocs = () => blocs.forEach(bloc => {
        const [nom, valeurs] = bloc.dataset.afficherSi.split("=");
        const champs = document.querySelectorAll(`[name="${nom}"]`);
        let valeur = null;
        champs.forEach(c => {
            if (c.type === "radio" || c.type === "checkbox") {
                if (c.checked) valeur = c.value;
            } else {
                valeur = c.options ? c.options[c.selectedIndex]?.dataset.code ?? c.value : c.value;
            }
        });
        bloc.classList.toggle("cache", !valeurs.split("|").includes(valeur ?? ""));
    });
    document.querySelectorAll("input, select").forEach(c => c.addEventListener("change", majBlocs));
    majBlocs();

    // Demande une confirmation avant une action irréversible : data-confirmer="message"
    document.querySelectorAll("form[data-confirmer]").forEach(f =>
        f.addEventListener("submit", e => {
            if (!window.confirm(f.dataset.confirmer)) e.preventDefault();
        }));

    // Onglets de la page de connexion
    document.querySelectorAll("[data-onglet]").forEach(bouton =>
        bouton.addEventListener("click", () => {
            document.querySelectorAll("[data-onglet]").forEach(b => b.classList.toggle("actif", b === bouton));
            document.querySelectorAll("[data-panneau]").forEach(p =>
                p.classList.toggle("cache", p.dataset.panneau !== bouton.dataset.onglet));
        }));

    // Éditeur de CV : toutes les rubriques acceptent plusieurs lignes.
    const definitions = {
        experience: [
            ["titre", "Entreprise", "text"], ["sousTitre", "Poste", "text"],
            ["dateDebut", "Date de début", "month"], ["dateFin", "Date de fin", "month"],
            ["description", "Missions et résultats", "textarea"]
        ],
        formation: [
            ["titre", "Établissement", "text"], ["sousTitre", "Diplôme / formation", "text"],
            ["dateDebut", "Début", "month"], ["dateFin", "Fin", "month"],
            ["description", "Description", "textarea"]
        ],
        competence: [["titre", "Compétence", "text"], ["niveau", "Niveau", "text"]],
        qualite: [["titre", "Qualité", "text"], ["description", "Exemple concret", "text"]],
        langue: [["titre", "Langue", "text"], ["niveau", "Niveau", "text"]],
        projet: [["titre", "Nom du projet", "text"], ["sousTitre", "Rôle / technologies", "text"], ["description", "Description et résultat", "textarea"], ["url", "Lien", "url"]],
        certification: [["titre", "Certification", "text"], ["sousTitre", "Organisme", "text"], ["dateFin", "Date d'obtention", "month"], ["url", "Lien", "url"]],
        interet: [["titre", "Centre d’intérêt", "text"], ["description", "Précision", "text"]],
        lien: [["titre", "Nom du lien", "text"], ["url", "Adresse web", "url"]]
    };

    document.querySelectorAll("[data-collection]").forEach(section => {
        const conteneur = section.querySelector("[data-items]");
        const champs = definitions[section.dataset.kind];
        let index = 0;
        const ajouter = () => {
            const ligne = document.createElement("div");
            ligne.className = "cv-element-form";
            const grille = document.createElement("div");
            grille.className = "ligne";
            champs.forEach(([propriete, libelle, type]) => {
                const bloc = document.createElement("div");
                bloc.className = "champ";
                const label = document.createElement("label");
                label.textContent = libelle;
                const champ = type === "textarea" ? document.createElement("textarea") : document.createElement("input");
                if (type !== "textarea") champ.type = type;
                champ.name = `${section.dataset.collection}[${index}].${propriete}`;
                bloc.append(label, champ);
                grille.appendChild(bloc);
            });
            const supprimer = document.createElement("button");
            supprimer.type = "button";
            supprimer.className = "bouton danger petit";
            supprimer.textContent = "Supprimer";
            supprimer.addEventListener("click", () => ligne.remove());
            ligne.append(grille, supprimer);
            conteneur.appendChild(ligne);
            index++;
        };
        section.querySelector("[data-ajouter]").addEventListener("click", ajouter);
        ajouter();
    });
});
