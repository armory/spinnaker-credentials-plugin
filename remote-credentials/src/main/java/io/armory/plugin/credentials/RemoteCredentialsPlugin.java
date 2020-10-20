package io.armory.plugin.credentials;

import com.netflix.spinnaker.kork.plugins.api.spring.SpringLoaderPlugin;
import org.pf4j.PluginWrapper;

import java.util.List;

public class RemoteCredentialsPlugin extends SpringLoaderPlugin {
    /**
     * Constructor to be used by plugin manager for plugin instantiation. Your plugins have to provide
     * constructor with this exact signature to be successfully loaded by manager.
     *
     * @param wrapper
     */
    public RemoteCredentialsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public List<String> getPackagesToScan() {
        return List.of("io.armory.plugin.credentials");
    }
}
