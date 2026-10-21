package com.mrtovo.commun.controller;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mrtovo.commun.service.JournalAuditService;

/**
 * API REST CRUD commune (reprise du projet Exemple) : GET liste / GET {id} / POST / PUT {id} / DELETE {id}.
 * Chaque sous-classe porte son {@code @RequestMapping("/api/...")} ; les écritures sont tracées dans le journal d'audit.
 */
public abstract class ApiCrudController<T> {

	private final JpaRepository<T, Integer> repository;
	private final JournalAuditService journalAuditService;
	private final String objet;
	private final Function<T, Integer> lireId;
	private final BiConsumer<T, Integer> fixerId;

	protected ApiCrudController(JpaRepository<T, Integer> repository, JournalAuditService journalAuditService,
			String objet, Function<T, Integer> lireId, BiConsumer<T, Integer> fixerId) {
		this.repository = repository;
		this.journalAuditService = journalAuditService;
		this.objet = objet;
		this.lireId = lireId;
		this.fixerId = fixerId;
	}

	@GetMapping
	public List<T> tous() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<T> un(@PathVariable Integer id) {
		return ResponseEntity.of(repository.findById(id));
	}

	@PostMapping
	public ResponseEntity<T> creer(@RequestBody T entite) {
		fixerId.accept(entite, null);
		T enregistree = repository.save(entite);
		journalAuditService.enregistrer("CREER", objet, lireId.apply(enregistree), "API REST");
		return ResponseEntity.status(HttpStatus.CREATED).body(enregistree);
	}

	@PutMapping("/{id}")
	public ResponseEntity<T> modifier(@PathVariable Integer id, @RequestBody T entite) {
		if (!repository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		fixerId.accept(entite, id);
		T enregistree = repository.save(entite);
		journalAuditService.enregistrer("MODIFIER", objet, id, "API REST");
		return ResponseEntity.ok(enregistree);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
		if (!repository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		repository.deleteById(id);
		journalAuditService.enregistrer("SUPPRIMER", objet, id, "API REST");
		return ResponseEntity.noContent().build();
	}

}
