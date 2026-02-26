package fr.ibrakash.helper.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerProfileCache
{

    private static final Map<UUID, PlayerProfile> PROFILE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, PlayerProfile> VALUE_PROFILE_CACHE = new HashMap<>();
    private static final Map<String, PlayerProfile> USERNAME_PROFILE_CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService PROFILE_POOL = Executors.newFixedThreadPool(4);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public static PlayerProfile fetchTextureSkin(String value) {
        return VALUE_PROFILE_CACHE.computeIfAbsent(value, s -> {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures", value));
            return playerProfile;
        });
    }

    public static CompletableFuture<PlayerProfile> fetchUsernameSkin(String username) {
        final PlayerProfile current = USERNAME_PROFILE_CACHE.get(username);
        if (current != null) {
            return CompletableFuture.completedFuture(current);
        }

        CompletableFuture<PlayerProfile> future = new CompletableFuture<>();

        PROFILE_POOL.submit(() -> {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            if (offlinePlayer.isOnline()) {
                final PlayerProfile onlineProfile = offlinePlayer.getPlayer().getPlayerProfile();
                USERNAME_PROFILE_CACHE.put(username, onlineProfile);
                future.complete(onlineProfile);
                return;
            }

            try
            {
                PlayerProfile profile = fetchSkinInternal(offlinePlayer.getUniqueId());
                USERNAME_PROFILE_CACHE.put(username, profile);
                future.complete(profile);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        return future;
    }

    public static CompletableFuture<PlayerProfile> fetchTextureSkin(UUID uuid) {
        final PlayerProfile current = PROFILE_CACHE.get(uuid);
        if (current != null) {
            return CompletableFuture.completedFuture(current);
        }

        final Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            final PlayerProfile onlineProfile = onlinePlayer.getPlayerProfile();
            PROFILE_CACHE.put(uuid, onlineProfile);
            return CompletableFuture.completedFuture(onlineProfile);
        }
        final CompletableFuture<PlayerProfile> future = new CompletableFuture<>();
        PROFILE_POOL.submit(() -> {
            try
            {
                PlayerProfile profile = fetchSkinInternal(uuid);
                PROFILE_CACHE.put(uuid, profile);
                future.complete(profile);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    private static PlayerProfile fetchSkinInternal(UUID uuid) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/"
                        + uuid.toString().replace("-", "")
                        + "?unsigned=false"))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Wrong mojang response : " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray properties = json.getAsJsonArray("properties");

        if (properties.isEmpty()) {
            throw new RuntimeException("No skin texture found for " + uuid);
        }

        JsonObject textureProperty = properties.get(0).getAsJsonObject();
        String value = textureProperty.get("value").getAsString();
        String signature = textureProperty.get("signature").getAsString();

        PlayerProfile profile = Bukkit.createProfile(uuid);
        profile.setProperty(new ProfileProperty("textures", value, signature));
        profile.complete();
        profile.completeFromCache();

        return profile;
    }
}
