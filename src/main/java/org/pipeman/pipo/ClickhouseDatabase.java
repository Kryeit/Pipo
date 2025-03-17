package org.pipeman.pipo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.pipeman.pipo.auth.User;

public class ClickhouseDatabase {
    private static final Jdbi jdbi;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:clickhouse://kryeit.com:8123/kryeit");
        config.setUsername("default");
        config.setPassword(Pipo.readClickHouseKey());

        jdbi = Jdbi.create(new HikariDataSource(config));
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }
}
