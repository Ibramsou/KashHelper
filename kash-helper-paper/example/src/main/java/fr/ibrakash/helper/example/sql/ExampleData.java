package fr.ibrakash.helper.example.sql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.ibrakash.helper.example.sql.blob.ExampleBadgeListBlobSerializer;
import fr.ibrakash.helper.example.sql.blob.ExampleStringListStorage;
import fr.ibrakash.helper.example.sql.blob.ExampleStringLongMapBlobSerializer;
import fr.ibrakash.helper.persistence.entity.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Example persisted entity.
 *
 * <p>Works with both SQL and JSON backends automatically.
 * <ul>
 *   <li>SQL  → one row per player in a {@code example_data} table.</li>
 *   <li>JSON → stored as a map entry in {@code example_data.json}.</li>
 * </ul>
 *
 * <p>Add {@code transient} to any field you don't want persisted.
 */
@PersistedEntity("example_data")
@PersistedJson(mode = PersistedJsonMode.AUTO)
@PersistedIndex(name = "idx_example_data_score", columns = {"score"})
public class ExampleData {

    /** Primary key — arbitrary string id (e.g. player name, UUID string, slug…). */
    @PersistedId("id")
    private String id;

    @PersistedColumn(value = "points", nullable = false, defaultValue = "0")
    private int points;

    @PersistedColumn(value = "score", nullable = false, defaultValue = "0")
    private long score;

    @PersistedColumn(value = "display_name", length = 64)
    private String displayName;

    @PersistedEmbedded(prefix = "settings_")
    private ExampleSettings settings = new ExampleSettings();

    // Primitive relation -> valueColumn required
    @PersistedRelation(table = "example_data_tags", joinColumn = "profile_id", valueColumn = "tag_value")
    private List<String> tags = new ArrayList<>();

    // Object relation -> valueColumn omitted, all serializable fields become columns
    @PersistedRelation(table = "example_data_homes", joinColumn = "profile_id", prefix = "home_")
    private List<ExampleHomePoint> homes = new ArrayList<>();

    // Blob examples -----------------------------------------------------------

    @PersistedBlob(value = "blob_badges", blobTier = PersistedBlobTier.NORMAL, serializer = ExampleBadgeListBlobSerializer.class)
    private List<ExampleBadge> badges = new ArrayList<>();

    @PersistedBlob(value = "blob_stats", length = 2048, serializer = ExampleStringLongMapBlobSerializer.class)
    private Map<String, Long> statBuckets = new LinkedHashMap<>();

    @PersistedBlob(value = "blob_notes")
    private ExampleStringListStorage notes = new ExampleStringListStorage();

    @PersistedBlob(value = "raw_snapshot", length = 1024)
    private byte[] rawSnapshot = new byte[0];

    @PersistedRank(sort_columns = {"score DESC", "points DESC"}, load_on_deserialize = true)
    private transient int leaderboardRank;

    /** No-arg constructor required by the persistence layer and Jackson. */
    public ExampleData() {
    }

    public ExampleData(String id) {
        this.id = id;
    }

    @JsonCreator
    public ExampleData(
            @JsonProperty("id") String id,
            @JsonProperty("points") int points,
            @JsonProperty("score") long score,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("settings") ExampleSettings settings,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("homes") List<ExampleHomePoint> homes,
            @JsonProperty("badges") List<ExampleBadge> badges,
            @JsonProperty("statBuckets") Map<String, Long> statBuckets,
            @JsonProperty("notes") ExampleStringListStorage notes,
            @JsonProperty("rawSnapshot") byte[] rawSnapshot
    ) {
        this.id = id;
        this.points = points;
        this.score = score;
        this.displayName = displayName;
        this.settings = settings == null ? new ExampleSettings() : settings;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.homes = homes == null ? new ArrayList<>() : new ArrayList<>(homes);
        this.badges = badges == null ? new ArrayList<>() : new ArrayList<>(badges);
        this.statBuckets = statBuckets == null ? new LinkedHashMap<>() : new LinkedHashMap<>(statBuckets);
        this.notes = notes == null ? new ExampleStringListStorage() : notes;
        this.rawSnapshot = rawSnapshot == null ? new byte[0] : rawSnapshot;
    }

    /**
     * Creates a blank profile with default values.
     *
     * @param id          arbitrary string identifier
     * @param displayName optional display name; falls back to {@code id} if null or blank
     */
    public static ExampleData of(String id, String displayName) {
        ExampleData data = new ExampleData();
        data.id = id;
        data.points = 0;
        data.score = 0L;
        data.displayName = (displayName == null || displayName.isBlank()) ? id : displayName;
        data.settings = new ExampleSettings(true, "default");
        return data;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ExampleSettings getSettings() {
        return settings;
    }

    public void setSettings(ExampleSettings settings) {
        this.settings = settings;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<ExampleHomePoint> getHomes() {
        return homes;
    }

    public List<ExampleBadge> getBadges() {
        return badges;
    }

    public Map<String, Long> getStatBuckets() {
        return statBuckets;
    }

    public ExampleStringListStorage getNotes() {
        return notes;
    }

    public byte[] getRawSnapshot() {
        return rawSnapshot;
    }

    public void setRawSnapshot(byte[] rawSnapshot) {
        this.rawSnapshot = rawSnapshot == null ? new byte[0] : rawSnapshot;
    }

    public int getLeaderboardRank() {
        return leaderboardRank;
    }
}
