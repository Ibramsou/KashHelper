package fr.ibrakash.helper.jda.example.embed.v2;

import fr.ibrakash.helper.jda.embed.PersistentChannelEmbed;
import fr.ibrakash.helper.jda.example.JdaExample;
import fr.ibrakash.helper.persistence.entity.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@PersistedEntity("persistence_examples")
@PersistedDefaultId("message_id")
public class PersistenceExample extends PersistentChannelEmbed {

    private static final String PATH = "persistence-example";
    private static final List<String> PERIODS = List.of("7d", "30d", "all");

    private transient long initialChannelId = -1L;

    @PersistedColumn("owner_id")
    private long ownerId;

    @PersistedBlob(value = "embed_blob")
    private EmbedBlobData blobExample = new EmbedBlobData();

    @PersistedEmbedded(prefix = "embed_")
    private EmbedColumnData embeddedEmbed = new EmbedColumnData();

    @PersistedRelation(table = "persistence_example_history", joinColumn = "embed_id", prefix = "history_")
    private List<EmbedHistoryEntry> history = new ArrayList<>();

    public PersistenceExample() {
    }

    public PersistenceExample(long targetChannelId, long ownerId) {
        super(targetChannelId);
        this.initialChannelId = targetChannelId;
        this.ownerId = ownerId;
        this.ensureState();
        this.recordHistory("created");
        this.registerActions();
    }

    public long id() {
        return this.messageId();
    }

    public long initialChannelId() {
        return this.initialChannelId;
    }

    @Override
    public JdaExample addon() {
        return JdaExample.getInstance();
    }

    @Override
    public String embedPath() {
        return PATH;
    }

    @Override
    protected CompletableFuture<MessageChannel> resolveChannel() {
        if (this.messageId() > 0L) {
            return this.resolveChannelByMessageId(this.messageId());
        }

        if (this.initialChannelId > 0L) {
            MessageChannel channel = this.requireAddon().getJda().getChannelById(MessageChannel.class, this.initialChannelId);
            if (channel != null) {
                return CompletableFuture.completedFuture(channel);
            }
        }

        return CompletableFuture.failedFuture(new IllegalStateException(
                "Cannot resolve channel before first send: no message id and no initial channel id."));
    }

    @Override
    public void onDeserialized() {
        this.ensureState();
        this.registerActions();
        this.reload();
    }

    @Override
    public Map<String, Object> placeholders() {
        this.ensureState();
        return Map.ofEntries(
                Map.entry("%embed_id%", String.valueOf(this.messageId())),
                Map.entry("%owner_id%", String.valueOf(this.ownerId)),
                Map.entry("%channel_id%", String.valueOf(this.initialChannelId)),
                Map.entry("%current_period%", this.currentPeriod()),
                Map.entry("%current_score%", this.currentScore()),
                Map.entry("%refresh_count%", String.valueOf(this.embeddedEmbed.refreshCount)),
                Map.entry("%stored_message_id%", String.valueOf(this.embeddedEmbed.messageId)),
                Map.entry("%history_size%", String.valueOf(this.history.size())),
                Map.entry("%latest_history%", this.latestHistoryText()),
                Map.entry("%last_action%", this.blobExample.lastAction)
        );
    }

    public static class EmbedBlobData {
        private String selectedPeriod = "7d";
        private int score = 42;
        private String lastAction = "created";
        private long lastRefreshEpochMillis = System.currentTimeMillis();

        public EmbedBlobData() {
        }
    }

    public static class EmbedColumnData {
        @PersistedColumn("message_id")
        private long messageId;

        @PersistedColumn("selected_period")
        private String selectedPeriod = "7d";

        @PersistedColumn("refresh_count")
        private int refreshCount;

        public EmbedColumnData() {
        }
    }

    public static class EmbedHistoryBase {
        @PersistedColumn("period_key")
        protected String periodKey = "7d";

        @PersistedColumn("score_value")
        protected int scoreValue;

        public EmbedHistoryBase() {
        }

        public EmbedHistoryBase(String periodKey, int scoreValue) {
            this.periodKey = periodKey;
            this.scoreValue = scoreValue;
        }
    }

    public static class EmbedHistoryEntry extends EmbedHistoryBase {
        @PersistedColumn("action_name")
        private String actionName;

        @PersistedColumn("created_at")
        private String createdAt;

        public EmbedHistoryEntry() {
        }

        public EmbedHistoryEntry(String periodKey, int scoreValue, String actionName, String createdAt) {
            super(periodKey, scoreValue);
            this.actionName = actionName;
            this.createdAt = createdAt;
        }
    }

    private void registerActions() {
        this.buttonAction("refresh_state", event -> {
            this.ensureState();
            this.embeddedEmbed.refreshCount++;
            this.blobExample.lastAction = "refresh";
            this.blobExample.lastRefreshEpochMillis = System.currentTimeMillis();
            this.recordHistory("refresh");
            this.reloadAndSave(event, "✅ Persistent embed refreshed.");
        });

        this.buttonAction("cycle_period", event -> {
            this.ensureState();
            this.blobExample.selectedPeriod = this.nextPeriod(this.currentPeriod());
            this.blobExample.score = this.scoreForPeriod(this.blobExample.selectedPeriod);
            this.blobExample.lastAction = "cycle-period";
            this.blobExample.lastRefreshEpochMillis = System.currentTimeMillis();
            this.embeddedEmbed.selectedPeriod = this.blobExample.selectedPeriod;
            this.recordHistory("cycle-period");
            this.reloadAndSave(event, "📆 Period changed to **" + this.blobExample.selectedPeriod + "**.");
        });

        this.buttonAction("show_state", event ->
                event.reply(this.buildStateMessage()).setEphemeral(true).queue());
    }

    private void reloadAndSave(net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent event, String successMessage) {
        event.deferReply(true).queue(hook ->
                this.reload().whenComplete((ignored, error) -> {
                    if (error != null) {
                        hook.editOriginal("Impossible to update the persisted embed: " + error.getMessage()).queue();
                        return;
                    }
                    this.saveState();
                    hook.editOriginal(successMessage).queue();
                })
        );
    }

    private void saveState() {
        this.ensureState();
        this.embeddedEmbed.messageId = this.messageId();
        this.embeddedEmbed.selectedPeriod = this.currentPeriod();
        this.addon().requirePersistenceExampleRepository().save(this);
    }

    private void ensureState() {
        if (this.blobExample == null) {
            this.blobExample = new EmbedBlobData();
        }
        if (this.embeddedEmbed == null) {
            this.embeddedEmbed = new EmbedColumnData();
        }
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        if (this.blobExample.selectedPeriod == null || this.blobExample.selectedPeriod.isBlank()) {
            this.blobExample.selectedPeriod = this.embeddedEmbed.selectedPeriod == null || this.embeddedEmbed.selectedPeriod.isBlank()
                    ? "7d"
                    : this.embeddedEmbed.selectedPeriod;
        }
        if (this.embeddedEmbed.selectedPeriod == null || this.embeddedEmbed.selectedPeriod.isBlank()) {
            this.embeddedEmbed.selectedPeriod = this.blobExample.selectedPeriod;
        }

        if (this.messageId() <= 0L && this.embeddedEmbed.messageId > 0L) {
            this.updateMessageId(this.embeddedEmbed.messageId);
        } else if (this.messageId() > 0L && this.embeddedEmbed.messageId <= 0L) {
            this.embeddedEmbed.messageId = this.messageId();
        }

        if (this.blobExample.score <= 0) {
            this.blobExample.score = this.scoreForPeriod(this.blobExample.selectedPeriod);
        }
        if (this.blobExample.lastAction == null || this.blobExample.lastAction.isBlank()) {
            this.blobExample.lastAction = "loaded";
        }
    }

    private void recordHistory(String actionName) {
        this.ensureState();
        this.history.add(new EmbedHistoryEntry(
                this.currentPeriod(),
                this.blobExample.score,
                actionName,
                Instant.now().toString()
        ));
        if (this.history.size() > 10) {
            this.history.remove(0);
        }
    }

    private String currentPeriod() {
        this.ensureState();
        return this.blobExample.selectedPeriod;
    }

    private String currentScore() {
        this.ensureState();
        return String.valueOf(this.blobExample.score);
    }

    private String latestHistoryText() {
        if (this.history == null || this.history.isEmpty()) {
            return "none";
        }
        EmbedHistoryEntry latest = this.history.get(this.history.size() - 1);
        return latest.actionName + " / " + latest.periodKey + " / score=" + latest.scoreValue + " / at=" + latest.createdAt;
    }

    private String nextPeriod(String currentPeriod) {
        int currentIndex = PERIODS.indexOf(currentPeriod);
        if (currentIndex < 0) {
            return PERIODS.get(0);
        }
        return PERIODS.get((currentIndex + 1) % PERIODS.size());
    }

    private int scoreForPeriod(String period) {
        return switch (period) {
            case "30d" -> 128;
            case "all" -> 512;
            default -> 42;
        };
    }

    private String buildStateMessage() {
        this.ensureState();
        return "embed=" + this.messageId()
                + "\nchannel=" + this.initialChannelId
                + "\nperiod=" + this.currentPeriod()
                + "\nscore=" + this.blobExample.score
                + "\nrefreshes=" + this.embeddedEmbed.refreshCount
                + "\nstored message=" + this.embeddedEmbed.messageId
                + "\nhistory=" + this.history.size()
                + "\nlast action=" + this.blobExample.lastAction
                + "\nlast refresh=" + this.blobExample.lastRefreshEpochMillis;
    }

    private CompletableFuture<MessageChannel> resolveChannelByMessageId(long messageId) {
        List<MessageChannel> channels = this.requireAddon().getJda().getGuilds().stream()
                .flatMap(guild -> guild.getChannels().stream())
                .filter(MessageChannel.class::isInstance)
                .map(MessageChannel.class::cast)
                .toList();

        if (channels.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("No message channels available in cache."));
        }

        return this.findMessageChannel(channels, messageId, 0);
    }

    private CompletableFuture<MessageChannel> findMessageChannel(List<MessageChannel> channels, long messageId, int index) {
        if (index >= channels.size()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Message not found for id: " + messageId));
        }

        MessageChannel channel = channels.get(index);
        return channel.retrieveMessageById(messageId)
                .submit()
                .thenApply(ignored -> channel)
                .exceptionallyCompose(ignored -> this.findMessageChannel(channels, messageId, index + 1));
    }
}
