package com.finance.tracker.repository;

import com.finance.tracker.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Custom query method to find a user by their email address.
     * Spring Data JPA automatically generates the SQL query based on this method name.
     * * @param email The email to search for.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByEmail(String email);
}
