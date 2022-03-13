---
id: configuration
title: Configuring Kubernetes integration
sidebar_label: Configuration
# prettier-ignore
description: Configuring the Kubernetes integration for Backstage expose your entity's objects
---

Configuring the Backstage Kubernetes integration involves two steps:

1. Enabling the backend to collect objects from your Kubernetes cluster(s).
2. Surfacing your Kubernetes objects in catalog entities

## Configuring Kubernetes Clusters

The following is a full example entry in `app-config.yaml`:

```yaml
kubernetes:
  serviceLocatorMethod:
    type: 'multiTenant'
  clusterLocatorMethods:
    - type: 'config'
      clusters:
        - url: http://127.0.0.1:9999
          name: minikube
          authProvider: 'serviceAccount'
          skipTLSVerify: false
          skipMetricsLookup: true
          serviceAccountToken: ${K8S_MINIKUBE_TOKEN}
          dashboardUrl: http://127.0.0.1:64713 # url copied from running the command: minikube service kubernetes-dashboard -n kubernetes-dashboard
          dashboardApp: standard
          caData: ${K8S_CONFIG_CA_DATA}
        - url: http://127.0.0.2:9999
          name: aws-cluster-1
          authProvider: 'aws'
    - type: 'gke'
      projectId: 'gke-clusters'
      region: 'europe-west1'
      skipTLSVerify: true
      skipMetricsLookup: true
      exposeDashboard: true
```

### `serviceLocatorMethod`

This configures how to determine which clusters a component is running in.

Currently, the only valid value is:

- `multiTenant` - This configuration assumes that all components run on all the
  provided clusters.

### `clusterLocatorMethods`

This is an array used to determine where to retrieve cluster configuration from.

Valid cluster locator methods are:

#### `config`

This cluster locator method will read cluster information from your app-config
(see below).

##### `clusters`

Used by the `config` cluster locator method to construct Kubernetes clients.

##### `clusters.\*.url`

The base URL to the Kubernetes control plane. Can be found by using the
"Kubernetes master" result from running the `kubectl cluster-info` command.

##### `clusters.\*.name`

A name to represent this cluster, this must be unique within the `clusters`
array. Users will see this value in the Software Catalog Kubernetes plugin.

##### `clusters.\*.authProvider`

This determines how the Kubernetes client authenticates with the Kubernetes
cluster. Valid values are:

| Value                  | Description                                                                                                                                                                                                                       |
| ---------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `serviceAccount`       | This will use a Kubernetes [service account](https://kubernetes.io/docs/reference/access-authn-authz/service-accounts-admin/) to access the Kubernetes API. When this is used the `serviceAccountToken` field should also be set. |
| `google`               | This will use a user's Google auth token from the [Google auth plugin](https://backstage.io/docs/auth/) to access the Kubernetes API.                                                                                             |
| `aws`                  | This will use AWS credentials to access resources in EKS clusters                                                                                                                                                                 |
| `googleServiceAccount` | This will use the Google Cloud service account credentials to access resources in clusters                                                                                                                                        |

##### `clusters.\*.skipTLSVerify`

This determines whether the Kubernetes client verifies the TLS certificate
presented by the API server. Defaults to `false`.

##### `clusters.\*.skipMetricsLookup`

This determines whether the Kubernetes client looks up resource metrics
CPU/Memory for pods returned by the API server. Defaults to `false`.

##### `clusters.\*.serviceAccountToken` (optional)

The service account token to be used when using the `serviceAccount` auth
provider. You could get the service account token with:

```sh
kubectl -n <NAMESPACE> get secret $(kubectl -n <NAMESPACE> get sa <SERVICE_ACCOUNT_NAME> -o=json \
| jq -r '.secrets[0].name') -o=json \
| jq -r '.data["token"]' \
| base64 --decode
```

##### `clusters.\*.dashboardUrl` (optional)

Specifies the link to the Kubernetes dashboard managing this cluster.

Note that you should specify the app used for the dashboard using the
`dashboardApp` property, in order to properly format links to kubernetes
resources, otherwise it will assume that you're running the standard one.

Note also that this attribute is optional for some kinds of dashboards, such as
GKE, which requires additional parameters specified in the `dashboardParameters`
option.

##### `clusters.\*.dashboardApp` (optional)

Specifies the app that provides the Kubernetes dashboard.

This will be used for formatting links to kubernetes objects inside the
dashboard.

The supported dashboards are: `standard`, `rancher`, `openshift`, `gke`, `aks`,
`eks`. However, not all of them are implemented yet, so please contribute!

Note that it will default to the regular dashboard provided by the Kubernetes
project (`standard`), that can run in any Kubernetes cluster.

Note that for the `gke` app, you must provide additional information in the
`dashboardParameters` option.

Note that you can add your own formatter by registering it to the
`clusterLinksFormatters` dictionary, in the app project.

Example:

```ts
import { clusterLinksFormatters } from '@backstage/plugin-kubernetes';
clusterLinksFormatters.myDashboard = (options) => ...;
```

See also
https://github.com/backstage/backstage/tree/master/plugins/kubernetes/src/utils/clusterLinks/formatters
for real examples.

##### `clusters.\*.dashboardParameters` (optional)

Specifies additional information for the selected `dashboardApp` formatter.

Note that, even though `dashboardParameters` is optional, it might be mandatory
for some dashboards, such as GKE.

###### required parameters for GKE

| Name        | Description                                                              |
| ----------- | ------------------------------------------------------------------------ |
| projectId   | the ID of the GCP project containing your Kubernetes clusters            |
| region      | the region of GCP containing your Kubernetes clusters                    |
| clusterName | the name of your kubernetes cluster, within your `projectId` GCP project |

Note that the GKE cluster locator can automatically provide the values for the
`dashboardApp` and `dashboardParameters` options if you set the
`exposeDashboard` property to `true`.

Example:

```yaml
kubernetes:
  serviceLocatorMethod:
    type: 'multiTenant'
  clusterLocatorMethods:
    - type: 'config'
      clusters:
        - url: http://127.0.0.1:9999
          name: my-cluster
          dashboardApp: gke
          dashboardParameters:
            projectId: my-project
            region: us-east1
            clusterName: my-cluster
```

##### `clusters.\*.caData` (optional)

PEM-encoded certificate authority certificates.

This values could be obtained via inspecting the Kubernetes config file (usually
at `~/.kube/config`) under `clusters.cluster.certificate-authority-data`. For
GKE, execute the following command to obtain the value

```
gcloud container clusters describe <YOUR_CLUSTER_NAME> \
    --zone=<YOUR_COMPUTE_ZONE> \
    --format="value(masterAuth.clusterCaCertificate)"
```

See also
https://cloud.google.com/kubernetes-engine/docs/how-to/api-server-authentication#environments-without-gcloud
for complete docs about GKE without `gcloud`.

#### `gke`

This cluster locator is designed to work with Kubernetes clusters running in
[GKE][1]. It will configure the Kubernetes backend plugin to make requests to
clusters running within a Google Cloud project.

This cluster locator method will use the `google` authentication mechanism.

The Google Cloud service account to use can be configured through the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable. Consult the [Google Cloud
docs][2] for more information.

For example:

```yaml
- type: 'gke'
  projectId: 'gke-clusters'
  region: 'europe-west1'
```

Will configure the Kubernetes plugin to connect to all GKE clusters in the
project `gke-clusters` in the region `europe-west1`.

Note that the GKE cluster locator can automatically provide the values for the
`dashboardApp` and `dashboardParameters` options if you enable the
`exposeDashboard` option.

##### `projectId`

The Google Cloud project to look for Kubernetes clusters in.

##### `region` (optional)

The Google Cloud region to look for Kubernetes clusters in. Defaults to all
regions.

##### `skipTLSVerify`

This determines whether the Kubernetes client verifies the TLS certificate
presented by the API server. Defaults to `false`.

##### `skipMetricsLookup`

This determines whether the Kubernetes client looks up resource metrics
CPU/Memory for pods returned by the API server. Defaults to `false`.

##### `exposeDashboard`

This determines wether the `dashboardApp` and `dashboardParameters` should be
automatically configured in order to expose the GKE dashboard from the
Kubernetes plugin.

Defaults to `false`.

### `customResources` (optional)

Configures which [custom resources][3] to look for when returning an entity's
Kubernetes resources.

Defaults to empty array. Example:

```yaml
---
kubernetes:
  customResources:
    - group: 'argoproj.io'
      apiVersion: 'v1alpha1'
      plural: 'rollouts'
```

#### `customResources.\*.group`

The custom resource's group.

#### `customResources.\*.apiVersion`

The custom resource's apiVersion.

#### `customResources.\*.plural`

The plural representing the custom resource.

### `apiVersionOverrides` (optional)

Overrides for the API versions used to make requests for the corresponding
objects. If using a legacy Kubernetes version, you may use this config to
override the default API versions to ones that are supported by your cluster.

Example:

```yaml
---
kubernetes:
  apiVersionOverrides:
    cronjobs: 'v1beta1'
```

For more information on which API versions are supported by your cluster, please
view the Kubernetes API docs for your Kubernetes version (e.g.
[API Groups for v1.22](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.22/#-strong-api-groups-strong-)
)

### Role Based Access Control

The current RBAC permissions required are read-only cluster wide, for the
following objects:

- pods
- services
- configmaps
- deployments
- replicasets
- horizontalpodautoscalers
- ingresses

The following RBAC permissions are required on the batch API group for the
following objects:

- jobs
- cronjobs

## Surfacing your Kubernetes components as part of an entity

There are two ways to surface your Kubernetes components as part of an entity.
The label selector takes precedence over the annotation/service id.

### Common `backstage.io/kubernetes-id` label

#### Adding the entity annotation

In order for Backstage to detect that an entity has Kubernetes components, the
following annotation should be added to the entity's `catalog-info.yaml`:

```yaml
annotations:
  'backstage.io/kubernetes-id': dice-roller
```

#### Labeling Kubernetes components

In order for Kubernetes components to show up in the software catalog as a part
of an entity, Kubernetes components themselves can have the following label:

```yaml
'backstage.io/kubernetes-id': <BACKSTAGE_ENTITY_NAME>
```

### Label selector query annotation

You can write your own custom label selector query that Backstage will use to
lookup the objects (similar to `kubectl --selector="your query here"`). Review
the
[labels and selectors Kubernetes documentation](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)
for more info.

```yaml
'backstage.io/kubernetes-label-selector': 'app=my-app,component=front-end'
```

[1]: https://cloud.google.com/kubernetes-engine
[2]: https://cloud.google.com/docs/authentication/production#linux-or-macos
[3]: https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/
