# Spinnaker Remote Credentials Management Plugin

This plugin will read Spinnaker cloud provider credentials information from a third party source.
It could be a remote server or a local file (`file://`).

## Installing the plugin

### With Halyard

Add the following to `clouddriver-local.yaml`:

```yaml
spinnaker:
  extensibility:
    plugins:
      Armory.RemoteCredentials:
        enabled: true
        version: 0.1.0
    repositories:
      example-repo:
        url: https://raw.githubusercontent.com/armory/spinnaker-credentials-plugin/master/repositories.json      
```


### With Operator

Add the following to your `SpinnakerService`

```yaml
kind: SpinnakerService
spec:
  spinnakerConfig:
    profiles:
      clouddriver:
        spinnaker:
          extensibility:
            plugins:
              Armory.RemoteCredentials:
                enabled: true
                version: 0.1.0
            repositories:
              credentials-plugin:
                url: https://raw.githubusercontent.com/armory/spinnaker-credentials-plugin/master/repositories.json
```


## Configuring the plugin

To enable a different Spinnaker to read a provider from a different source, add the following to `clouddriver-local.yaml`
or to `spec.spinnakerConfig.profiles.clouddriver` (Spinnaker Operator):

```yaml
credentials:
  remote:
    [provider]: # kubernetes, cloudfoundry
      enabled: true
      endpoint: [URI to the endpoint]
      format: [format] # YAML (default), JSON
```

If you want to reload the credentials on a regular basis, you can use `credentials.poller`:

```yaml
credentials:
  poller:
    enabled: true
    types:
      [provider]: # kubernetes, cloudfoundry
        reloadFrequencyMs: 30000 # 30s
```

As of 1.23, provider can be one of `kubernetes` and `cloudfoundry`. More providers are coming!

## Loading Kubernetes account in parallel
If `credentials.loader.kubernetes.parallel: true` the default credentials loader will be changed to add accounts
in a parallel stream. There are tradeoffs to using parallel streams in the JVM so read it first. We expect boot time
improvements when adding many accounts.

## Extending this plugin

If the server you're connecting to doesn't support a simple GET, you can extend `ExternalCredentialsDefinitionSource` 
and implement the protocol of your choice.

If you think your use case is common, please submit a PR.


