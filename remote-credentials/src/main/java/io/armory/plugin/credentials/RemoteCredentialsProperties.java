package io.armory.plugin.credentials;

import lombok.Data;

@Data
public class RemoteCredentialsProperties {
    private boolean enabled;
    private String endpoint;
}
