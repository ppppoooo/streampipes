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

import { Directive, Input, OnChanges, SimpleChanges } from '@angular/core';
import {
  DataExplorerWidgetModel,
  EventPropertyPrimitive,
  EventPropertyUnion,
  EventSchema
} from '../../../../core-model/gen/streampipes-model';
import { WidgetConfigurationService } from '../../../services/widget-configuration.service';
import {
  DataExplorerField,
  FieldProvider,
  SourceConfig
} from '../../../models/dataview-dashboard.model';
import { DataExplorerFieldProviderService } from '../../../services/data-explorer-field-provider-service';

@Directive()
// tslint:disable-next-line:directive-class-suffix
export abstract class BaseWidgetConfig<T extends DataExplorerWidgetModel> implements OnChanges {

  @Input() currentlyConfiguredWidget: T;

  fieldProvider: FieldProvider;

  constructor(protected widgetConfigurationService: WidgetConfigurationService,
              protected fieldService: DataExplorerFieldProviderService) { }

  onInit() {
    this.makeFields();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.makeFields();
    if (changes.currentlyConfiguredWidget) {
      this.updateWidgetConfigOptions();
    }
  }

  makeFields() {
    const sourceConfigs: SourceConfig[] = this.currentlyConfiguredWidget.dataConfig.sourceConfigs;
    this.fieldProvider = this.fieldService.generateFieldLists(sourceConfigs);
  }

  triggerDataRefresh() {
    this.widgetConfigurationService.notify({
      widgetId: this.currentlyConfiguredWidget._id,
      refreshData: true,
      refreshView: false
    });
  }

  triggerViewRefresh() {
    this.widgetConfigurationService.notify({
      widgetId: this.currentlyConfiguredWidget._id,
      refreshData: false,
      refreshView: true
    });
  }


  getValuePropertyKeys(eventSchema: EventSchema) {
    const propertyKeys: EventPropertyUnion[] = [];

    eventSchema.eventProperties.forEach(p => {
      if (!(p.domainProperties.some(dp => dp === 'http://schema.org/DateTime'))) {
        propertyKeys.push(p);
      }
    });

    return propertyKeys;
  }

  getDimensionProperties(eventSchema: EventSchema) {
    const result: EventPropertyUnion[] = [];
    eventSchema.eventProperties.forEach(property => {
      if (this.fieldService.isDimensionProperty(property)) {
        result.push(property);
      }
    });

    return result;
  }

  getNonNumericProperties(eventSchema: EventSchema): EventPropertyUnion[] {
    const result: EventPropertyUnion[] = [];
    const b = new EventPropertyPrimitive();
    b['@class'] = 'org.apache.streampipes.model.schema.EventPropertyPrimitive';
    b.runtimeType = 'https://www.w3.org/2001/XMLSchema#string';
    b.runtimeName = '';

    result.push(b);

    eventSchema.eventProperties.forEach(p => {
      if (!(p.domainProperties.some(dp => dp === 'http://schema.org/DateTime')) &&
          !this.fieldService.isNumber(p)) {
        result.push(p);
      }
    });


    return result;
  }

  getRuntimeNames(properties: DataExplorerField[]): string[] {
    const result = [];
    properties.forEach(p => {
      result.push(p.runtimeName);
    });

    return result;
  }

  getNumericProperty(eventSchema: EventSchema) {
    const propertyKeys: EventPropertyUnion[] = [];

    eventSchema.eventProperties.forEach(p => {
      if (!(p.domainProperties.some(dp => dp === 'http://schema.org/DateTime')) &&
          this.fieldService.isNumber(p)) {
        propertyKeys.push(p);
      }
    });

    return propertyKeys;
  }

  getTimestampProperty(eventSchema: EventSchema) {
    return eventSchema.eventProperties.find(p =>
        this.fieldService.isTimestamp(p)
    );
  }

  protected abstract updateWidgetConfigOptions();

}