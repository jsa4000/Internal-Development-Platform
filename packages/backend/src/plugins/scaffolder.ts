import { CatalogClient } from '@backstage/catalog-client';
import { createRouter, createBuiltinActions } from '@backstage/plugin-scaffolder-backend';
import { createRunYeomanAction } from '@backstage/plugin-scaffolder-backend-module-yeoman';
import { ScmIntegrations } from '@backstage/integration';
import { Router } from 'express';
import type { PluginEnvironment } from '../types';

export default async function createPlugin({
  logger,
  config,
  database,
  reader,
  discovery,
}: PluginEnvironment): Promise<Router> {
  const catalogClient = new CatalogClient({ discoveryApi: discovery });
  const integrations = ScmIntegrations.fromConfig(config);
  const actions = [
    createRunYeomanAction(),
    ...createBuiltinActions({
      integrations,
      config,
      catalogClient,
      reader,
    }),
  ];
  return await createRouter({
    logger,
    config,
    database,
    catalogClient,
    reader,
    actions,
  });
}
