package org.akashbag.ecommerce;

import lombok.extern.slf4j.Slf4j;
import org.akashbag.ecommerce.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Slf4j
@SpringBootTest
public class EmailServiceTest {
    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        String otp = emailService.generateOtp();
        emailService.sendEmail("milanbhai225@gmail.com", "testing", otp);
    }
}
