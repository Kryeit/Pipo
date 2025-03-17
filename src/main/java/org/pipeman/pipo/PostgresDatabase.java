package org.pipeman.pipo;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.pipeman.pipo.auth.JsonObjectMapper;
import org.pipeman.pipo.auth.User;
import org.pipeman.pipo.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresDatabase {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDatabase.class);
    private static final Jdbi JDBI;
    private static final HikariDataSource dataSource;

    public static void initialize() {
    }

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.dbUser);
        hikariConfig.setPassword(Config.dbPassword);
        hikariConfig.setJdbcUrl(Config.dbUrl);

        try {
            dataSource = new HikariDataSource(hikariConfig);
            JDBI = Jdbi.create(dataSource);
            JDBI.registerRowMapper(ConstructorMapper.factory(User.class));

            JDBI.registerColumnMapper(JsonObject.class, new JsonObjectMapper());

        } catch (Exception e) {
            logger.error("Failed to initialize database connection", e);
            throw new ExceptionInInitializerError(e);
        }

    }

    public static Jdbi getJdbi() {
        return JDBI;
    }

    public static void closeDataSource() {
        LogUtils.getLogger().info("Closing database connection...");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
