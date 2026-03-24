package fr.ibrakash.helper.sql;

public enum SqlDriverType {
    MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://%s:%d/%s"),
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%3$s");

    private final String driverClassName;
    private final String urlFormat;

    SqlDriverType(String driverClassName, String urlFormat) {
        this.driverClassName = driverClassName;
        this.urlFormat = urlFormat;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrlFormat() {
        return urlFormat;
    }
}
