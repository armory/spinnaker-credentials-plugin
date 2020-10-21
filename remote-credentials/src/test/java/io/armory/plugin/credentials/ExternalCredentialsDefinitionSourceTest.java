package io.armory.plugin.credentials;

import com.netflix.spinnaker.clouddriver.kubernetes.config.KubernetesConfigurationProperties;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

@RunWith(JUnitPlatform.class)
public class ExternalCredentialsDefinitionSourceTest {

    @Test
    public void testYaml() {
        SecretManager manager = Mockito.mock(SecretManager.class);
        Mockito.when(manager.decrypt(ArgumentMatchers.anyString())).thenAnswer(Object::toString);
        RemoteCredentialsProperties props = new RemoteCredentialsProperties();
        props.setEndpoint("https://example.com");

        ExternalCredentialsDefinitionSource<KubernetesConfigurationProperties.ManagedAccount> src =
                new ExternalCredentialsDefinitionSource<>(manager, props) {
            @Override
            protected Class<KubernetesConfigurationProperties.ManagedAccount> getCredentialsClass() {
                return KubernetesConfigurationProperties.ManagedAccount.class;
            }

            @Override
            protected Reader getDefinitionReader() throws IOException {
                return new BufferedReader(new InputStreamReader(
                        ExternalCredentialsDefinitionSource.class.getResourceAsStream("/k8s-creds-test.yaml"),
                        Charset.defaultCharset()));
            }
        };
        long now = System.currentTimeMillis();
        List<?> creds = src.getCredentialsDefinitions();
        long duration = System.currentTimeMillis() - now;
        System.out.println("YAML Duration " + duration + "ms");
        Assertions.assertThat(creds).hasSize(1000);
    }

    @Test
    public void testJson() {
        SecretManager manager = Mockito.mock(SecretManager.class);
        Mockito.when(manager.decrypt(ArgumentMatchers.anyString())).thenAnswer(Object::toString);
        RemoteCredentialsProperties props = new RemoteCredentialsProperties();
        props.setEndpoint("https://example.com");
        props.setFormat(RemoteCredentialsProperties.OutputFormat.JSON);

        ExternalCredentialsDefinitionSource<KubernetesConfigurationProperties.ManagedAccount> src =
                new ExternalCredentialsDefinitionSource<>(manager, props) {
                    @Override
                    protected Class<KubernetesConfigurationProperties.ManagedAccount> getCredentialsClass() {
                        return KubernetesConfigurationProperties.ManagedAccount.class;
                    }

                    @Override
                    protected Reader getDefinitionReader() {
                        return new BufferedReader(new InputStreamReader(
                                ExternalCredentialsDefinitionSource.class.getResourceAsStream("/k8s-creds-test.json"),
                                Charset.defaultCharset()));
                    }
                };
        long now = System.currentTimeMillis();
        List<?> creds = src.getCredentialsDefinitions();
        long duration = System.currentTimeMillis() - now;
        System.out.println("JSON duration " + duration + "ms");
        Assertions.assertThat(creds).hasSize(1000);
    }
}
