package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller.impl;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.security.JwtUtil;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.controller.UserController;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.RegisterDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserControllerImpl implements UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        Optional<User> optional = userRepository.findByUsername(registerDTO.getUsername());
        if (optional.isPresent()) {
            return ResponseEntity.badRequest().body(createResponse(false, "Username has already registered."));
        }
        userService.saveUser(registerDTO);
        return ResponseEntity.ok(createResponse(true, "User has registered successfully."));

    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            final String jwt = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(jwt, request.getUsername(), "Login Successfully."));

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO(null, request.getUsername(), "Login Unsuccessfully. Invalid Credentials."));
        }
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok("Logged out successfully.");
    }


    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}
