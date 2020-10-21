package io.armory.plugin.credentials;

import lombok.Data;

@Data
public class RemoteCredentialsProperties {
    private boolean enabled;
    private String endpoint;
    private OutputFormat format = OutputFormat.YAML;

    public enum OutputFormat {
        JSON,
        YAML
    }
}
