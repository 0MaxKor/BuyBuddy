package com.example.BuyBuddy.database;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Table extends Database {
    public String TABLE_NAME;
    String type;

    public Table(Database database, String TABLE_NAME, String type) {
        super(database.DB_NAME, database.DB_HOST, database.DB_PORT, database.DB_USER, database.DB_PASSWORD);
        this.TABLE_NAME = TABLE_NAME;
        this.type = type;
    }

    public ArrayList<String[]> select(String[] selectingColumns, String[] targetColumns, String[] targetValues) {
        ArrayList<String[]> reply = new ArrayList<>();
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            String part1 = "";
            for (int i = 0; i < selectingColumns.length; i++) {
                part1 += selectingColumns[i];
                if (!(i == selectingColumns.length - 1)) {
                    part1 += ", ";
                }
            }
            String sql = "SELECT " + part1 + " FROM " + TABLE_NAME;
            if (targetColumns.length != 0) {
                sql += " WHERE";
            }
            for (int i = 0; i < targetColumns.length; i++) {
                sql += (" " + targetColumns[i] + " LIKE '" + targetValues[i] + "'");
                if (!(i == targetColumns.length - 1)) {
                    sql += " AND";
                }
            }
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    String[] reply1 = new String[selectingColumns.length];
                    for (int i = 0; i < selectingColumns.length; i++) {
                        reply1[i] = resultSet.getString(selectingColumns[i]);
                    }
                    reply.add(reply1);

                }
            }

            connection.close();
        } catch (SQLException e) {
            System.err.println(e);

        }
        return reply;
    }


    public void insert(String[] columns, String[] values) {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO " + TABLE_NAME + " (";
            for (int i = 0; i < columns.length; i++) {
                sql += columns[i];
                if (i != (columns.length - 1)) {
                    sql += ",";
                }
            }
            sql += ") VALUES (";
            for (int i = 0; i < values.length; i++) {
                sql += "'" + values[i] + "'";
                if (i != (values.length) - 1) {
                    sql += ",";
                }
            }
            sql += ")";

            Statement statement = connection.createStatement();
            statement.execute(sql);


            connection.close();
        } catch (SQLException e) {
            System.err.println(e);

        }
    }

    public void delete(String[] columnsToDelete, String[] targetColumns, String[] targetValues) {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);

            String sql = "DELETE ";
            for (int i = 0; i < columnsToDelete.length; i++) {
                sql += columnsToDelete[i];
                if (i != (columnsToDelete.length - 1)) {
                    sql += ",";
                } else if (i == (columnsToDelete.length - 1)) {
                    sql += " ";
                }
            }
            sql += "FROM " + TABLE_NAME + " WHERE ";
            for (int i = 0; i < targetColumns.length; i++) {
                sql += targetColumns[i] + " LIKE '" + targetValues[i] + "'";
                if (i != (targetColumns.length - 1)) {
                    sql += " AND ";
                }
            }

            Statement statement = connection.createStatement();
            statement.execute(sql);

            connection.close();
        } catch (SQLException e) {

            System.err.println(e);

        }
    }

    public void delete() {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);

            String sql = "DELETE FROM " + TABLE_NAME;

            Statement statement = connection.createStatement();
            statement.execute(sql);


            connection.close();
        } catch (SQLException e) {

            System.err.println(e);

        }
    }

    public void executeQuery(String query) {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);

            Statement statement = connection.createStatement();
            statement.execute(query);


            connection.close();
        } catch (SQLException e) {

            System.err.println(e);

        }
    }


    public void update(String[] columnsToUpdate, String[] newValues, String[] targetColumns, String[] targetValues) {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        try {
            Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);

            String sql = "UPDATE " + TABLE_NAME + " SET ";
            for (int i = 0; i < columnsToUpdate.length; i++) {
                sql += columnsToUpdate[i] + " = '" + newValues[i] + "'";
                if (i != (columnsToUpdate.length) - 1) {
                    sql += ", ";
                }
            }
            sql += " WHERE ";
            for (int i = 0; i < targetValues.length; i++) {
                sql += targetColumns[i] + " LIKE '" + targetValues[i] + "'";
                if (i != (targetColumns.length - 1)) {
                    sql += " AND ";
                }
            }
            Statement statement = connection.createStatement();
            statement.execute(sql);

            connection.close();
        } catch (SQLException e) {
            System.err.println(e);

        }
    }


    //---------------------

    public synchronized ArrayList<String[]> selectFast(Connection connection, String[] selectingColumns, String[] targetColumns, String[] targetValues) {
        ArrayList<String[]> reply = new ArrayList<>();
        if (type.equals("mysql")) {
            try {
                String part1 = "";
                for (int i = 0; i < selectingColumns.length; i++) {
                    part1 += selectingColumns[i];
                    if (!(i == selectingColumns.length - 1)) {
                        part1 += ", ";
                    }
                }
                String sql = "SELECT " + part1 + " FROM " + TABLE_NAME;
                if (targetColumns.length != 0) {
                    sql += " WHERE";
                }
                for (int i = 0; i < targetColumns.length; i++) {
                    sql += (" " + targetColumns[i] + " LIKE '" + targetValues[i] + "'");
                    if (!(i == targetColumns.length - 1)) {
                        sql += " AND";
                    }
                }
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        String[] reply1 = new String[selectingColumns.length];
                        for (int i = 0; i < selectingColumns.length; i++) {
                            reply1[i] = resultSet.getString(selectingColumns[i]);
                        }
                        reply.add(reply1);

                    }
                }

            } catch (SQLException e) {
                System.err.println(e);

            }
        } else if (type.equals("sqlite")) {
            try {
                String part1 = "";
                for (int i = 0; i < selectingColumns.length; i++) {
                    part1 += selectingColumns[i];
                    if (!(i == selectingColumns.length - 1)) {
                        part1 += ", ";
                    }
                }
                if (part1.equals("")) {
                    part1 = "*";
                }
                String sql = "SELECT " + part1 + " FROM " + TABLE_NAME;
                if (targetColumns.length != 0) {
                    sql += " WHERE";
                }
                for (int i = 0; i < targetColumns.length; i++) {
                    sql += (" " + targetColumns[i] + " LIKE '" + targetValues[i] + "'");
                    if (!(i == targetColumns.length - 1)) {
                        sql += " AND";
                    }
                }
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        String[] reply1 = new String[selectingColumns.length];
                        for (int i = 0; i < selectingColumns.length; i++) {
                            reply1[i] = resultSet.getString(selectingColumns[i]);
                        }
                        reply.add(reply1);

                    }
                }

            } catch (SQLException e) {
                System.err.println(e);

            }
        }


        return reply;
    }

    public synchronized void insertFast(Connection connection, String[] columns, String[] values) {

        String sql = "INSERT INTO " + TABLE_NAME + " (";
        for (int i = 0; i < columns.length; i++) {
            sql += columns[i];
            if (i != (columns.length - 1)) {
                sql += ",";
            }
        }
        sql += ") VALUES (";
        for (int i = 0; i < values.length; i++) {
            sql += "'" + values[i] + "'";
            if (i != (values.length) - 1) {
                sql += ",";
            }
        }
        sql += ")";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);


        } catch (SQLException e) {
            System.err.println(e);

        }
    }

    public synchronized void deleteFast(Connection connection, String[] columnsToDelete, String[] targetColumns, String[] targetValues) {


        String sql = "DELETE ";
        for (int i = 0; i < columnsToDelete.length; i++) {
            sql += columnsToDelete[i];
            if (i != (columnsToDelete.length - 1)) {
                sql += ",";
            } else if (i == (columnsToDelete.length - 1)) {
                sql += " ";
            }
        }
        sql += "FROM " + TABLE_NAME + " WHERE ";
        for (int i = 0; i < targetColumns.length; i++) {
            sql += targetColumns[i] + " LIKE '" + targetValues[i] + "'";
            if (i != (targetColumns.length - 1)) {
                sql += " AND ";
            }
        }
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);

        } catch (SQLException e) {

            System.err.println(e);

        }
    }


    public synchronized void executeQueryFast(Connection connection, String query) {
        try {

            Statement statement = connection.createStatement();
            statement.execute(query);


        } catch (SQLException e) {

            System.err.println(e);

        }
    }

    public synchronized void updateFast(Connection connection, String[] columnsToUpdate, String[] newValues, String[] targetColumns, String[] targetValues) {


        String sql = "UPDATE " + TABLE_NAME + " SET ";
        for (int i = 0; i < columnsToUpdate.length; i++) {
            sql += columnsToUpdate[i] + " = '" + newValues[i] + "'";
            if (i != (columnsToUpdate.length) - 1) {
                sql += ", ";
            }
        }
        sql += " WHERE ";
        for (int i = 0; i < targetValues.length; i++) {
            sql += targetColumns[i] + " LIKE '" + targetValues[i] + "'";
            if (i != (targetColumns.length - 1)) {
                sql += " AND ";
            }
        }
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);

        } catch (SQLException e) {
            System.err.println(e);

        }
    }




    @Override
    public String toString() {
        return "Table{" +
                "TABLE_NAME='" + TABLE_NAME + '\'' +
                ", DB_NAME='" + DB_NAME + '\'' +
                ", DB_HOST='" + DB_HOST + '\'' +
                ", DB_PORT='" + DB_PORT + '\'' +
                ", DB_USER='" + DB_USER + '\'' +
                ", DB_PASSWORD='" + DB_PASSWORD + '\'' +
                '}';
    }

}

