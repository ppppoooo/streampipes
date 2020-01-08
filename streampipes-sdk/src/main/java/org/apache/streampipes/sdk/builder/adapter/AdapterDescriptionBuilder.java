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
package org.apache.streampipes.sdk.builder.adapter;

import org.apache.streampipes.model.AdapterType;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.sdk.builder.AbstractConfigurablePipelineElementBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AdapterDescriptionBuilder<BU extends
        AdapterDescriptionBuilder<BU, T>, T extends AdapterDescription> extends
        AbstractConfigurablePipelineElementBuilder<BU, T> {

  protected AdapterDescriptionBuilder(String id, T element) {
    super(id, element);
    this.elementDescription.setAdapterId(id);
  }

  protected AdapterDescriptionBuilder(String id, String label, String description,
                                   T adapterTypeInstance) {
    super(id, label, description, adapterTypeInstance);
    this.elementDescription.setAdapterId(id);
  }

  public AdapterDescriptionBuilder<BU, T> category(AdapterType... categories) {
    this.elementDescription
            .setCategory(Arrays
                    .stream(categories)
                    .map(Enum::name)
                    .collect(Collectors.toList()));
    return me();
  }
}
