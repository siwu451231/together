package com.example.backend.history.repository;

import com.example.backend.collab.entity.DocumentOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentOperationRepository extends JpaRepository<DocumentOperation, Long> {
    List<DocumentOperation> findTop100ByDocumentIdOrderByIdDesc(Long documentId);

    Optional<DocumentOperation> findTopByDocumentIdOrderByIdDesc(Long documentId);

    Optional<DocumentOperation> findByIdAndDocumentId(Long id, Long documentId);

    void deleteByDocumentId(Long documentId);
}
