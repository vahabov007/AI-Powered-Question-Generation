package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions.CustomException;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username) // Use the new method
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
         if(!user.isEnabled()) {
             throw new CustomException("User is disabled.", HttpStatus.CONFLICT);
         }
         return user;

    }
}
