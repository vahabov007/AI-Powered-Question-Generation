package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.RegisterDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface UserController {

    public ResponseEntity<?> register(RegisterDTO registerDTO, BindingResult bindingResult);

    public ResponseEntity<?> login(AuthDTO authDTO, BindingResult bindingResult);

    public ResponseEntity<?> logout(String header);

}
