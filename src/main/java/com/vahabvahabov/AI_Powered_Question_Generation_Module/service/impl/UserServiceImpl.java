package com.vahabvahabov.AI_Powered_Question_Generation_Module.service.impl;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.RegisterDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void saveUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRoles(registerDTO.getRoles());

        userRepository.save(user);
    }

    @Override
    public boolean authenticate(AuthDTO authDTO) {
        Optional<User> optional = userRepository.findByUsername(authDTO.getUsername());
        if (optional.isPresent()) {
            User user = optional.get();
            return passwordEncoder.matches(authDTO.getPassword(), user.getPassword());
        }
        return false;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
