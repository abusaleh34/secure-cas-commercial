package com.securecas.commercial.provisioning.event;

import com.securecas.commercial.provisioning.model.ProvisionedUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class UserProvisionedEvent extends ApplicationEvent {
    
    private final ProvisionedUser user;
    private final boolean newUser;
    private final ProvisionedUser.ProvisionSource source;
    private final Map<String, Object> originalAttributes;
    private final LocalDateTime timestamp;
    
    public UserProvisionedEvent(Object source, ProvisionedUser user, boolean newUser, 
                               ProvisionedUser.ProvisionSource provisionSource,
                               Map<String, Object> originalAttributes) {
        super(source);
        this.user = user;
        this.newUser = newUser;
        this.source = provisionSource;
        this.originalAttributes = originalAttributes;
        this.timestamp = LocalDateTime.now();
    }
}