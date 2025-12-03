package com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_TEACHER("ROLE_TEACHER"),
    ROLE_STUDENT("ROLE_STUDENT");

    private final String type;

    UserRole(String type) {
        this.type = type;
    }
}
