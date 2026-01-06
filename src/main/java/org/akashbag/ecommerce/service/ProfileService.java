package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.PhoneOtpRequest;
import org.akashbag.ecommerce.dto.request.ProfileRequest;
import org.akashbag.ecommerce.model.PhoneOtp;
import org.akashbag.ecommerce.model.Profile;
import org.akashbag.ecommerce.model.User;
import org.akashbag.ecommerce.repository.PhoneOtpRepository;
import org.akashbag.ecommerce.repository.ProfileRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TwilioSmsService twilioSmsService;
    private final PhoneOtpRepository phoneOtpRepository;
    private final UploadService uploadService;

    public Profile createProfile(String userId) {
        Profile profile = Profile.builder()
                .userId(userId)
                .build();
        return profileRepository.save(profile);
    }

    @Transactional
    public void updateProfile(ProfileRequest profileRequest, MultipartFile profileImage) {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.getUserByEmail(auth.getName());
        Profile profile = profileRepository.findByUserId(user.getId());

        if (profile == null) profile = createProfile(user.getId());

        if (profileRequest.getUserName() != null && !profileRequest.getUserName().equals(profile.getUsername())) {
            if (profileRepository.existsByUsername(profileRequest.getUserName())) {
                throw new DuplicateKeyException("Username '" + profileRequest.getUserName() + "' is already taken");
            }
            profile.setUsername(profileRequest.getUserName());
        }

        if (profileRequest.getContactNo() != null && !profileRequest.getContactNo().equals(profile.getContactNo())) {
            if (profileRepository.existsByContactNo(profileRequest.getContactNo())) {
                throw new DuplicateKeyException("Contact No '" + profileRequest.getContactNo() + "' is already taken");
            }
            profile.setContactNo(profileRequest.getContactNo());
            profile.setPhoneVerified(false);
        }

        if (profileImage!=null){
            String url = uploadService.upload(profileImage, user.getId());
            profile.setProfileUrl(url);
        }

        profile.setFirstName(profileRequest.getFirstName()!=null? profileRequest.getFirstName():profile.getFirstName());
        profile.setLastName(profileRequest.getLastName()!=null? profileRequest.getLastName():profile.getLastName());
        profile.setAbout(profileRequest.getAbout()!=null? profileRequest.getAbout():profile.getAbout());
        profile.setAddress(profileRequest.getAddress()!=null? profileRequest.getAddress():profile.getAddress());

        profileRepository.save(profile);
    }

    public Profile getUserProfile() {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(auth.getName());

        Profile profile = profileRepository.findByUserId(user.getId());
        if (profile == null) return createProfile(user.getId());
        return profile;
    }

    @Transactional
    public void sendPhoneOtp() {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(auth.getName());
        Profile profile = profileRepository.findByUserId(user.getId());

        if (profile.isPhoneVerified()) throw new IllegalArgumentException("Phone number already verified");


        phoneOtpRepository.deleteByUserId(user.getId());
        String otp = twilioSmsService.sendSms(profile.getContactNo());

        PhoneOtp phoneOtp = PhoneOtp.builder()
                .userId(user.getId())
                .phoneNumber(profile.getContactNo())
                .otp(otp)
                .build();
        phoneOtpRepository.save(phoneOtp);
    }

    @Transactional
    public void verifyPhoneNo(PhoneOtpRequest phoneOtpRequest) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());
        Profile profile = profileRepository.findByUserId(user.getId());
        if (profile.isPhoneVerified()) throw new IllegalArgumentException("Phone number already verified");


        PhoneOtp phoneOtp = phoneOtpRepository.findByUserId(profile.getUserId());
        if (phoneOtp == null) throw new IllegalArgumentException("Otp is already Expired");

        if (!phoneOtp.getOtp().equals(phoneOtpRequest.getOtp())) throw new IllegalArgumentException("Incorrect Otp");

        phoneOtpRepository.delete(phoneOtp);
        profile.setPhoneVerified(true);
        profileRepository.save(profile);
    }
}
