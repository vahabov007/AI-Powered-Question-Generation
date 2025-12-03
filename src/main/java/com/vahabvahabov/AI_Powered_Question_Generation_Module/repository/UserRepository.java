package com.vahabvahabov.AI_Powered_Question_Generation_Module.repository;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
