package com.cts.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; // <-- Import Page
import org.springframework.stereotype.Repository;

import com.cts.user.model.Role;
import com.cts.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    long countByRole(Role role);
    
    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByRole(Role role, Pageable pageable);
    
    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndRole(String email, Role role);

  
    Optional<User> findByPhoneNumberAndRole(String phoneNumber, Role role);
  


}