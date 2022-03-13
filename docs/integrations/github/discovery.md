---
id: discovery
title: GitHub Discovery
sidebar_label: Discovery
# prettier-ignore
description: Automatically discovering catalog entities from repositories in a GitHub organization
---

The GitHub integration has a special discovery processor for discovering catalog
entities within a GitHub organization. The processor will crawl the GitHub
organization and register entities matching the configured path. This can be
useful as an alternative to static locations or manually adding things to the
catalog.

## Installation

You will have to add the processors in the catalog initialization code of your
backend. They are not installed by default, therefore you have to add a
dependency to `@backstage/plugin-catalog-backend-module-github` to your backend
package.

```bash
# From your Backstage root directory
cd packages/backend
yarn add @backstage/plugin-catalog-backend-module-github
```

And then add the processors to your catalog builder:

```diff
// In packages/backend/src/plugins/catalog.ts
+import {
+  GithubDiscoveryProcessor,
+  GithubOrgReaderProcessor,
+} from '@backstage/plugin-catalog-backend-module-github';
+import {
+  ScmIntegrations,
+  DefaultGithubCredentialsProvider
+} from '@backstage/integration';

 export default async function createPlugin(
   env: PluginEnvironment,
 ): Promise<Router> {
   const builder = await CatalogBuilder.create(env);
+  const integrations = ScmIntegrations.fromConfig(config);
+  const githubCredentialsProvider =
+    DefaultGithubCredentialsProvider.fromIntegrations(integrations);
+  builder.addProcessor(
+    GithubDiscoveryProcessor.fromConfig(config, {
+      logger,
+      githubCredentialsProvider,
+    }),
+    GithubOrgReaderProcessor.fromConfig(config, {
+      logger,
+      githubCredentialsProvider,
+    }),
+  );
```

## Configuration

To use the discovery processor, you'll need a GitHub integration
[set up](locations.md) with a `GITHUB_TOKEN`. Then you can add a location target
to the catalog configuration:

```yaml
catalog:
  locations:
    # (since 0.13.5) Scan all repositories for a catalog-info.yaml in the root of the default branch
    - type: github-discovery
      target: https://github.com/myorg
    # Or use a custom pattern for a subset of all repositories with default repository
    - type: github-discovery
      target: https://github.com/myorg/service-*/blob/-/catalog-info.yaml
    # Or use a custom file format and location
    - type: github-discovery
      target: https://github.com/*/blob/-/docs/your-own-format.yaml
    # Or use a specific branch-name
    - type: github-discovery
      target: https://github.com/*/blob/backstage-docs/catalog-info.yaml
```

Note the `github-discovery` type, as this is not a regular `url` processor.

When using a custom pattern, the target is composed of three parts:

- The base organization URL, `https://github.com/myorg` in this case
- The repository blob to scan, which accepts \* wildcard tokens. This can simply
  be `*` to scan all repositories in the organization. This example only looks
  for repositories prefixed with `service-`.
- The path within each repository to find the catalog YAML file. This will
  usually be `/blob/main/catalog-info.yaml`, `/blob/master/catalog-info.yaml` or
  a similar variation for catalog files stored in the root directory of each
  repository. You could also use a dash (`-`) for referring to the default
  branch.

## GitHub API Rate Limits

GitHub [rate limits] API requests to 5,000 per hour (or more for Enterprise
accounts). The default Backstage catalog backend refreshes data every 100
seconds, which issues an API request for each discovered location.

This means if you have more than ~140 catalog entities, you may get throttled by
rate limiting. This will soon be resolved once catalog refreshes make use of
ETags; to work around this in the meantime, you can change the refresh rate of
the catalog in your `packages/backend/src/plugins/catalog.ts` file:

```typescript
const builder = await CatalogBuilder.create(env);

// For example, to refresh every 5 minutes (300 seconds).
builder.setRefreshIntervalSeconds(300);
```

Alternatively, or additionally, you can configure [github-apps] authentication
which carries a much higher rate limit at GitHub.

This is true for any method of adding GitHub entities to the catalog, but
especially easy to hit with automatic discovery.

[rate limits]: https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting
[github-apps]: ../../plugins/github-apps.md
