---
id: discovery
title: AWS S3 Discovery
sidebar_label: Discovery
# prettier-ignore
description: Automatically discovering catalog entities from an AWS S3 Bucket
---

The AWS S3 integration has a special discovery processor for discovering catalog
entities located in an S3 Bucket. If you have a bucket that contains multiple
catalog-info files and want to automatically discover them, you can use this
processor. The processor will crawl your S3 bucket and register entities
matching the configured path. This can be useful as an alternative to static
locations or manually adding things to the catalog.

To use the discovery processor, you'll need an AWS S3 integration
[set up](locations.md) with an `AWS_ACCESS_KEY`, `AWS_SECRET_ACCESS_KEY`, and
optionally a `roleArn`. Then you can add a location target to the catalog
configuration:

```yaml
catalog:
  locations:
    - type: s3-discovery
      target: https://sample-bucket.s3.us-east-2.amazonaws.com/
```

Note the `s3-discovery` type, as this is not a regular `url` processor.

As this processor is not one of the default providers, you will first need to install the AWS catalog plugin:

```shell
cd packages/backend
yarn install @backstage/plugin-catalog-backend-module-aws
```

Once you've done that, you'll also need to add the segment below to `packages/backend/src/plugins/catalog.ts`:

```ts
/* packages/backend/src/plugins/catalog.ts */

import { AwsS3DiscoveryProcessor } from '@backstage/plugin-catalog-backend-module-aws';

const builder = await CatalogBuilder.create(env);
/** ... other processors ... */
builder.addProcessor(new AwsS3DiscoveryProcessor(env.reader));
```
