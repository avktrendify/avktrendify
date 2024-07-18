package com.github.hkzorman.avakinitemdb.repositories;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItemChangeProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvakinItemChangeRepository extends JpaRepository<AvakinItemChangeProposal, String> {

    AvakinItemChangeProposal save(AvakinItemChangeProposal change);

    Optional<Page<AvakinItemChangeProposal>> findByItemId(String itemId, Pageable pageable);

    Optional<Page<AvakinItemChangeProposal>> findByIsActive(boolean isActive, Pageable pageable);
}
