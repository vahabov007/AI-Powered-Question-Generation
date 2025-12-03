package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required.")
    private Set<UserRole> roles;


}
