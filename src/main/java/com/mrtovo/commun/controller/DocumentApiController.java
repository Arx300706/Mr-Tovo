package com.mrtovo.commun.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.commun.entity.Document;
import com.mrtovo.commun.repository.DocumentRepository;

@RestController
@RequestMapping("/api/documents")
public class DocumentApiController extends ApiCrudController<Document> {

	public DocumentApiController(DocumentRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Document", Document::getIdDocument, Document::setIdDocument);
	}

}
