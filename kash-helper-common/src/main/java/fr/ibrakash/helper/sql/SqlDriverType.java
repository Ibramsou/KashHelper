package fr.ibrakash.helper.sql;

public enum SqlDriverType {
    MYSQL(com.mysql.jdbc.Driver.class.getName(), "jdbc:mysql://%s:%d/%s"),
    MARIADB(org.mariadb.jdbc.Driver.class.getName(), "jdbc:mariadb://%s:%d/%s");

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
