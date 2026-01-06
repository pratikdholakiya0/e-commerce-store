package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.OtpRequest;
import org.akashbag.ecommerce.dto.request.PasswordResetRequest;
import org.akashbag.ecommerce.dto.request.UserRequest;
import org.akashbag.ecommerce.enums.Role;
import org.akashbag.ecommerce.model.JwtToken;
import org.akashbag.ecommerce.model.Otp;
import org.akashbag.ecommerce.model.User;
import org.akashbag.ecommerce.repository.JwtTokenRepository;
import org.akashbag.ecommerce.repository.OtpRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.akashbag.ecommerce.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenRepository jwtTokenRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    @Transactional
    public void register(UserRequest userRequest) {
        User user = userRepository.getUserByEmail(userRequest.getEmail());
        if (user != null) throw new DuplicateKeyException("user with this email already exists !");
        user = User.builder()
            .email(userRequest.getEmail())
            .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(Role.USER)
            .build();

        userRepository.save(user);
    }

    @Transactional
    public String login(UserRequest userRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userRequest.getEmail(),
                            userRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username or password!");
        }
        User user = userRepository.getUserByEmail(userRequest.getEmail());
        JwtToken jwtToken = JwtToken.builder()
                .username(userRequest.getEmail())
                .token(jwtUtil.generateJwtToken(user))
                .build();

        jwtTokenRepository.save(jwtToken);

        return jwtToken.getToken();
    }

    @Transactional
    public void logout(String username) {
        jwtTokenRepository.deleteJwtTokensByUsername(username);
    }

    @Transactional
    public void sendVerificationMail(String email) {
        String otpGenerated = emailService.generateOtp();
        User user = userRepository.getUserByEmail(email);

        if (user==null) throw new UsernameNotFoundException("User not found!");
        if (user.isVerified()) throw new IllegalArgumentException("User is already verified!");

        Otp otp = otpRepository.getOtpByEmail(email);
        if (otp!=null) {
            otpRepository.delete(otp);
        }
        otp = Otp.builder()
                .email(email)
                .otp(otpGenerated)
                .expiredAt(new Date(System.currentTimeMillis() + 10*60*1000))
                .build();
        otpRepository.save(otp);

        emailService.sendEmail(email, "User verification Otp", otpGenerated);

        user.setOtpId(otp.getId());
        userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(String email, OtpRequest otpRequest) {
        User user = userRepository.getUserByEmail(email);

        if (user==null) throw new IllegalArgumentException("User not found!");
        if (user.getOtpId()==null) throw new IllegalArgumentException("Otp not found!");

        Otp otp = otpRepository.getOtpById(user.getOtpId());

        if (!otp.getOtp().equals(otpRequest.getOtp()) || otp.getExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("Invalid Otp or already expired!");
        }

        user.setOtpId(null);
        user.setVerified(true);
        userRepository.save(user);
        otpRepository.delete(otp);
    }

    @Transactional
    public void sendPasswordResetToken(String email) {
        User user = userRepository.getUserByEmail(email);
        if (user==null) throw new UsernameNotFoundException("User not found!");

        String token = jwtUtil.generateJwtToken(user);
        String passwordResetToken = "http://localhost:8080/reset-password/"+token;

        user.setPasswordResetToken(token);
        userRepository.save(user);

        emailService.sendEmail(email, "Password-Reset-Token", passwordResetToken);
    }

    public void resetPassword(PasswordResetRequest passwordResetRequest, String token) {
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.getUserByEmail(email);

        if (user==null) throw new UsernameNotFoundException("User not found!");

        if (!user.getPasswordResetToken().equals(token)) throw new IllegalArgumentException("Invalid Password reset token!");

        user.setPasswordResetToken(null);
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(user);

        emailService.sendEmail(email, "Password updated", "Password has changed at"+ new Date(System.currentTimeMillis())+ "if it is not by you then change password and report us");
    }
}
