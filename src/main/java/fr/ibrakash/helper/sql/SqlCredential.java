package fr.ibrakash.helper.sql;

public interface SqlCredential {

    SqlDriverType getDriverType();

    String getCustomUrl();

    String getHost();

    int getPort();

    String getDatabase();

    String getUser();

    String getPassword();

    int getPoolSize();

    default String buildUrl() {
        final String customUrl = this.getCustomUrl();
        if (customUrl == null || customUrl.isEmpty()) {
            return this.getDriverType().getUrlFormat();
        }

        return customUrl;
    }
}
