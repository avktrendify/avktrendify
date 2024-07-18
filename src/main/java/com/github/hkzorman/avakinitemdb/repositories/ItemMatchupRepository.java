package com.github.hkzorman.avakinitemdb.repositories;

import com.github.hkzorman.avakinitemdb.models.db.ItemMatchup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemMatchupRepository extends JpaRepository<ItemMatchup, Integer> {
    ItemMatchup save(ItemMatchup item);

    Optional<ItemMatchup> findById(int id);
}