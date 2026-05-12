package com.example.backend.document.repository;

import com.example.backend.document.entity.DocumentMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentMemberRepository extends JpaRepository<DocumentMember, Long> {
    List<DocumentMember> findByUserId(Long userId);

    List<DocumentMember> findByDocumentId(Long documentId);

    Optional<DocumentMember> findByDocumentIdAndUserId(Long documentId, Long userId);

    boolean existsByDocumentIdAndUserId(Long documentId, Long userId);

    void deleteByDocumentId(Long documentId);
}
