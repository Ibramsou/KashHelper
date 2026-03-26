package fr.ibrakash.helper.jda.embed.spec;

public enum PersistentButtonStyle {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER,
    LINK;

    public static PersistentButtonStyle fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            return SECONDARY;
        }

        return switch (raw.trim().toLowerCase()) {
            case "primary" -> PRIMARY;
            case "secondary" -> SECONDARY;
            case "success" -> SUCCESS;
            case "danger" -> DANGER;
            case "link" -> LINK;
            default -> SECONDARY;
        };
    }
}

