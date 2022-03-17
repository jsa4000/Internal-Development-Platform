import { CatalogClient } from '@backstage/catalog-client';
import { createRouter, createBuiltinActions } from '@backstage/plugin-scaffolder-backend';
import { createRunYeomanAction } from 'plugin-scaffolder-backend-module-yeoman';
import { createZipAction, createWriteFileAction, createAppendFileAction, createSleepAction } from '@roadiehq/scaffolder-backend-module-utils';
import { createHttpBackstageAction } from '@roadiehq/scaffolder-backend-module-http-request';
import { ScmIntegrations } from '@backstage/integration';
import { Router } from 'express';
import type { PluginEnvironment } from '../types';

export default async function createPlugin(
  env: PluginEnvironment,
): Promise<Router> {
  const catalogClient = new CatalogClient({
    discoveryApi: env.discovery,
  });
  const integrations = ScmIntegrations.fromConfig(env.config);
  const actions = [
    createRunYeomanAction(),
    createWriteFileAction(),
    createSleepAction(),
    createZipAction(),
    createAppendFileAction(),
    createHttpBackstageAction({ config: env.config }),
    ...createBuiltinActions({
    integrations,
    config: env.config,
    catalogClient,
    reader: env.reader,
  }),
  ];
  return await createRouter({
    logger: env.logger,
    config: env.config,
    database: env.database,
    reader: env.reader,
    catalogClient,
    actions,
  });
}
