package fr.ibrakash.helper.example.home;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.entity.PersistedIndex;
import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted home entry.
 *
 * <p>The {@link PersistedId} is the composite key {@code "ownerUuid:name"}.
 * The {@code owner_uuid} column has a dedicated index for fast per-player queries.
 */
@PersistedEntity("homes")
@PersistedIndex(name = "idx_homes_owner", columns = {"owner_uuid"})
public class HomeRecord {

    /** Composite key: {@code "<ownerUuid>:<name>"}. Used as primary key in both SQL and JSON. */
    @PersistedId("home_key")
    private String key;

    @PersistedColumn(value = "owner_uuid", nullable = false, length = 36)
    private String ownerUuid;

    @PersistedColumn(value = "home_name", nullable = false, length = 64)
    private String name;

    @PersistedColumn(nullable = false, length = 64)
    private String world;

    @PersistedColumn(nullable = false)
    private double x;

    @PersistedColumn(nullable = false)
    private double y;

    @PersistedColumn(nullable = false)
    private double z;

    @PersistedColumn(nullable = false)
    private float yaw;

    @PersistedColumn(nullable = false)
    private float pitch;

    @PersistedColumn(nullable = false, defaultValue = "false")
    private boolean favorite;

    @PersistedColumn(value = "updated_at", nullable = false)
    private long updatedAt;

    /** No-arg constructor required by the persistence layer. */
    public HomeRecord() {
    }

    @JsonCreator
    public HomeRecord(
            @JsonProperty("key") String key,
            @JsonProperty("ownerUuid") String ownerUuid,
            @JsonProperty("name") String name,
            @JsonProperty("world") String world,
            @JsonProperty("x") double x,
            @JsonProperty("y") double y,
            @JsonProperty("z") double z,
            @JsonProperty("yaw") float yaw,
            @JsonProperty("pitch") float pitch,
            @JsonProperty("favorite") boolean favorite,
            @JsonProperty("updatedAt") long updatedAt
    ) {
        this.key = key;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.favorite = favorite;
        this.updatedAt = updatedAt;
    }

    public static HomeRecord of(UUID ownerUuid, String name, Location location) {
        HomeRecord record = new HomeRecord();
        String lowerName = name.toLowerCase();
        record.key = ownerUuid + ":" + lowerName;
        record.ownerUuid = ownerUuid.toString();
        record.name = lowerName;
        record.world = location.getWorld() == null ? null : location.getWorld().getName();
        record.x = location.getX();
        record.y = location.getY();
        record.z = location.getZ();
        record.yaw = location.getYaw();
        record.pitch = location.getPitch();
        record.favorite = false;
        record.updatedAt = Instant.now().getEpochSecond();
        return record;
    }

    public String key() {
        return key;
    }

    public UUID getOwnerUuid() {
        return UUID.fromString(ownerUuid);
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        this.updatedAt = Instant.now().getEpochSecond();
    }
}
