package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;


import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.AuthDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.RegisterDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserService {

    public Optional<User> findByUsername(String username);

    public void saveUser(RegisterDTO registerDTO);

    public boolean authenticate(AuthDTO authDTO);

    public User saveUser(User user);
}
