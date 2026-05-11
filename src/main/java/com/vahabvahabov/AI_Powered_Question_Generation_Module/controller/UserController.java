package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.*;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.ApiResponse;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.RefreshToken;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.security.JwtTokenProvider;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.RefreshTokenService;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "User Management",
     description = "APIs for registering and managing signing up")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Registering the user.")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterDTO registerDTO,
                                      HttpServletRequest request) {
        userService.registerUser(registerDTO);
        return ResponseEntity.status(201).body(ApiResponse.success("The user successfully registered.",
                                                        null, 201,
                                                             request.getServletPath()));
    }

    @PostMapping("/login")
    @Operation(summary = "Signing in the user.")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthDTO authDTO,
                                   HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getUsername(),
                                                                                                                   authDTO.getPassword()));
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthResponseDTO authResponseDTO = new AuthResponseDTO(accessToken, refreshToken.getToken(),
                                                              authDTO.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Login Successfully!",
                                                              authResponseDTO, 200,
                                                              request.getServletPath()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout the user.")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user,
                                                    HttpServletRequest request) {

        refreshTokenService.deleteByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("The user logged out successfully.",
                                                         null, 200,
                                                              request.getServletPath()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody TokenRequest request,
                                                                   HttpServletRequest servletRequest) {
        TokenResponse tokenResponse = refreshTokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token successfully refreshed.",
                                                               tokenResponse, 200,
                                                               servletRequest.getServletPath()));

    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/role")
    public ResponseEntity<ApiResponse<Void>> addRoleToUser(@RequestBody RoleDTO roleDTO,
                                                           HttpServletRequest servletRequest) {
        userService.addRoleToUser(roleDTO.getUserId(), roleDTO.getUserRole());
        return ResponseEntity.ok(ApiResponse.success("The role successfully assigned.",
                                                   null, 201,
                                                        servletRequest.getServletPath()));

    }


}
