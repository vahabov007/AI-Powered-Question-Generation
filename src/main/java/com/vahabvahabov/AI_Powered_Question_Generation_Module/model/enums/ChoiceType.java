package com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums;

import lombok.Getter;

@Getter
public enum ChoiceType {

    MULTIPLE_CHOICE("Multiple"),
    TRUE_FALSE("True/False"),
    SHORT_ANSWER("Short answer"),
    MIXED("Mixed");

    private final String choiceType;


    ChoiceType(String choiceType) {
        this.choiceType = choiceType;
    }
}
