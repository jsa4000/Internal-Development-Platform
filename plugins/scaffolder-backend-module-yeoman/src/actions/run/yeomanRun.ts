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

import { ChildProcess, spawn } from 'child_process';
import { JsonObject } from '@backstage/types';

/**
 * Get options from JsonObject and convert them into an array. i.e. ['--key1=value1', '--key2=value2', ...]
 * 
 * @param opts 
 * @returns 
 */
function parseOptions(opts?: JsonObject) {
  let result = [];
  for (const [key, value] of Object.entries({...opts})) {
    if (!value) continue;
    result.push(`--${key}=${value}`);
  }
  return result;
}

/**
 * Get outputs from a child process
 * 
 * @param child 
 * @returns 
 */
async function getOutputs(child: ChildProcess) {
  let data = '';
  for await (const chunk of child?.stdout!) {
      //console.log('stdout chunk: '+ chunk);
      data += chunk;
  }
  let error = '';
  for await (const chunk of child?.stderr!) {
      //console.error('stderr chunk: '+ chunk);
      error += chunk;
  }
  return [data, error];
}

/**
 * Run Yeoman Generator using provided parameters
 * 
 * @param workspace 
 * @param namespace 
 * @param packageName 
 * @param args 
 * @param opts 
 * @returns 
 */
export async function yeomanRun(
  workspace: string,
  namespace: string,
  packageName?: string,
  args?: string[],
  opts?: JsonObject,
) {
  var cmd = [
    '--yes',
    '--',
    'yo',
    namespace,
    ...(args ?? []).concat(parseOptions(opts))
  ];
  if (packageName) {
    cmd = [
      '--package' ,
      packageName,
      ...cmd
    ];
  } 
  const child = spawn('npx', cmd, { cwd: workspace});
  const [data, error] = await getOutputs(child);
  const exitCode = await new Promise((resolve, _) => {
      child.on('close', resolve);
  });

  if(exitCode) {
      throw new Error(`subprocess error exit ${exitCode}, ${error}`);
  }
  return data;  
  
}