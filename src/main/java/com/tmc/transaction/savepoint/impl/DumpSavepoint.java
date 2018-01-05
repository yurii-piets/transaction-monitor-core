package com.tmc.transaction.savepoint.impl;

import com.tmc.transaction.savepoint.def.Savepoint;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DumpSavepoint implements Savepoint {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private boolean wasLoaded = false;

    private File dumpFile = null;

    private final Connection conn;

    private final String query;

    private String tableName;

    private ResultSetMetaData rsmd;

    private ResultSet selectResultSet;

    private final List<String> listOfScalableTypes = Arrays.asList("varchar", "char", "year", "float", "double", "decimal");

    //TODO: implement some code allowing creation of only one savepoint per transaction.
    //TODO: implement code checking if any appends need to be made. maybe have a list of tables already dumped on this savepoint?
    //TODO: unit tests

    @Override
    public boolean setSavepoint() {
        try {
            if(dumpFile == null)
                createDumpFile();

            tableName = extractTableNameFromQuery();

            getMetaData();
            appendTableToDump();
            appendDataToDump();
        }
        catch(Exception e){
            logger.error("Unexpected: ", e);
            return false;
        }

        return true;
    }




    @Override
    public boolean revert() {
        if(!wasLoaded) {
            try {
                executeDumpFile();
            } catch (Exception e) {
                logger.error("Unexpected: ", e);
                return false;
            }

        }

        return true;
    }

    private void createDumpFile() throws IOException{
        dumpFile = new File("testfile");
        try {
            PrintWriter printWriter = new PrintWriter(dumpFile);
            printWriter.print("");
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String extractTableNameFromQuery(){

        //TODO: implement this method.

        String tableName = null;

        return tableName;
    }

    private void appendDataToDump() throws Exception {
        appendLineToDumpFile("");
        appendLineToDumpFile("insert into " + tableName + " values");

        while(selectResultSet.next()){
            int numberOfColumns = rsmd.getColumnCount();

            String prefix = "";
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for(int i = 1 ; i <= numberOfColumns; i++) {
                sb.append(prefix);
                prefix = ", ";
                sb.append(selectResultSet.getString(i) + "");
            }
            sb.append(")");
            if(!selectResultSet.isLast())
                sb.append(",");
            else
                sb.append(";");

            appendLineToDumpFile(sb.toString());
        }


    }

    private void appendTableToDump() throws Exception {
        appendLineToDumpFile("drop table " + tableName + ";");
        appendLineToDumpFile("");
        appendLineToDumpFile("create table " + tableName + " (");
        appendLineToDumpFile(getColumnsStructure());
        appendLineToDumpFile(");");

    }

    private String getColumnsStructure() throws Exception {

        //TODO: extract some methods

        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet rsKey = dbmd.getPrimaryKeys(null, null, tableName);
        rsKey.next();
        String primaryKeyColumn = rsKey.getString("COLUMN_NAME");

        int numberOfColumns = rsmd.getColumnCount();

        StringBuilder sb = new StringBuilder();
        String suffix = ",";

        for(int i = 1 ; i <= numberOfColumns; i++) {
            String columnName = rsmd.getColumnName(i);
            String columnTypeName = rsmd.getColumnTypeName(i);
            int precision = rsmd.getPrecision(i);
            int scale = rsmd.getScale(i);
            int nullable = rsmd.isNullable(i);
            sb.append(columnName + " ");
            sb.append(columnTypeName + "");

            if (listOfScalableTypes.contains(columnTypeName))
                sb.append("(" + precision);
            if (scale != 0) {
                sb.append(",");
                sb.append(scale);
            }
            sb.append(")");
            if (nullable == 0)
                if (primaryKeyColumn.equals(columnName))
                    sb.append(" primary key");
                else
                    sb.append(" not null");
            if (i != numberOfColumns)
                sb.append(suffix);

            sb.append("\n");
        }
        return sb.toString();
    }

    private void executeDumpFile() throws Exception {
        String query = Files.readAllLines(dumpFile.toPath())
                .stream()
                .collect(Collectors.joining());
        Statement st = conn.createStatement();
        st.execute(query);
    }

    private void appendLineToDumpFile(String line) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(dumpFile, true);
            writer.append(line + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void getMetaData() throws Exception {
        Statement st = conn.createStatement();
        selectResultSet = st.executeQuery("select * from " + tableName);
        rsmd = selectResultSet.getMetaData();
    }


}
