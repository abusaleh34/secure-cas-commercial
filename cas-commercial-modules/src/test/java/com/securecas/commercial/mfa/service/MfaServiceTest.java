package com.securecas.commercial.mfa.service;

import com.securecas.commercial.config.CommercialModuleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private CommercialModuleProperties properties;

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        CommercialModuleProperties.Mfa mfaProperties = new CommercialModuleProperties.Mfa();
        mfaProperties.setOtpLength(6);
        mfaProperties.setOtpValiditySeconds(300);
        mfaProperties.setEmailEnabled(true);
        mfaProperties.setSmsEnabled(false);
        
        when(properties.getMfa()).thenReturn(mfaProperties);
        
        mfaService = new MfaService(properties, mailSender);
    }

    @Test
    void testGenerateOtp() {
        String principal = "testuser";
        String otp = mfaService.generateOtp(principal);
        
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testSendEmailOtp() {
        String principal = "testuser";
        String email = "test@example.com";
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        boolean result = mfaService.sendEmailOtp(principal, email);
        
        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testVerifyOtpSuccess() {
        String principal = "testuser";
        String otp = mfaService.generateOtp(principal);
        
        boolean result = mfaService.verifyOtp(principal, otp);
        
        assertTrue(result);
    }

    @Test
    void testVerifyOtpFailure() {
        String principal = "testuser";
        mfaService.generateOtp(principal);
        
        boolean result = mfaService.verifyOtp(principal, "000000");
        
        assertFalse(result);
    }

    @Test
    void testVerifyOtpNotFound() {
        boolean result = mfaService.verifyOtp("unknownuser", "123456");
        
        assertFalse(result);
    }

    @Test
    void testOtpExpiry() throws InterruptedException {
        CommercialModuleProperties.Mfa mfaProperties = new CommercialModuleProperties.Mfa();
        mfaProperties.setOtpLength(6);
        mfaProperties.setOtpValiditySeconds(1); // 1 second validity
        when(properties.getMfa()).thenReturn(mfaProperties);
        
        MfaService shortExpiryService = new MfaService(properties, mailSender);
        
        String principal = "testuser";
        String otp = shortExpiryService.generateOtp(principal);
        
        Thread.sleep(1500); // Wait for OTP to expire
        
        boolean result = shortExpiryService.verifyOtp(principal, otp);
        assertFalse(result);
    }
}