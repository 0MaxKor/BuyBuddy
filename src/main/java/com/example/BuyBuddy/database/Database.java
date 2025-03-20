package com.example.BuyBuddy.database;

public class Database {


    String DB_NAME;

    String DB_HOST="local";

    String DB_PORT="local";

    String DB_USER="local";

    String DB_PASSWORD="local";

    public Database(String name){
        DB_NAME = name;
    }


    public Database(String DB_NAME, String DB_HOST, String DB_PORT, String DB_USER, String DB_PASSWORD) {
        this.DB_NAME = DB_NAME;
        this.DB_HOST = DB_HOST;
        this.DB_PORT = DB_PORT;
        this.DB_USER = DB_USER;
        this.DB_PASSWORD = DB_PASSWORD;
    }
}