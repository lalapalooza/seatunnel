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

import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.RowKind;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowAccessor;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.transform.common.SingleFieldOutputTransform;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class DeleteRowKindExtractorTransform extends SingleFieldOutputTransform {

    private final DeleteRowKindExtractorTransformConfig config;

    public DeleteRowKindExtractorTransform(
            @NonNull DeleteRowKindExtractorTransformConfig config,
            @NonNull CatalogTable inputCatalogTable) {
        super(inputCatalogTable);
        this.config = config;
    }

    @Override
    public String getPluginName() {
        return DeleteRowKindExtractorTransformConfig.PLUGIN_NAME;
    }

    @Override
    protected SeaTunnelRow transformRow(SeaTunnelRow inputRow) {
        Object fieldValue = getOutputFieldValue(new SeaTunnelRowAccessor(inputRow));
        if (RowKind.DELETE.equals(inputRow.getRowKind())) {
            inputRow.setRowKind(RowKind.UPDATE_AFTER);
        }

        SeaTunnelRow outputRow = getRowContainerGenerator().apply(inputRow);
        outputRow.setField(getFieldIndex(), fieldValue);
        return outputRow;
    }

    @Override
    protected Object getOutputFieldValue(SeaTunnelRowAccessor inputRow) {
        final Map<String, Integer> map =
                config.getDataValueMap().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> Integer.parseInt(String.valueOf(entry.getValue())),
                                        (existing, replacement) -> replacement));
        // Map<String, Integer> map =
        // config.get(DeleteRowKindExtractorTransformConfig.CUSTOM_FIELD_VALUE);
        return Optional.ofNullable(map.get(inputRow.getRowKind().name()))
                .orElse(map.get(RowKind.INSERT.name()));
    }

    @Override
    protected Column getOutputColumn() {
        String customFieldName = config.getFieldName();
        String[] fieldNames = inputCatalogTable.getTableSchema().getFieldNames();
        boolean isExist = Arrays.asList(fieldNames).contains(customFieldName);
        if (isExist) {
            throw new IllegalArgumentException(
                    String.format("field name %s already exists", customFieldName));
        }
        return PhysicalColumn.of(
                customFieldName, BasicType.INT_TYPE, 2, true, 0, "the row kind flag");
    }
}
