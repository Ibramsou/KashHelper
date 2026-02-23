package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigMessage {

    private String message;
    private ConfigSound sound;
    private ConfigTitle title;
    private String actionBar;

    public ConfigMessage(String message) {
        this.message = message;
    }

    public ConfigMessage sound(ConfigSound sound) {
        this.sound = sound;
        return this;
    }

    public ConfigMessage title(ConfigTitle title) {
        this.title = title;
        return this;
    }

    public ConfigMessage actionBar(String actionBar) {
        this.actionBar = actionBar;
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
        if (this.actionBar == null && this.title == null && this.sound == null) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach((player) -> {
            this.sendActionBar(player, replacers);
            this.playSound(player);
            this.sendTitle(player, replacers);
        });
    }

    private void sendActionBar(Player player, Object... replacers) {
        if (this.actionBar == null) return;
        player.sendActionBar(TextUtil.replacedComponent(this.actionBar, replacers));
    }

    private void playSound(Player player) {
        if (this.sound == null) return;
        this.sound.play(player);
    }

    private void sendTitle(Player player, Object... replacers) {
        if (this.title == null) return;
        this.title.send(player, replacers);
    }

    public String getMessage(Object... replacers) {
        return TextUtil.replaced(message, replacers);
    }

    public String getMessage() {
        return message;
    }

    public ConfigSound getSound() {
        return sound;
    }

    public ConfigTitle getTitle() {
        return title;
    }

    public String getActionBar() {
        return actionBar;
    }
}