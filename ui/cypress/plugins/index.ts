/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// ***********************************************************
// This example plugins/index.js can be used to load plugins
//
// You can change the location of this file or turn off loading
// the plugins file with the 'pluginsFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/plugins-guide
// ***********************************************************
// This function is called when a project is opened or re-opened (e.g. due to
// the project's config changing)
import * as fs from 'fs';
import { ProcessorTest } from '../support/model/ProcessorTest';

function readProcessingElements(): ProcessorTest[] {
  const result: ProcessorTest[] = [];

  const allPipelineTests = fs.readdirSync('cypress/fixtures/pipelineElement');

  allPipelineTests.forEach(test => {
    const testDescription = fs.readFileSync('cypress/fixtures/pipelineElement/' + test + '/description.json');
    // @ts-ignore
    const pt = new ProcessorTest();
    pt.name = test;
    // @ts-ignore
    pt.processor = JSON.parse(testDescription);

    result.push(pt);
  });

  return result;
}

module.exports = (on, config) => {

  config.env.processingElements = readProcessingElements();

  return config;
};

