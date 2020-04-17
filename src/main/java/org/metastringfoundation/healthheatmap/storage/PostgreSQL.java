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
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.metastringfoundation.healthheatmap.dataset.table.Table;
import org.metastringfoundation.healthheatmap.dataset.table.TableCellReference;
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;

import javax.json.Json;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class PostgreSQL implements Database {
    private static final Logger LOG = LogManager.getLogger(PostgreSQL.class);

    private final Connection psqlConnection;
    private final DSLContext dslContext;

    public PostgreSQL() throws ApplicationError {
        //TODO: Make this loaded from configuration/environment
        String username = "metastring";
        String password = "metastring";
        String url = "jdbc:postgresql://localhost:5432/healthheatmap";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ApplicationError("No driver found");
        }
        try {
            psqlConnection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            throw new RuntimeException();
        }

        dslContext = DSL.using(psqlConnection, SQLDialect.POSTGRES);
    }

    public void close() throws SQLException {
        dslContext.close();
        psqlConnection.close();
    }

    public void createArbitraryTable(String name, Table table) throws ApplicationError {
        List<String> columnNames = new ArrayList<>();
        for (int col = 0; col < table.getNumberOfColumns(); col++) {
            String columnName = TableCellReference.convertNumToColString(col);
            columnNames.add(columnName);
        }

        List<List<String>> rows = table.getTable();
        try {
            createArbitraryTable(name, columnNames, rows);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationError("SQL Exception\n");
        }
    }

    private void createArbitraryTable(String name, List<String> columnNames, List<List<String>> rows) throws SQLException {
        org.jooq.Table<Record> TABLE = DSL.table(DSL.name(name));
        CreateTableColumnStep tableColumnStep =  dslContext.createTableIfNotExists(TABLE);
        for (String column: columnNames) {
            Name fieldName = DSL.name(column);
            tableColumnStep.column(fieldName, SQLDataType.VARCHAR(300));
        }
        tableColumnStep.execute();

        for (List<String> row: rows) {
            InsertSetStep<Record> insertDataStep = dslContext.insertInto(TABLE);
            for (int col = 0; col < row.size(); col++) {
                String colName = columnNames.get(col);
                Field<String> fieldName = DSL.field(DSL.name(colName), String.class);
                insertDataStep.set(fieldName, row.get(col));
            }
            @SuppressWarnings("unchecked") // only because the code works.
            InsertSetMoreStep<Record> step = (InsertSetMoreStep<Record>) insertDataStep;
            step.execute();
        }

    }

    private void oldCreateArbitraryTable(String name, List<String> columnNames, List<List<String>> rows) throws SQLException {
        boolean prevAutoCommit = psqlConnection.getAutoCommit();
        psqlConnection.setAutoCommit(false);

        StringJoiner columns = new StringJoiner(",\n", "(", ")");
        for (String column: columnNames) {
            String columnSql = column + " VARCHAR(300)";
            columns.add(columnSql);
        }

        String sql = "CREATE TABLE " +
                name +
                " " +
                columns.toString() +
                ";";

        LOG.debug(sql);

        try (PreparedStatement createStatement = psqlConnection.prepareStatement(sql)) {
            createStatement.executeUpdate();
            LOG.debug(createStatement.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringJoiner valuesRow = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < columnNames.size(); i++) {
            valuesRow.add("?");
        }
        String insertRows = "INSERT INTO " +
                name +
                " VALUES " +
                valuesRow.toString() +
                ";";

        try (PreparedStatement insertStatement = psqlConnection.prepareStatement(insertRows)) {
            insertStatement.setString(1, name);
            for (List<String> row: rows) {
                for (int param = 0; param < row.size(); param++) {
                    LOG.debug("Inserting " + row.get(param) + " at position " + (param + 1));
                    insertStatement.setString(param + 1, row.get(param));
                }
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        psqlConnection.commit();
        psqlConnection.setAutoCommit(prevAutoCommit);
    }

    private String returnQueryResult(String query) throws SQLException {
        String result;
        try (
                PreparedStatement statement = psqlConnection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()
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
