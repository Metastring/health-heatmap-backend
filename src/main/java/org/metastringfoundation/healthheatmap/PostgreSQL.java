/*
 *    Copyright 2020 Metastring Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.metastringfoundation.healthheatmap;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.jooq.tools.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;
import org.metastringfoundation.healthheatmap.codegen.*;
import org.metastringfoundation.healthheatmap.codegen.tables.DatasetMetadata;
import org.metastringfoundation.healthheatmap.codegen.tables.Entities;
import org.metastringfoundation.healthheatmap.codegen.tables.Indicators;

import static org.jooq.impl.DSL.constraint;
import static org.metastringfoundation.healthheatmap.codegen.tables.DatasetMetadata.DATASET_METADATA;
import static org.metastringfoundation.healthheatmap.codegen.tables.Entities.ENTITIES;
import static org.metastringfoundation.healthheatmap.codegen.tables.Indicators.*;

public class PostgreSQL implements Database {

    private Connection psqlConnection;
    private DSLContext dslContext;

    PostgreSQL() {
        //TODO: Make this loaded from configuration/environment
        String username = "metastring";
        String password = "metastring";
        String url = "jdbc:postgresql://localhost:5432/healthheatmap";
        try {
            psqlConnection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            throw new RuntimeException();
        }

        dslContext = DSL.using(psqlConnection, SQLDialect.POSTGRES);
    }

    private void createMetaTablesIfNotExists() throws SQLException {
        java.sql.Statement psqlStatement = psqlConnection.createStatement();

        String createEntityTable = "CREATE TABLE IF NOT EXISTS entities (" +
                "id serial PRIMARY KEY," +
                "name VARCHAR(300) UNIQUE NOT NULL" +
                ");";

        psqlStatement.executeUpdate(createEntityTable);

        String createIndicatorTable = "CREATE TABLE IF NOT EXISTS indicators (" +
                "id SERIAL PRIMARY KEY," +
                "name VARCHAR(300) UNIQUE NOT NULL," +
                "formula JSON," +
                "present_in JSON" +
                ");";

        psqlStatement.executeUpdate(createIndicatorTable);

        String createDatasetTable = "CREATE TABLE IF NOT EXISTS dataset_metadata (" +
                "id serial PRIMARY KEY," +
                "name VARCHAR(300) UNIQUE NOT NULL," +
                "table_name VARCHAR(300) UNIQUE NOT NULL" +
                ");";

        psqlStatement.executeUpdate(createDatasetTable);

    }

    @Override
    public void addDataset(Dataset dataset) {
        try {
            createMetaTablesIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot run program without meta tables");
        }


        // Create a table to hold the data of this dataset
        String datasetName = dataset.getMetadata().getName();
        Table<Record> TABLE = DSL.table(DSL.name(datasetName));
        CreateTableColumnStep tableColumnStep =  dslContext.createTableIfNotExists(TABLE);
        Name entityFieldName = DSL.name("entity_id");
        tableColumnStep.column(entityFieldName, SQLDataType.INTEGER);
        for (String indicator: dataset.getIndicators()) {
            Name fieldName = DSL.name(indicator);
            tableColumnStep.column(fieldName, SQLDataType.VARCHAR(300));
        }
        tableColumnStep.execute();


        // Get entity references, creating new on the way if required.
        List<String> rawEntityNames = dataset.getEntities();
        List<Integer> entityReferences = rawEntityNames.stream().map(entityName -> {
            Integer id = dslContext.select(ENTITIES.ID)
                    .from(ENTITIES)
                    .where(ENTITIES.NAME.eq(entityName))
                    .execute();

            if (id == 0) {
                id = dslContext.insertInto(ENTITIES, ENTITIES.NAME)
                        .values(entityName)
                        .returning(ENTITIES.ID)
                        .fetchOne()
                        .getValue(ENTITIES.ID);
            }
            return id;
        }).collect(Collectors.toList());


        // Update indicators table to reflect that this table has indicator of this type
        List<String> indicators = dataset.getIndicators();
        for (String indicator: indicators) {
            JSONArray presentInJSON;
            Record1 previousRecord = dslContext.select(INDICATORS.PRESENT_IN)
                    .from(INDICATORS)
                    .where(INDICATORS.NAME.eq(indicator))
                    .fetchOne();
            if (previousRecord != null) {
                String previousPresentIn = previousRecord.getValue(INDICATORS.PRESENT_IN)
                        .toString();
                try {
                    presentInJSON = (JSONArray) new JSONParser().parse(previousPresentIn);
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            } else {
                presentInJSON = new JSONArray();
            }

            JSONObject presentInMap = new JSONObject();
            presentInMap.put("table", datasetName);
            presentInMap.put("column", indicator);
            presentInJSON.add(presentInMap);
            JSON presentIn = JSON.valueOf(presentInJSON.toString());

            if (previousRecord == null) {
                dslContext.insertInto(INDICATORS)
                        .set(INDICATORS.NAME, indicator)
                        .set(INDICATORS.PRESENT_IN, presentIn)
                        .execute();
            } else {
                dslContext.update(INDICATORS)
                        .set(INDICATORS.PRESENT_IN, presentIn)
                        .where(INDICATORS.NAME.eq(indicator))
                        .execute();
            }
        }

        // Update dataset metadata table
        dslContext.insertInto(DATASET_METADATA)
                .set(DATASET_METADATA.NAME, datasetName)
                .set(DATASET_METADATA.TABLE_NAME, datasetName)
                .execute();

        // Save the actual data into our table
        for (int entityIndex = 0; entityIndex < dataset.getEntities().size(); entityIndex++) {
            List<DataPoint> dataOfAnEntity = dataset.getDataGroupedByEntities().get(entityIndex);
            Integer entityId = entityReferences.get(entityIndex);


            InsertSetStep insertDataStep = dslContext.insertInto(TABLE);
            for (int indicatorIndex = 0;
                 indicatorIndex < indicators.size() && indicatorIndex < dataOfAnEntity.size();
                 indicatorIndex++
            ) {
                String indicator =  indicators.get(indicatorIndex);
                Field fieldName = DSL.field(DSL.name(indicator));
                insertDataStep.set(fieldName, dataOfAnEntity.get(indicatorIndex).getValue());
            }
            insertDataStep.set(DSL.field("entity_id"), entityId).execute();
        }
    }
}
