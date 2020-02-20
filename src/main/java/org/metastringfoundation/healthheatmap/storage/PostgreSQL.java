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

package org.metastringfoundation.healthheatmap.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.dataset.Dataset;

import javax.json.Json;
import java.sql.*;

public class PostgreSQL implements Database {
    private static final Logger LOG = LogManager.getLogger(PostgreSQL.class);

    private Connection psqlConnection;

    public PostgreSQL() {
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
    }

    private String returnQueryResult(String query) throws SQLException {
        String result;
        try (
                PreparedStatement statement = psqlConnection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                ) {
            if (resultSet.next()) {
                result = resultSet.getString(1);
            } else {
                result = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "No data returned")
                        .build()
                        .toString();
            }
        }
        return result;
    }

    private static String genericInternalServerError(Exception ex) {
        return Json.createObjectBuilder()
                .add("status", "error")
                .add("message", "Internal server error\n" + ex.toString())
                .build()
                .toString();
    }

    public String getHealth() {
        String healthQuery = "SELECT json_build_object('tables', json_agg(schemaname)) AS names from pg_statio_all_tables;";
        try {
            return returnQueryResult(healthQuery);
        } catch (SQLException ex) {
            return genericInternalServerError(ex);
        }
    }
}
