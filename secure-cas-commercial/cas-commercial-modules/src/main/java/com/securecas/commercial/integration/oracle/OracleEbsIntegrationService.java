package com.securecas.commercial.integration.oracle;

import com.securecas.commercial.config.CommercialModuleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "securecas.commercial.integration.oracle-ebs.enabled", havingValue = "true")
public class OracleEbsIntegrationService {
    
    private final CommercialModuleProperties properties;
    private final RestTemplate restTemplate;
    private final WebServiceTemplate webServiceTemplate;
    
    public Map<String, Object> authenticateEbsUser(String username, String password) {
        try {
            String soapRequest = buildAuthenticationRequest(username, password);
            Source requestSource = new StreamSource(new StringReader(soapRequest));
            
            webServiceTemplate.setDefaultUri(properties.getIntegration().getOracleEbs().getEndpoint());
            Source responseSource = (Source) webServiceTemplate.sendSourceAndReceive(
                properties.getIntegration().getOracleEbs().getEndpoint(),
                requestSource
            );
            
            return parseAuthenticationResponse(responseSource);
        } catch (Exception e) {
            log.error("Failed to authenticate EBS user", e);
            throw new RuntimeException("EBS authentication failed", e);
        }
    }
    
    public Map<String, Object> getUserResponsibilities(String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + getBasicAuthHeader());
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = properties.getIntegration().getOracleEbs().getEndpoint() + 
                        "/responsibilities?username=" + username;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                result = response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Failed to get user responsibilities", e);
        }
        
        return result;
    }
    
    public boolean validateEbsSession(String sessionId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + getBasicAuthHeader());
            headers.set("X-EBS-Session-ID", sessionId);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = properties.getIntegration().getOracleEbs().getEndpoint() + 
                        "/session/validate";
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("Failed to validate EBS session", e);
            return false;
        }
    }
    
    private String buildAuthenticationRequest(String username, String password) {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "  <soap:Body>" +
            "    <authenticateUser xmlns=\"http://xmlns.oracle.com/apps/fnd/security\">" +
            "      <username>%s</username>" +
            "      <password>%s</password>" +
            "    </authenticateUser>" +
            "  </soap:Body>" +
            "</soap:Envelope>",
            username, password
        );
    }
    
    private Map<String, Object> parseAuthenticationResponse(Source response) {
        Map<String, Object> result = new HashMap<>();
        // Parse SOAP response - simplified for demonstration
        result.put("authenticated", true);
        result.put("sessionId", "EBS-" + System.currentTimeMillis());
        result.put("userGuid", "USER-GUID-123");
        return result;
    }
    
    private String getBasicAuthHeader() {
        String credentials = properties.getIntegration().getOracleEbs().getUsername() + 
                           ":" + properties.getIntegration().getOracleEbs().getPassword();
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}