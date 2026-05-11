package com.vahabvahabov.AI_Powered_Question_Generation_Module.repository;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.RefreshToken;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    int deleteByUser(Long id);
}
