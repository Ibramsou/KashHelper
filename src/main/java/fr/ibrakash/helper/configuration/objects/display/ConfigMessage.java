package fr.ibrakash.helper.configuration.objects.display;

import fr.ibrakash.helper.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class ConfigMessage {

    private String message;
    private Optional<ConfigSound> sound = Optional.empty();
    private Optional<ConfigTitle> title = Optional.empty();
    private Optional<String> actionBar = Optional.empty();

    public ConfigMessage() {
        this("Not configured");
    }

    public ConfigMessage(String message) {
        this.message = message;
    }

    public ConfigMessage sound(ConfigSound sound) {
        this.sound = Optional.of(sound);
        return this;
    }

    public ConfigMessage title(ConfigTitle title) {
        this.title = Optional.of(title);
        return this;
    }

    public ConfigMessage actionBar(String actionBar) {
        this.actionBar = Optional.of(actionBar);
        return this;
    }

    public void send(CommandSender sender, Object... replacers) {
        sender.sendMessage(TextUtil.replacedComponent(this.message, replacers));

        if (sender instanceof Player player) {
            this.sendTitle(player, replacers);
            this.sendActionBar(player, replacers);
            this.playSound(player);
        }
    }

    public void broadcast(Object... replacers) {
        Bukkit.broadcast(TextUtil.replacedComponent(this.message, replacers));
        if (this.actionBar.isEmpty() && this.title.isEmpty() && this.sound.isEmpty()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach((player) -> {
            this.sendActionBar(player, replacers);
            this.playSound(player);
            this.sendTitle(player, replacers);
        });
    }

    private void sendActionBar(Player player, Object... replacers) {
        this.actionBar.ifPresent(string -> player.sendActionBar(TextUtil.replacedComponent(string, replacers)));
    }

    private void playSound(Player player) {
        this.sound.ifPresent(sound -> sound.play(player));
    }

    private void sendTitle(Player player, Object... replacers) {
        this.title.ifPresent(title -> title.send(player, replacers));
    }

    public Component serialized(Object... replacers) {
        return TextUtil.replacedComponent(this.message, replacers);
    }

    public String getMessage(Object... replacers) {
        return TextUtil.replaced(message, replacers);
    }

    public String getMessage() {
        return message;
    }

    public ConfigSound getSound() {
        return sound.orElse(null);
    }

    public ConfigTitle getTitle() {
        return title.orElse(null);
    }

    public String getActionBar() {
        return actionBar.orElse(null);
    }
}