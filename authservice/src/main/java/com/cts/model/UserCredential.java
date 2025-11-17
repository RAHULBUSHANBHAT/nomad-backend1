package com.cts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user's credentials in the
 * auth-service's local database.
 */
@Data
@Entity
@Table(name = "user_credentials")
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {

    @Id
    @Column(name = "user_id")
    private String userId; // This is the UUID from the user-service

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // This will be the HASHED password

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", nullable = false)
    private Status status;
}