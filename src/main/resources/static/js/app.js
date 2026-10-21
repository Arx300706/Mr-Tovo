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
});
