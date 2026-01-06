package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.PhoneOtpRequest;
import org.akashbag.ecommerce.dto.request.ProfileRequest;
import org.akashbag.ecommerce.model.Profile;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateProfile(@RequestPart ProfileRequest profileRequest, @RequestPart(required = false) MultipartFile profileImage) {
        profileService.updateProfile(profileRequest, profileImage);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Profile updated successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<Profile> getProfile() {
        Profile profile = profileService.getUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/send-phone-otp")
    public ResponseEntity<ApiResponse> sendPhoneOtp() {
        profileService.sendPhoneOtp();

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Otp sent successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyPhoneNo(@RequestBody PhoneOtpRequest phoneOtpRequest) {
        profileService.verifyPhoneNo(phoneOtpRequest);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phone number verified successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
