package com.github.hkzorman.avakinitemdb.repositories;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvakinItemRepository extends JpaRepository<AvakinItem, Integer> {

    AvakinItem save(AvakinItem item);

    Optional<AvakinItem> findByItemId(String itemId);

    Optional<Page<AvakinItem>> findByTitleContaining(String title, Pageable pageable);

    Optional<Page<AvakinItem>> findByTitleEsContaining(String titleEs, Pageable pageable);

    //Optional<Page<AvakinItem>> findAllByTitleContaining(String title, Pageable pageable);

    Optional<Page<AvakinItem>> findByTitleContainingAndType(String title, String type, Pageable pageable);

    Optional<Page<AvakinItem>> findByTitleEsContainingAndType(String titleEs, String type, Pageable pageable);

    //Optional<Page<AvakinItem>> findAllByTitleContainingAndType(String title, String type, Pageable pageable);

    Optional<Page<AvakinItem>> findByType(String type, Pageable pageable);

    Optional<List<AvakinItem>> findByType(String type);

    Optional<Page<AvakinItem>> findByTypeAndSubType(String type, String subType, Pageable pageable);

}
