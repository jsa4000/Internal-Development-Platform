---
id: search-engines
title: Search Engines
description: Choosing and configuring your search engine for Backstage
---

Backstage supports 3 search engines by default, an in-memory engine called Lunr,
ElasticSearch and Postgres. You can configure your own search engines by
implementing the provided interface as mentioned in the
[search backend documentation.](./getting-started.md#Backend)

Provided search engine implementations have their own way of constructing
queries, which may be something you want to modify. Alterations to the querying
logic of a search engine can be made by providing your own implementation of a
QueryTranslator interface. This modification can be done without touching
provided search engines by using the exposed setter to set the modified query
translator into the instance.

```typescript
const searchEngine = new LunrSearchEngine({ logger });
searchEngine.setTranslator(new MyNewAndBetterQueryTranslator());
```

## Lunr

Lunr search engine is enabled by default for your backstage instance if you have
not done additional changes to the scaffolded app.

Lunr can be instantiated like this:

```typescript
// app/backend/src/plugins/search.ts
const searchEngine = new LunrSearchEngine({ logger });
const indexBuilder = new IndexBuilder({ logger, searchEngine });
```

## Postgres

The Postgres based search engine only requires that postgres being configured as
the database engine for Backstage. Therefore it targets setups that want to
avoid maintaining another external service like elastic search. The search
provides decent results and performs well with ten thousands of indexed
documents. The connection to postgres is established via the database manager
also used by other plugins.

> **Important**: The search plugin requires at least Postgres 12!

To use the `PgSearchEngine`, make sure that you have a Postgres database
configured and make the following changes to your backend:

1. Add a dependency on `@backstage/plugin-search-backend-module-pg` to your
   backend's `package.json`.
2. Initialize the search engine. It is recommended to initialize it with a
   fallback to the lunr search engine if you are running Backstage for
   development locally with SQLite:

```typescript
// In packages/backend/src/plugins/search.ts

// Initialize a connection to a search engine.
const searchEngine = (await PgSearchEngine.supported(database))
  ? await PgSearchEngine.from({ database })
  : new LunrSearchEngine({ logger });
```

## ElasticSearch

Backstage supports ElasticSearch search engine connections, indexing and
querying out of the box. Available configuration options enable usage of either
AWS or Elastic.co hosted solutions, or a custom self-hosted solution.

Similarly to Lunr above, ElasticSearch can be set up like this:

```typescript
// app/backend/src/plugins/search.ts
const searchEngine = await ElasticSearchSearchEngine.initialize({
  logger,
  config,
});
const indexBuilder = new IndexBuilder({ logger, searchEngine });
```

For the engine to be available, your backend package needs a dependency into
package `@backstage/plugin-search-backend-module-elasticsearch`.

ElasticSearch needs some additional configuration before it is ready to use
within your instance. The configuration options are documented in the
[configuration schema definition file.](https://github.com/backstage/backstage/blob/master/plugins/search-backend-module-elasticsearch/config.d.ts)

The underlying functionality is using official ElasticSearch client version 7.x,
meaning that ElasticSearch version 7 is the only one confirmed to be supported.

Should you need to create your own bespoke search experiences that require more
than just a query translator (such as faceted search or Relay pagination), you
can access the configuration of the search engine in order to create new elastic
search clients. The version of the client need not be the same as one used
internally by the elastic search engine plugin. For example:

```typescript
import { Client } from '@elastic/elastic-search';

const client = searchEngine.newClient(options => new Client(options));
```

## Example configurations

### AWS

Using AWS hosted ElasticSearch the only configuration option needed is the URL
to the ElasticSearch service. The implementation assumes that environment
variables for AWS access key id and secret access key are defined in accordance
to the
[default AWS credential chain.](https://docs.aws.amazon.com/sdk-for-javascript/v2/developer-guide/setting-credentials-node.html).

```yaml
search:
  elasticsearch:
    provider: aws
    node: https://my-backstage-search-asdfqwerty.eu-west-1.es.amazonaws.com
```

### Elastic.co

Elastic Cloud hosted ElasticSearch uses a Cloud ID to determine the instance of
hosted ElasticSearch to connect to. Additionally, username and password needs to
be provided either directly or using environment variables like defined in
[Backstage documentation.](https://backstage.io/docs/conf/writing#includes-and-dynamic-data)

```yaml
search:
  elasticsearch:
    provider: elastic
    cloudId: backstage-elastic:asdfqwertyasdfqwertyasdfqwertyasdfqwerty==
    auth:
      username: elastic
      password: changeme
```

### Others

Other ElasticSearch instances can be connected to by using standard
ElasticSearch authentication methods and exposed URL, provided that the cluster
supports that. The configuration options needed are the URL to the node and
authentication information. Authentication can be handled by either providing
username/password or an API key. For more information how to create an API key,
see
[Elastic documentation on API keys](https://www.elastic.co/guide/en/elasticsearch/reference/current/security-api-create-api-key.html).

#### Configuration examples

##### With username and password

```yaml
search:
  elasticsearch:
    node: http://localhost:9200
    auth:
      username: elastic
      password: changeme
```

##### With API key

```yaml
search:
  elasticsearch:
    node: http://localhost:9200
    auth:
      apiKey: base64EncodedKey
```
