package com.securecas.commercial.mfa.service;

import com.securecas.commercial.config.CommercialModuleProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MfaService {
    
    private final CommercialModuleProperties properties;
    private final JavaMailSender mailSender;
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    public MfaService(CommercialModuleProperties properties, JavaMailSender mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
        
        if (properties.getMfa().isSmsEnabled() && 
            properties.getMfa().getTwilioAccountSid() != null &&
            properties.getMfa().getTwilioAuthToken() != null) {
            Twilio.init(properties.getMfa().getTwilioAccountSid(), 
                       properties.getMfa().getTwilioAuthToken());
        }
    }
    
    public String generateOtp(String principal) {
        int length = properties.getMfa().getOtpLength();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        
        String otpCode = otp.toString();
        otpCache.put(principal, new OtpEntry(otpCode, System.currentTimeMillis()));
        
        log.debug("Generated OTP for principal: {}", principal);
        return otpCode;
    }
    
    public boolean sendSmsOtp(String principal, String phoneNumber) {
        try {
            String otp = generateOtp(principal);
            String messageText = String.format("Your SecureCAS verification code is: %s", otp);
            
            Message message = Message.creator(
                new com.twilio.type.PhoneNumber(phoneNumber),
                new com.twilio.type.PhoneNumber(properties.getMfa().getTwilioFromNumber()),
                messageText
            ).create();
            
            log.info("SMS OTP sent to {} for principal {}", phoneNumber, principal);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS OTP", e);
            return false;
        }
    }
    
    public boolean sendEmailOtp(String principal, String email) {
        try {
            String otp = generateOtp(principal);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("SecureCAS Verification Code");
            message.setText(String.format(
                "Your SecureCAS verification code is: %s\n\n" +
                "This code will expire in %d seconds.\n\n" +
                "If you did not request this code, please ignore this email.",
                otp, properties.getMfa().getOtpValiditySeconds()
            ));
            
            mailSender.send(message);
            log.info("Email OTP sent to {} for principal {}", email, principal);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email OTP", e);
            return false;
        }
    }
    
    public boolean verifyOtp(String principal, String otp) {
        OtpEntry entry = otpCache.get(principal);
        
        if (entry == null) {
            log.warn("No OTP found for principal: {}", principal);
            return false;
        }
        
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(
            System.currentTimeMillis() - entry.timestamp
        );
        
        if (elapsedSeconds > properties.getMfa().getOtpValiditySeconds()) {
            otpCache.remove(principal);
            log.warn("OTP expired for principal: {}", principal);
            return false;
        }
        
        boolean valid = entry.otp.equals(otp);
        if (valid) {
            otpCache.remove(principal);
            log.info("OTP verified successfully for principal: {}", principal);
        } else {
            log.warn("Invalid OTP provided for principal: {}", principal);
        }
        
        return valid;
    }
    
    private static class OtpEntry {
        final String otp;
        final long timestamp;
        
        OtpEntry(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}