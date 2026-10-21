package com.mrtovo.commun.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.commun.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

}
