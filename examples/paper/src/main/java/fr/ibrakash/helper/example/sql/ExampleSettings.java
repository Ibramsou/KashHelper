package fr.ibrakash.helper.example.sql;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;

public class ExampleSettings extends ExampleDisplayAnchor {

    @PersistedColumn(value = "notify", nullable = false, defaultValue = "true")
    private boolean notifications = true;

    @PersistedColumn(value = "theme", length = 24)
    private String theme = "default";

    public ExampleSettings() {
        super();
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
