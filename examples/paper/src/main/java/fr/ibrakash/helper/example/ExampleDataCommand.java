package fr.ibrakash.helper.example;

import fr.ibrakash.helper.example.sql.ExampleBadge;
import fr.ibrakash.helper.example.sql.ExampleData;
import fr.ibrakash.helper.example.sql.ExampleHomePoint;
import fr.ibrakash.helper.example.sql.ExampleRepository;
import fr.ibrakash.helper.example.sql.ExampleSettings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * {@code /exampledata <sub> [args...]}
 */
public class ExampleDataCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final List<String> SUBS = List.of(
            "create", "score", "points", "setting", "anchor", "nullsettings", "tag", "home", "blob",
            "top", "rank", "rankupdate", "rankbulk", "seed", "saveall", "save", "delete", "bulkload", "info"
    );

    private final ExamplePlugin plugin;

    public ExampleDataCommand(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player, label);
            return true;
        }

        ExampleRepository repo = plugin.getExampleRepository();
        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " create <id> [displayName]"));
                    return true;
                }
                String id = args[1];
                String displayName = args.length >= 3
                        ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                        : null;

                ExampleData data = ExampleData.of(id, displayName);
                repo.save(data);
                player.sendMessage(MM.deserialize("<green>Created profile <white>" + id));
            }

            case "score" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " score <id> <value>"));
                    return true;
                }
                String id = args[1];
                long value;
                try {
                    value = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(MM.deserialize("<red>Invalid number: <white>" + args[2]));
                    return true;
                }

                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    data.setScore(value);
                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Score updated for <white>" + id + "<green> -> <white>" + value));
                });
            }

            case "points" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " points <id> <value>"));
                    return true;
                }
                String id = args[1];
                int value;
                try {
                    value = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(MM.deserialize("<red>Invalid number: <white>" + args[2]));
                    return true;
                }

                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    data.setPoints(value);
                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Points updated for <white>" + id + "<green> -> <white>" + value));
                });
            }

            case "setting" -> {
                if (args.length < 4) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " setting <id> <notifications:true|false> <theme>"));
                    return true;
                }

                String id = args[1];
                String notifArg = args[2].toLowerCase(Locale.ROOT);
                if (!notifArg.equals("true") && !notifArg.equals("false")) {
                    player.sendMessage(MM.deserialize("<red>notifications must be <white>true <red>or <white>false"));
                    return true;
                }

                boolean notifications = Boolean.parseBoolean(notifArg);
                String theme = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }

                    ExampleSettings settings = data.getSettings();
                    if (settings == null) {
                        settings = new ExampleSettings();
                        data.setSettings(settings);
                    }
                    settings.setNotifications(notifications);
                    settings.setTheme(theme);

                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Settings updated for <white>" + id
                            + "<green> -> notify=<white>" + notifications
                            + " <green>theme=<white>" + theme));
                });
            }

            case "anchor" -> {
                if (args.length < 5) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " anchor <id> <x> <y> <z>"));
                    return true;
                }
                String id = args[1];
                int x;
                int y;
                int z;
                try {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    z = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    player.sendMessage(MM.deserialize("<red>x/y/z must be integers."));
                    return true;
                }

                final int fx = x;
                final int fy = y;
                final int fz = z;
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    if (data.getSettings() == null) {
                        data.setSettings(new ExampleSettings());
                    }
                    data.getSettings().setX(fx);
                    data.getSettings().setY(fy);
                    data.getSettings().setZ(fz);
                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Anchor updated for <white>" + id + "<green> -> <white>" + fx + "," + fy + "," + fz));
                });
            }

            case "nullsettings" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " nullsettings <id>"));
                    return true;
                }
                String id = args[1];
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    data.setSettings(null);
                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Settings forced to <white>null <green>for <white>" + id));
                });
            }

            case "tag" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " tag <add|remove|list> <id> [tag]"));
                    return true;
                }
                String action = args[1].toLowerCase(Locale.ROOT);
                String id = args[2];

                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }

                    switch (action) {
                        case "add" -> {
                            if (args.length < 4) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " tag add <id> <tag>"));
                                return;
                            }
                            String tag = args[3];
                            if (!data.getTags().contains(tag)) data.getTags().add(tag);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Tag added to <white>" + id));
                        }
                        case "remove" -> {
                            if (args.length < 4) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " tag remove <id> <tag>"));
                                return;
                            }
                            String tag = args[3];
                            data.getTags().remove(tag);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Tag removed from <white>" + id));
                        }
                        case "list" -> player.sendMessage(MM.deserialize("<gray>Tags: <white>" + data.getTags()));
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " tag <add|remove|list> <id> [tag]"));
                    }
                });
            }

            case "home" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home <add|remove|list> <id> [...]"));
                    return true;
                }
                String action = args[1].toLowerCase(Locale.ROOT);
                String id = args[2];

                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }

                    switch (action) {
                        case "add" -> {
                            if (args.length < 8) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home add <id> <name> <world> <x> <y> <z>"));
                                return;
                            }
                            String name = args[3];
                            String world = args[4];
                            try {
                                double x = Double.parseDouble(args[5]);
                                double y = Double.parseDouble(args[6]);
                                double z = Double.parseDouble(args[7]);
                                data.getHomes().add(new ExampleHomePoint(name, world, x, y, z));
                                repo.save(data);
                                player.sendMessage(MM.deserialize("<green>Home added for <white>" + id));
                            } catch (NumberFormatException e) {
                                player.sendMessage(MM.deserialize("<red>x/y/z must be numeric."));
                            }
                        }
                        case "remove" -> {
                            if (args.length < 4) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home remove <id> <name>"));
                                return;
                            }
                            String name = args[3];
                            data.getHomes().removeIf(h -> h.getName().equalsIgnoreCase(name));
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Home removed for <white>" + id));
                        }
                        case "list" -> {
                            if (data.getHomes().isEmpty()) {
                                player.sendMessage(MM.deserialize("<yellow>No homes for <white>" + id));
                                return;
                            }
                            player.sendMessage(MM.deserialize("<gold>Homes for <white>" + id));
                            for (ExampleHomePoint home : data.getHomes()) {
                                player.sendMessage(MM.deserialize("<gray>- <white>" + home.getName() + " <gray>@ <white>" + home.getWorld()
                                        + " <gray>(" + home.getX() + ", " + home.getY() + ", " + home.getZ() + ")"));
                            }
                        }
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home <add|remove|list> <id> [...]"));
                    }
                });
            }

            case "blob" -> {
                return handleBlob(label, args, repo, player);
            }

            case "top" -> {
                String mode = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "cache";
                int limit = 10;
                if (args.length >= 3) {
                    try {
                        limit = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException ignored) {
                    }
                }

                try {
                    switch (mode) {
                        case "cache" -> {
                            List<ExampleData> top = repo.getTopByScore(limit);
                            if (top.isEmpty()) {
                                player.sendMessage(MM.deserialize("<yellow>No profiles in cache yet."));
                                return true;
                            }
                            player.sendMessage(MM.deserialize("<gold>Top " + top.size() + " profiles by score <gray>(cache)"));
                            for (int i = 0; i < top.size(); i++) {
                                ExampleData d = top.get(i);
                                player.sendMessage(MM.deserialize("<gray>#" + (i + 1) + " <white>" + d.getDisplayName()
                                        + " <gray>id=<white>" + d.getId()
                                        + " <gray>score=<white>" + d.getScore()
                                        + " <gray>pts=<white>" + d.getPoints()
                                        + " <gray>rankField=<white>" + d.getLeaderboardRank()));
                            }
                        }
                        case "db" -> {
                            List<ExampleData> top = repo.getTopProfiles();
                            if (top.isEmpty()) {
                                player.sendMessage(MM.deserialize("<yellow>No profiles in storage."));
                                return true;
                            }
                            int cap = Math.min(limit, top.size());
                            player.sendMessage(MM.deserialize("<gold>Top " + cap + " profiles by score <gray>(db full columns)"));
                            for (int i = 0; i < cap; i++) {
                                ExampleData d = top.get(i);
                                player.sendMessage(MM.deserialize("<gray>#" + (i + 1) + " <white>" + d.getDisplayName()
                                        + " <gray>id=<white>" + d.getId()
                                        + " <gray>score=<white>" + d.getScore()
                                        + " <gray>pts=<white>" + d.getPoints()
                                        + " <gray>rankField=<white>" + d.getLeaderboardRank()));
                            }
                        }
                        case "partial" -> {
                            List<ExampleData> top = repo.getTop10Profiles();
                            int cap = Math.min(limit, top.size());
                            player.sendMessage(MM.deserialize("<gold>Top " + cap + " profiles <gray>(db partial columns)"));
                            for (int i = 0; i < cap; i++) {
                                ExampleData d = top.get(i);
                                player.sendMessage(MM.deserialize("<gray>#" + (i + 1) + " <white>" + d.getDisplayName()
                                        + " <gray>id=<white>" + d.getId()
                                        + " <gray>score=<white>" + d.getScore()));
                            }
                        }
                        case "dto" -> {
                            List<ExampleRepository.ExampleTopEntry> top = repo.getLeaderboardPage(limit);
                            if (top.isEmpty()) {
                                player.sendMessage(MM.deserialize("<yellow>No profiles in cache yet."));
                                return true;
                            }
                            player.sendMessage(MM.deserialize("<gold>Top " + top.size() + " profiles <gray>(dto from cache)"));
                            for (int i = 0; i < top.size(); i++) {
                                ExampleRepository.ExampleTopEntry d = top.get(i);
                                player.sendMessage(MM.deserialize("<gray>#" + (i + 1) + " <white>" + d.displayName()
                                        + " <gray>id=<white>" + d.id()
                                        + " <gray>score=<white>" + d.score()));
                            }
                        }
                        case "window" -> {
                            List<ExampleRepository.ExampleTopEntry> page = repo.getTop10To20();
                            if (page.isEmpty()) {
                                player.sendMessage(MM.deserialize("<yellow>No profiles in storage for window query."));
                                return true;
                            }
                            player.sendMessage(MM.deserialize("<gold>Window 10..20 <gray>(db query)"));
                            for (int i = 0; i < page.size(); i++) {
                                int absolute = 10 + i;
                                ExampleRepository.ExampleTopEntry d = page.get(i);
                                player.sendMessage(MM.deserialize("<gray>#" + absolute + " <white>" + d.displayName()
                                        + " <gray>id=<white>" + d.id()
                                        + " <gray>score=<white>" + d.score()));
                            }
                        }
                        case "refresh" -> {
                            List<ExampleData> top = repo.refreshLeaderboard(limit);
                            player.sendMessage(MM.deserialize("<green>Leaderboard refreshed from storage. Loaded <white>" + top.size() + "<green> entries."));
                        }
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " top <cache|db|partial|dto|window|refresh> [limit]"));
                    }
                } catch (Exception e) {
                    player.sendMessage(MM.deserialize("<red>Top query failed: <white>" + e.getMessage()));
                }
            }

            case "rank" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " rank <id>"));
                    return true;
                }
                String id = args[1];
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    try {
                        int rank = repo.getLeaderboardPosition(data);
                        player.sendMessage(MM.deserialize("<green>Rank for <white>" + id + "<green>: <white>#" + rank));
                    } catch (Exception e) {
                        player.sendMessage(MM.deserialize("<red>Rank query failed: <white>" + e.getMessage()));
                    }
                });
            }

            case "rankupdate" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " rankupdate <id>"));
                    return true;
                }
                String id = args[1];
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    try {
                        repo.updateLeaderboardPosition(data);
                        player.sendMessage(MM.deserialize("<green>Updated rank field for <white>" + id + "<green>: <white>#" + data.getLeaderboardRank()));
                    } catch (Exception e) {
                        player.sendMessage(MM.deserialize("<red>Rank update failed: <white>" + e.getMessage()));
                    }
                });
            }

            case "rankbulk" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " rankbulk <id1> [id2 ...]"));
                    return true;
                }
                List<String> ids = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                repo.bulkLoad(ids);
                List<ExampleData> loaded = ids.stream()
                        .map(repo::getIfPresent)
                        .flatMap(Optional::stream)
                        .toList();
                if (loaded.isEmpty()) {
                    player.sendMessage(MM.deserialize("<yellow>No matching cached profiles after bulk load."));
                    return true;
                }
                try {
                    repo.updateLeaderboardPositions(loaded);
                    player.sendMessage(MM.deserialize("<green>Bulk rank update done for <white>" + loaded.size() + "<green> profile(s)."));
                } catch (Exception e) {
                    player.sendMessage(MM.deserialize("<red>Bulk rank update failed: <white>" + e.getMessage()));
                }
            }

            case "seed" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " seed <count>"));
                    return true;
                }
                int count;
                try {
                    count = Math.max(1, Integer.parseInt(args[1]));
                } catch (NumberFormatException e) {
                    player.sendMessage(MM.deserialize("<red>Invalid number: <white>" + args[1]));
                    return true;
                }

                List<ExampleData> batch = new java.util.ArrayList<>(count);
                java.util.Random random = new java.util.Random();
                for (int i = 1; i <= count; i++) {
                    String id = "seed_" + i;
                    ExampleData data = ExampleData.of(id, "Seed " + i);
                    data.setScore(50L + random.nextLong(10_000));
                    data.setPoints(1 + random.nextInt(2_000));
                    batch.add(data);
                }
                repo.bulkSave(batch);
                player.sendMessage(MM.deserialize("<green>Seeded and saved <white>" + count + "<green> profile(s)."));
            }

            case "saveall" -> {
                int size = repo.getCache().size();
                repo.saveAll();
                player.sendMessage(MM.deserialize("<green>Bulk-saved <white>" + size + " <green>profile(s)."));
            }

            case "save" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " save <id>"));
                    return true;
                }
                String id = args[1];
                repo.getIfPresent(id).ifPresentOrElse(data -> {
                    repo.save(data);
                    player.sendMessage(MM.deserialize("<green>Saved profile <white>" + id));
                }, () -> player.sendMessage(MM.deserialize("<red>Profile not cached: <white>" + id)));
            }

            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " delete <id>"));
                    return true;
                }
                String id = args[1];
                repo.delete(id);
                player.sendMessage(MM.deserialize("<red>Deleted profile <white>" + id));
            }

            case "bulkload" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " bulkload <id1> [id2 ...]"));
                    return true;
                }
                List<String> ids = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                repo.bulkLoad(ids);
                long loaded = ids.stream().filter(id -> repo.getIfPresent(id).isPresent()).count();
                player.sendMessage(MM.deserialize("<green>Bulk-loaded <white>" + loaded + "<green>/<white>" + ids.size()));
            }

            case "info" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " info <id>"));
                    return true;
                }
                String id = args[1];
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }

                    ExampleSettings settings = data.getSettings();
                    if (settings == null) {
                        plugin.getLogger().info("Settings are null on profile " + id);
                    }

                    player.sendMessage(MM.deserialize("<gold>Profile <white>" + id));
                    player.sendMessage(MM.deserialize("<gray>  display : <white>" + data.getDisplayName()));
                    player.sendMessage(MM.deserialize("<gray>  score   : <white>" + data.getScore()));
                    player.sendMessage(MM.deserialize("<gray>  points  : <white>" + data.getPoints()));
                    if (settings == null) {
                        player.sendMessage(MM.deserialize("<gray>  settings: <white>null"));
                    } else {
                        player.sendMessage(MM.deserialize("<gray>  settings: <white>notify=" + settings.isNotifications()
                                + " <gray>theme=<white>" + settings.getTheme()
                                + " <gray>anchor=<white>" + settings.getX() + "," + settings.getY() + "," + settings.getZ()));
                    }
                    player.sendMessage(MM.deserialize("<gray>  tags    : <white>" + data.getTags()));
                    player.sendMessage(MM.deserialize("<gray>  homes   : <white>" + data.getHomes().size()));
                    player.sendMessage(MM.deserialize("<gray>  badges  : <white>" + data.getBadges()));
                    player.sendMessage(MM.deserialize("<gray>  stats   : <white>" + data.getStatBuckets()));
                    player.sendMessage(MM.deserialize("<gray>  notes   : <white>" + data.getNotes().getValue()));
                    player.sendMessage(MM.deserialize("<gray>  raw len : <white>" + data.getRawSnapshot().length));
                    player.sendMessage(MM.deserialize("<gray>  backend : <white>" + repo.backendType()));
                });
            }

            default -> sendHelp(player, label);
        }

        return true;
    }

    private boolean handleBlob(String label, String[] args, ExampleRepository repo, Player player) {
        if (args.length < 4) {
            player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob <badge|stat|note|raw> <id> ..."));
            return true;
        }

        String family = args[1].toLowerCase(Locale.ROOT);
        String id = args[2];

        return switch (family) {
            case "badge" -> {
                if (args.length < 4) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob badge <id> <add|remove|list> [BADGE]"));
                    yield true;
                }
                String action = args[3].toLowerCase(Locale.ROOT);
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    switch (action) {
                        case "add" -> {
                            if (args.length < 5) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob badge <id> add <BADGE>"));
                                return;
                            }
                            try {
                                ExampleBadge badge = ExampleBadge.valueOf(args[4].toUpperCase(Locale.ROOT));
                                if (!data.getBadges().contains(badge)) data.getBadges().add(badge);
                                repo.save(data);
                                player.sendMessage(MM.deserialize("<green>Badge added: <white>" + badge));
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(MM.deserialize("<red>Unknown badge. Use: <white>" + Arrays.toString(ExampleBadge.values())));
                            }
                        }
                        case "remove" -> {
                            if (args.length < 5) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob badge <id> remove <BADGE>"));
                                return;
                            }
                            try {
                                ExampleBadge badge = ExampleBadge.valueOf(args[4].toUpperCase(Locale.ROOT));
                                data.getBadges().remove(badge);
                                repo.save(data);
                                player.sendMessage(MM.deserialize("<green>Badge removed: <white>" + badge));
                            } catch (IllegalArgumentException e) {
                                player.sendMessage(MM.deserialize("<red>Unknown badge. Use: <white>" + Arrays.toString(ExampleBadge.values())));
                            }
                        }
                        case "list" -> player.sendMessage(MM.deserialize("<gray>Badges: <white>" + data.getBadges()));
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob badge <id> <add|remove|list> [BADGE]"));
                    }
                });
                yield true;
            }
            case "stat" -> {
                if (args.length < 4) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob stat <id> <put|remove|list> ..."));
                    yield true;
                }
                String action = args[3].toLowerCase(Locale.ROOT);
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    switch (action) {
                        case "put" -> {
                            if (args.length < 6) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob stat <id> put <key> <long>"));
                                return;
                            }
                            try {
                                long value = Long.parseLong(args[5]);
                                data.getStatBuckets().put(args[4], value);
                                repo.save(data);
                                player.sendMessage(MM.deserialize("<green>Blob stat updated."));
                            } catch (NumberFormatException e) {
                                player.sendMessage(MM.deserialize("<red>Invalid long: <white>" + args[5]));
                            }
                        }
                        case "remove" -> {
                            if (args.length < 5) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob stat <id> remove <key>"));
                                return;
                            }
                            data.getStatBuckets().remove(args[4]);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Blob stat removed."));
                        }
                        case "list" -> player.sendMessage(MM.deserialize("<gray>Blob stats: <white>" + data.getStatBuckets()));
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob stat <id> <put|remove|list> ..."));
                    }
                });
                yield true;
            }
            case "note" -> {
                if (args.length < 4) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob note <id> <add|remove|list> [text]"));
                    yield true;
                }
                String action = args[3].toLowerCase(Locale.ROOT);
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    switch (action) {
                        case "add" -> {
                            if (args.length < 5) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob note <id> add <text>"));
                                return;
                            }
                            String text = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                            data.getNotes().add(text);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Blob note added."));
                        }
                        case "remove" -> {
                            if (args.length < 5) {
                                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob note <id> remove <text>"));
                                return;
                            }
                            String text = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                            data.getNotes().remove(text);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Blob note removed."));
                        }
                        case "list" -> player.sendMessage(MM.deserialize("<gray>Blob notes: <white>" + data.getNotes().getValue()));
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob note <id> <add|remove|list> [text]"));
                    }
                });
                yield true;
            }
            case "raw" -> {
                if (args.length < 5) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob raw <id> <set|clear|len> [text]"));
                    yield true;
                }
                String action = args[3].toLowerCase(Locale.ROOT);
                repo.getCached(id).thenAccept(data -> {
                    if (data == null) {
                        player.sendMessage(MM.deserialize("<red>No profile found: <white>" + id));
                        return;
                    }
                    switch (action) {
                        case "set" -> {
                            String text = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                            data.setRawSnapshot(text.getBytes(StandardCharsets.UTF_8));
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Raw blob bytes set (<white>" + data.getRawSnapshot().length + "<green>)."));
                        }
                        case "clear" -> {
                            data.setRawSnapshot(new byte[0]);
                            repo.save(data);
                            player.sendMessage(MM.deserialize("<green>Raw blob cleared."));
                        }
                        case "len" -> player.sendMessage(MM.deserialize("<gray>Raw blob length: <white>" + data.getRawSnapshot().length));
                        default -> player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob raw <id> <set|clear|len> [text]"));
                    }
                });
                yield true;
            }
            default -> {
                player.sendMessage(MM.deserialize("<red>Usage: /" + label + " blob <badge|stat|note|raw> <id> ..."));
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            return SUBS.stream().filter(s -> s.startsWith(partial)).toList();
        }
        if (args.length == 2 && "top".equalsIgnoreCase(args[0])) {
            return List.of("cache", "db", "partial", "dto", "window", "refresh").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return List.of();
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(MM.deserialize("<gold>ExampleData commands"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " create <id> [displayName]"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " score <id> <value>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " points <id> <value>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " setting <id> <notifications:true|false> <theme>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " anchor <id> <x> <y> <z>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " nullsettings <id>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " tag <add|remove|list> <id> [tag]"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " home <add|remove|list> <id> [...]"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " blob <badge|stat|note|raw> <id> ..."));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " rankupdate <id>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " rankbulk <id1> [id2 ...]"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " seed <count>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " saveall"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " save <id>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " delete <id>"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " bulkload <id1> [id2 ...]"));
        player.sendMessage(MM.deserialize("<gray>  /" + label + " info <id>"));
    }
}
