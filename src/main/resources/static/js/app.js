document.addEventListener("DOMContentLoaded", () => {
	// Menu latéral repliable sur téléphone (fragments/entete.html + fragments/menu.html)
	const bouton = document.querySelector("[data-bascule-menu]");
	const menu = document.getElementById("menu-lateral");
	if (bouton && menu) {
		bouton.addEventListener("click", () => menu.classList.toggle("ouvert"));
	}
});
