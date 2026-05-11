package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class RoleDTO {
    private Long userId;
    private UserRole userRole;
}
