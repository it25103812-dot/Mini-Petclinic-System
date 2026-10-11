package com.example.clinic;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<AppUser> findByRoleAndEnabledTrueOrderByFullNameAsc(String role);
    List<AppUser> findAllByOrderByRoleAscFullNameAsc();
}
