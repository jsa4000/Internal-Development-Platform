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

import { yeomanRun } from './yeomanRun';
import fs from 'fs';
import os from 'os';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';

describe('run:yeoman', () => {

  it('should call yeomanRun to start the generator', async () => {
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), uuidv4()));
    const namespace = 'nitro:app';
    //const packageName = 'generator-nitro@6.0.9';
    const args = ['--skip-install'];
    const options = {
      name: '',
      templateEngine:  '',
      jsCompiler:  '',
      themes:  '',
      clientTpl: true,
      exampleCode: true,
      exporter: true,
      'skip-questions': true,
      'skip-install': true,
    };

    console.log(tmpDir);
   
    var data = await yeomanRun(tmpDir, namespace, undefined, args, options);

    expect(data).not.toBeNull;

    fs.rmdirSync(tmpDir, { recursive: true }); 
  });
  
});
