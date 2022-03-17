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

import { JsonObject } from '@backstage/types';

/*
export async function yeomanRun(
  workspace: string,
  namespace: string,
  args?: string[],
  opts?: JsonObject,
) {
  const yeoman = require('yeoman-environment');
  const generator = yeoman.lookupGenerator(namespace);
  const env = yeoman.createEnv(undefined, { cwd: workspace });
  env.register(generator, namespace);
  const yeomanArgs = [namespace, ...(args ?? [])];
  await env.run(yeomanArgs, opts);
}
*/

export async function yeomanRun(
  workspace: string,
  namespace: string,
  args?: string[],
  opts?: JsonObject,
) {
  const { spawn } = require('child_process');

  args = (args ?? []);
  for (const [key, value] of Object.entries({...opts})) {
    args.push(`--${key}=${value}`);
  }
  
  const child = spawn('npx', ['yo', namespace, ...args], { cwd: workspace});
  let data = "";
  for await (const chunk of child.stdout) {
      console.log('stdout chunk: '+ chunk);
      data += chunk;
  }
  let error = "";
  for await (const chunk of child.stderr) {
      console.error('stderr chunk: '+ chunk);
      error += chunk;
  }
  const exitCode = await new Promise((resolve, _) => {
      child.on('close', resolve);
  });

  if(exitCode) {
      throw new Error(`subprocess error exit ${exitCode}, ${error}`);
  }
  return data;  
}