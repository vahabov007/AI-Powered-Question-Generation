package com.vahabvahabov.AI_Powered_Question_Generation_Module.repository;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Role;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(UserRole role);

}
