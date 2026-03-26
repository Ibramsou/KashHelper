package fr.ibrakash.helper.jda.admin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persisted admin entry – stores the Discord user ID and optional 2-FA secret.
 */
public final class JdaAdminData {

    @JsonProperty("user-id")
    private final long userId;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("secret-timestamp")
    private long secretTimestamp;

    /** Jackson / no-arg constructor. */
    public JdaAdminData() {
        this(0L, null, 0L);
    }

    @JsonCreator
    public JdaAdminData(
            @JsonProperty("user-id")        long userId,
            @JsonProperty("secret")         String secret,
            @JsonProperty("secret-timestamp") long secretTimestamp
    ) {
        this.userId          = userId;
        this.secret          = secret;
        this.secretTimestamp = secretTimestamp;
    }

    public long getUserId()           { return userId; }
    public String getSecret()         { return secret; }
    public long getSecretTimestamp()  { return secretTimestamp; }

    public void setSecret(String secret)             { this.secret = secret; }
    public void setSecretTimestamp(long ts)          { this.secretTimestamp = ts; }
}

