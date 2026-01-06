package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.OtpRequest;
import org.akashbag.ecommerce.dto.request.PasswordResetRequest;
import org.akashbag.ecommerce.dto.request.UserRequest;
import org.akashbag.ecommerce.dto.response.TokenResponse;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.repository.UserRepository;
import org.akashbag.ecommerce.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRequest user) {
        authService.register(user);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Successfully created user")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserRequest user) {
        String token = authService.login(user);

        TokenResponse tokenResponse =   TokenResponse.builder()
                .username(user.getEmail())
                .token(token)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authService.logout(auth.getName());

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Successfully logged out")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/send-email-verification")
    public ResponseEntity<ApiResponse> sendEmailVerification() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authService.sendVerificationMail(auth.getName());

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Verification mail sent successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody OtpRequest otpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authService.verifyEmail(auth.getName(), otpRequest);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Successfully verified email")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/send-reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody UserRequest userRequest) {
        authService.sendPasswordResetToken(userRequest.getEmail());

        ApiResponse apiResponse = ApiResponse.builder()
                .message("password reset token sent successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/reset-password/{id}")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest, @PathVariable String id) {
        authService.resetPassword(passwordResetRequest, id);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("password has changed successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
