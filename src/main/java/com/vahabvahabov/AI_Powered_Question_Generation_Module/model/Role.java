package com.vahabvahabov.AI_Powered_Question_Generation_Module.model;


import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@Data @AllArgsConstructor @NoArgsConstructor
public class Role implements GrantedAuthority {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private UserRole role = UserRole.ROLE_STUDENT;

    @Override
    public String getAuthority() {
        return role.name();
    }
}
