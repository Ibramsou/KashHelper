package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.example.gui.ExampleGui;
import fr.ibrakash.helper.example.gui.ExamplePagedGui;
import fr.ibrakash.helper.example.home.HomeRecord;
import fr.ibrakash.helper.example.home.HomeRepository;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExampleCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ExamplePlugin plugin;

    public ExampleCommand(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return false;

        String sub = args.length == 0 ? "" : args[0].toLowerCase();

        switch (sub) {
            case "gui" -> {
                new ExampleGui(this.plugin).open(player);
                return true;
            }
            case "paged" -> {
                new ExamplePagedGui(this.plugin, player).open(player);
                return true;
            }
            case "persistence" -> {
                return handlePersistence(label, args, player);
            }
            case "home" -> {
                return handleHome(label, args, player);
            }
            default -> {
                player.sendMessage(MM.deserialize("<yellow>Usage: <white>/" + label + " home <set|tp|list|fav|delete|top>"));
                return true;
            }
        }
    }

    private boolean handlePersistence(String label, String[] args, Player player) {
        if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
            this.plugin.reloadPersistenceDemo();
            player.sendMessage(MM.deserialize("<green>Persistence config reloaded from <white>persistence.yml<green>."));
            return true;
        }

        ConfigPersistence cfg = this.plugin.getPersistenceConfig().getPersistence();
        String fallbacks = cfg.getFallbacks() == null || cfg.getFallbacks().isEmpty() ? "none" : cfg.getFallbacks().toString();

        HomeRepository repo = this.plugin.getHomeRepository();

        player.sendMessage(MM.deserialize("<gold>Persistence info"));
        player.sendMessage(MM.deserialize("<gray>  configured primary : <white>" + cfg.getType()));
        player.sendMessage(MM.deserialize("<gray>  configured fallbacks: <white>" + fallbacks));
        player.sendMessage(MM.deserialize("<gray>  homes backend       : <white>" + repo.backendType()));
        player.sendMessage(MM.deserialize("<gray>  homes in cache      : <white>" + repo.cacheSize()));
        player.sendMessage(MM.deserialize("<gray>  sql driver          : <white>" + cfg.getSql().getDriverType()));
        player.sendMessage(MM.deserialize("<gray>  json folder         : <white>" + cfg.getJson().getFolder()));
        player.sendMessage(MM.deserialize("<yellow>Tip: edit persistence.yml then run /" + label + " persistence reload"));
        return true;
    }

    private boolean handleHome(String label, String[] args, Player player) {
        HomeRepository repo = this.plugin.getHomeRepository();

        if (args.length < 2) {
            player.sendMessage(MM.deserialize("<yellow>Usage: <white>/" + label + " home <set|tp|list|fav|delete|top>"));
            return true;
        }

        String action = args[1].toLowerCase();
        UUID owner = player.getUniqueId();

        switch (action) {
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home set <name>"));
                    return true;
                }
                String name = args[2].toLowerCase();
                HomeRecord home = HomeRecord.of(owner, name, player.getLocation());
                repo.save(home);
                player.sendMessage(MM.deserialize("<green>Home <white>" + name + " <green>saved in backend <white>" + repo.backendType()));
                return true;
            }
            case "tp" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home tp <name>"));
                    return true;
                }
                String name = args[2].toLowerCase();
                return repo.find(owner, name).map(home -> {
                    World world = Bukkit.getWorld(home.getWorld());
                    if (world == null) {
                        player.sendMessage(MM.deserialize("<red>World not found for home <white>" + name));
                        return true;
                    }
                    Location loc = new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
                    player.teleport(loc);
                    player.sendMessage(MM.deserialize("<green>Teleported to home <white>" + name));
                    return true;
                }).orElseGet(() -> {
                    player.sendMessage(MM.deserialize("<red>Unknown home: <white>" + name));
                    return true;
                });
            }
            case "list" -> {
                List<HomeRecord> homes = repo.list(owner);
                if (homes.isEmpty()) {
                    player.sendMessage(MM.deserialize("<yellow>You don't have any homes yet. Use /" + label + " home set <name>."));
                    return true;
                }
                player.sendMessage(MM.deserialize("<gold>Your homes <gray>(backend: <white>" + repo.backendType() + "<gray>)"));
                for (HomeRecord home : homes) {
                    String fav = home.isFavorite() ? "<gold>* " : "<gray>- ";
                    player.sendMessage(MM.deserialize(fav + "<white>" + home.getName() + " <gray>@ <white>" + home.getWorld() + " <gray>(" +
                            String.format("%.1f", home.getX()) + ", " + String.format("%.1f", home.getY()) + ", " + String.format("%.1f", home.getZ()) + ")"));
                }
                return true;
            }
            case "fav" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home fav <name> [true|false]"));
                    return true;
                }
                String name = args[2].toLowerCase();
                return repo.find(owner, name).map(home -> {
                    boolean value;
                    if (args.length >= 4) {
                        value = Boolean.parseBoolean(args[3]);
                    } else {
                        value = !home.isFavorite();
                    }
                    home.setFavorite(value);
                    repo.save(home);
                    player.sendMessage(MM.deserialize("<green>Home <white>" + name + " <green>favorite = <white>" + value));
                    return true;
                }).orElseGet(() -> {
                    player.sendMessage(MM.deserialize("<red>Unknown home: <white>" + name));
                    return true;
                });
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage(MM.deserialize("<red>Usage: /" + label + " home delete <name>"));
                    return true;
                }
                String name = args[2].toLowerCase();
                repo.delete(owner, name);
                player.sendMessage(MM.deserialize("<red>Home <white>" + name + " <red>deleted."));
                return true;
            }
            case "top" -> {
                int limit = 5;
                if (args.length >= 3) {
                    try {
                        limit = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException ignored) { }
                }
                Map<UUID, Integer> top = repo.topOwners(limit);
                if (top.isEmpty()) {
                    player.sendMessage(MM.deserialize("<yellow>No homes stored yet."));
                    return true;
                }
                player.sendMessage(MM.deserialize("<gold>Top home owners <gray>(backend: <white>" + repo.backendType() + "<gray>)"));
                int i = 1;
                for (Map.Entry<UUID, Integer> entry : top.entrySet()) {
                    String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (playerName == null) playerName = entry.getKey().toString();
                    player.sendMessage(MM.deserialize("<gray>#" + i++ + " <white>" + playerName + " <gray>→ <white>" + entry.getValue() + " homes"));
                }
                return true;
            }
            case "reload" -> {
                repo.reload();
                player.sendMessage(MM.deserialize("<green>Homes cache reloaded from backend <white>" + repo.backendType()));
                return true;
            }
            default -> {
                player.sendMessage(MM.deserialize("<yellow>Usage: <white>/" + label + " home <set|tp|list|fav|delete|top>"));
                return true;
            }
        }
    }
}
