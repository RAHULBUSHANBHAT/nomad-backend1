package com.cts.repository;

import com.cts.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {
    
    /**
     * Spring Data JPA will automatically create the SQL query for this method:
     * "SELECT * FROM user_credentials WHERE email = ?"
     */
    Optional<UserCredential> findByEmail(String email);
}