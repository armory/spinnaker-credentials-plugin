/*
 * Copyright 2020 Armory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.armory.plugin.credentials;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinition;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@ExposeToApp
public abstract class ExternalCredentialsDefinitionSource<T extends CredentialsDefinition>
    implements CredentialsDefinitionSource<T> {
  protected final RemoteCredentialsProperties remoteCredentialsProperties;
  private final SecretManager secretManager;
  private ObjectMapper mapper;
  private URL url;
  private final Yaml yaml = new Yaml(new SafeConstructor());

  public ExternalCredentialsDefinitionSource(
          SecretManager secretManager,
      RemoteCredentialsProperties remoteCredentialsProperties) {
    this.remoteCredentialsProperties = remoteCredentialsProperties;
    try {
      this.url = new URL(remoteCredentialsProperties.getEndpoint());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Incorrect URL set for external credentials source:" +
              remoteCredentialsProperties.getEndpoint(), e);
    }
    this.secretManager = secretManager;
    this.initializeMapper();
  }

  protected void initializeMapper() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    // We decrypt strings in case they contain encrypted secrets
    module.addDeserializer(String.class, new JsonDeserializer<>() {
      @Override
      public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return secretManager.decrypt(p.getText());
      }
    });
    mapper.registerModule(module);
  }

  protected Reader getDefinitionReader() throws IOException {
    return new BufferedReader(new InputStreamReader(url.openStream(), Charset.defaultCharset()));
  }

  @Override
  public @NotNull List<T> getCredentialsDefinitions() {
    try (Reader reader = getDefinitionReader()) {
      return mapper.convertValue(yaml.load(reader), getType());
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read credential information", e);
    }
  }

  /**
   * @return Class of credentials parsed
   */
  protected abstract Class<T> getCredentialsClass();

  private CollectionType getType() {
    return mapper.getTypeFactory().
            constructCollectionType(List.class, getCredentialsClass());
  }
}
