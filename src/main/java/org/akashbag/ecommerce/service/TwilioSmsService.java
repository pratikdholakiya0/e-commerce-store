package org.akashbag.ecommerce.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsService {
    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init(){
        Twilio.init(accountSid, authToken);
    }

    public String generateOtp(){
        Random random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public String sendSms(String phoneNumber){
        String otp = generateOtp();
        try{
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    "Your Verification OTP for Akash Bag is: " + otp
            ).create();
            log.info("SMS sent successfully to {}: SID {}", phoneNumber, message.getSid());

            return otp;
        }catch(Exception e){
            log.error("Failed to send SMS: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP via SMS");
        }
    }
}
