package org.pipeman.pipo.config;


import static org.pipeman.pipo.config.ConfigReader.*;

public class Config {
    public static final boolean production = false;

    public static final String dbUrl = production
            ? DB_URL
            : "jdbc:postgresql://kryeit.com:5432/servus";

    public static final String dbUser = production
            ? DB_USER
            : "postgres";
    public static final String dbPassword = production
            ? DB_PASSWORD
            : "lel";
}
