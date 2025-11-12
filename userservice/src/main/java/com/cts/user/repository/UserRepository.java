package com.cts.user.repository;

import com.cts.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page; // <-- Import Page
import org.springframework.data.domain.Pageable;
import com.cts.user.model.Role;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    long countByRole(Role role);
    
    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByRole(Role role, Pageable pageable);

}