package fr.ibrakash.helper.configuration.objects.database;

import fr.ibrakash.helper.sql.SqlCredential;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigSql implements SqlCredential {

    private SqlDriverType driver = SqlDriverType.MYSQL;
    private String customUrl = "";
    private String host = "localhost";
    private int port = 3306;
    private String database = "example";
    private String user = "root";
    private String password = "root";
    private int poolSize = 5;

    public ConfigSql() {}

    public ConfigSql driver(SqlDriverType driver) {
        this.driver = driver;
        return this;
    }

    public ConfigSql customUrl(String customUrl) {
        this.customUrl = customUrl;
        return this;
    }

    public ConfigSql host(String host) {
        this.host = host;
        return this;
    }

    public ConfigSql port(int port) {
        this.port = port;
        return this;
    }

    public ConfigSql database(String database) {
        this.database = database;
        return this;
    }

    public ConfigSql user(String user) {
        this.user = user;
        return this;
    }

    public ConfigSql password(String password) {
        this.password = password;
        return this;
    }

    public ConfigSql poolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    @Override
    public SqlDriverType getDriverType() {
        return this.driver;
    }

    @Override
    public String getCustomUrl() {
        return this.customUrl;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getDatabase() {
        return this.database;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public int getPoolSize() {
        return this.poolSize;
    }
}