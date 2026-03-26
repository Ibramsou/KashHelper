package fr.ibrakash.helper.jda.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight admin manager that persists a list of authorised Discord user IDs.
 *
 * <p>Admin data is stored in {@code administrators.json} inside the bot working directory.
 * The session-expiry mechanism uses a simple in-memory cache backed by timestamps.
 *
 * <p>Inspired by Cordzy's {@code AdminManager} but without external cache libraries.
 *
 * <p><b>Typical usage</b>
 * <pre>{@code
 * adminManager = new JdaAdminManager(jda, new File(dataFolder, "administrators.json"));
 * adminManager.addAdmin(userId);
 * }</pre>
 */
public class JdaAdminManager {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Default active-session duration: 15 minutes. */
    public static final long DEFAULT_SESSION_DURATION_MS = TimeUnit.MINUTES.toMillis(15);

    private final JDA jda;
    private final File storageFile;
    private final long sessionDurationMs;

    private final Map<Long, JdaAdminData>    admins          = new LinkedHashMap<>();
    private final Map<Long, Long>            activeSessions  = new HashMap<>(); // userId → expiry epoch

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public JdaAdminManager(JDA jda, File storageFile) {
        this(jda, storageFile, DEFAULT_SESSION_DURATION_MS);
    }

    public JdaAdminManager(JDA jda, File storageFile, long sessionDurationMs) {
        this.jda              = jda;
        this.storageFile      = storageFile;
        this.sessionDurationMs = sessionDurationMs;
        this.load();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns {@code true} if {@code userId} is registered as an admin. */
    public boolean isAdmin(long userId) {
        return this.admins.containsKey(userId);
    }

    /** Returns {@code true} if {@code userId} has an active session. */
    public boolean hasActiveSession(long userId) {
        Long expiry = this.activeSessions.get(userId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            this.activeSessions.remove(userId);
            return false;
        }
        return true;
    }

    /**
     * Opens an admin session for {@code userId} for {@link #sessionDurationMs} ms.
     * Silently ignored if the user is not a registered admin.
     */
    public void openSession(long userId) {
        if (!isAdmin(userId)) return;
        this.activeSessions.put(userId, System.currentTimeMillis() + this.sessionDurationMs);
    }

    /** Closes the active session for {@code userId}. */
    public void closeSession(long userId) {
        this.activeSessions.remove(userId);
    }

    /**
     * Guards an admin action: checks registration and active session, then calls {@code action}.
     * Replies with an error message via {@code callback} if the check fails.
     *
     * @param callback the interaction to reply to if the check fails
     * @param action   the code to run if the user passes all checks
     */
    public void requireAdmin(IReplyCallback callback, Runnable action) {
        long userId = callback.getUser().getIdLong();

        if (!isAdmin(userId)) {
            callback.reply("⛔ You are not registered as an administrator.").setEphemeral(true).queue();
            return;
        }

        if (!hasActiveSession(userId)) {
            callback.reply("⛔ Your admin session has expired or you are not logged in.").setEphemeral(true).queue();
            return;
        }

        action.run();
    }

    /** Adds a user as admin and persists the list. */
    public void addAdmin(long userId) {
        this.admins.put(userId, new JdaAdminData(userId, null, 0L));
        this.save();
        JdaBotLogger.info("Admin added: %d", userId);
    }

    /** Removes a user from the admin list and persists the change. */
    public void removeAdmin(long userId) {
        this.admins.remove(userId);
        this.activeSessions.remove(userId);
        this.save();
        JdaBotLogger.info("Admin removed: %d", userId);
    }

    /** Returns a copy of the current admin map. */
    public Map<Long, JdaAdminData> getAdmins() {
        return Collections.unmodifiableMap(this.admins);
    }

    /** Retrieves admin data for a {@link User}. */
    public Optional<JdaAdminData> getAdmin(User user) {
        return Optional.ofNullable(this.admins.get(user.getIdLong()));
    }

    public JDA jda() {
        return jda;
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    public void load() {
        if (!storageFile.exists()) return;
        try {
            Map<Long, JdaAdminData> loaded = MAPPER.readValue(storageFile,
                    new TypeReference<LinkedHashMap<Long, JdaAdminData>>() {});
            this.admins.clear();
            if (loaded != null) this.admins.putAll(loaded);
        } catch (IOException e) {
            JdaBotLogger.error("Failed to load admin data: %s", e.getMessage());
        }
    }

    public void save() {
        try {
            storageFile.getParentFile().mkdirs();
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(storageFile, this.admins);
        } catch (IOException e) {
            JdaBotLogger.error("Failed to save admin data: %s", e.getMessage());
        }
    }
}


