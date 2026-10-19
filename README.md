# Mr-Tovo

Application web construite avec **Spring Boot 4** côté serveur et **Thymeleaf** pour le rendu des pages HTML.

| Élément       | Version / choix          |
|---------------|--------------------------|
| Java          | 21                       |
| Spring Boot   | 4.1.1                    |
| Build         | Maven (wrapper `mvnw`)   |
| Moteur de vue | Thymeleaf                |
| Package       | `com.mrtovo`             |

---

## 1. Prérequis

- **JDK 21** installé (`java -version` doit afficher 21).
- Maven n'est **pas** obligatoire : le projet fournit le wrapper `./mvnw` (Linux/macOS) et `mvnw.cmd` (Windows), qui télécharge automatiquement la bonne version de Maven.
- Un IDE (IntelliJ IDEA, VS Code avec l'extension Java, Eclipse…) est recommandé mais pas indispensable.

---

## 2. Lancer le projet

### Option A : en ligne de commande (développement)

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Puis ouvrir **http://localhost:8080** dans le navigateur.

> Le premier lancement peut prendre plusieurs minutes : Maven télécharge toutes les dépendances dans `~/.m2`. Les lancements suivants sont rapides.

Pour arrêter l'application : `Ctrl + C`.

### Option B : depuis l'IDE

1. Ouvrir le dossier du projet (l'IDE détecte le `pom.xml`).
2. Lancer la classe `src/main/java/com/mrtovo/MrTovoApplication.java` (méthode `main`).

### Option C : construire un JAR exécutable (production)

```bash
./mvnw clean package
java -jar target/mr-tovo-0.0.1-SNAPSHOT.jar
```

### Changer le port

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
# ou
java -jar target/mr-tovo-0.0.1-SNAPSHOT.jar --server.port=9090
```

### Lancer les tests

```bash
./mvnw test
```

---

## 3. Structure du projet

```
Mr-Tovo/
├── pom.xml                              # Dépendances et configuration Maven
├── mvnw, mvnw.cmd, .mvn/                # Wrapper Maven
└── src/
    ├── main/
    │   ├── java/com/mrtovo/
    │   │   ├── MrTovoApplication.java   # Point d'entrée Spring Boot
    │   │   └── controller/
    │   │       └── HomeController.java  # Contrôleur de la page d'accueil
    │   └── resources/
    │       ├── application.properties   # Configuration de l'application
    │       ├── templates/               # Vues Thymeleaf (.html)
    │       │   ├── fragments/
    │       │   │   └── layout.html      # head / header / footer communs
    │       │   └── index.html           # Page d'accueil
    │       └── static/                  # Fichiers servis tels quels
    │           ├── css/style.css
    │           └── js/app.js
    └── test/java/com/mrtovo/            # Tests (JUnit + MockMvc)
```

---

## 4. Spring Boot : l'essentiel

### Dépendances (`pom.xml`)

| Starter                          | Rôle                                                         |
|----------------------------------|--------------------------------------------------------------|
| `spring-boot-starter-webmvc`     | Serveur web (Tomcat embarqué) + Spring MVC                   |
| `spring-boot-starter-thymeleaf`  | Moteur de templates HTML                                     |
| `spring-boot-starter-validation` | Validation des formulaires (`@Valid`, `@NotBlank`, …)        |
| `spring-boot-devtools`           | Redémarrage automatique en développement                     |

### Configuration (`application.properties`)

```properties
spring.application.name=mr-tovo
server.port=8080

spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false   # false en dev pour voir les changements sans redémarrer
```

### Le cycle d'une requête

1. Le navigateur appelle `GET /`.
2. Spring trouve la méthode annotée `@GetMapping("/")` dans un `@Controller`.
3. La méthode ajoute des données au `Model` et renvoie un **nom de vue** (`"index"`).
4. Thymeleaf charge `templates/index.html`, y injecte les données et renvoie le HTML.

```java
@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Accueil");
		model.addAttribute("message", "Bienvenue sur Mr Tovo !");
		return "index"; // -> src/main/resources/templates/index.html
	}
}
```

> `@Controller` renvoie une **vue** HTML. Pour une API JSON, utiliser `@RestController`.

### Rechargement à chaud (DevTools)

Avec DevTools, l'application redémarre automatiquement quand une classe Java est recompilée :
- **IntelliJ** : `Build > Build Project` (`Ctrl + F9`), ou activer *Build project automatically*.
- **VS Code** : la recompilation à la sauvegarde est automatique.

Les fichiers `templates/` et `static/` sont rechargés sans redémarrage (cache Thymeleaf désactivé) : il suffit de rafraîchir la page.

---

## 5. Thymeleaf : l'essentiel

Les templates sont des fichiers HTML valides enrichis d'attributs `th:*`. Ne pas oublier le namespace :

```html
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
```

### Syntaxe courante

| Besoin                    | Syntaxe                                                        |
|---------------------------|----------------------------------------------------------------|
| Afficher une variable     | `<h1 th:text="${message}">Texte par défaut</h1>`               |
| Lien vers une URL         | `<a th:href="@{/produits}">Produits</a>`                       |
| Lien avec paramètre       | `<a th:href="@{/produits/{id}(id=${p.id})}">Voir</a>`          |
| Ressource statique        | `<link rel="stylesheet" th:href="@{/css/style.css}">`          |
| Condition                 | `<p th:if="${liste.isEmpty()}">Aucun élément</p>`              |
| Boucle                    | `<li th:each="p : ${produits}" th:text="${p.nom}"></li>`       |
| Attribut dynamique        | `<img th:src="@{${p.image}}" th:alt="${p.nom}">`               |
| Classe conditionnelle     | `<li th:classappend="${actif} ? 'active'">`                    |

Expressions : `${...}` variable du modèle · `@{...}` URL · `~{...}` fragment · `*{...}` champ de l'objet du formulaire · `#{...}` message i18n.

### Layout et fragments

Les parties communes sont définies une seule fois dans `templates/fragments/layout.html` avec `th:fragment` :

```html
<head th:fragment="head(title)"> ... </head>
<header th:fragment="header"> ... </header>
<footer th:fragment="footer"> ... </footer>
```

Chaque page les réutilise avec `th:replace` :

```html
<head th:replace="~{fragments/layout :: head(${title})}"></head>
<body>
<header th:replace="~{fragments/layout :: header}"></header>

<main class="container">
	<!-- contenu de la page -->
</main>

<footer th:replace="~{fragments/layout :: footer}"></footer>
</body>
```

### Formulaire avec validation (exemple)

```html
<form th:action="@{/contact}" th:object="${contactForm}" method="post">
	<input type="text" th:field="*{nom}">
	<span th:if="${#fields.hasErrors('nom')}" th:errors="*{nom}"></span>
	<button type="submit">Envoyer</button>
</form>
```

```java
@PostMapping("/contact")
public String envoyer(@Valid @ModelAttribute ContactForm contactForm, BindingResult result) {
	if (result.hasErrors()) {
		return "contact";
	}
	return "redirect:/";
}
```

---

## 6. Ajouter une nouvelle page

1. **Contrôleur** : créer `src/main/java/com/mrtovo/controller/AProposController.java`

   ```java
   @Controller
   public class AProposController {

   	@GetMapping("/a-propos")
   	public String aPropos(Model model) {
   		model.addAttribute("title", "À propos");
   		return "a-propos";
   	}
   }
   ```

2. **Vue** : créer `src/main/resources/templates/a-propos.html` en copiant la structure de `index.html`.
3. **Menu** (optionnel) : ajouter le lien dans le fragment `header` de `layout.html` :

   ```html
   <a th:href="@{/a-propos}">À propos</a>
   ```

4. Ouvrir http://localhost:8080/a-propos.

---

## 7. Problèmes fréquents

| Symptôme                                     | Solution                                                                 |
|----------------------------------------------|--------------------------------------------------------------------------|
| `Port 8080 was already in use`               | Arrêter l'autre processus ou changer le port (voir section 2)            |
| `./mvnw: Permission denied`                  | `chmod +x mvnw`                                                          |
| `TemplateInputException: Error resolving template` | Le nom renvoyé par le contrôleur ne correspond à aucun fichier dans `templates/` |
| Modifications HTML non visibles              | Vérifier `spring.thymeleaf.cache=false` et rafraîchir (`Ctrl + F5`)      |
| Mauvaise version de Java                     | Vérifier `java -version` et `JAVA_HOME` (JDK 21 requis)                  |

---

## 8. Documentation officielle

- Spring Boot : https://docs.spring.io/spring-boot/
- Thymeleaf : https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
- Thymeleaf + Spring : https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html
