package io.armory.plugin.credentials;

import com.netflix.spinnaker.clouddriver.cloudfoundry.config.CloudFoundryConfigurationProperties;
import com.netflix.spinnaker.clouddriver.kubernetes.config.KubernetesConfigurationProperties;
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesCredentials;
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesNamedAccountCredentials;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

@Configuration
@EnableConfigurationProperties
public class RemoteCredentialsConfig {

    @Bean("kubernetesCredentialsProperties")
    @ConfigurationProperties("credentials.remote.kubernetes")
    public RemoteCredentialsProperties kubernetesCredentialsProperties() {
        return new RemoteCredentialsProperties();
    }

    @Bean
    @ConditionalOnProperty("credentials.remote.kubernetes.enabled")
    @ExposeToApp
    public CredentialsDefinitionSource<KubernetesConfigurationProperties.ManagedAccount> remoteKubernetesCredentials(
            SecretManager secretManager,
            @Qualifier("kubernetesCredentialsProperties")
                    RemoteCredentialsProperties properties) {
        return new ExternalCredentialsDefinitionSource<>(secretManager, properties) {
            @Override
            protected Class<KubernetesConfigurationProperties.ManagedAccount> getCredentialsClass() {
                return KubernetesConfigurationProperties.ManagedAccount.class;
            }
        };
    }

    @Bean("cloudfoundryCredentialsProperties")
    @ConfigurationProperties("credentials.remote.cloudfoundry")
    public RemoteCredentialsProperties cloudfoundryCredentialsProperties() {
        return new RemoteCredentialsProperties();
    }

    @Bean
    @ConditionalOnProperty("credentials.remote.cloudfoundry.enabled")
    @ExposeToApp
    public CredentialsDefinitionSource<CloudFoundryConfigurationProperties.ManagedAccount> remoteCloudfoundryCredentials(
            SecretManager secretManager,
            @Qualifier("cloudfoundryCredentialsProperties")
                    RemoteCredentialsProperties properties) {
        return new ExternalCredentialsDefinitionSource<>(secretManager, properties) {
            @Override
            protected Class<CloudFoundryConfigurationProperties.ManagedAccount> getCredentialsClass() {
                return CloudFoundryConfigurationProperties.ManagedAccount.class;
            }
        };
    }

    @Bean
    @ExposeToApp
    @ConditionalOnProperty("credentials.loader.kubernetes.parallel")
    public AbstractCredentialsLoader<KubernetesNamedAccountCredentials> parallelKubernetesCredentialsLoader(
            @Nullable
                    CredentialsDefinitionSource<KubernetesConfigurationProperties.ManagedAccount>
                    kubernetesCredentialSource,
            KubernetesConfigurationProperties configurationProperties,
            KubernetesCredentials.Factory credentialFactory,
            CredentialsRepository<KubernetesNamedAccountCredentials> kubernetesCredentialsRepository) {

        if (kubernetesCredentialSource == null) {
            kubernetesCredentialSource = configurationProperties::getAccounts;
        }
        BasicCredentialsLoader loader = new BasicCredentialsLoader<>(
                kubernetesCredentialSource,
                a -> new KubernetesNamedAccountCredentials(a, credentialFactory),
                kubernetesCredentialsRepository);
        loader.setParallel(true);
        return loader;
    }
}