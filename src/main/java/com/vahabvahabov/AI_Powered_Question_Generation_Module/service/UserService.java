package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.RegisterDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions.CustomException;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Role;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.RoleRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(RegisterDTO registerDTO) {
        Optional<User> optional = userRepository.findByUsername(registerDTO.getUsername());
        if (optional.isPresent()) {
            throw new CustomException("User has already registered.", HttpStatus.FORBIDDEN);
        }
        saveUser(registerDTO);
    }

    @Transactional
    public void saveUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        Role defaultRole = roleRepository.findByRole(UserRole.ROLE_STUDENT)
                        .orElseThrow(() -> new CustomException("Default role not found.", HttpStatus.NOT_FOUND));
        user.getRoles().add(defaultRole);
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void addRoleToUser(Long userId, UserRole userRole) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        Role role = roleRepository.findByRole(userRole)
                .orElseThrow(() -> new CustomException("There is no such as a role in the database.", HttpStatus.CONFLICT));
        if(user.getRoles().contains(role)) {
            throw new CustomException("User has already had this role.", HttpStatus.FORBIDDEN);
        }
        user.getRoles().add(role);
    }

    public boolean authenticate(AuthDTO authDTO) {
        Optional<User> optional = userRepository.findByUsername(authDTO.getUsername());
        if (optional.isPresent()) {
            User user = optional.get();
            return passwordEncoder.matches(authDTO.getPassword(), user.getPassword());
        }
        return false;
    }




}
