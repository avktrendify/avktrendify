package com.github.hkzorman.avakinitemdb.repositories;

import com.github.hkzorman.avakinitemdb.models.db.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByUsername(String username);
}
