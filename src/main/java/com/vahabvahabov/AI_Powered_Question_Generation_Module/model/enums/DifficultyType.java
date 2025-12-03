package com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums;

import lombok.Getter;

@Getter
public enum DifficultyType {

    EASY_LEVEL("Easy"),
    MEDIUM_LEVEL("Medium"),
    HARD_LEVEL("Hard"),
    MIXED("Mixed");

    private final String type;

    DifficultyType(String type) {
        this.type = type;
    }
}
