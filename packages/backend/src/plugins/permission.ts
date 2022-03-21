/*
 * Copyright 2021 The Backstage Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { 
  IdentityClient,
  BackstageIdentityResponse,
} from '@backstage/plugin-auth-node';
import { createRouter } from '@backstage/plugin-permission-backend';
import { AuthorizeResult } from '@backstage/plugin-permission-common';
import {
  PermissionPolicy,
  PolicyAuthorizeQuery,
  PolicyDecision,
} from '@backstage/plugin-permission-node';
import { Router } from 'express';
import { PluginEnvironment } from '../types';
import {
  catalogConditions,
  createCatalogPolicyDecision,
} from '@backstage/plugin-catalog-backend/alpha';

class AllowAllPermissionPolicy implements PermissionPolicy {
  async handle(
    request: PolicyAuthorizeQuery, 
    user?: BackstageIdentityResponse
  )
  : Promise<PolicyDecision> {
    //console.info("********************************************************************************************");
    //console.info(request);
    //console.info(user);
    //console.info("********************************************************************************************");
    //if (request.permission.name.includes('read')) {
    //  return {result: AuthorizeResult.ALLOW };
    //}
    if (request.permission.resourceType == 'catalog-entity') {
      return createCatalogPolicyDecision(
          catalogConditions.isEntityOwner(
            user?.identity.ownershipEntityRefs ?? [],
          ),
      );
    }
    return {result: AuthorizeResult.DENY };
  }
}

export default async function createPlugin(
  env: PluginEnvironment,
): Promise<Router> {
  return await createRouter({
    config: env.config,
    logger: env.logger,
    discovery: env.discovery,
    policy: new AllowAllPermissionPolicy(),
    identity: IdentityClient.create({
      discovery: env.discovery,
      issuer: await env.discovery.getExternalBaseUrl('auth'),
    }),
  });
}

