package com.github.hkzorman.avakinitemdb.repositories;

import com.github.hkzorman.avakinitemdb.models.db.ItemMatchupProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemMatchupProposalRepository extends JpaRepository<ItemMatchupProposal, String> {

    ItemMatchupProposal save(ItemMatchupProposal item);

    Optional<ItemMatchupProposal> findById(String uuid);

    Optional<List<ItemMatchupProposal>> findByIsActive(boolean isActive);

    Optional<List<ItemMatchupProposal>> findByCreatedByUsername(String username);

    Optional<ItemMatchupProposal> findByCreatedOn(LocalDateTime dateTime);
}
