package com.example.clinic;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Integer> {
    List<Owner> findByLastNameContainingIgnoreCase(String lastName);
}
