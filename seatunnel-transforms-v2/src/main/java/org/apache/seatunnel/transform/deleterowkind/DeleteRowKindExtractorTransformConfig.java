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
 */

package org.apache.seatunnel.transform.deleterowkind;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.type.RowKind;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DeleteRowKindExtractorTransformConfig implements Serializable {

    public static final String PLUGIN_NAME = "DeleteRowKindExtractor";

    private String fieldName;

    private Map<String, Object> dataValueMap =
            new HashMap<String, Object>() {
                {
                    put(RowKind.DELETE.name(), 1);
                    put(RowKind.UPDATE_AFTER.name(), 4);
                    put(RowKind.INSERT.name(), 2);
                }
            };;

    public static final Option<String> CUSTOM_FIELD_NAME =
            Options.key("custom_field_name")
                    .stringType()
                    .defaultValue("isdeleted")
                    .withDescription("Custom field name of the RowKind field");

    public static final Option<Map<String, Object>> CUSTOM_FIELD_VALUE =
            Options.key("custom_field_value")
                    .mapObjectType()
                    .noDefaultValue()
                    .withDescription("Custom field value of the RowKind field");

    public static DeleteRowKindExtractorTransformConfig of(ReadonlyConfig config) {
        DeleteRowKindExtractorTransformConfig fieldMapperTransformConfig =
                new DeleteRowKindExtractorTransformConfig();
        if (null != config.get(CUSTOM_FIELD_VALUE)) {
            fieldMapperTransformConfig.setDataValueMap(config.get(CUSTOM_FIELD_VALUE));
        }
        fieldMapperTransformConfig.setFieldName(config.get(CUSTOM_FIELD_NAME));
        return fieldMapperTransformConfig;
    }
}
