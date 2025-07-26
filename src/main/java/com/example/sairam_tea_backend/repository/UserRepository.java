// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/repository/UserRepository.java
package com.example.sairam_tea_backend.repository;

import com.example.sairam_tea_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
